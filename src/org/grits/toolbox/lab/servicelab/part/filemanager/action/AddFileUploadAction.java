/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.filemanager.action;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.entry.archive.preference.doctype.DocTypePreference;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;

/**
 * 
 *
 */
public class AddFileUploadAction extends Action
{
	private static Logger logger = Logger.getLogger(AddFileUploadAction.class);
	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;

	public AddFileUploadAction(TableViewer tableViewer, MDirtyable dirtyable)
	{
		this.tableViewer  = tableViewer;
		this.dirtyable = dirtyable;
		this.setText("Add a new File upload");
		this.setToolTipText("Add a file to be added for this protocol");
		this.setImageDescriptor(ImageShare.ADD_ICON);
		DocTypePreference.loadPreferences();
	}

	@Override
	public void run()
	{
		logger.info("Adding file upload to the protocol");
		if(tableViewer.getInput() != null)
		{
			FileInfoProtocol fileInfoProtocol = (FileInfoProtocol) tableViewer.getInput();
			logger.info("selected protocol : " + fileInfoProtocol.getLabel());
			DocumentType nextDocumentType = getNextDocumentType(fileInfoProtocol);
			if(nextDocumentType != null)
			{
				FileUpload fileUpload =  new FileUpload(nextDocumentType.getLabel(),
						nextDocumentType.getSelectedSubType());

				logger.info("adding file upload with category : " + fileUpload.getFileCategory());

				fileInfoProtocol.getFileUploads().add(fileUpload);
				tableViewer.refresh();
				dirtyable.setDirty(true);
			}
			else
			{
				logger.info("No document type available");
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						"Unavailable Document Category", "No additional document category available to be added."
								+ " Please create a document category in preference before adding.");
			}
		}
		else
		{
			logger.info("no protocol selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No Protocol is selected. Please select a protocol first.");
		}
	}

	public static DocumentType getNextDocumentType(FileInfoProtocol fileInfoProtocol)
	{
		DocumentType documentType = null;

		// make a set of current document category-subCategory that have been already added
		Set<String> allDocTypeSet = new HashSet<String>();
		String subCategory = null;
		for(FileUpload fileUpload : fileInfoProtocol.getFileUploads())
		{
			subCategory = fileUpload.getFileSubCategory() == null
					? "" : fileUpload.getFileSubCategory();
			allDocTypeSet.add(fileUpload.getFileCategory() + subCategory);
		}

		// check against all the docType-subType available in preference
		for(DocumentType docType : DocTypePreference.ALL_DOCUMENT_TYPES)
		{
			if(docType.getSubTypes().isEmpty())
			{
				if(!allDocTypeSet.contains(docType.getLabel()))
				{
					documentType = docType.clone();
					break;
				}
			}
			else
			{
				// try a different subtype for a category
				for(String subType : docType.getSubTypes())
				{
					if(!allDocTypeSet.contains(docType.getLabel() + subType))
					{
						documentType = docType.clone();
						documentType.setSelectedSubType(subType);
						break;
					}
				}
			}
		}
		return documentType;
	}
}
