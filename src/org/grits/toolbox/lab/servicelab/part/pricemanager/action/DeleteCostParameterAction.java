/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.pricemanager.action;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class DeleteCostParameterAction extends Action
{
	private static Logger logger = Logger.getLogger(DeleteCostParameterAction.class);
	private TableViewer protocolInfoTableViewer = null;
	private MDirtyable dirtyable = null;

	public DeleteCostParameterAction(TableViewer protocolInfoTableViewer, MDirtyable dirtyable)
	{
		this.protocolInfoTableViewer  = protocolInfoTableViewer;
		this.dirtyable = dirtyable;
		this.setText("Delete Cost Parameter");
		this.setToolTipText("Delete cost parameter from the protocol");
		this.setImageDescriptor(ImageShare.DELETE_ICON);
	}

	@Override
	public void run()
	{
		logger.info("Deleting cost parameter to the protocol");
		if(!protocolInfoTableViewer.getSelection().isEmpty())
		{
			Object selection = ((StructuredSelection) protocolInfoTableViewer.getSelection()).getFirstElement();
			if(selection instanceof CostParameter)
			{
				// get the selected cost parameter
				CostParameter costParameter = (CostParameter) selection;

				// get the selected service protocol whose cost parameter is selected
				PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) protocolInfoTableViewer.getInput();

				logger.info("selected protocol : " + priceInfoProtocol.getLabel());
				logger.info("cost parameter to remove  : " + costParameter.getCostParameterName());

				// remove the cost parameter from the protocol
				priceInfoProtocol.getCostParameters().remove(costParameter);

				protocolInfoTableViewer.refresh();
				dirtyable.setDirty(true);
			}
		}
		else
		{
			logger.info("no cost parameter selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No cost parameter is selected. Please select a cost parameter to delete.");
		}
	}
}
