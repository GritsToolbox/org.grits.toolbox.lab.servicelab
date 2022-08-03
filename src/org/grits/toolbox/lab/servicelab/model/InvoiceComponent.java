/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 *
 */
public class InvoiceComponent
{
	private String protocolName = null;
	private double rate = 0.0;
	private int quantity = 0;
	private double amount = 0.0;
	private Set<String> sampleNames = new HashSet<String>();
	private String inaccuracyMessage = null;

	/**
	 * @return the protocolName
	 */
	public String getProtocolName()
	{
		return protocolName;
	}

	/**
	 * @param protocolName the protocolName to set
	 */
	public void setProtocolName(String protocolName)
	{
		this.protocolName = protocolName;
	}

	/**
	 * @return the sampleNames
	 */
	public Set<String> getSampleNames()
	{
		return sampleNames;
	}

	/**
	 * @param sampleNames the sampleNames to set
	 */
	public void setSampleNames(Set<String> sampleNames)
	{
		this.sampleNames = sampleNames;
	}

	/**
	 * @return the rate
	 */
	public double getRate()
	{
		return rate;
	}

	/**
	 * @param rate the rate to set
	 */
	public void setRate(double rate)
	{
		this.rate = rate;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity()
	{
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	/**
	 * @return the totalAmount
	 */
	public double getAmount()
	{
		return amount;
	}

	/**
	 * 
	 * @param amount the amount to set
	 */
	public void setAmount(double amount)
	{
		this.amount = amount;
	}

	/**
	 * @return the inaccuracyMessage
	 */
	public String getInaccuracyMessage()
	{
		return inaccuracyMessage;
	}

	/**
	 * @param inaccuracyMessage the inaccuracyMessage to set
	 */
	public void setInaccuracyMessage(String inaccuracyMessage)
	{
		this.inaccuracyMessage = inaccuracyMessage;
	}
}
