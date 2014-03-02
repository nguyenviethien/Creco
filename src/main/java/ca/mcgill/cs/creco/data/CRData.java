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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import ca.mcgill.cs.creco.data.json.JsonLoadingService;

/**
 * Root of the object graph representing the consumer reports database. 
 * All of the data is accessible through a singleton CRData object. Just 
 * create a new CRData object to get started. Normally in production this
 * will be built when the server starts up, and might be provided to your
 * class by a master controller. For now, make one yourself:
 * CRData crData = new CRData(DataPath.get()); 
 * The main entry points to the data are via the CategoryList and ProductList.
 * CategoryList catList = crData.getCategoryList();
 * These provide access to categories or products by id, and are iterables.
 */
public final class CRData implements IDataCollector
{
	private static final String DEFAULT_CATEGORY_FILENAME = "category.json";
	
	private static final String[] DEFAULT_PRODUCT_FILENAMES = {
			"appliances.json", "electronicsComputers.json",
			"cars.json", "health.json", "homeGarden.json", 
			"food.json", "babiesKids.json", "money.json"
		};
	
	private static CRData instance = null;
	
	private CategoryList aCategoryList;
	private HashMap<String, Product> aProducts = new HashMap<String, Product>();
	
	private CRData(String[] pProductFileNames, String pCategoryFileName) throws IOException
	{
		IDataLoadingService loadingService = new JsonLoadingService(DataPath.get(), pCategoryFileName, pProductFileNames);
				
		aCategoryList = loadingService.loadCategories();
		aCategoryList.eliminateSingletons();
		
		loadingService.loadProducts(this);
		
		// Put links from products to categories and vice-versa
		 aCategoryList.associateProducts(getProducts()); 
		
		// Roll up useful pre-processed statistics and find equivalence classes
		aCategoryList.refresh();
		aCategoryList.findEquivalenceClasses();		
	}
	
	/**
	 * Initializes the CR Data on the first call, and subsequently
	 * returns the singleton instance of the CR data.
	 * @return CRData singleton
	 * @throws IOException if the data cannot be accessed
	 */
	public static CRData getData() throws IOException
	{
		if (instance == null)
		{
			instance = new CRData(DEFAULT_PRODUCT_FILENAMES, DEFAULT_CATEGORY_FILENAME);
		}
		return instance;
	}
	
	@Override
	public void addCategory(Category pCategory)
	{
		// TODO
	}
	
	@Override
	public void addProduct(Product pProduct)
	{
		aProducts.put(pProduct.getId(), pProduct);
	}

	/**
	 * Initializes the CRData based on specified filenames.
	 * @return CRData singleton
	 * @param pProductFileNames The list of file names for the product data.
	 * @param pCategoryFileName The name of the category file.
	 * @throws IOException If the database was already initialized.
	 */
	public static CRData setupWithFileNames(String[] pProductFileNames, String pCategoryFileName) throws IOException 
	{
		if (instance == null)
		{
			instance = new CRData(pProductFileNames, pCategoryFileName);
		} 
		else
		{
			throw new IOException("CR Database was already initialized. Use CRData.getData() instead.");
		}
		return instance;
	}
	
	/**
	 * @return The list of categories.
	 */
	public CategoryList getCategoryList() 
	{ return aCategoryList; }
	
	/**
	 * @return An interator on the product list.
	 */
	public Iterator<Product> getProducts() 
	{
		return Collections.unmodifiableCollection(aProducts.values()).iterator();
	}
}