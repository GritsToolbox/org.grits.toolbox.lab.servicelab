package org.grits.toolbox.lab.servicelab.part.pricemanager.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

public class ParameterListContentProvider implements IStructuredContentProvider
{
	private Map<Parameter, String> parameterLabelMap = null;

	public ParameterListContentProvider(Map<Parameter, String> parameterLabelMap)
	{
		this.parameterLabelMap  = parameterLabelMap;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		
	}
	
	@Override
	public void dispose()
	{
		
	}
	
	@Override
	public Object[] getElements(Object inputElement)
	{
		if(inputElement instanceof PriceInfoProtocol)
		{
			PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) inputElement;
			List<Parameter> parameterList = new ArrayList<Parameter>();
			parameterLabelMap.clear();
			if(priceInfoProtocol.getParameters() != null)
			{
				for(Parameter parameter : priceInfoProtocol.getParameters())
				{
					parameterList.add(parameter);
					parameterLabelMap.put(parameter, parameter.getName());
				}
			}
			if(priceInfoProtocol.getParameterGroups() != null)
			{
				for(ParameterGroup parameterGroup : priceInfoProtocol.getParameterGroups())
				{
					for(Parameter parameter : parameterGroup.getParameters())
					{
						parameterList.add(parameter);
						parameterLabelMap.put(parameter,
								parameterGroup.getLabel() + " - " + parameter.getName());
					}
				}
			}
			return parameterList.toArray();
		}
		return null;
	}
}
