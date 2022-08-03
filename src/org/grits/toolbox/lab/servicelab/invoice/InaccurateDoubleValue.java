/**
 * 
 */
package org.grits.toolbox.lab.servicelab.invoice;

/**
 * 
 *
 */
public class InaccurateDoubleValue
{
	private double value = 0.0;
	private String inaccuracyMessage = null;

	/**
	 * @return the value
	 */
	public double getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value)
	{
		this.value = value;
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
