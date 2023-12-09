package com.walmart.ecartReviews.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Annotations
@Data

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_collection")

//Class
public class ReviewSearch 
{
	@Id
	private String id;
	@Field("id")
	private int reviewSearchId;
    public int getReviewSearchId() {
		return reviewSearchId;
	}
	public void setReviewSearchId(int reviewSearchId) {
		this.reviewSearchId = reviewSearchId;
	}

	private String title;
	private int minQuantity;

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	private String gender;
	private String iconUrl;
	private String shortDescription;
	private String longDescription;
	private String productName;
	private String productCategory;
	private String inventryStatus;
	private int availableQuantity;
	private String purchasable;
	private List<String> searchTags;
	private String model;
	private String brand;
	private String specification;
	private int warrantyDuration;
	private List<String> imageUrls;
	private int orderLimit;
	private int returnDates;
	private int length;
	private int width;
	private int height;
	private double weight;
	private Ratings ratings;
	private boolean isElegibileForPromotion;
	private int discount;
	private String isHazardous;
	private String isReturnable;


}


