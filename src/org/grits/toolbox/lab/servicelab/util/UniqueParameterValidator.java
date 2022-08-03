package org.grits.toolbox.lab.servicelab.util;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 * 
 *
 */
public class UniqueParameterValidator implements ICellEditorValidator
{
	private static Logger logger = Logger.getLogger(UniqueParameterValidator.class);

	private PriceInfoProtocol priceInfoProtocol = null;
	private CostParameter costParameter = null;

	public void setServiceLabProtocol(PriceInfoProtocol priceInfoProtocol)
	{
		this.priceInfoProtocol = priceInfoProtocol;
	}

	public void setCostParameter(CostParameter costParameter)
	{
		this.costParameter = costParameter;
	}

	@Override
	public String isValid(Object value)
	{
		// no protocol to check with
		if(priceInfoProtocol == null)
			return "Protocol not selected";

		// no cost parameter to check for
		if(costParameter == null)
			return "Cost Parameter not selected";

		String errorMessage = null;
		if(value instanceof Parameter)
		{
			for(CostParameter costParam : priceInfoProtocol.getCostParameters())
			{
				if(!costParam.equals(costParameter)
						&& value.equals(costParam.getAssociatedParameter()))
				{
					errorMessage = "Parameter \"" + costParam.getAssociatedParameter().getName()
							+ "\" has been already added to another cost parameter - "
							+ costParam.getCostParameterName();
					logger.error(errorMessage);
					break;
				}
			}
		}
		else // value is of some other type
		{
			errorMessage = "Selected value is not a parameter";
		}

		logger.info(errorMessage);
		return errorMessage;
	}
}
