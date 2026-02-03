# Exception Handling Architecture Guide

## Overview

This project implements a professional, enterprise-grade exception handling system following Java and Spring best practices. The architecture provides consistent error responses across all API endpoints with proper HTTP status codes, error codes, and detailed error information.

## Exception Hierarchy

### Base Exception: `AppException`

All application-specific exceptions extend `AppException`, which is an abstract class that:

- Extends `RuntimeException` (unchecked exception)
- Contains HTTP status code and error code fields
- Provides constructor overloads for message and cause
- Enables consistent error handling in the global exception handler

```java
public abstract class AppException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;
    
    public AppException(String message, HttpStatus httpStatus, String errorCode) { ... }
    public HttpStatus getHttpStatus() { ... }
    public String getErrorCode() { ... }
}
```

### Custom Exception Classes

#### 1. **ResourceNotFoundException** (HTTP 404)
**When to use:** Resource (user, auction, category, etc.) is not found

```java
throw new ResourceNotFoundException("User", userId);
throw new ResourceNotFoundException("User not found");
```

**Error Code:** `RESOURCE_NOT_FOUND`

#### 2. **InvalidOperationException** (HTTP 409 Conflict)
**When to use:** Business logic violation or invalid state transition

Examples:
- Bidding on ended auctions
- Updating auctions with existing bids
- Deleting auctions that have bids
- Bidding on own auction (validation)

```java
throw new InvalidOperationException("This auction has ended. Bidding is no longer allowed.");
```

**Error Code:** `INVALID_OPERATION`

#### 3. **UnauthorizedException** (HTTP 403 Forbidden)
**When to use:** User lacks permission to perform an action

Examples:
- Updating another user's auction
- Deleting someone else's auction
- Adding images to another user's auction

```java
throw new UnauthorizedException("You can only update your own auctions");
```

**Error Code:** `UNAUTHORIZED`

#### 4. **ValidationException** (HTTP 400 Bad Request)
**When to use:** Input validation fails at the business logic level (different from Bean Validation)

Examples:
- Bidding on own auction
- Bid amount too low
- Invalid end time (before start time)
- Invalid category IDs

```java
throw new ValidationException("Bid amount must be higher than current price");
```

**Error Code:** `VALIDATION_ERROR`

#### 5. **DuplicateResourceException** (HTTP 409 Conflict)
**When to use:** Attempting to create a resource that already exists

Examples:
- Username already registered
- Email already in use
- Duplicate entries

```java
throw new DuplicateResourceException("User", "username", "john_doe");
```

**Error Code:** `DUPLICATE_RESOURCE`

#### 6. **FileStorageException** (HTTP 400 / 413)
**When to use:** File upload or storage operations fail

Examples:
- Invalid file type
- File size exceeded
- Invalid file path
- Storage I/O errors

```java
throw new FileStorageException("Invalid file type");
throw FileStorageException.fileSizeExceeded("File size exceeds 10MB");
```

**Error Code:** `FILE_STORAGE_ERROR`

## Error Response Format

All API error responses follow this standardized format:

```json
{
  "timestamp": "2026-02-03T10:30:45.123456",
  "status": 404,
  "error": "Not Found",
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with ID: 123",
  "fieldErrors": null,
  "path": "/api/users/123"
}
```

### Fields:
- **timestamp**: When the error occurred (ISO-8601 format)
- **status**: HTTP status code
- **error**: HTTP status reason phrase
- **errorCode**: Application-specific error code (for client-side error handling)
- **message**: Human-readable error message
- **fieldErrors**: Map of validation field errors (from @Valid)
- **path**: Request path that caused the error

## Global Exception Handler

The `GlobalExceptionHandler` is a `@RestControllerAdvice` that:

1. **Catches custom AppException and subclasses** - Extracts HTTP status and error code
2. **Handles Spring/Jakarta exceptions** - Maps to appropriate HTTP responses
3. **Processes validation errors** - Collects and reports field-level errors
4. **Provides fallback handling** - Catches unexpected exceptions safely

### Exception Handling Order (Priority)

```
1. AppException subclasses (custom)
   ├── ResourceNotFoundException
   ├── InvalidOperationException
   ├── UnauthorizedException
   ├── ValidationException
   ├── DuplicateResourceException
   └── FileStorageException

2. Spring Security exceptions
   ├── AuthenticationException
   ├── BadCredentialsException
   └── AccessDeniedException

3. Validation exceptions
   ├── MethodArgumentNotValidException (@Valid)
   └── ConstraintViolationException (@Validated)

4. Jakarta/Spring exceptions
   └── EntityNotFoundException

5. Miscellaneous exceptions
   ├── MaxUploadSizeExceededException
   └── Exception (catch-all)
```

## HTTP Status Code Mapping

| Exception | Status | Code |
|-----------|--------|------|
| ResourceNotFoundException | 404 | RESOURCE_NOT_FOUND |
| InvalidOperationException | 409 | INVALID_OPERATION |
| UnauthorizedException | 403 | UNAUTHORIZED |
| ValidationException | 400 | VALIDATION_ERROR |
| DuplicateResourceException | 409 | DUPLICATE_RESOURCE |
| FileStorageException | 400 | FILE_STORAGE_ERROR |
| FileStorageException (size) | 413 | FILE_STORAGE_ERROR |

## Usage Guidelines

### In Service Classes

Always throw the appropriate exception for the business logic error:

```java
@Service
public class UserService {
    
    public UserDTO registerUser(RegisterRequestDTO request) {
        // Check for duplicates
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        
        // ... create user
    }
    
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
```

### In Controller Classes

No exception handling needed in controllers - the `GlobalExceptionHandler` catches all exceptions:

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        // Service throws exceptions, handler catches them
        UserDTO user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        // If not found, ResourceNotFoundException is thrown and caught by handler
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
```

## Best Practices

### ✅ DO:

1. **Use specific exception types** for different error scenarios
2. **Include context in messages** - User IDs, auction names, etc.
3. **Use constructors with ID or name parameters** when available
4. **Log at appropriate levels** - Handler logs at WARN/ERROR level
5. **Don't catch exceptions** in controllers - let handler manage them
6. **Provide error codes** for client-side error handling
7. **Include request path** in error response for debugging

### ❌ DON'T:

1. **Throw generic exceptions** like `Exception`, `RuntimeException`, `IllegalArgumentException`
2. **Catch exceptions in controllers** - this defeats the purpose of global handling
3. **Return different error formats** from different endpoints
4. **Expose stack traces** to clients in production
5. **Use InvalidOperationException for input validation** - use `ValidationException`
6. **Swallow exceptions** - always log or rethrow

## Examples

### Example 1: Valid Bid Placement

```java
public BidDTO placeBid(BidRequestDTO request, User bidder) {
    AuctionItem auction = auctionItemRepository.findById(request.itemId())
            .orElseThrow(() -> new ResourceNotFoundException("Auction", request.itemId()));
    
    // Auction state validations
    if (auction.getAuctionEndTime().isBefore(LocalDateTime.now())) {
        throw new InvalidOperationException("This auction has ended");
    }
    
    if (auction.getOwner().getId().equals(bidder.getId())) {
        throw new ValidationException("You cannot bid on your own auction");
    }
    
    BigDecimal currentPrice = auction.getCurrentPrice();
    if (request.amount().compareTo(currentPrice) <= 0) {
        throw new ValidationException("Bid must be higher than current price");
    }
    
    // ... place bid
}
```

**Response if auction not found (404):**
```json
{
  "timestamp": "2026-02-03T10:30:45.123456",
  "status": 404,
  "error": "Not Found",
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "Auction not found with ID: 999",
  "path": "/api/bids/place"
}
```

**Response if auction has ended (409):**
```json
{
  "timestamp": "2026-02-03T10:30:45.123456",
  "status": 409,
  "error": "Conflict",
  "errorCode": "INVALID_OPERATION",
  "message": "This auction has ended",
  "path": "/api/bids/place"
}
```

### Example 2: User Registration

```java
public UserDTO registerUser(RegisterRequestDTO request) {
    if (userRepository.existsByUsername(request.username())) {
        throw new DuplicateResourceException("User", "username", request.username());
    }
    
    if (userRepository.existsByEmail(request.email())) {
        throw new DuplicateResourceException("User", "email", request.email());
    }
    
    // ... create user
}
```

**Response if duplicate username (409):**
```json
{
  "timestamp": "2026-02-03T10:30:45.123456",
  "status": 409,
  "error": "Conflict",
  "errorCode": "DUPLICATE_RESOURCE",
  "message": "User with username 'john_doe' already exists",
  "path": "/api/auth/register"
}
```

### Example 3: Validation Error

Controller with @Valid annotation:

```java
@PostMapping("/register")
public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
}
```

**Response if validation fails (400):**
```json
{
  "timestamp": "2026-02-03T10:30:45.123456",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "VALIDATION_ERROR",
  "message": "One or more fields have validation errors",
  "fieldErrors": {
    "username": "Username is required",
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  },
  "path": "/api/auth/register"
}
```

## Deprecated Exceptions

The following exception classes are deprecated and should not be used:

- `CategoryNotFoundException` - Use `ResourceNotFoundException` instead
- `IllegalImageUploadException` - Use `FileStorageException` instead

These exist for backward compatibility but will be removed in future versions.

## Summary

This exception handling architecture provides:

✅ Consistency across all endpoints
✅ Proper HTTP status codes
✅ Meaningful error codes for client handling
✅ Detailed error information for debugging
✅ Separated concerns (services vs handlers)
✅ Easy to extend for new error types
✅ Professional, production-ready error responses
✅ Compliance with REST API standards
