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


/**
 * Represents an attribute of a product.
 */
public class Attribute 
{
	private String aDisplayName;
	private String aDescription;
	private String aAttributeId;
	private TypedVal aTypedValue;
	
	/**
	 * Creates a new Attribute.
	 * @param pId The id of the attribute.
	 * @param pName The display name.
	 * @param pDescription The description.
	 * @param pValue The value for the attribute.
	 */
	public Attribute( String pId, String pName, String pDescription, Object pValue )
	{
		aAttributeId = pId;
		aDescription = pDescription;
		aDisplayName = pName;
		aTypedValue = new TypedVal(pValue);
	}

	/**
	 * @return The display name of the attribute.
	 */
	public String getName() 
	{ return aDisplayName; }

	/**
	 * @return The description of the attribute.
	 */
	public String getDescription() 
	{ return aDescription; }

	/**
	 * @return The id of this attribute.
	 */
	public String getId() 
	{ return aAttributeId; }

	/**
	 * @return The value for this attribute.
	 */
	public Object getValue() 
	{ return this.aTypedValue.getValue(); }
	
	/**
	 * @return The typed value.
	 */
	public TypedVal getTypedValue() 
	{ return this.aTypedValue; }

	/**
	 * @return The type of the value.
	 */
	public String getType() 
	{ return this.aTypedValue.getType(); }
}
