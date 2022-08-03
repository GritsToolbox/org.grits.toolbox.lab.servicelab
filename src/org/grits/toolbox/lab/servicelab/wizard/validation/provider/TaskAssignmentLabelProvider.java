/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.validation.TaskAssignment;

/**
 * 
 *
 */
public class TaskAssignmentLabelProvider implements ITableLabelProvider
{
	private Image tickedImage = null;
	private Image errorImage = null;

	public TaskAssignmentLabelProvider()
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
		if(element instanceof TaskAssignment)
		{
			if(columnIndex == 3)
			{
				return ((TaskAssignment) element).getErrorMessage() == null ?
					tickedImage : errorImage;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(element instanceof TaskAssignment)
		{
			String value = null;
			switch (columnIndex)
			{
				case 0:
					value = ((TaskAssignment) element).getServiceLabTask().getTaskName();
					break;

				case 1 :
					value = ((TaskAssignment) element).getAssignedPerson();
					break;

				case 2 :
					value = ((TaskAssignment) element).getNumberOfTasks() + "";
					break;

				default:
					break;
			}
			return value;
		}
		return null;
	}
}
