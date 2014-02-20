package ca.mcgill.cs.creco.logic.model;

/**
 *AttributeValue class can either be boolean, nominal or numeric
 *Use boolean checks to know what type of attribute before getting
 *value. If you try to get a value different from the one of the 
 *attribute it will return null.
 *
 */
/**
 *
 */
/**
 *
 */
public class AttributeValue {
	
	private boolean aBool;
	private boolean aNominal;
	private boolean aNumeric;
	
	private boolean aBoolValue;
	private String aNominalValue;
	private double aNumericValue;
	
	
	/**Constructor for boolean attribute.
	 * @param pBool attribute value
	 */
	public AttributeValue(boolean pBool)
	{
		aBoolValue = pBool;
		aBool = true;
	}
	/**Constructor for nominal attribute.
	 * @param pNominal attribute value
	 */
	public AttributeValue(String pNominal)
	{
		aNominalValue = pNominal;
		aNominal = true;
	}
	/**Constructor for numeric attribute.
	 * @param pNumeric attribute value
	 */
	public AttributeValue(double pNumeric)
	{
		aNumericValue = pNumeric;
		aNumeric = true;
	}
	/**Check is attribute is boolean.
	 * @return true is boolean attribute
	 */
	public boolean isBool() 
	{
		return aBool;
	}
	/**Check is attribute is numeric.
	 * @return true is numeric attribute
	 */
	public boolean isNominal() 
	{
		return aNominal;
	}
	/**Check is attribute is numeric.
	 * @return true is numeric attribute
	 */
	public boolean isNumeric() 
	{
		return aNumeric;
	}

	/**
	 * @return boolean value
	 */
	public boolean getBoolValue() 
	{
		return aBoolValue;
	}

	/**
	 * @return nominal value as String
	 */
	public String getNominalValue() 
	{
		return aNominalValue;
	}
	
	/**
	 * @return numeric value as double
	 */
	public double getNumericValue() 
	{
		return aNumericValue;
	}
 
	@Override
	public String toString()
	{
		if(aBool)
		{
			return aBoolValue +"";
		}
		if(aNumeric)
		{
			return aNumericValue +"";
		}
		return aNominalValue;
	}
}
