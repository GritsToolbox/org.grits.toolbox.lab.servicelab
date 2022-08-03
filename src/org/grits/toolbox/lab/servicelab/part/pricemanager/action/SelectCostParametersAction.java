/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.pricemanager.action;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.dialog.SelectParametersDialog;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class SelectCostParametersAction extends Action
{
	private static Logger logger = Logger.getLogger(SelectCostParametersAction.class);
	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;
	private SelectParametersDialog selectionDialog = null;

	public SelectCostParametersAction(TableViewer tableViewer, MDirtyable dirtyable)
	{
		this.tableViewer  = tableViewer;
		this.dirtyable = dirtyable;
		this.setText("Add Cost Parameters");
		this.setToolTipText("Select parameters for adding as cost parameters of protocol");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(ImageRegistry.PluginIcon.CHECKLIST_ICON));
		selectionDialog  = new SelectParametersDialog(Display.getCurrent().getActiveShell());
	}

	@Override
	public void run()
	{
		logger.info("Selecting cost parameters for the protocol");
		if(tableViewer.getInput() != null)
		{
			PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) tableViewer.getInput();
			logger.info("selected protocol : " + priceInfoProtocol.getLabel());

			selectionDialog.setServiceLabProtocol(priceInfoProtocol);

			// only cost parameters with an associated parameter
			Map<Parameter, CostParameter> parameterMap =
					AddCostParameterAction.getParameterMap(priceInfoProtocol);

			if(selectionDialog.open() == Window.OK)
			{
				// remove all the cost parameters with an associated parameter
				// except those cost parameters whose associated parameter is null
				// and hence not in the parameterMap
				for(CostParameter previousCostParameter : parameterMap.values())
				{
					priceInfoProtocol.getCostParameters().remove(previousCostParameter);
				}

				for(Parameter parameter : selectionDialog.getSelectedParameters())
				{
					logger.info("parameter : " + parameter.getName());
					// if parameter was previously added do not create a new cost parameter
					if(parameterMap.containsKey(parameter))
					{
						// add the previous cost parameter with price and other edited values
						priceInfoProtocol.getCostParameters().add(parameterMap.get(parameter));
					}
					else // create a new cost parameter
					{
						logger.info("adding new cost parameter for : " + parameter.getName());
						// create a cost parameter from the selected parameter
						CostParameter costParameter = createCostParameter(parameter);
						priceInfoProtocol.getCostParameters().add(costParameter);

						if(priceInfoProtocol.getParameterGroups() != null)
						{
							// check if group contains it
							for(ParameterGroup parameterGroup : priceInfoProtocol.getParameterGroups())
							{
								if(parameterGroup.getId().equals(parameter.getGroupId()))
								{
									costParameter.setAssociatedParameterGroup(parameterGroup);
									break;
								}
							}
						}
					}
				}

				dirtyable.setDirty(true);
				tableViewer.refresh();
			}
		}
		else
		{
			logger.info("no protocol selected.");
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					"No Selection", "No Protocol is selected. Please select a protocol first.");
		}
	}

	private CostParameter createCostParameter(Parameter parameter)
	{
		CostParameter costParameter = new CostParameter();
		costParameter.setCostParameterName(parameter.getName());
		costParameter.setAssociatedParameter(parameter);
		return costParameter;
	}
}
