/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.provider;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.validation.FileUploadError;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;

/**
 * 
 *
 */
public class SampleFileValidationLabelProvider implements ITableLabelProvider
{
	private List<FileUploadError> fileUploadErrors = null;
	private Image tickedImage = null;
	private Image errorImage = null;

	public SampleFileValidationLabelProvider(List<FileUploadError> fileUploadErrors)
	{
		this.fileUploadErrors  = fileUploadErrors;
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
				String sampleName = ((SampleExpValidation) element).getSampleName();
				return matchSampleName(sampleName) ? errorImage : tickedImage;
			}
		}
		return null;
	}

	private boolean matchSampleName(String sampleName)
	{
		boolean matchedSample = false;
		for(FileUploadError fileUploadError : fileUploadErrors)
		{
			if(fileUploadError.getSampleExpName().equals(sampleName))
			{
				matchedSample = true;
				break;
			}
		}
		return matchedSample;
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
