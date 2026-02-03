package backend.biddingwars.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import backend.biddingwars.dto.AuctionItemDetailDTO;
import backend.biddingwars.exception.IllegalImageUploadException;
import backend.biddingwars.model.User;
import backend.biddingwars.service.AuctionItemService;
import backend.biddingwars.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for image upload and retrieval.
 * Handles file upload for auction item images.
 *
 * @author Oleander Tengesdal
 * @version 1.0
 * @since 02-02-2026
 */
@RestController
@RequestMapping("/items")
@Tag(name = "Images", description = "Image upload and retrieval endpoints")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final FileStorageService fileStorageService;
    private final AuctionItemService auctionItemService;

    public ImageController(FileStorageService fileStorageService, 
                           AuctionItemService auctionItemService) {
        this.fileStorageService = fileStorageService;
        this.auctionItemService = auctionItemService;
    }

    /**
     * Upload images for an auction item.
     * Stores the file locally and updates the auction with the image URL.
     *
     * @param id the auction item ID
     * @param files the image files to upload (max 5)
     * @param currentUser the authenticated user
     * @return the updated auction with new image URLs
     */
    @PostMapping("/{id}/image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload images", description = "Uploads images for an auction item")
    public ResponseEntity<AuctionItemDetailDTO> uploadImages(
            @Parameter(description = "Auction item ID") @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files,
            @AuthenticationPrincipal User currentUser) {

        logger.info("User {} uploading {} images for auction {}", 
                currentUser.getUsername(), files.length, id);

        if (files.length > 5) {
            logger.warn("Maximum 5 images allowed per upload, received {} images from user {}", files.length, currentUser.getUsername());
            throw new IllegalImageUploadException("Maximum 5 images allowed per upload.");
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = fileStorageService.storeFile(file);
            // Store the relative URL path
            imageUrls.add("/items/" + id + "/image/" + filename);
        }

        AuctionItemDetailDTO updatedAuction = auctionItemService.addImages(id, imageUrls, currentUser);
        
        logger.info("Successfully uploaded {} images for auction {}", imageUrls.size(), id);
        return ResponseEntity.ok(updatedAuction);
    }

    /**
     * Get an image file.
     * Serves the image from local storage.
     *
     * @param id the auction item ID (for URL consistency)
     * @param filename the image filename
     * @return the image file
     */
    @GetMapping("/{id}/image/{filename}")
    @Operation(summary = "Get image", description = "Retrieves an image file")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "Auction item ID") @PathVariable Long id,
            @Parameter(description = "Image filename") @PathVariable String filename) {

        Resource resource = fileStorageService.loadFileAsResource(filename);
        String contentType = fileStorageService.getContentType(filename);

        logger.info("Serving image {} for auction {}", filename, id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
