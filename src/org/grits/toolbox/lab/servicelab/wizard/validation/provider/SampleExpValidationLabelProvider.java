/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;

/**
 * 
 *
 */
public class SampleExpValidationLabelProvider implements ITableLabelProvider
{
	private Image tickedImage = null;
	private Image errorImage = null;

	public SampleExpValidationLabelProvider()
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
		if(element instanceof SampleExpValidation)
		{
			if(columnIndex == 1)
			{
				return ((SampleExpValidation) element).getErrorMessage() == null ?
					tickedImage : errorImage;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(element instanceof SampleExpValidation)
		{
			String value = null;
			switch (columnIndex)
			{
				case 0:
					value = ((SampleExpValidation) element).getSampleName();
					break;

				default:
					break;
			}
			return value;
		}
		return null;
	}
}
