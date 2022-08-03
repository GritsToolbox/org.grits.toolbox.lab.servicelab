/**
 * 
 */
package org.grits.toolbox.lab.servicelab.invoice;

import org.apache.log4j.Logger;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.ontology.OntologyManager;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * a class containing general purpose methods for calculating price
 * of protocols and other price related information 
 * 
 *
 */
public class ProtocolPriceCalculator
{
	private static Logger logger = Logger.getLogger(ProtocolPriceCalculator.class);

	public static InaccurateDoubleValue calculatePriceWithInaccuracy(ProtocolNode protocolNode,
			PriceInfoProtocol priceInfoProtocol, int industryOrNonProfit)
	{
		InaccurateDoubleValue inaccuratePrice = new InaccurateDoubleValue();
		InaccurateDoubleValue inaccurateParameterValue = null;
		StringBuilder inaccuracyMessageBuilder = new StringBuilder();
		for(CostParameter costParameter : priceInfoProtocol.getCostParameters())
		{
			if(costParameter.getAssociatedParameter() == null)
			{
				inaccuratePrice.setValue(inaccuratePrice.getValue()
						+ costParameter.getPrice(industryOrNonProfit));
			}
			else
			{
				inaccurateParameterValue = getParameterValueWithInaccuracy(costParameter, protocolNode);
				inaccuratePrice.setValue(inaccuratePrice.getValue()
						+ costParameter.getPrice(industryOrNonProfit)*inaccurateParameterValue.getValue());

				// check if it has inaccuracy
				if(inaccurateParameterValue.getInaccuracyMessage() != null)
				{
					inaccuracyMessageBuilder.append(inaccurateParameterValue.getInaccuracyMessage())
					.append(System.lineSeparator());
				}
			}
		}

		// check if there was any inaccuracy
		if(inaccuracyMessageBuilder.length() > 0)
			inaccuratePrice.setInaccuracyMessage(inaccuracyMessageBuilder.toString().trim());

		return inaccuratePrice;
	}

	public static double getConstantRate(PriceInfoProtocol priceInfoProtocol, int industryOrNonProfit)
	{
		double constantRate = 0;
		for(CostParameter costParameter : priceInfoProtocol.getCostParameters())
		{
			// if one of its cost parameter is dynamic, set the constant rate to zero
			if(costParameter.getAssociatedParameter() != null)
			{
				constantRate = 0;
				break;
			}
			constantRate += costParameter.getPrice(industryOrNonProfit);
		}
		return constantRate;
	}

	private static InaccurateDoubleValue getParameterValueWithInaccuracy(
			CostParameter costParameter, ProtocolNode protocolNode)
	{
		InaccurateDoubleValue inaccurateParameterValue = new InaccurateDoubleValue();
		double totalValue = 0;
		StringBuilder errorStringBuilder = new StringBuilder();
		String parameterName = null;
		int numOfOccurrence = 0;
		if(costParameter.getAssociatedParameterGroup() == null)
		{
			for(Parameter parameter : protocolNode.getParameters())
			{
				// check each of protocol parameters with cost parameter
				if(parameter.getUri().equals(costParameter.getAssociatedParameter().getUri()))
				{
					parameterName = parameter.getName();
					inaccurateParameterValue = getInaccurateValueForParameter(parameter);

					// in any case add numeric values for this parameter
					totalValue += inaccurateParameterValue.getValue();
					if(inaccurateParameterValue.getInaccuracyMessage() != null)
					{
						errorStringBuilder.append(inaccurateParameterValue.getInaccuracyMessage())
						.append(System.lineSeparator());
					}
					numOfOccurrence++;
				}
			}
		}
		// check if protocol has any parameter group to match
		else if(protocolNode.getParameterGroups() == null)
		{
			logger.error("No parameter group was found to match : "
					+ costParameter.getAssociatedParameterGroup().getLabel());
			errorStringBuilder.append("No parameter group was found to match : "
					+ costParameter.getAssociatedParameterGroup().getLabel());
		}
		else
		{
			for(ParameterGroup parameterGroup : protocolNode.getParameterGroups())
			{
				if(parameterGroup.getUri().equals(costParameter.getAssociatedParameterGroup().getUri()))
				{
					for(Parameter parameter : parameterGroup.getParameters())
					{
						// check each of protocol parameters with cost parameter
						if(parameter.getUri().equals(costParameter.getAssociatedParameter().getUri()))
						{
							parameterName = parameter.getName();
							inaccurateParameterValue = getInaccurateValueForParameter(parameter);

							// in any case add numeric values for this parameter
							totalValue += inaccurateParameterValue.getValue();
							if(inaccurateParameterValue.getInaccuracyMessage() != null)
							{
								errorStringBuilder.append(inaccurateParameterValue.getInaccuracyMessage())
								.append(System.lineSeparator());
							}
							numOfOccurrence++;
						}
					}
				}
			}
		}

		if(numOfOccurrence > 1)
		{
			logger.error("Multiple parameters " + parameterName +
					" of cost parameter " + costParameter.getCostParameterName() +
					" in protocol " + protocolNode.getLabel());
			errorStringBuilder.append("Parameter \"").append(parameterName)
			.append("\" has been added multiple times.").append(System.lineSeparator());
		}


		inaccurateParameterValue.setValue(totalValue);
		// check if there was any inaccuracy
		if(errorStringBuilder.length() > 0)
			inaccurateParameterValue.setInaccuracyMessage(errorStringBuilder.toString().trim());

		return inaccurateParameterValue;
	}

	private static InaccurateDoubleValue getInaccurateValueForParameter(Parameter parameter)
	{
		InaccurateDoubleValue inaccurateParameterValue = new InaccurateDoubleValue();

		if((OntologyManager.baseURI + "double").equals(parameter.getNamespace()) ||
				(OntologyManager.baseURI + "integer").equals(parameter.getNamespace()))
		{
			try
			{
				inaccurateParameterValue.setValue(getParameterValue(parameter));

				if(parameter.getAvailableUnits() != null
						&& !parameter.getAvailableUnits().isEmpty())
				{
					inaccurateParameterValue.setInaccuracyMessage(
							"Unit conversion is not yet available for parameter - "
									+ parameter.getName());
				}
			} catch (NumberFormatException e)
			{
				logger.error(e);
				inaccurateParameterValue.setInaccuracyMessage(e.getMessage());
			}
		}
		else
		{
			logger.error("Parameter does not have a double/integer namespace - "
					+ parameter.getName() + " : " + parameter.getNamespace());
			inaccurateParameterValue.setInaccuracyMessage(
					"Parameter of non-numeric type is not supported - "
							+ parameter.getName());
		}

		return inaccurateParameterValue;
	}

	private static double getParameterValue(Parameter parameter) throws NumberFormatException
	{
		if(parameter.getValue() != null && !parameter.getValue().isEmpty())
		{
			try
			{
				return Double.parseDouble(parameter.getValue());
			} catch (NumberFormatException e)
			{
				logger.error("Cannot parse double value for parameter : " + parameter.getName() +
						" - " + parameter.getValue());
				throw new NumberFormatException("Not a numeric value for parameter - "
						+ parameter.getName() + " : " + parameter.getValue());
			}
		}
		logger.error("null/empty value for the parameter." + parameter.getName());
		throw new NumberFormatException("Empty value for parameter - " + parameter.getName());
	}
}
