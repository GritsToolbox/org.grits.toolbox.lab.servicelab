/**
 * 
 */
package org.grits.toolbox.lab.servicelab.exception;

/**
 * 
 * An exception class to describe errors occurred during loading (most commonly a file).
 * It extends InitializationException with two fields describing error title and error message
 * that can be displayed in a message dialog.
 */
public class LoadingException extends InitializationException
{
	private static final long serialVersionUID = 1L;

	public LoadingException(String errorTitle, String errorMessage)
	{
		super(errorTitle, errorMessage);
	}

	public LoadingException(String errorTitle, String errorMessage, Throwable cause)
	{
		super(errorTitle, errorMessage, cause);
	}
}
