package org.grits.toolbox.lab.servicelab.part.pricemanager.provider;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.lab.servicelab.model.CostParameter;

public class CostParameterLabelProvider implements ITableLabelProvider
{
	@Override
	public void addListener(ILabelProviderListener listener)
	{
		
	}

	@Override
	public void dispose()
	{
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
		
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(element instanceof CostParameter)
		{
			CostParameter costParameter = ((CostParameter) element);
			String value = null;
			switch (columnIndex)
			{
				case 0:
					value = costParameter.getCostParameterName();
					break;
				case 1:
					value = costParameter.getIndustryPrice() + "";
					break;
				case 2:
					value = costParameter.getNonProfitPrice() + "";
					break;
				case 3:
					value = costParameter.getAssociatedParameterGroup() == null ?
							"" : costParameter.getAssociatedParameterGroup().getLabel() + " - ";
					value += costParameter.getAssociatedParameter() == null ?
							"" : costParameter.getAssociatedParameter().getName();
					break;
				default:
					break;
			}
			return value;
		}
		return null;
	}
}
