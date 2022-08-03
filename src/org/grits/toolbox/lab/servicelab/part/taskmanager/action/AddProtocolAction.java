/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.taskmanager.action;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.lab.servicelab.dialog.SelectProtocolsDialog;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;
import org.grits.toolbox.lab.servicelab.part.taskmanager.TaskProtocolManager;

/**
 * 
 *
 */
public class AddProtocolAction extends Action
{
	private static Logger logger = Logger.getLogger(AddProtocolAction.class);

	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;
	private SelectProtocolsDialog selectProtocolsDialog = null;

	public AddProtocolAction(TableViewer tableViewer,
			HashMap<String, MinInfoProtocol> protocolUriLabelNodeMap, MDirtyable dirtyable)
	{
		this.tableViewer  = tableViewer;
		this.dirtyable = dirtyable;
		this.setText("Add a protocol");
		this.setToolTipText("Add a protocol to this task.");
		this.setImageDescriptor(ImageShare.ADD_ICON);

		// create a selection dialog instance to be reused
		selectProtocolsDialog = new SelectProtocolsDialog(
				Display.getCurrent().getActiveShell(), protocolUriLabelNodeMap);
	}

	@Override
	public void run()
	{
		logger.info("Adding protocol to the task");
		if(tableViewer.getInput() != null)
		{
			// get the selected task for adding the protocols
			ServiceLabTask serviceLabTask = (ServiceLabTask) tableViewer.getInput();
			logger.info("selected task : " + serviceLabTask.getTaskName());

			// set the selection with currently selected protocols
			selectProtocolsDialog.setSelectedNodes(serviceLabTask.getProtocolNodes());

			if(selectProtocolsDialog.open() == Window.OK)
			{
				// get the latest selection
				serviceLabTask.setProtocolNodes(selectProtocolsDialog.getSelectedNodes());

				// for non-empty protocols, set min. no. of protocol atleast to 1
				if(serviceLabTask.getProtocolNodes().size() >= 1)
				{
					if(serviceLabTask.getMinProtocols() < 1)
					{
						serviceLabTask.setMinProtocols(1);

						// max no. of protocol should atleast be equal to min no. of protocol
						if(serviceLabTask.getMaxProtocols()
								< serviceLabTask.getMinProtocols())
						{
							serviceLabTask.setMaxProtocols(
									serviceLabTask.getMinProtocols());
						}

						// refresh the left-side table after resetting min/max value
						((TableViewer) tableViewer.getData(
								TaskProtocolManager.KEY_LEFT_SIDE_TABLE)).refresh();
					}
				}

				dirtyable.setDirty(true);
				tableViewer.refresh();
			}
		}
		else
		{
			logger.info("no task selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No task is selected. Please select a task first.");
		}
	}
}
