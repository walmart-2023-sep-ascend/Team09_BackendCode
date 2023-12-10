package com.walmart.ecartReviews.controller;


import com.walmart.ecartReviews.model.Comment;
import com.walmart.ecartReviews.model.NewComment;
import com.walmart.ecartReviews.service.NewCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approval")
public class NewCommentController {

    @Value("${product.rating.invalid}")
    private String product_rating_invalid;
    @Value("${user.mail.not.found}")
    private String user_mail_not_found;

    @Value("${no.user.found}")
    private String no_user_found;

    @Value("${comment.deleted.success}")
    private String comment_deleted_success;

    @Value("${comment.not.found}")
    private String comment_not_found;

    private static final Logger logger = LoggerFactory.getLogger(NewCommentController.class);
    private final NewCommentService newCommentService;

    @Autowired
    public NewCommentController(NewCommentService newCommentService) {
        this.newCommentService = newCommentService;
    }

    @GetMapping("/")
    public List<NewComment> getAllreview() {
        return newCommentService.getAllreview();
    }


    @PostMapping("/{productId}/comment")
    public ResponseEntity<Object> addNewComment(@PathVariable String productId, @RequestBody NewComment newComment, @RequestHeader Map<String, String> headers) {
        try {
            logger.info("" + headers);
            System.out.println(headers.get("cookie"));
            String cookieHeader = headers.get("cookie");

            if (cookieHeader != null && cookieHeader.contains("Token=")) {
                // Token found in the Cookie header
                if (headers.get("user-id-email") != null) {
                    // user-id-email found in headers
                    Object response = newCommentService.addComment(Integer.parseInt(productId), newComment, headers.get("user-id-email"));
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    logger.error("user-id-email not found in headers");
                    return new ResponseEntity<>("user-id-email not found in headers", HttpStatus.BAD_REQUEST);
                }
            } else {
                logger.error("Token not found in the Cookie header");
                return new ResponseEntity<>("Token not found in the Cookie header", HttpStatus.UNAUTHORIZED);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid product ID format: " + e.getMessage());
            return new ResponseEntity<>(product_rating_invalid, HttpStatus.BAD_REQUEST);
        } catch (HttpMessageNotReadableException e) {
            logger.error("Invalid JSON payload: " + e.getMessage());
            return new ResponseEntity<>("Invalid JSON payload: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{productId}/{userId}")
    public ResponseEntity<String> deleteComment(@PathVariable String productId, @PathVariable String userId) {
        try {
            boolean deleted = newCommentService.deleteComment(Integer.parseInt(productId), userId);

            if (deleted) {
                return new ResponseEntity<>(comment_deleted_success, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(comment_not_found, HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/productId/{productId}")
    public List<NewComment> getByproductId(@PathVariable String productId) {
        try {
            return newCommentService.findByproductId(Integer.parseInt(productId));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

}
