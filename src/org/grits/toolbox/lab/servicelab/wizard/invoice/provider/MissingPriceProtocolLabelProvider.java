package org.grits.toolbox.lab.servicelab.wizard.invoice.provider;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;

/**
 * 
 *
 */
public class MissingPriceProtocolLabelProvider implements ITableLabelProvider
{
	@Override
	public void addListener(ILabelProviderListener listener)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		String value = null;
		if(element instanceof Entry<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Entry<org.grits.toolbox.core.datamodel.Entry, List<ProtocolNode>>
			mapEntry = (Entry<org.grits.toolbox.core.datamodel.Entry, List<ProtocolNode>>) element;
			switch (columnIndex)
			{
				case 0:
					value = mapEntry.getKey().getDisplayName();
					break;

				case 1:
					value = getCSVSet(mapEntry.getValue());
					break;

				case 2:
					value = "0.0";
					break;

				default:
					break;
			}
		}
		return value;
	}

	private String getCSVSet(List<ProtocolNode> protocolNodes)
	{
		String csvSet = "";
		Set<String> addedNames = new HashSet<String>();
		for(ProtocolNode protocolNode : protocolNodes)
		{
			if(!addedNames.contains(protocolNode.getLabel()))
			{
				csvSet += protocolNode.getLabel() + ", ";
				addedNames.add(protocolNode.getLabel());
			}
		}
		csvSet = csvSet.trim();
		csvSet = csvSet.endsWith(",")
				? csvSet.substring(0, csvSet.lastIndexOf(",")) : csvSet;
		return csvSet;
	}
}
