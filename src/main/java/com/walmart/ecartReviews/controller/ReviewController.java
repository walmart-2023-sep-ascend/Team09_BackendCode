package com.walmart.ecartReviews.controller;
import com.walmart.ecartReviews.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.walmart.ecartReviews.service.ReviewService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@CrossOrigin(origins = "http://172.172.241.64:3000")
@RequestMapping("/api/product")
public class ReviewController {

    @Value("${product.rating.invalid}")
    private String product_rating_invalid;
    @Value("${comment.not.added}")
    private String comment_not_added;
    @Value("${review.not.approved}")
    private String review_not_approved;
    @Value("${user.mail.not.found}")
    private String user_mail_not_found;

    @Value("${no.user.found}")
    private String no_user_found;
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public List<ReviewSearch> getAllreviews() {
        return reviewService.getAllreviews();
    }
    
    @GetMapping("/reviewSearchId/{reviewSearchId}")
    public List<ReviewSearch> getByReviewSearchId(@PathVariable int reviewSearchId) {
        return reviewService.findByReviewSearchId(reviewSearchId);
    }

    @GetMapping("/{product_id}/by-ratings/{rating}")
    public Object getCommentsForGivenRating(@PathVariable String product_id, @PathVariable String rating) {
        try {
            return reviewService.getCommentsForGivenRating(Integer.parseInt(product_id), Integer.parseInt(rating));
        }
        catch (NumberFormatException e)
        {
            return product_rating_invalid;
        }
    }

    @PostMapping("/{product_id}/comment/")
    public Object addNewComments(@PathVariable String product_id, @RequestBody Comment comment,@RequestHeader Map<String,String> headers){
        try {
            logger.info("" + headers);

            if (headers.get("approval-status") == null || !headers.get("approval-status").toLowerCase().equals("approved")) {
                logger.error("===Comment not added to DB as the Review is not approved ===== ");
                return "Review is not yet approved ";
            }


            if (headers.get("user-id-email") != null)
                return reviewService.addComments(Integer.parseInt(product_id), comment, headers.get("user-id-email"));
            else {
                logger.error("==== No user email id details found in the header===== ");

                return "No user details found";
            }
        }
        catch (NumberFormatException e)
        {
            return product_rating_invalid ;
        }
    }

    @GetMapping("/by-average-ratings/{averageRatings}")
    public List<ReviewSearch> getProductsByAverageRatings(@PathVariable double averageRatings) {
        return reviewService.getProductsByAverageRatings(averageRatings);
    }

    @GetMapping("/{id}")
    public Optional<ReviewSearch> getById(@PathVariable String id) {
        return reviewService.findById(id);
    }

    @GetMapping("/high-ratings/{averageRatings}")
    public List<ReviewSearch> getProductsWithHigherRatings(@PathVariable double averageRatings) {
        return reviewService.getProductsWithHigherRatings(averageRatings);
    }   
    
    @PostMapping("/{reviewSearchId}/comments")
    public ResponseEntity<String> addCommentToReview(@PathVariable String reviewSearchId, @RequestBody User newComment, @RequestHeader Map<String,String> headers){
        try {
            logger.info("" + headers);

            if (headers.get("approval-status") == null || !headers.get("approval-status").toLowerCase().equals("approved")) {
                logger.error("===Comment not added to DB as the Review is not approved ===== ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Review is not yet approved");
            }

            if (headers.get("user-id-email") != null) {
                String result = reviewService.addCommentToReview(Integer.parseInt(reviewSearchId), newComment, headers.get("user-id-email"));
                // Assuming result is a success message or an error message
                HttpStatus httpStatus = result.contains("error") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
                return ResponseEntity.status(httpStatus).body(result);
            } else {
                logger.error("==== No user email id details found in the header===== ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No user details found");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(product_rating_invalid);
        }
    }
    
    /*
    @PostMapping("/{reviewId}/comments")
    public ReviewSearch addCommentToReview(
            @PathVariable String reviewId,
            @RequestBody Comment comment) {
        // Find the review by ID
        ReviewSearch review = reviewService.findById(reviewId).orElse(null);

        if (review != null) {
            // Add the new comment to the review
            Ratings ratings = review.getRatings();
            if (ratings == null) {
                ratings = new Ratings();
                review.setRatings(ratings);
            }

            List<Comment> comments = ratings.getComments();
            if (comments == null) {
                comments = new ArrayList<>();
                ratings.setComments(comments);
            }

            Comment newComment = new Comment(Comment.getuserId(), Comment.getComment(), Comment.getRate());
            comments.add(newComment);

            // Save the updated review back to the database
            return reviewService.save(review);
        } else {
            // Handle the case where the review with the given ID is not found
            return null;
        }
    }
         */
}
