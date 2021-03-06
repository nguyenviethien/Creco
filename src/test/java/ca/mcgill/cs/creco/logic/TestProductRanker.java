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
package ca.mcgill.cs.creco.logic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ca.mcgill.cs.creco.data.Category;
import ca.mcgill.cs.creco.data.IDataStore;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/test-context.xml"})
public class TestProductRanker {
	
	@Autowired
	IDataStore aDataStore;
	
	@Autowired
	AttributeExtractor aAttributeExtractor;
	
	@Autowired
	ProductRanker aProductRanker;
	
	private static String TOASTER_CATEGORY_ID = "28732";
	private static int TOASTER_CATEGORY_NUM_PRODUCTS = 407;
	
	private static final String HUMIDIFIER_CATEGORY_ID = "32968";
	private static final String HUMIDIFIER_OUTPUT_ID = "4556";
	private static final String HUMIDIFIER_FULL_TANK_WEIGHT_ID = "6929";

	@Test
	public void testRankingContainsAllProducts()
	{    
		Category category = aDataStore.getCategory(TOASTER_CATEGORY_ID); 
		List<ScoredAttribute> scoredAttributes = aAttributeExtractor.getAttributesForCategory(category.getId());
		List<UserScoredAttribute> userScoredAttributes = new ArrayList<UserScoredAttribute>();
		for(ScoredAttribute sa : scoredAttributes)
		{
			userScoredAttributes.add(new UserScoredAttribute(sa , 0));
		}
		
		List<RankExplanation> scoredProducts = aProductRanker.rankProducts(userScoredAttributes, category);
		
		assertEquals(TOASTER_CATEGORY_NUM_PRODUCTS, scoredProducts.size());
	}
	
	@Test
	public void testRankNumericMoreIsBetter()
	{
		Category category = aDataStore.getCategory(HUMIDIFIER_CATEGORY_ID); 
		UserScoredAttribute humidifierOutputAttribute = new UserScoredAttribute(
								aAttributeExtractor.getScoredAttributeInCategory(HUMIDIFIER_CATEGORY_ID, HUMIDIFIER_OUTPUT_ID),
								1);
		
		List<UserScoredAttribute> userScoredAttributes = new ArrayList<UserScoredAttribute>();
		userScoredAttributes.add(humidifierOutputAttribute);
		
		List<RankExplanation> scoredProducts = aProductRanker.rankProducts(userScoredAttributes, category);
		
		// Highest humidifier output should be 5.0/5.0
		assertEquals(scoredProducts.get(0).getaProduct().getAttribute(HUMIDIFIER_OUTPUT_ID).getTypedValue().getNumeric(), 5.0, 0.00001);
	}
	
	@Test
	public void testRankNumericLessIsBetter()
	{
		Category category = aDataStore.getCategory(HUMIDIFIER_CATEGORY_ID); 
		
		ScoredAttribute humidifierWeightAttribute = aAttributeExtractor.getScoredAttributeInCategory(HUMIDIFIER_CATEGORY_ID, HUMIDIFIER_FULL_TANK_WEIGHT_ID);
		
		List<UserScoredAttribute> userScoredAttributes = new ArrayList<UserScoredAttribute>();
		userScoredAttributes.add(new UserScoredAttribute(humidifierWeightAttribute,1));
		
		List<RankExplanation> scoredProducts = aProductRanker.rankProducts(userScoredAttributes, category);
		
		// Lowest weight is 7.5
		assertEquals(scoredProducts.get(0).getaProduct().getAttribute(HUMIDIFIER_FULL_TANK_WEIGHT_ID).getTypedValue().getNumeric(), 7.5, 0.0001);	
	}
	
	@Test
	public void testRankByTwoNumeric()
	{
		Category category = aDataStore.getCategory(HUMIDIFIER_CATEGORY_ID); 
		
		ScoredAttribute humidifierOutputAttribute = aAttributeExtractor.getScoredAttributeInCategory(HUMIDIFIER_CATEGORY_ID, HUMIDIFIER_OUTPUT_ID);
		ScoredAttribute humidifierWeightAttribute = aAttributeExtractor.getScoredAttributeInCategory(HUMIDIFIER_CATEGORY_ID, HUMIDIFIER_FULL_TANK_WEIGHT_ID);
		
		List<UserScoredAttribute> userScoredAttributes = new ArrayList<UserScoredAttribute>();
		userScoredAttributes.add(new UserScoredAttribute(humidifierWeightAttribute,1));
		userScoredAttributes.add(new UserScoredAttribute(humidifierOutputAttribute,1));
		
		List<RankExplanation> scoredProducts = aProductRanker.rankProducts(userScoredAttributes, category);
		
		assertEquals(scoredProducts.get(0).getaProduct().getName(), "Safety 1st 49292");
	}
	
}
