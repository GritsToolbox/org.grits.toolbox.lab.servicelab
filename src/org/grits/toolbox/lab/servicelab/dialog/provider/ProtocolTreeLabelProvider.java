/**
 * 
 */
package org.grits.toolbox.lab.servicelab.dialog.provider;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;

/**
 * 
 *
 */
public class ProtocolTreeLabelProvider implements ITableLabelProvider, ITableFontProvider
{
	private Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void dispose()
	{
		
	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{
		
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(columnIndex == 1)
		{
			if(element instanceof ParameterGroup)
				return ((ParameterGroup) element).getLabel();
			if(element instanceof Parameter)
				return ((Parameter) element).getName();
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public Font getFont(Object element, int columnIndex)
	{
		return element instanceof ParameterGroup ? boldFont : null;
	}
}
