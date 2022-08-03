/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.taskmanager.action;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;
import org.grits.toolbox.lab.servicelab.part.taskmanager.TaskProtocolManager;

/**
 * 
 *
 */
public class DeleteProtocolAction extends Action
{
	private static Logger logger = Logger.getLogger(DeleteProtocolAction.class);
	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;

	public DeleteProtocolAction(TableViewer tableViewer, MDirtyable dirtyable)
	{
		this.tableViewer  = tableViewer;
		this.dirtyable = dirtyable;
		this.setText("Remove the selected protocol");
		this.setToolTipText("Remove the selected protocol from this task.");
		this.setImageDescriptor(ImageShare.DELETE_ICON);
	}

	@Override
	public void run()
	{
		logger.info("Removing selected protocol for the selected task.");
		if(!tableViewer.getSelection().isEmpty())
		{
			Object selection = ((StructuredSelection)
					tableViewer.getSelection()).getFirstElement();
			if(selection instanceof MinInfoProtocol)
			{
				// get the selected protocol
				MinInfoProtocol protocolNode = (MinInfoProtocol) selection;

				// get the selected task whose protocol is selected
				ServiceLabTask serviceLabTask =
						(ServiceLabTask) tableViewer.getInput();

				logger.info("selected task : " + serviceLabTask.getTaskName());
				logger.info("Protocol to remove  : " + protocolNode.getLabel());

				// remove the protocol from the task
				serviceLabTask.getProtocolNodes().remove(protocolNode);

				// for empty protocols, set min/max no. of protocols to 0
				if(serviceLabTask.getProtocolNodes().isEmpty())
				{
					serviceLabTask.setMinProtocols(0);
					serviceLabTask.setMaxProtocols(0);

					// refresh the left-side table after resetting min/max value
					((TableViewer) tableViewer.getData(
							TaskProtocolManager.KEY_LEFT_SIDE_TABLE)).refresh();
				}

				tableViewer.refresh();
				dirtyable.setDirty(true);
			}
			else
			{
				logger.info("selected object is not a MinInfoProtocol object : " + selection);
			}
		}
		else
		{
			logger.info("No protocol selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No protocol is selected. Please select a protocol to remove.");
		}
	}
}
