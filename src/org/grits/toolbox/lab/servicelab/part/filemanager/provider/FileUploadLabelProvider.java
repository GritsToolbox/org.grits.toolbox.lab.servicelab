package org.grits.toolbox.lab.servicelab.part.filemanager.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.model.FileUpload;

public class FileUploadLabelProvider implements ITableLabelProvider
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
		if(element instanceof FileUpload)
		{
			FileUpload fileUpload = ((FileUpload) element);
			String value = null;
			switch (columnIndex)
			{
				case 0:
					value = fileUpload.getNumberOfFiles() + "";
					break;
				case 1:
					value = fileUpload.getFileCategory();
					break;
				case 2:
					value = fileUpload.getFileSubCategory();
					break;
				default:
					break;
			}
			return value;
		}
		return null;
	}
}
