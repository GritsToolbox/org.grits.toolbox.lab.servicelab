/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.validation.TaskUnit;

/**
 * 
 *
 */
public class TaskUnitLabelProvider implements ITableLabelProvider
{
	private Image tickedImage = null;
	private Image errorImage = null;

	public TaskUnitLabelProvider()
	{
		tickedImage  = ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.TICK_ICON).createImage();
		errorImage  = ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.ERROR_ICON).createImage();
	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{

	}

	@Override
	public void dispose()
	{
		if(tickedImage != null && !tickedImage.isDisposed())
			tickedImage.dispose();
		if(errorImage != null && !errorImage.isDisposed())
			errorImage.dispose();
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
		if(element instanceof TaskUnit)
		{
			String value = null;
			switch (columnIndex)
			{
				case 0:
					value = ((TaskUnit) element).getServiceLabTask().getTaskName();
					break;

				case 1 :
					value = ((TaskUnit) element).getAssignedPerson();
					break;

				default:
					break;
			}
			return value;
		}
		return null;
	}
}
