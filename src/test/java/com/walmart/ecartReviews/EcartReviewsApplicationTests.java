package com.walmart.ecartReviews;


import com.walmart.ecartReviews.service.NewCommentService;
import com.walmart.ecartReviews.model.ReviewSearch;
import com.walmart.ecartReviews.service.ReviewService;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.*;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
class EcartReviewsApplicationTests {

	@Value("${product.not.found}")
	private String product_not_found;
	@Value("${ratings.not.found}")
	private String ratings_not_found;
	@Value("${comments.added}")
	private String comments_added;
	@Value("${review.not.approved}")
	private String review_not_approved;
	@Value("${no.user.found}")
	private String no_user_found;
	@Value("${comment.deleted.success}")
	private String comment_deleted_success;
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private NewCommentService newCommentService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@org.junit.jupiter.api.Order(1)
	public void testMain() {
		EcartReviewsApplication.main(new String[] {});
		assertTrue(true);
	}

	@Test
	@org.junit.jupiter.api.Order(2)
	public void test_getAllreviews() throws Exception
	{
		List<ReviewSearch> allreviewList = reviewService.getAllreviews();
		Assertions.assertThat(allreviewList.size()).isGreaterThan(0);

		mockMvc.perform(get("/api/product/"))
					.andExpect(status().isOk());
	}

	@Test
	@org.junit.jupiter.api.Order(3)
	public void test_productNotFound()
	{
		assertEquals(product_not_found, reviewService.getCommentsForGivenRating(3424,3));
	}

	@Test
	@org.junit.jupiter.api.Order(4)
	public void test_ratingNotFound()
	{
		assertEquals(ratings_not_found, reviewService.getCommentsForGivenRating(1,22323));
	}

	@Test
	@org.junit.jupiter.api.Order(5)
	public void test_getCommentsForGivenRating() throws Exception
	{
		List reviewList = (List) reviewService.getCommentsForGivenRating(1,3);
		Assertions.assertThat(reviewList.size()).isGreaterThan(0);

		mockMvc.perform(get("/api/product/1/by-ratings/3"))
				.andExpect(status().isOk());
	}

	@Test
	@org.junit.jupiter.api.Order(6)
	public void test_addNewCommentsWithoutHeader() throws Exception {
		//String requestBody = "{\"email\": \"arun777@gmail.com\", \"password\": \"123456\"}";
		String requestBody = "{\"user\": {\"userId\": \"Muthu\",\"comment\": \"Test comment 1\",\"rate\": 16}}";

		MvcResult mvcResult =mockMvc.perform(post("/api/product/1/comment/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andReturn();
		String resultStr=mvcResult.getResponse().getContentAsString();
		assertEquals(review_not_approved.trim(),resultStr.trim());

	}
	@Test
	@org.junit.jupiter.api.Order(7)
	public void test_addNewCommentsWithHeader() throws Exception {
		//String requestBody = "{\"email\": \"arun777@gmail.com\", \"password\": \"123456\"}";
		String requestBody = "{\"user\": {\"userId\": \"Muthu\",\"comment\": \"Test comment 1\",\"rate\": 16}}";

		MvcResult mvcResult =mockMvc.perform(post("/api/product/1/comment/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
						.header("user-id-email", "swethag196@gmail.com")
						.header("approval-status", "approved"))
				.andExpect(status().isOk())
				.andReturn();
		String resultStr=mvcResult.getResponse().getContentAsString();
		assertEquals(comments_added.trim(),resultStr.trim());

	}
	@Test
	@org.junit.jupiter.api.Order(8)
	public void test_cmt_getAllreview() throws Exception
	{
		List allCmtReviewList=newCommentService.getAllreview();
		Assertions.assertThat(allCmtReviewList.size()).isGreaterThan(0);

		mockMvc.perform(get("/api/approval/"))
				.andExpect(status().isOk());
	}

	@Test
	@org.junit.jupiter.api.Order(9)
	public void test_cmt_getByproductId() throws Exception
	{
		List productIDList=newCommentService.findByproductId(8);
		Assertions.assertThat(productIDList.size()).isGreaterThan(0);

		mockMvc.perform(get("/api/approval/productId/8"))
				.andExpect(status().isOk());
	}

	@Test
	@org.junit.jupiter.api.Order(10)
	public void test_cmt_addNewCommentWithHeader() throws Exception
	{
		String requestBody ="{\"productId\": 10,\"mail\":\"swetharaman196@gmail.com\",\"comments\": [{\"user\": {\"userId\": \"Muthu12\",\"comment\": \"Test product1\",\"rate\": 5	}}]}";
		MvcResult mvcResult =mockMvc.perform(post("/api/approval/1/comment")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody)
						.header("user-id-email", "swethag196@gmail.com"))
				.andExpect(status().isOk())
				.andReturn();

		String resultStr=mvcResult.getResponse().getContentAsString();
		assertEquals(comments_added.trim(),resultStr.trim());

	}
	@Test
	@org.junit.jupiter.api.Order(11)
	public void test_cmt_addNewCommentWithoutHeader() throws Exception {
		String requestBody = "{\"productId\": 1,\"mail\":\"swethag196@gmail.com\",\"comments\": [{\"user\": {\"userId\": \"Muthu\",\"comment\": \"Test product1\",\"rate\": 5}}]}";

		mockMvc.perform(post("/api/approval/1/comment")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isBadRequest())  // Update expectation to 400 for a Bad Request
				.andReturn();
	}

	@Test
	@org.junit.jupiter.api.Order(12)
	public void test_cmt_deleteComment() throws Exception
	{
			MvcResult mvcResult =mockMvc.perform(MockMvcRequestBuilders.delete("/api/approval/delete/1/Muthu12")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String resultStr=mvcResult.getResponse().getContentAsString();
		assertEquals(comment_deleted_success.trim(),resultStr.trim());

	}




}

