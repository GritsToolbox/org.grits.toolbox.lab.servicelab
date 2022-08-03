/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.provider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;

/**
 * 
 *
 */
public class SampleExpProtocolsContentProvider implements IStructuredContentProvider
{
	public SampleExpProtocolsContentProvider()
	{
		
	}

	@Override
	public void dispose()
	{

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{

	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if(inputElement instanceof SampleExpValidation)
		{
			return ((SampleExpValidation) inputElement).getProtocols().toArray();
		}
		return null;
	}
}
