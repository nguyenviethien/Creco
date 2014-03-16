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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.attributeSelection.AttributeSelection;
//import weka.attributeSelection.CfsSubsetEval;
//import weka.attributeSelection.GreedyStepwise;
//import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
//import weka.attributeSelection.UnsupervisedAttributeEvaluator;
//import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import ca.mcgill.cs.creco.data.AttributeStat;
import ca.mcgill.cs.creco.data.Category;
import ca.mcgill.cs.creco.data.Product;
import ca.mcgill.cs.creco.data.TypedValue;
import ca.mcgill.cs.creco.logic.search.ScoredProduct;

import com.google.common.collect.Lists;



 /**
 * This class handles the extraction of most relevant attribute from an equivalence class
 * with respect to a product list.
 * 
 * @see ProductList
 * @see Attribute
 */
public class AttributeExtractor
{

	private static final double DEFAULT_MIN = 10000000;
	private static final double DEFAULT_MAX = -10000000;
	private final Logger logger = LoggerFactory.getLogger(AttributeExtractor.class);
	private List<Product> aProductList;
	private Category aEquivalenceClass;
	private Iterable<AttributeStat> aSpecList;
	private Iterable<AttributeStat> aRatingList;
	private ArrayList<ScoredAttribute> aScoredSpecList;
	private ArrayList<ScoredAttribute> aScoredRatingList;

	/**Constructor that takes a ProductSearchResult and an equivalence class.
	 * @param pProductSearchResult a lucene result
	 * @param pEquivalenceClass the whole space of interesting products
	 */
	public AttributeExtractor(List<ScoredProduct> pProductSearchResult, Category pEquivalenceClass)
	{
		aProductList = new ArrayList<Product>();
		for (ScoredProduct scoredProduct : pProductSearchResult)
		{
			//split the products with LuceneScore > 0;
			if(scoredProduct.getLuceneScore() > 0.0)
			{
				aProductList.add(scoredProduct.getProduct());
			}
		}
		aEquivalenceClass = pEquivalenceClass;
		aSpecList = aEquivalenceClass.getSpecifications();
		aRatingList = aEquivalenceClass.getRatings();
	}
	/**Constructor that takes a product list and an equivalence class.
	 * @param pProductList subset of interesting products
	 * @param pEquivalenceClass the whole space of interesting products
	 */
	public AttributeExtractor(Set<Product> pProductList, Category pEquivalenceClass)
	{
		aProductList = Lists.newArrayList(pProductList);
		aEquivalenceClass = pEquivalenceClass;
		aSpecList = aEquivalenceClass.getSpecifications();
		aRatingList = aEquivalenceClass.getRatings();
	}
	/**
	 * Computes the mean value of the attribute given a list of products if this attribute is numerical,
	 * otherwise computes the mode of the value based on the same list of products.
	 * If the attribute is not homogeneous will either return the string value mode of N/A,
	 * when not string values are present
	 * NOTE: will eventually be implemented on the category also
	 * @param pProductList List of products on which to evaluate the attribute
	 * @param pAttributeID Id of the Attribute to be evaluated
	 * @return AttributeValue which corresponds to the stat of that attribute
	 */
	public static TypedValue extractMean(List<Product> pProductList, String pAttributeID)
	{
		double numericCount = 0;
		double numericSum = 0;
		int trueCount = 0;
		int falseCount = 0;
		double max = DEFAULT_MAX;
		double min = DEFAULT_MIN;
		HashMap<String, Integer> nominalCounts = new HashMap<String, Integer>();
		for( Product p : pProductList)
		{
			ca.mcgill.cs.creco.data.Attribute s = p.getSpec(pAttributeID);
			if( s != null)
			{
				TypedValue tv = s.getTypedValue();
//				MODIFY WHEN ADDING CLASSES
				//String specString = s.getValue().toString();
				if(tv.isNumeric() )
				{
					double val = tv.getNumeric();
					if(val > max)
					{
						max = val;
					}
					if(val < min)
					{
						min  = val;
					}
					numericCount ++;
					numericSum += tv.getNumeric();					
				}
				else if (tv.isBoolean())
				{
					if(tv.getBoolean())
					{
						trueCount ++;
					}
					else
					{
						falseCount ++;
					}
				}
				else
				{
					int count;
					if (nominalCounts.containsKey(tv.getString()))
					{
						count = nominalCounts.get(tv.getString());
					}
					else
					{
						count = 0;
					}
					nominalCounts.put(tv.getString(), count + 1);
				}
			}
			
		}
		if(numericCount > 0 && numericCount == pProductList.size())
		{
			return new TypedValue(numericSum/numericCount);
		}
		else if ((trueCount > 0 || falseCount > 0) && trueCount +falseCount == pProductList.size())
		{
			if(trueCount > falseCount)
			{
				return new TypedValue(true);
			}
			return new TypedValue(false);
			
		}
		//to change
		String maxAtt = "N/A";
		int maxCount = 0;
		for(String key : nominalCounts.keySet())
		{
			int count = nominalCounts.get(key);
			if(count > maxCount)
			{
				maxAtt = key;
				maxCount = count;			
			}
		}
		return new TypedValue(maxAtt);
	}
	

		
	private void generateSpecList()
	{
		updateRelativeSpecScores();
		Collections.sort(aScoredSpecList, ScoredAttribute.SORT_BY_SCORE);		
	}
	private void generateRatingList()
	{
		updateRelativeRatingScores();
		Collections.sort(aScoredRatingList, ScoredAttribute.SORT_BY_SCORE);		
	}
	
	//make helper functions to simplify this
	/**Private class that updates the spec scores if the object's attributes.
	 * Currently uses PCA to give scores to the attributes. 
	 * 
	 */
	private void updateRelativeSpecScores()
	{
//		this is where the magic happens
//		to keep track of which attributes are being taken into account	
//		uses hash map to avoid attributes with same or similar names which
//		cause conflicts in weka <Name,index>
//		HashMap<String, String> attributeNames = new HashMap<String, String>();
		ArrayList<ScoredAttribute> scoredAttributes = new ArrayList<ScoredAttribute>();
//		FastVector attributeVector = new FastVector();
		ArrayList<AttributeStat> ssa = Lists.<AttributeStat>newArrayList(aSpecList);
//		if there are no specs set scored attributes to null 
	    if(ssa.size() <= 0)
		{
			aScoredSpecList = scoredAttributes;
			return;
		}

	    AttributeHashMap ahm = computeAttributeHash(ssa);
	    FastVector attributeVector = ahm.getVector();
	    //make scored attributes
	    scoredAttributes = generateScoredAttribute(ssa);
		Instances dataset = new Instances("attributes", attributeVector, aProductList.size());
		
		
//		
//		make all instances and add them to the instances object
//		

		for (Iterator<Product> it = aProductList.iterator(); it.hasNext(); )
		{		
		
			Product p = it.next();
			Instance inst = new Instance(attributeVector.size()); 

			for(int i = 0 ; i < scoredAttributes.size(); i ++)
			{
				String indexName = ahm.getHashValue(scoredAttributes.get(i).getAttributeName());
				Attribute wekaAtt = dataset.attribute(indexName);
				
				TypedValue newValue = null;
				try
				{
					if(!scoredAttributes.get(i).isCat())
					{
						newValue = p.getSpec(scoredAttributes.get(i).getAttributeID()).getTypedValue();
					}
					else
					{
						newValue = new TypedValue(p.getCategory().getName());
					}
					
				}
				catch(NullPointerException npe)
				{
					if(wekaAtt.isNominal())
					{
						newValue = new TypedValue(wekaAtt.value(0));
//						entry zero should be N/A
					}
					else
					{
//						default no value for now 
						newValue = new TypedValue("-127");
					}
				}
				
				
				if( newValue.isNumeric() )
				{
					inst.setValue(wekaAtt, newValue.getNumeric());
				}
				else if( newValue.isString() )
				{
					inst.setValue(wekaAtt, newValue.getString());
				}
				else if(newValue.isBoolean())
				{
					inst.setValue(wekaAtt, new Boolean(newValue.getBoolean()).toString());
				}
				else if( newValue.isNA())
				{
					inst.setValue(wekaAtt, "NA" );
				}
			}
			inst.setDataset(dataset); 
			dataset.add(inst);
		}
//		compute means
		for(ScoredAttribute sa: scoredAttributes)
		{
			sa.setAttributeMean(AttributeExtractor.extractMean(aProductList, sa.getAttributeID()));
		}
		
//		use tree to extract feature importance
		try
		{
			
			AttributeSelection attsel = new AttributeSelection();  // package weka.attributeSelection!
			//CfsSubsetEval eval = new CfsSubsetEval();
			PrincipalComponents eval = new PrincipalComponents();
			//GreedyStepwise search = new GreedyStepwise();
			//search.setSearchBackwards(true);
			Ranker search = new Ranker();
			attsel.setEvaluator(eval);
			attsel.setSearch(search);
			attsel.SelectAttributes(dataset);
			double[][] meritScores = attsel.rankedAttributes();
			
			for(double[] score :meritScores)
			{
				if((int)score[0]< scoredAttributes.size())
				{
					scoredAttributes.get((int)score[0]).setAttributeScore(score[1]);
				}
				
			}
			

//			InfoGainAttributeEval eval = new InfoGainAttributeEval();
//			AttributeSelection trainSelector = new	AttributeSelection();
//			Ranker searchMethod=new Ranker();
//            trainSelector.setSearch(searchMethod);
//			trainSelector.setEvaluator(eval);
//			trainSelector.SelectAttributes(dataset);
//			String Results = trainSelector.toResultsString();
		}
		catch(weka.core.WekaException e1)
		{
			logger.error("Weka ERROR:" + e1);

		}
		catch(Exception e2)
		{
			logger.info("Weka Attribute ERROR:\n" + e2 +"\nContinuing");
		}
		aScoredSpecList = completeScoredSpecs(scoredAttributes);
		
		
	}
	
	/**Private class that updates the rating scores if the object's attributes.
	 * Currently uses PCA to give scores to the attributes. 
	 * 
	 */
	private void updateRelativeRatingScores()
	{
//		this is where the magic happens
//		to keep track of which attributes are being taken into account	
//		uses hash map to avoid attributes with same or similar names which
//		cause conflicts in weka <Name,index>
		ArrayList<ScoredAttribute> scoredAttributes = new ArrayList<ScoredAttribute>();
		ArrayList<AttributeStat> ssa = Lists.<AttributeStat>newArrayList(aRatingList);
//		if there are no specs set scored attributes to null 
	    if(ssa.size() <= 0)
		{
			aScoredRatingList = scoredAttributes;
			return;
		}

	    AttributeHashMap ahm = computeAttributeHash(ssa);
	    FastVector attributeVector = ahm.getVector();
	    //make scored attributes
	    scoredAttributes = generateScoredAttribute(ssa);
	    
		Instances dataset = new Instances("attributes", attributeVector, aProductList.size());
		
//		
//		make all instances and add them to the instances object
//		
		
		
		for (Iterator<Product> it = aProductList.iterator(); it.hasNext(); )
		{		
		
			Product p = it.next();
			Instance inst = new Instance(attributeVector.size()); 

			for(int i = 0 ; i < scoredAttributes.size(); i ++)
			{
				String indexName = ahm.getHashValue(scoredAttributes.get(i).getAttributeName());
				Attribute wekaAtt = dataset.attribute(indexName);
				
				TypedValue newValue = null;
				try
				{
					if(!scoredAttributes.get(i).isCat())
					{
						newValue = p.getRating(scoredAttributes.get(i).getAttributeID()).getTypedValue();
					}
					else
					{
						newValue = new TypedValue( p.getCategory().getName());
					}
					

				}
				catch(NullPointerException npe)
				{
					if(wekaAtt.isNominal())
					{
//					entry zero should be N/A
						newValue = new TypedValue( wekaAtt.value(0) );
					}
					else
					{
//						default no value for now 
						newValue = new TypedValue("-127" );
					}
				}
				
				
				if( newValue.isNumeric() )
				{
					inst.setValue(wekaAtt, newValue.getNumeric());
				}
				else if( newValue.isString() )
				{
					inst.setValue(wekaAtt, newValue.getString());
				}	
				else if( newValue.isBoolean())
				{
					inst.setValue(wekaAtt, new Boolean(newValue.getBoolean()).toString());
				}
				else if( newValue.isNA())
				{
					inst.setValue(wekaAtt, "NA" );
				}
			}
			inst.setDataset(dataset); 
			dataset.add(inst);
		}
//		compute means
		for(ScoredAttribute sa: scoredAttributes)
		{
			sa.setAttributeMean(AttributeExtractor.extractMean(aProductList, sa.getAttributeID()));
		}
		
//		use tree to extract feature importance
		try
		{
			
			AttributeSelection attsel = new AttributeSelection();  // package weka.attributeSelection!
			//CfsSubsetEval eval = new CfsSubsetEval();
			PrincipalComponents eval = new PrincipalComponents();
			//GreedyStepwise search = new GreedyStepwise();
			//search.setSearchBackwards(true);
			Ranker search = new Ranker();
			attsel.setEvaluator(eval);
			attsel.setSearch(search);
			attsel.SelectAttributes(dataset);
			double[][] meritScores = attsel.rankedAttributes();
			
			
			for(double[] score :meritScores)
			{
				if((int)score[0]< scoredAttributes.size())
				{
					scoredAttributes.get((int)score[0]).setAttributeScore(score[1]);
				}
				
			}
			

//			InfoGainAttributeEval eval = new InfoGainAttributeEval();
//			AttributeSelection trainSelector = new	AttributeSelection();
//			Ranker searchMethod=new Ranker();
//            trainSelector.setSearch(searchMethod);
//			trainSelector.setEvaluator(eval);
//			trainSelector.SelectAttributes(dataset);
//			String Results = trainSelector.toResultsString();

		}
		catch(weka.core.WekaException e1)
		{
			logger.error("Weka ERROR:" + e1);
		}
		catch(Exception e2)
		{
			logger.info("Weka Attribute ERROR:\n" + e2 +"\nContinuing");

		}
		aScoredRatingList = completeScoredRatings(scoredAttributes);
		
		
	}
	
	private AttributeHashMap computeAttributeHash(ArrayList<AttributeStat> pAS)
	{
		HashMap<String, String> attributeNames = new HashMap<String, String>();
		FastVector attributeVector = new FastVector();
		int index = 0;
		for(AttributeStat a : pAS)
		{
			
//			check value type skip attributes with mixed type
			if(a.getValueMin() != null && a.getValueEnum().size() > 0)
			{
				continue;
			}
//			numerical attribute
			else if(a.getValueMin() != null)
			{
				attributeNames.put(a.getAttribute().getName(), "I"+index);
				Attribute newAttribute = new Attribute("I"+index);
				//scoredAttributes.add(new ScoredAttribute(a.getAttribute()));
				attributeVector.addElement(newAttribute);
			}
//			nominal attribute
			else if(a.getValueEnum().size() > 0)
			{

				FastVector nominalValues = new FastVector();

				for (String value : a.getValueEnum())
				{
					nominalValues.addElement(value);
				}
//				add the N/A String in case the object doesn't have the attribute
				if(!nominalValues.contains("N/A"))
				{
					nominalValues.addElement("N/A");
				}
				attributeNames.put(a.getAttribute().getName(), "I"+index);
				Attribute newAttribute = new Attribute("I"+index, nominalValues);
				//scoredAttributes.add(new ScoredAttribute(a.getAttribute()));
				attributeVector.addElement(newAttribute);
			}
			index +=1;
		}
		return new AttributeHashMap(attributeVector, attributeNames);
	}
		
	private ArrayList<ScoredAttribute> generateScoredAttribute(ArrayList<AttributeStat> pAS)
	{
		ArrayList<ScoredAttribute> scoredAttributes = new ArrayList<ScoredAttribute>();
	    for(AttributeStat a : pAS)
	    {
	    	// skip complex attributes
	    	if(a.getValueMin() != null && a.getValueEnum().size() > 0)
	    	{
					continue;
	    	}
	    	scoredAttributes.add(new ScoredAttribute(a.getAttribute(),aEquivalenceClass));
	    }
	    return scoredAttributes;
	}
	
	private ArrayList<ScoredAttribute> completeScoredSpecs(ArrayList<ScoredAttribute> pScored)
	{
		HashMap<String, ScoredAttribute> fullMap = new HashMap<String, ScoredAttribute>();
		//put
		for(ScoredAttribute sAtt : pScored)
		{
			fullMap.put(sAtt.getAttributeID(), sAtt);
		}
		//add missing
		for(AttributeStat ss : aSpecList)
		{
			if(!fullMap.containsKey(ss.getAttribute().getId()))
			{
				ScoredAttribute sa = new ScoredAttribute(ss.getAttribute(), aEquivalenceClass);

				sa.setAttributeMean(new TypedValue("N/A"));
				fullMap.put(ss.getAttribute().getId(), sa);
			}
		}
		ArrayList<ScoredAttribute> outList = Lists.newArrayList(fullMap.values());
		Collections.sort(outList, ScoredAttribute.SORT_BY_SCORE);
		return outList;
		
	}
	private ArrayList<ScoredAttribute> completeScoredRatings(ArrayList<ScoredAttribute> pScored)
    {
		HashMap<String, ScoredAttribute> fullMap = new HashMap<String, ScoredAttribute>();
		//put
		for(ScoredAttribute sAtt : pScored)
		{
			fullMap.put(sAtt.getAttributeID(), sAtt);
		}
		//add missing
		for(AttributeStat ss : aRatingList)
		{
			if(!fullMap.containsKey(ss.getAttribute().getId()))
			{
				ScoredAttribute sa = new ScoredAttribute(ss.getAttribute(),aEquivalenceClass);

				sa.setAttributeMean(new TypedValue("N/A"));
				fullMap.put(ss.getAttribute().getId(), sa);
			}
		}
		
		ArrayList<ScoredAttribute> outList = Lists.newArrayList(fullMap.values());
		Collections.sort(outList, ScoredAttribute.SORT_BY_SCORE);
		return outList;
    }
	

	/**
	 * @return The list of products used by the extractor
	 */
	public List<Product> getProductList() 
	{
		return aProductList;
	}

	/**
	 * @param pProductList The list of products to be used by the extractor
	 */
	public void setProductList(List<Product> pProductList) 
	{
		this.aProductList = pProductList;
	}

	/**
	 * @return The equivalence class used by the extractor
	 */
	public Category getEquivalenceClass() 
	{
		return aEquivalenceClass;
	}

	/**
	 * @param pEquivalenceClass The equivalence class to be used by the extractor
	 */
	public void setEquivalenceClass(Category pEquivalenceClass) 
	{
		aEquivalenceClass = pEquivalenceClass;
	}

	/**
	 * Call this method to get the list of scored Specs ranked from most important
	 * to least important.
	 * can return null pointers if it doesn't have any attributes to work with.
	 * @return list of scored attributes ranked from most important
	 * to least important.
	 */
	public ArrayList<ScoredAttribute> getScoredSpecList() 
	{
		generateSpecList();
		return aScoredSpecList;
	}
	/**
	 * Call this method to get the list of scored Ratings ranked from most important
	 * to least important.
	 * can return null pointers if it doesn't have any attributes to work with.
	 * @return list of scored attributes ranked from most important
	 * to least important.
	 */
	public ArrayList<ScoredAttribute> getScoredRatingList() 
	{
		generateRatingList();
		return aScoredRatingList;
	}
	
	public Logger getLogger()
	{
		return logger;
	}

	
}
