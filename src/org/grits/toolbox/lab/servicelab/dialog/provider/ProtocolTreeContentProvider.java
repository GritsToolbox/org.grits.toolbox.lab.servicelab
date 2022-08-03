/**
 * 
 */
package org.grits.toolbox.lab.servicelab.dialog.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class ProtocolTreeContentProvider implements ITreeContentProvider
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
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		if(parentElement instanceof PriceInfoProtocol)
		{
			PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) parentElement;
			int size = priceInfoProtocol.getParameterGroups() == null ?
					0 : priceInfoProtocol.getParameterGroups().size();
			size += priceInfoProtocol.getParameters() == null ?
					0 : + priceInfoProtocol.getParameters().size();
			Object[] array = new Object[size];
			int i = 0;
			if(priceInfoProtocol.getParameterGroups() != null)
			{
				for(ParameterGroup parameterGroup : priceInfoProtocol.getParameterGroups())
				{
					array[i++] = parameterGroup;
				}
			}
			if(priceInfoProtocol.getParameters() != null)
			{
				for(Parameter parameter : priceInfoProtocol.getParameters())
				{
					array[i++] = parameter;
				}
			}
			return array;
		}
		if(parentElement instanceof ParameterGroup)
		{
			ParameterGroup parameterGroup = (ParameterGroup) parentElement;
			// parameter group should atleast have a parameter
			Object[] array = new Object[parameterGroup.getParameters().size()];
			int i = 0;
			for(Parameter parameter : parameterGroup.getParameters())
			{
				array[i++] = parameter;
			}

			return array;
		}
		return null;
	}

	@Override
	public Object getParent(Object element)
	{
		return null;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		return element instanceof PriceInfoProtocol
				|| element instanceof ParameterGroup;
	}
}