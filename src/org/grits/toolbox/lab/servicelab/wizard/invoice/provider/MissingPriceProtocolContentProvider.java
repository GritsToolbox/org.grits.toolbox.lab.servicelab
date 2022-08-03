package org.grits.toolbox.lab.servicelab.wizard.invoice.provider;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 *
 */
public class MissingPriceProtocolContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if(inputElement instanceof Map<?, ?>)
		{
			return ((Map<?, ?>) inputElement).entrySet().toArray();
		}
		return null;
	}
}
