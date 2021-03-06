/*
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.mcgill.cs.creco.data.Attribute;
import ca.mcgill.cs.creco.data.Category;
import ca.mcgill.cs.creco.data.IDataStore;
import ca.mcgill.cs.creco.data.Product;

 /**
 * This class handles the extraction of most relevant attribute from a category.
 * This class is a Bean (singleton) and it is created at startup. You should access this class
 * to get information on attributes within a category such as attribute entropy
 * and correlation with overall score. Unmodifiable lists are returned and ScoredAttribtues 
 * are immutable. If a category is not found will on request the extractor will
 * return an empty list, and log an error.
 */
@Component
public class AttributeExtractor
{
	/** Sorting methods for attributes. */
	public static enum SORT_METHOD
	{ ENTROPY, SCORE, CORRELATION }
	private static final SORT_METHOD DEFAULT_SORT = SORT_METHOD.ENTROPY;
	private static final Logger LOG = LoggerFactory.getLogger(AttributeExtractor.class);
 	private IDataStore aDataStore;
	private HashMap<String, ArrayList<ScoredAttribute>> aAllAttributes;
	/** Constructor that takes a category.
	 * @param pDataStore the whole space of interesting products
	 */
	@Autowired
	public AttributeExtractor(IDataStore pDataStore)
	{
		aDataStore = pDataStore;
		aAllAttributes = new HashMap<String, ArrayList<ScoredAttribute>>();
		for(Category cat : aDataStore.getCategories())
		{
			ArrayList<ScoredAttribute> scoredAttributeList = new ArrayList<ScoredAttribute>();
			HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();
			for(Product prod : cat.getProducts())
			{
				for(Attribute att : prod.getAttributes())
				{
					attributes.put(att.getId(), att);
				}
			}
			for(String key : attributes.keySet())
			{
				ScoredAttribute sa = null;
				try
				{
					sa = new ScoredAttribute(attributes.get(key), cat);
					sa.getAttributeID();
				}
				catch(IllegalArgumentException iae)
				{
					LOG.error(iae.toString());
				}
				
				scoredAttributeList.add(sa);
			}
			sort(DEFAULT_SORT, scoredAttributeList);
			aAllAttributes.put(cat.getId(), scoredAttributeList);

		}
		
		
		
	}
	

	/**
	 * Call this method to get the list of scored Attributes ranked from most important
	 * to least important by DEFAULT_SORT
	 * can return null pointers if it doesn't have any attributes to work with.
	 * @param pCatID Id of the category you want
	 * @return list of scored attributes ranked from most important
	 * to least important (by default).If the category is not found will return an
	 * empty list, and log an error.
	 */
	public List<ScoredAttribute> getAttributesForCategory(String pCatID) 
	{
		ArrayList<ScoredAttribute> scoredAttributeList = aAllAttributes.get(pCatID);
		if(scoredAttributeList != null)
		{
			sort(SORT_METHOD.CORRELATION, scoredAttributeList);
			return Collections.unmodifiableList(scoredAttributeList);
		}		
		LOG.error("Category("+pCatID+") not found returning empty ScoredAttribute List");
		return Collections.unmodifiableList(new ArrayList<ScoredAttribute>());
	}
	/**
	 * Call this method to get the list of scored Attributes ranked from most important
	 * to least important by the passed sort method.
	 * @param pCatID Id of the category you want
	 * @param pSortMethod Sort logic you want the list to be sorted by
	 * can return null pointers if it doesn't have any attributes to work with.
	 * @return list of scored attributes ranked from most important
	 * to least important (by passed method). If the category is not found will return an
	 * empty list, and log an error.
	 */
	public List<ScoredAttribute> getAttributesForCategory(String pCatID, SORT_METHOD pSortMethod) 
	{
		ArrayList<ScoredAttribute> scoredAttributeList = aAllAttributes.get(pCatID);
		if(scoredAttributeList != null)
		{
			sort(pSortMethod, scoredAttributeList);
			return Collections.unmodifiableList(scoredAttributeList);
		}	
		LOG.error("Category("+pCatID+") not found returning empty ScoredAttribute List");	
		return Collections.unmodifiableList(new ArrayList<ScoredAttribute>());
	}
	
	/**
	 * Call this method to get the a single ScoredAttribute for a specified category
	 * can return null pointers if it doesn't find the category or the attribute.
	 * @param pCatID Id of the category you want
	 * @param pAttributeID Id of the attribute you want
	 * @return ScoredAttribute with ID pAttributeID
	 */
	public ScoredAttribute getScoredAttributeInCategory(String pCatID, String pAttributeID) 
	{
		ArrayList<ScoredAttribute> scoredAttributeList = aAllAttributes.get(pCatID);
		if(scoredAttributeList != null)
		{
			for(ScoredAttribute sa : scoredAttributeList)
			{
				if(sa.getAttributeID().equals(pAttributeID))
				{
					return sa;
				}
			}
			LOG.error("AttributeID: " + pAttributeID + " Not Found in CategoryID: " +pCatID );	
		}	
		else
		{
			LOG.error("Category("+pCatID+") not found returning default null ScoredAttribute");	
		}
		
		return new ScoredAttribute();
	}
	
	private void sort(SORT_METHOD pSortMethod, ArrayList<ScoredAttribute> pScoredAttributeList)
	{
		if (pSortMethod == SORT_METHOD.CORRELATION)
		{
			Collections.sort(pScoredAttributeList, ScoredAttribute.SORT_BY_CORRELATION);

		}
		else if (pSortMethod == SORT_METHOD.SCORE)
		{
			Collections.sort(pScoredAttributeList, ScoredAttribute.SORT_BY_SCORE);
		}
		else
		{
			Collections.sort(pScoredAttributeList, ScoredAttribute.SORT_BY_ENTROPY);
		}
		
	}

}
