package com.walmart.ecartReviews.service;

import java.util.*;

import com.walmart.ecartReviews.model.*;
import com.walmart.ecartReviews.repository.NewCommentRepository;
import com.walmart.ecartReviews.repository.ProductRepository;

import com.walmart.ecartReviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NewCommentService {

    @Value("${comments.added}")
    private  String comments_added;
    @Value("${unknown.product}")
    private String unknown_product;

    @Value("${mail.fromID}")
    private  String mail_fromID;
    @Value("${mail.review.subject}")
    private  String mail_review_subject;
    @Value("${mail.sendBody}")
    private  String mail_sendBody;
    @Autowired
    private JavaMailSender javaMailSender;
    private final NewCommentRepository newCommentRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Autowired

    public NewCommentService(NewCommentRepository newCommentRepository, ProductRepository productRepository,ReviewRepository reviewRepository) {
        this.newCommentRepository = newCommentRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }



    public List<NewComment> getAllreview() {
        try {
            return newCommentRepository.findAll();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }


    public List<NewComment> findByproductId(int productId) {
        try {
            return newCommentRepository.findByproductId(productId);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }

    }


    public Object addComment(int productId, NewComment newComment, String mailId) {
        try {
            // Check if the productId already exists
            List<NewComment> existingComments = newCommentRepository.findByproductId(productId);

            // Set the productId for the new comment
            newComment.setProductId(productId);
            newComment.setMail(mailId);

            List<Comment> newComments = newComment.getComments();

            // Process each new comment individually
            for (Comment comment : newComments) {
                String userId = comment.getUser().getUserId();

                // Check if the productId and userId already exist in comments
                if (isProductIdAndUserIdExists(existingComments, productId, userId)) {
                    // ProductId and UserId exist, overwrite the existing comment with new comment
                    overwriteComment(existingComments, productId, userId, newComments);
                    return "Comment for productId " + productId + " and userId " + userId + " overwritten successfully.";
                } else {
                    // ProductId or UserId doesn't exist, create a new entry
                    NewComment newCommentEntry = new NewComment();
                    newCommentEntry.setProductId(productId);
                    newCommentEntry.setMail(mailId);
                    newCommentEntry.setComments(Collections.singletonList(comment));
                    newCommentRepository.save(newCommentEntry);
                }
            }
            // Fetch the product name using the productId
            String productName = getProductNameById(productId);
            sendEmail(mailId, productName);
            return comments_added;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }
    private String getProductNameById(int productId) {
        try {
            // Assuming you have a method to fetch the product by productId
            List<ReviewSearch> products = reviewRepository.findByReviewSearchId(productId);

            // Check if the list is not empty
            if (!products.isEmpty()) {
                // Get the first item from the list
                ReviewSearch product = products.get(0);

                // Assuming ReviewSearch has a getProductName() method or productName property
                return product.getProductName();
            } else {
                return unknown_product;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }



    // Helper method to check if productId and userId already exist in comments
    private boolean isProductIdAndUserIdExists(List<NewComment> existingComments, int productId, String userId) {
                return existingComments != null && existingComments.stream()
                .anyMatch(comment -> comment.getProductId() == productId &&
                        comment.getComments().stream()
                                .anyMatch(c -> userId.equals(c.getUser().getUserId())));
    }

    // Helper method to overwrite comments for existing productId and userId
    private void overwriteComment(List<NewComment> existingComments, int productId, String userId, List<Comment> newComments) {
        try {
            existingComments.stream()
                    .filter(comment -> comment.getProductId() == productId &&
                            comment.getComments().stream()
                                    .anyMatch(c -> userId.equals(c.getUser().getUserId())))
                    .findFirst()
                    .ifPresent(comment -> {
                        // Overwrite the existing comment with new comment
                        comment.getComments().clear();
                        comment.getComments().addAll(newComments);
                        newCommentRepository.save(comment);
                    });
        }
        catch (NumberFormatException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void sendEmail(String mailId, String productName) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mail_fromID);
            message.setTo(mailId);
            message.setSubject(mail_review_subject + productName);
            message.setText(mail_sendBody);
            javaMailSender.send(message);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    public boolean deleteComment(int productId, String userId) {
        // Check if the data exists for the specified productId and userId
        List<NewComment> comments = newCommentRepository.findByProductIdAndCommentsUserUserId(productId, userId);

        if (!comments.isEmpty()) {
            NewComment foundComment = comments.get(0);
            // Assuming NewCommentRepository has a method to delete by productId and userId
            newCommentRepository.deleteByProductIdAndCommentsUserUserId(productId, userId);

             return true;

        }

        return false;
    }
}