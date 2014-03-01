/**
 * Copyright 2014 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.mcgill.cs.creco.data;

import java.util.Collections;
import java.util.HashMap;

import ca.mcgill.cs.creco.data.json.ProductStub;
import ca.mcgill.cs.creco.data.json.RatingStub;
import ca.mcgill.cs.creco.data.json.SpecStub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Product 
{
	// Fields directly copied from CR Data fields
	private String id;
	private String displayName;
	private String review;
	private String highs;
	private String lows;
	private String bottomLine;
	private String description;
	private String dontBuyType;
	private Double overallScoreMax;
	private Double overallScoreMin; 
	private Double overallScore;
	private Boolean isRecommended;
	private Boolean isBestSeller;
	private Boolean isTested;
	private Boolean isBestBuy;
		
	// Derived fields
	private int numRatings;
	private HashMap<String, Rating> ratings;
	private HashMap<String, Spec> specs;
	private String brandId;
	private String brandName;
	private Double price;
	private String categoryId;
	private Category category;
	
	Product(ProductStub prodStub) 
	{
		// Copy fields from the stub
		this.id = prodStub.id;                     
		this.displayName = prodStub.displayName;            
		this.review = prodStub.review;                 
		this.highs = prodStub.highs;                  
		this.lows = prodStub.lows;                   
		this.bottomLine = prodStub.bottomLine;             
		this.description = prodStub.description;            
		this.dontBuyType = prodStub.dontBuyType;            
		this.overallScoreMax = prodStub.overallScoreMax;
		this.overallScoreMin = prodStub.overallScoreMin;
		this.overallScore = prodStub.overallScore;   
		if(prodStub.price != null)
		{
			this.price = prodStub.price.value;
		}
		this.isRecommended = prodStub.isRecommended; 
		this.isBestSeller = prodStub.isBestSeller;  
		this.isTested = prodStub.isTested;      
		this.isBestBuy = prodStub.isBestBuy;
		
		// Calculate derived fields
		this.categoryId = prodStub.category.id;
		this.brandId = (prodStub.brand != null)? prodStub.brand.id : null;
		this.brandName = (prodStub.brand != null)? prodStub.brand.displayName : null;
		
		this.specs = new HashMap<String, Spec>();
		if(prodStub.specs != null)
		{
			for(SpecStub spec : prodStub.specs)
			{
				this.specs.put(spec.attributeId, new Spec(spec));
			}
		}
		
		this.numRatings = 0;
		this.ratings = new HashMap<String, Rating>();
		if(prodStub.ratings != null)
		{
			for(RatingStub rating : prodStub.ratings)
			{
				this.ratings.put(rating.attributeId, new Rating(rating));
				this.numRatings++;
			}
		}
	}
	
	void setCategory(Category pCategory)
	{
		category = pCategory;
	}
	
	/**
	 * @return The category ID.
	 */
	public String getCategoryId() 
	{ return categoryId; }
	
	/**
	 * @return The number of ratings.
	 */
	public int getNumRatings() 
	{ return numRatings; }

	/**
	 * @return The product ID.
	 */
	public String getId() 
	{ return id; }

	/**
	 * @return The display name of the product.
	 */
	public String getName() 
	{ return displayName; }
	
	public String getReview() {
		return review;
	}

	public String getHighs() {
		return highs;
	}

	public String getLows() {
		return lows;
	}

	public String getBottomLine() {
		return bottomLine;
	}

	public String getDescription() {
		return description;
	}

	public String getDontBuyType() {
		return dontBuyType;
	}

	public Double getOverallScoreMax() {
		return overallScoreMax;
	}

	public Double getOverallScoreMin() {
		return overallScoreMin;
	}

	public Double getOverallScore() {
		return overallScore;
	}

	public Boolean getIsRecommended() {
		return isRecommended;
	}

	public Boolean getIsBestSeller() {
		return isBestSeller;
	}

	public Boolean getIsTested() {
		return isTested;
	}

	public Boolean getIsBestBuy() {
		return isBestBuy;
	}

	public String getBrandId() {
		return brandId;
	}

	public String getBrandName() {
		return brandName;
	}

	public Double getPrice() {
		return price;
	}

	public Category getCategory() {
		return category;
	}

	public Iterable<Rating> getRatings() {
		return Collections.unmodifiableCollection(Product.this.ratings.values());
	}
	
	public Rating getRating(String id)
	{
		return this.ratings.get(id);
	}
	
	public Iterable<Spec> getSpecs() {	
		return Collections.unmodifiableCollection(Product.this.specs.values());
	}
	
	public Spec getSpec(String id)
	{
		return this.specs.get(id);
	}
	
	public String dump() 
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this);
	}	
}
