package com.walmart.ecartReviews.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.walmart.ecartReviews.model.Product;
import com.walmart.ecartReviews.service.ProductService;

@RestController
@CrossOrigin(origins = "http://172.172.241.64:3000")
@RequestMapping("/product")
public class ProductController {
	
	@Autowired
	private ProductService productService;
	
	@GetMapping
	public List<Product> findProductAll()
	{
	  return productService.findProductAll();
	}
	 
	@GetMapping("/productId/{productId}")
	public Product getProductById(@PathVariable int productId)
	{
		return productService.getProductById(productId);
	}
	
}
