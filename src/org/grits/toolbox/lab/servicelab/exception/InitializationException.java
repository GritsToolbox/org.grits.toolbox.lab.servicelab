/**
 * 
 */
package org.grits.toolbox.lab.servicelab.exception;

/**
 * 
 * An exception class to describe errors occurred during initialization of values
 * for ui components most commonly a wizard.
 * It has two fields describing error title and error message that can be displayed
 * in a message dialog. 
 */
public class InitializationException extends Exception
{
	private static final long serialVersionUID = 1L;

	private String errorTitle = null;
	private String errorMessage = null;

	public InitializationException(String errorTitle, String errorMessage)
	{
		this.errorTitle = errorTitle;
		this.errorMessage = errorMessage;
	}

	public InitializationException(String errorTitle, String errorMessage, Throwable cause)
	{
		super(cause);
		this.errorTitle = errorTitle;
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the errorTitle
	 */
	public String getErrorTitle()
	{
		return errorTitle;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
