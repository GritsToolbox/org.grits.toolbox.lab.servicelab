package org.grits.toolbox.lab.servicelab.dialog.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;

/**
 * 
 *
 */
public class CheckboxProtocolLabelProvider implements ITableLabelProvider
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
		return columnIndex == 1 && element instanceof MinInfoProtocol
				? ((MinInfoProtocol) element).getLabel() : null;
	}
}
