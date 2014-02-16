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
package ca.mcgill.cs.creco.web.model.search;

import java.util.List;

import ca.mcgill.cs.creco.data.Product;

/**
 * NOTE: TEMPORARY CLASS! The contents of this class will change when the
 * real "Product" and "Category" objects are pushed to git.
 * 
 * Real class
 */
public class SearchResult
{
	private final List<Product> products;
	
	public SearchResult(List<Product> products){

		this.products = products;
	}
	
	public List<Product> getProducts()
	{
		return products;
	}
}
