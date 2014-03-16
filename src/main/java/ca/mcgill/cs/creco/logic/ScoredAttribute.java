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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ca.mcgill.cs.creco.data.Attribute;
import ca.mcgill.cs.creco.data.AttributeStat;
import ca.mcgill.cs.creco.data.CategoryBuilder;
import ca.mcgill.cs.creco.data.Category;
import ca.mcgill.cs.creco.data.IDataStore;
import ca.mcgill.cs.creco.data.TypedValue;

import com.google.common.collect.Lists;

/**
 *
 */
public class ScoredAttribute 
{
	
	
	public static final Comparator<ScoredAttribute> SORT_BY_SCORE = 
	new Comparator<ScoredAttribute>() 
    {
		
		/**
		 * Compare a score placing the highest score first.
		 * @param pA score A
		 * @param pB score B
		 * @return -1 for A > B, 1 for B > A, 0 if A==B
		 */
		@Override
        public int compare(ScoredAttribute pA, ScoredAttribute pB) 
        {
        	if(pA.getAttributeScore() >  pB.getAttributeScore())
        	{
        		return -1;
        	}
        	else if(pA.getAttributeScore() < pB.getAttributeScore())
        	{
        		return 1;
        	}
        	else
        	{
        		return 0;
        	}
        }

     };
	
 	@Autowired
 	private IDataStore aDataStore;
    private String aAttributeID;
 	private String aAttributeName;
 	private double aAttributeScore;
 	private TypedValue aAttributeMean;
 	private boolean aIsCat;

 	private String aCategoryID;
 	private String aAttributeDesc;
	   

	/**Constructor from an attribute.
	 * @param pAttribute attribute to build score for.
	 * @param Category in which the attribute is present
	 */
	public ScoredAttribute(Attribute pAttribute, Category pCat)
	{
		aIsCat = false;
		aAttributeID = pAttribute.getId();
		aAttributeScore = 0.0;
		aAttributeName = pAttribute.getName();
		aCategoryID = pCat.getId();

		aAttributeDesc = pAttribute.getDescription();

	}
		
	/**
	 * @return attribute or category ID check first
	 * @see isCat()
	 */
	public String getAttributeID() 
	{
		return aAttributeID;
	}

	/**
	 * @return the score of the attribute as a double
	 * 
	 */
	public double getAttributeScore() 
	{
		return aAttributeScore;
	}

	/**
	 * @param pAttributeScore score to set the attribute to
	 */
	public void setAttributeScore(double pAttributeScore)
	{
		this.aAttributeScore = pAttributeScore;
	}

	/**
	 * @return name of the attribute as String
	 */
	public String getAttributeName() 
	{
		return aAttributeName;
	}

	@Override
	public String toString()
	{
		return aAttributeName + ", " + aAttributeID + ", "+ aAttributeDesc +": " + aAttributeScore + ", " + aAttributeMean + "||";
	}

	/**
	 * @return mean or mode of this attribute given a product list used to 
	 * calculate the score
	 */
	public TypedValue getAttributeMean() 
	{
		return aAttributeMean;
	}

	/**
	 * @param pAttributeMean mean or mode of this attribute given a product list used to 
	 * calculate the score
	 */
	public void setAttributeMean(TypedValue pAttributeMean) 
	{
		this.aAttributeMean = pAttributeMean;
	}
	/**
	 * @return true if the attribute was derived from a cat
	 */
	public boolean isCat()
	{
		return aIsCat;
	}
	
	public TypedValue getMin()
	{
		AttributeStat a = null;
		try
		{
			a = aDataStore.getCategory(aCategoryID).getSpecification(aAttributeID);
		}
		catch(NullPointerException npe)
		{
			try
			{
			a = aDataStore.getCategory(aCategoryID).getRating(aAttributeID);
			}
			catch(NullPointerException npe2)
			{
				
			}
		}
		if(a == null)
		{
			return new  TypedValue(0);
		}
		return new TypedValue(a.getValueMin());
	}
	
	public TypedValue getMax()
	{
		AttributeStat a = null;
		try{
			a = aDataStore.getCategory(aCategoryID).getSpecification(aAttributeID);
		}
		catch(NullPointerException npe)
		{
			try
			{
			a = aDataStore.getCategory(aCategoryID).getRating(aAttributeID);
			}
			catch(NullPointerException npe2)
			{
				
			}
		}
		if(a == null)
		{
			return new TypedValue(0);
		}
		return new TypedValue(a.getValueMax());
	}
	
	public List<String> getDict()
	{
		AttributeStat a = null;
		try
		{
			a = aDataStore.getCategory(aCategoryID).getSpecification(aAttributeID);
		}
		catch(NullPointerException npe)
		{
			try
			{
			a = aDataStore.getCategory(aCategoryID).getRating(aAttributeID);
			}
			catch(NullPointerException npe2)
			{
				
			}
		}
		if(a == null)
		{
			return new ArrayList<String>();
		}
		return Lists.newArrayList(a.getValueEnum());
	}
	
	/**
	 * @return String representing the attribute description
	 * */	
	public String getAttributeDesc() 
	{
		return aAttributeDesc;
	}

	
	/**
	 * @param pAttributeDesc description of this attribute
	 * ***/	
	public void setAttributeDesc(String pAttributeDesc)
	{
		this.aAttributeDesc = pAttributeDesc;
	}
	
}
