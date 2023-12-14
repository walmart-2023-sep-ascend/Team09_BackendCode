package com.walmart.ecartReviews.service;

import java.util.*;

import com.walmart.ecartReviews.model.*;
import com.walmart.ecartReviews.repository.ProductRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.walmart.ecartReviews.repository.ReviewRepository;

@Service
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Value("${unknown.product}")
    private String unknown_product;
    @Value("${product.not.found}")
    private String product_not_found;
    @Value("${ratings.not.found}")
    private String ratings_not_found;
    @Value("${comments.added}")
    private  String comments_added;
    @Value("${mail.fromID}")
    private  String mail_fromID;
    @Value("${mail.subject}")
    private  String mail_subject;
    @Value("${mail.sendBody}")
    private  String mail_sendBody;
    private final ReviewRepository reviewRepository;


    @Autowired
    private JavaMailSender javaMailSender;
    private final ProductRepository productRepository;


    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public List<ReviewSearch> getAllreviews()
    {
        try
        {
            return reviewRepository.findAll();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    public Optional<ReviewSearch> findById(String id)
    {
        try
        {
            return reviewRepository.findById(id);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }   
    
    public List<ReviewSearch> findByReviewSearchId(int reviewSearchId)
    {
        try
        {
            return reviewRepository.findByReviewSearchId(reviewSearchId);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }  

    public List<ReviewSearch> getProductsByAverageRatings(double averageRatings)
    {
        try
        {
            return reviewRepository.findProductsByAverageRatings(averageRatings);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    	
    }
   /* List<ReviewSearch> allReviews = getAllreviews();
        return allReviews.stream()	
                .filter(reviews -> {Ratings ratings = reviews.getRatings();
                    if (ratings != null) {
                        Double averageRatingsFromReview = ratings.getAverageRatings();
                        return averageRatingsFromReview != null && averageRatingsFromReview == averageRatings;
                    }
                    return false; // Filter out reviews with null ratings
                })
                .collect(Collectors.toList());
    
    	}
*/
    public List<ReviewSearch> getProductsWithHigherRatings(double averageRatings)
    {
        try {
            //double threshold = 4.0; // The minimum averageRatings value
            return reviewRepository.findByAverageRatingsGreaterThan(averageRatings);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }
/*
	public ReviewSearch save(ReviewSearch review) {
		// TODO Auto-generated method stub
		return null;
	}
*/
public String addCommentToReview(int reviewSearchId, User newComment, String mailId) {
    try {
        List<ReviewSearch> existingReview = reviewRepository.findByReviewSearchId(reviewSearchId);

        if (existingReview != null && !existingReview.isEmpty()) {
            // Assuming there's only one review with the given reviewSearchId
            ReviewSearch review = existingReview.get(0);

            Ratings ratings = review.getRatings();
            if (ratings == null) {
                // If there are no ratings yet, create a new Ratings object
                ratings = new Ratings();
                review.setRatings(ratings);
            }
            List<Comment> comments = ratings.getComments();
            if (comments == null) {
                // If there are no comments yet, create a new list
                comments = new ArrayList<>();
                ratings.setComments(comments);
            }

            // Assuming newComment is the Comment object from the request body
            User newUser = new User();
            newUser.setUserId(newComment.getUserId());
            newUser.setComment(newComment.getComment());
            newUser.setRate(newComment.getRate());
            // Create a new Comment object and set the User
            Comment comment = new Comment();
            comment.setUser(newUser);

            // Add the new comment to the list of comments
            comments.add(comment);
            // Save the updated review to the database
            reviewRepository.save(review);
            // Fetch the product name using the productId
            String productName = getProductNameById(reviewSearchId);
            sendEmail(mailId, productName);
            return comments_added;
        } else {
            // Handle the case when no review is found with the given reviewSearchId
            // You can throw an exception or return an appropriate response.
            return null; // For simplicity, returning null here.
        }
        // Fetch the product name using the productId

    } catch (Exception e) {
        logger.error("Error adding comment to review: " + e.getMessage());
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

    public Object getCommentsForGivenRating(int productId, int averageRatings)
    {
        try
        {
            List<ReviewSearch> product = reviewRepository.findByReviewSearchId(productId);
            if (product == null || product.isEmpty()) {
                logger.info(product_not_found);
                return product_not_found;
            }
            Ratings ratings = !product.isEmpty() ? product.get(0).getRatings() : null;
            List<User> resultObj = new ArrayList<>();
            if (ratings != null && ratings.getComments() != null) {
                for (Comment comment : ratings.getComments()) {
                    User user = comment.getUser();

                    if (user.getRate() >= averageRatings) {
                        resultObj.add(user);
                    }
                }
            } else {
                return ratings_not_found;
            }

            return resultObj.size() > 0 ? resultObj : ratings_not_found;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }

    public Object addComments(int productId, Comment comment, String mailId) {

        try {
            List<Product> productList = productRepository.findById(productId);
            if (productList == null) {
                return product_not_found;

            }
            Product product = productList.get(0);

            Ratings ratings = product.getRatings();
            if (ratings != null) {
                if (ratings.getComments().isEmpty()) {
                    ratings.setComments(Arrays.asList(comment));
                } else {
                    ratings.getComments().add(comment);
                }
                productRepository.save(product);
            }


            sendEmail(mailId, productList.get(0).getProductName());
            return comments_added;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }




    public void sendEmail(String mailId, String productName)
    {
        try {
            //SimpleMailMessage message = new SimpleMailMessage();
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8");
            String htmlMsg="<html>\n" +
                    "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #58595b;background-color:#fff\" width=\"600\">\n" +
                    "\t<tbody>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td>\n" +
                    "\t\t\t<p style=\"margin:0;font:14px arial;color:#231f20;padding:0 25px;line-height:18px;text-align:center\"><b>Thanks for sharing your feedback, your feedback matters to us!</b></p>\n" +
                    "\t\t\t<p style=\"font:14px arial;color:#231f20;padding:0 25px;line-height:18px;text-align:justify\">This is to inform that, your comment has been published to the product page successfully<br>\n" +
                    "\t\t\t\n" +
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
            message.setSubject(mail_subject + productName);
            message.setText(htmlMsg,true);
            javaMailSender.send(mimeMessage);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }



//    public void sendEmail1() {
//        jakarta.mail.internet.MimeMessage mail = javaMailSender.createMimeMessage();
//        try{
//            MimeMessageHelper helper = new MimeMessageHelper(mail,true);
//helper.setFrom("danicoolbug@gmail.com");
//            helper.setTo("rajesh.ramakrishnan16589@gmail.com");
//            helper.setSubject("Test Email");
//            helper.setText("This is a test email sent from Spring Boot.");
//
//            javaMailSender.send(helper.getMimeMessage());
//        }
//        catch (Exception e ){
//            System.out.println(e);
//
//        }
////        SimpleMailMessage message = new SimpleMailMessage();
////        message.setTo("rajesh.ramakrishnan16589@gmail.com");
////        message.setSubject("Test Email");
////        message.setText("This is a test email sent from Spring Boot.");
//
////        javaMailSender.send(message);
//    }
//    private void sendmail(String mail) {
//        String to = "rajesh.ramakrishnan16589@gmail.com";
//        String from = "danicoolbug@gmail.com";
//        String host = "smtp.gmail.com";
//
//
////        MimeMessage maill = javaMailSender.createMimeMessage();
//
//
//        //Get the session object
//        Properties properties = System.getProperties();
//        properties.setProperty("mail.smtp.host", host);
//        properties.setProperty("spring.mail.port","587");
//        properties.setProperty("spring.mail.properties.mail.smtp.auth","true");
//        properties.setProperty("spring.mail.properties.mail.smtp.starttls.enable","true");
//
//        Session session = Session.getDefaultInstance(properties);
//
//        //compose the message
//        try{
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(from));
//            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
//            message.setSubject("Ping");
//            message.setText("Hello, this is example of sending email  ");
//
//            // Send message
//            Transport.send(message);
//            System.out.println("message sent successfully....");
//
//        }catch (MessagingException mex) {
//            System.out.println(""+mex);}
//            System.out.println(""+mex);}
//    }
}
