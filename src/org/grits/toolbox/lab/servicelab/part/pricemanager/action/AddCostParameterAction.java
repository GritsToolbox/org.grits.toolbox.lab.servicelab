/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.pricemanager.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.lab.servicelab.dialog.AddCostParameterDialog;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class AddCostParameterAction extends Action
{
	private static Logger logger = Logger.getLogger(AddCostParameterAction.class);
	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;
	private AddCostParameterDialog addCostParameterDialog = null;

	public AddCostParameterAction(TableViewer tableViewer, MDirtyable dirtyable)
	{
		this.tableViewer  = tableViewer;
		this.dirtyable = dirtyable;
		this.setText("Add a Cost Parameter");
		this.setToolTipText("Add a cost parameter to the protocol");
		this.setImageDescriptor(ImageShare.ADD_ICON);
		addCostParameterDialog  = new AddCostParameterDialog(Display.getCurrent().getActiveShell());
	}

	@Override
	public void run()
	{
		logger.info("Adding cost parameter to the protocol");
		if(tableViewer.getInput() != null)
		{
			PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) tableViewer.getInput();
			logger.info("selected protocol : " + priceInfoProtocol.getLabel());

			addCostParameterDialog.setServiceLabProtocol(priceInfoProtocol);
			Map<Parameter, CostParameter> parameterMap = getParameterMap(priceInfoProtocol);
			if(addCostParameterDialog.open() == Window.OK)
			{
				CostParameter costParameter = addCostParameterDialog.getCostParameter();
				logger.info("adding cost parameter : " + costParameter.getCostParameterName());

				Parameter parameter = costParameter.getAssociatedParameter();
				// remove the previous cost parameter with price and other edited values
				if(parameterMap.containsKey(parameter))
				{
					logger.info("removing previous similar cost parameter for : " + parameter.getName());
					priceInfoProtocol.getCostParameters().remove(parameterMap.get(parameter));
				}

				priceInfoProtocol.getCostParameters().add(costParameter);

				tableViewer.refresh();
				dirtyable.setDirty(true);
			}

		}
		else
		{
			logger.info("no protocol selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No Protocol is selected. Please select a protocol first.");
		}
	}

	public static Map<Parameter, CostParameter> getParameterMap(PriceInfoProtocol priceInfoProtocol)
	{
		Map<Parameter, CostParameter> parameterMap = new HashMap<Parameter, CostParameter>();
		for(CostParameter costParameter : priceInfoProtocol.getCostParameters())
		{
			if(costParameter.getAssociatedParameter() != null)
			{
				parameterMap.put(costParameter.getAssociatedParameter(), costParameter);
			}
		}
		return parameterMap;
	}
}
