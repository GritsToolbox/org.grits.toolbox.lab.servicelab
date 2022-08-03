/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model.validation;

import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;

/**
 * 
 *
 */
public class FileUploadError
{
	private String sampleExpName = null;
	private MinInfoProtocol protocol = null;
	private String errorMessage = null;

	public FileUploadError(String sampleExpName, MinInfoProtocol protocol, String errorMessage)
	{
		this.sampleExpName = sampleExpName;
		this.protocol = protocol;
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the sampleExpName
	 */
	public String getSampleExpName()
	{
		return sampleExpName;
	}

	/**
	 * @param sampleExpName the sampleExpName to set
	 */
	public void setSampleExpName(String sampleExpName)
	{
		this.sampleExpName = sampleExpName;
	}

	/**
	 * @return the protocol
	 */
	public MinInfoProtocol getProtocol()
	{
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(MinInfoProtocol protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * get error message that can be displayed
	 * @return
	 */
	public String getFullErrorMessage()
	{
		return new StringBuilder().append(sampleExpName)
				.append(" Protocol : ").append(protocol.getLabel())
				.append(" ").append(errorMessage).toString();
	}
}
