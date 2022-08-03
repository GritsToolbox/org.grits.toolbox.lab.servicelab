/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;

/**
 * 
 *
 */
@XmlRootElement(name="costParameter")
public class CostParameter
{
	private String costParameterName = null;
	private double industryPrice = 0.0;
	private double nonProfitPrice = 0.0;
	private Parameter associatedParameter = null;
	private ParameterGroup associatedParameterGroup = null;

	@XmlAttribute(name="costParameterName", required=true)
	public String getCostParameterName()
	{
		return costParameterName;
	}

	public void setCostParameterName(String costParameterName)
	{
		this.costParameterName = costParameterName;
	}

	@XmlElement(name="industryPrice")
	public double getIndustryPrice()
	{
		return industryPrice;
	}

	public void setIndustryPrice(double industryPrice)
	{
		this.industryPrice = industryPrice;
	}

	@XmlElement(name="nonProfitPrice")
	public double getNonProfitPrice()
	{
		return nonProfitPrice;
	}

	public void setNonProfitPrice(double nonProfitPrice)
	{
		this.nonProfitPrice = nonProfitPrice;
	}

	@XmlElement(name="associatedParameter")
	public Parameter getAssociatedParameter()
	{
		return associatedParameter;
	}

	public void setAssociatedParameter(Parameter associatedParameter)
	{
		this.associatedParameter = associatedParameter;
	}

	@XmlElement(name="associatedParameterGroup")
	public ParameterGroup getAssociatedParameterGroup()
	{
		return associatedParameterGroup;
	}

	public void setAssociatedParameterGroup(ParameterGroup associatedParameterGroup)
	{
		this.associatedParameterGroup = associatedParameterGroup;
	}

	public static final int INDUSTRY_TYPE = 0;
	public static final int NON_PROFIT_TYPE = 1;

	/**
	 * returns appropriate price based on its price type
	 * if price type is not known returns default price type, i.e. industry price
	 * @param priceType industry or non-profit type
	 * @return a price based on type
	 */
	public double getPrice(int priceType)
	{
		double price = 0;
		switch (priceType)
		{
			case INDUSTRY_TYPE:
				price = industryPrice;
				break;

			case NON_PROFIT_TYPE:
				price = nonProfitPrice;
				break;

			default:
				price = industryPrice;
				break;
		}
		return price;
	}
}
