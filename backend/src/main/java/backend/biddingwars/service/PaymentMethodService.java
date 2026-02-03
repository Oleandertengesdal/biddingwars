package backend.biddingwars.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.biddingwars.dto.PaymentMethodDTO;
import backend.biddingwars.dto.PaymentMethodRequestDTO;
import backend.biddingwars.dto.PaymentMethodVerificationDTO;
import backend.biddingwars.exception.InvalidOperationException;
import backend.biddingwars.exception.PaymentMethodNotFoundException;
import backend.biddingwars.exception.PaymentVerificationException;
import backend.biddingwars.exception.ResourceNotFoundException;
import backend.biddingwars.exception.UnauthorizedException;
import backend.biddingwars.exception.ValidationException;
import backend.biddingwars.mapper.PaymentMethodMapper;
import backend.biddingwars.model.PaymentMethod;
import backend.biddingwars.model.PaymentMethodType;
import backend.biddingwars.model.User;
import backend.biddingwars.repository.PaymentMethodRepository;
import backend.biddingwars.repository.UserRepository;

/**
 * Service class for payment method operations.
 * Handles adding, verifying, and managing payment methods with mock verification.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 03-02-2026
 */
@Service
@Transactional
public class PaymentMethodService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodService.class);
    private static final Random random = new Random();

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
                                UserRepository userRepository,
                                PaymentMethodMapper paymentMethodMapper) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.userRepository = userRepository;
        this.paymentMethodMapper = paymentMethodMapper;
    }

    /**
     * Add a new payment method for a user.
     * Validates format, stores masked data, and generates verification code.
     *
     * @param userId the ID of the user
     * @param request the payment method request
     * @return PaymentMethodDTO of the created payment method
     */
    public PaymentMethodDTO addPaymentMethod(Long userId, PaymentMethodRequestDTO request) {
        logger.info("Adding payment method for user {}: type {}", userId, request.type());

        // Validate request based on type
        request.validate();

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Create payment method entity
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setUser(user);
        paymentMethod.setType(PaymentMethodType.valueOf(request.type()));

        // Populate fields based on type
        if (request.type().equals("VIPPS")) {
            populateVippsFields(paymentMethod, request);
        } else {
            populateCardFields(paymentMethod, request);
        }

        // Generate verification code (6-digit code, expires in 15 minutes)
        String verificationCode = String.format("%06d", random.nextInt(1000000));
        paymentMethod.setVerificationCode(verificationCode);
        paymentMethod.setVerificationExpiry(LocalDateTime.now().plusMinutes(15));
        paymentMethod.setVerified(false);

        // Set as default if requested or if it's the first payment method
        List<PaymentMethod> existingMethods = paymentMethodRepository.findByUserId(userId);
        if (request.setAsDefault() || existingMethods.isEmpty()) {
            // Remove default from other methods
            existingMethods.forEach(pm -> pm.setDefault(false));
            paymentMethod.setDefault(true);
        }

        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        
        logger.info("Payment method added with ID {} (verification code: {})", saved.getId(), verificationCode);
        // In production, send this code via email/SMS. For mock: log it or return in response
        
        return paymentMethodMapper.toDTO(saved);
    }

    /**
     * Verify a payment method using the verification code.
     * Mock verification - in production this would charge $0.01-$0.99.
     *
     * @param userId the ID of the user
     * @param verificationDTO the verification request
     * @return PaymentMethodDTO of the verified payment method
     */
    public PaymentMethodDTO verifyPaymentMethod(Long userId, PaymentMethodVerificationDTO verificationDTO) {
        logger.info("Verifying payment method {} for user {}", verificationDTO.paymentMethodId(), userId);

        PaymentMethod paymentMethod = paymentMethodRepository
                .findById(verificationDTO.paymentMethodId())
                .orElseThrow(() -> new PaymentMethodNotFoundException(verificationDTO.paymentMethodId()));

        // Verify ownership
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to verify this payment method");
        }

        // Check if already verified
        if (paymentMethod.isVerified()) {
            throw new PaymentVerificationException("Payment method is already verified");
        }

        // Check verification code expiry
        if (paymentMethod.getVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new PaymentVerificationException("Verification code has expired. Please request a new one.");
        }

        // Validate verification code
        if (!paymentMethod.getVerificationCode().equals(verificationDTO.verificationCode())) {
            throw new PaymentVerificationException("Invalid verification code");
        }

        // Mark as verified
        paymentMethod.setVerified(true);
        paymentMethod.setVerificationCode(null);
        paymentMethod.setVerificationExpiry(null);

        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        logger.info("Payment method {} verified successfully", saved.getId());

        return paymentMethodMapper.toDTO(saved);
    }

    /**
     * Set a payment method as the default for a user.
     *
     * @param userId the ID of the user
     * @param paymentMethodId the ID of the payment method
     * @return PaymentMethodDTO of the updated payment method
     */
    public PaymentMethodDTO setDefaultPaymentMethod(Long userId, Long paymentMethodId) {
        logger.info("Setting payment method {} as default for user {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new PaymentMethodNotFoundException(paymentMethodId));

        // Verify ownership
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this payment method");
        }

        // Must be verified to set as default
        if (!paymentMethod.isVerified()) {
            throw new InvalidOperationException("Cannot set an unverified payment method as default");
        }

        // Remove default from other methods
        List<PaymentMethod> userMethods = paymentMethodRepository.findByUserId(userId);
        userMethods.forEach(pm -> pm.setDefault(false));

        // Set as default
        paymentMethod.setDefault(true);
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);

        return paymentMethodMapper.toDTO(saved);
    }

    /**
     * Delete a payment method.
     *
     * @param userId the ID of the user
     * @param paymentMethodId the ID of the payment method to delete
     */
    public void deletePaymentMethod(Long userId, Long paymentMethodId) {
        logger.info("Deleting payment method {} for user {}", paymentMethodId, userId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new PaymentMethodNotFoundException(paymentMethodId));

        // Verify ownership
        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this payment method");
        }

        // If it's the default, check if there are other methods
        if (paymentMethod.isDefault()) {
            List<PaymentMethod> otherMethods = paymentMethodRepository.findByUserId(userId)
                    .stream()
                    .filter(pm -> !pm.getId().equals(paymentMethodId))
                    .toList();

            if (!otherMethods.isEmpty()) {
                // Set another verified method as default
                otherMethods.stream()
                        .filter(PaymentMethod::isVerified)
                        .findFirst()
                        .ifPresent(pm -> pm.setDefault(true));
            }
        }

        paymentMethodRepository.delete(paymentMethod);
        logger.info("Payment method {} deleted successfully", paymentMethodId);
    }

    /**
     * Get all payment methods for a user.
     *
     * @param userId the ID of the user
     * @return list of PaymentMethodDTOs
     */
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getUserPaymentMethods(Long userId) {
        logger.debug("Fetching payment methods for user {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        return paymentMethodRepository.findByUserId(userId)
                .stream()
                .map(paymentMethodMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has at least one verified payment method.
     *
     * @param userId the ID of the user
     * @return true if user has verified payment method
     */
    @Transactional(readOnly = true)
    public boolean hasVerifiedPaymentMethod(Long userId) {
        return paymentMethodRepository.existsByUserIdAndVerified(userId, true);
    }

    // Private helper methods

    private void populateVippsFields(PaymentMethod paymentMethod, PaymentMethodRequestDTO request) {
        String phoneNumber = request.phoneNumber();
        
        // Normalize phone number (add +47 if not present)
        if (!phoneNumber.startsWith("+47")) {
            phoneNumber = "+47" + phoneNumber;
        }

        // Store full number (in production, this would be encrypted)
        paymentMethod.setVippsPhoneFull(phoneNumber);

        // Store masked number for display
        paymentMethod.setVippsPhoneNumber(paymentMethodMapper.maskPhoneNumber(phoneNumber));
    }

    private void populateCardFields(PaymentMethod paymentMethod, PaymentMethodRequestDTO request) {
        // Extract last 4 digits (never store full card number in production)
        paymentMethod.setCardLastFour(paymentMethodMapper.getLastFourDigits(request.cardNumber()));

        // Determine card type
        paymentMethod.setCardType(paymentMethodMapper.determineCardType(request.cardNumber()));

        // Store other fields
        paymentMethod.setCardholderName(request.cardholderName());
        paymentMethod.setExpiryMonth(request.expiryMonth());
        paymentMethod.setExpiryYear(request.expiryYear());

        // Validate expiry date
        if (paymentMethod.isExpired()) {
            throw new ValidationException("Card expiry date is in the past");
        }
    }
}
