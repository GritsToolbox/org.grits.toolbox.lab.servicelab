package org.grits.toolbox.lab.servicelab.util;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;

/**
 * 
 * 
 *
 */
public class UniqueCategoryValidator implements ICellEditorValidator
{
	private static Logger logger = Logger.getLogger(UniqueCategoryValidator.class);

	private FileInfoProtocol fileInfoProtocol = null;
	private FileUpload fileUpload = null;

	public void setProtocolWithFileUpload(FileInfoProtocol fileInfoProtocol)
	{
		this.fileInfoProtocol = fileInfoProtocol;
	}

	public void setFileUpload(FileUpload fileUpload)
	{
		this.fileUpload = fileUpload;
	}

	@Override
	public String isValid(Object value)
	{
		// no protocol to check with
		if(fileInfoProtocol == null)
			return "Protocol not selected";

		// no file upload to check for
		if(fileUpload == null)
			return "File upload not selected";

		String errorMessage = null;
		if(value instanceof String)
		{
			String selectionSubCategory = fileUpload.getFileSubCategory() == null
					? "" : fileUpload.getFileSubCategory();
			String subCategory = null;
			for(FileUpload fileUpld : fileInfoProtocol.getFileUploads())
			{
				subCategory = fileUpld.getFileSubCategory() == null ? "" : fileUpld.getFileSubCategory();
				if(!fileUpld.equals(fileUpload)
						&& value.equals(fileUpld.getFileCategory())
						&& selectionSubCategory.equals(subCategory))
				{
					errorMessage = "Category \"" + fileUpld.getFileCategory() + 
							"-" + selectionSubCategory + "\" has been already added to this protocol";
					logger.error(errorMessage);
					break;
				}
			}
		}
		else // value is of some other type
		{
			logger.error("No selection. empty(null) value for the category");
		}

		logger.info(errorMessage);
		return errorMessage;
	}
}
