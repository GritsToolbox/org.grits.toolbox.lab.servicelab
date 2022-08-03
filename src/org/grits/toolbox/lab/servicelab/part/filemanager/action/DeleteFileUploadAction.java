/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.filemanager.action;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;

/**
 * 
 *
 */
public class DeleteFileUploadAction extends Action
{
	private static Logger logger = Logger.getLogger(DeleteFileUploadAction.class);
	private TableViewer fileUploadTableViewer = null;
	private MDirtyable dirtyable = null;

	public DeleteFileUploadAction(TableViewer fileUploadTableViewer, MDirtyable dirtyable)
	{
		this.fileUploadTableViewer  = fileUploadTableViewer;
		this.dirtyable = dirtyable;
		this.setText("Remove the selected file upload");
		this.setToolTipText("Remove the selected file upload for a protocol");
		this.setImageDescriptor(ImageShare.DELETE_ICON);
	}

	@Override
	public void run()
	{
		logger.info("Deleting file upload to the protocol");
		if(!fileUploadTableViewer.getSelection().isEmpty())
		{
			Object selection = ((StructuredSelection)
					fileUploadTableViewer.getSelection()).getFirstElement();
			if(selection instanceof FileUpload)
			{
				// get the selected file upload
				FileUpload fileUpload = (FileUpload) selection;

				// get the selected file upload protocol whose file upload is selected
				FileInfoProtocol fileInfoProtocol =
						(FileInfoProtocol) fileUploadTableViewer.getInput();

				logger.info("selected protocol : " + fileInfoProtocol.getLabel());
				logger.info("File upload to remove  : " + fileUpload.getFileCategory());

				// remove the file upload from the protocol
				fileInfoProtocol.getFileUploads().remove(fileUpload);

				fileUploadTableViewer.refresh();
				dirtyable.setDirty(true);
			}
		}
		else
		{
			logger.info("No file upload selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No file upload is selected. Please select a file upload to remove.");
		}
	}
}
