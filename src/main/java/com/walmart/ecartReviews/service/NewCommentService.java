package com.walmart.ecartReviews.service;

import java.util.*;

import com.walmart.ecartReviews.model.*;
import com.walmart.ecartReviews.repository.NewCommentRepository;
import com.walmart.ecartReviews.repository.ProductRepository;

import com.walmart.ecartReviews.repository.ReviewRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NewCommentService {

    @Value("${comments.added}")
    private  String comments_added;
    @Value("${comments.rejected}")
    private  String comments_rejected;
    @Value("${unknown.product}")
    private String unknown_product;
    @Value("${mail.review.reject}")
    private  String mail_review_reject;
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
    public void processRejection(String productId, String mailId, String userId) {
        try {
            String productName = getProductNameById(Integer.parseInt(productId));
            sendEmail(mailId, productName,"reject");

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());

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
            sendEmail(mailId, productName,"addcomment");
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

    public void sendEmail(String mailId, String productName, String methodName) {
        try {

            //SimpleMailMessage message = new SimpleMailMessage();
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8");
            String approvalMsg="<html>\n" +
                    "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #58595b;background-color:#fff\" width=\"600\">\n" +
                    "\t<tbody>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td>\n" +
                    "\t\t\t<p style=\"margin:0;font:14px arial;color:#231f20;padding:0 25px;line-height:18px;text-align:center\"><b>Thanks for sharing your feedback, your feedback matters to us!</b></p>\n" +
                    "\t\t\t<p style=\"font:14px arial;color:#231f20;padding:0 25px;line-height:18px;text-align:justify\">Your comments are currently under review.<br>\n" +
                    "\t\t\t\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\tPlease be patient, we will let you know once your comment has been approved<br>\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\tFor any queries or clarification please call us on 9999999999 or write to us at <a href=\"mailto:swethag196@gmail.com\" style=\"color:#231f20\" target=\"_blank\">customerfeedback@ecart.com</a><br>\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\tBest Regards,<br>\n" +
                    "\t\t\teCart System</p>\n" +
                    "\t\t\t</td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td align=\"center\"><img alt=\"\" src=\"https://st3.depositphotos.com/2274151/36576/v/450/depositphotos_365760986-stock-illustration-review-stamp-review-vintage-blue.jpg\" class=\"CToWUd\" data-bit=\"iit\" width=598 height=150></td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t\n" +
                    "\t</tbody>\n" +
                    "</table>\n" +
                    "</html>";

            String rejectMsg="<html>\n" +
                    "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #58595b;background-color:#fff\" width=\"600\">\n" +
                    "\t<tbody>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td>\n" +
                    "\t\t\t<p style=\"margin:0;font:14px arial;color:#231f20;padding:0 25px;line-height:18px;text-align:center\"><b>Thanks for sharing your feedback, your feedback matters to us!</b></p>\n" +
                    "\t\t\t<p style=\"font:14px arial;color:#231f20;padding:0 25px;line-height:18px;text-align:justify\">Your comments are rejected<br>\n" +
                    "\t\t\t\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\tThank you for submitting your review. However, your review could not be approved as it could not meet our guidelines<br>\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\tFor any queries or clarification please call us on 9999999999 or write to us at <a href=\"mailto:swethag196@gmail.com\" style=\"color:#231f20\" target=\"_blank\">customerfeedback@ecart.com</a><br>\n" +
                    "\t\t\t<br>\n" +
                    "\t\t\tBest Regards,<br>\n" +
                    "\t\t\teCart System</p>\n" +
                    "\t\t\t</td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td align=\"center\"><img alt=\"\" src=\"https://media.istockphoto.com/id/1406937965/vector/color-megphone-icon-with-word-thank-you-in-white-banner-on-blue-background.jpg?s=612x612&w=0&k=20&c=pN0OFPO1I9K0_2iKT98RYAb0YBXc0PH8rpdvQAvCtEg=\" class=\"CToWUd\" data-bit=\"iit\" width=598 height=98></td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t\n" +
                    "\t</tbody>\n" +
                    "</table>\n" +
                    "</html>";

            message.setFrom(mail_fromID);
            message.setTo(mailId);

            if ("addcomment".equals(methodName)) {
                message.setSubject(mail_review_subject + productName);
                message.setText(approvalMsg,true);
            } else {
                message.setSubject(mail_review_reject + productName);
                message.setText(rejectMsg,true);
            }


            //javaMailSender.send(message);

            //message.setText(mail_sendBody);

            javaMailSender.send(mimeMessage);
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
