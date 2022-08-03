package org.grits.toolbox.lab.servicelab.part.taskmanager.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;

public class ProtocolNodeLabelProvider implements ITableLabelProvider
{
	@Override
	public void addListener(ILabelProviderListener listener)
	{

	}

	@Override
	public void dispose()
	{

	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(element instanceof MinInfoProtocol)
		{
			switch (columnIndex)
			{
				case 0:
					return ((MinInfoProtocol) element).getLabel();
				default:
					break;
			}
		}
		return null;
	}
}
