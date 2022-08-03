package org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport;

import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class CommonNameEditSupport extends EditingSupport
{
	private TextCellEditor textCellEditor = null;
	private MDirtyable dirtyable = null;

	public CommonNameEditSupport(TableViewer tableViewer, MDirtyable dirtyable)
	{
		super(tableViewer);
		textCellEditor = new TextCellEditor(tableViewer.getTable());
		this.dirtyable  = dirtyable;
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return textCellEditor;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return true;
	}

	@Override
	protected Object getValue(Object element)
	{
		return element instanceof PriceInfoProtocol ?
				((PriceInfoProtocol) element).getCommonName() : null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if (element instanceof PriceInfoProtocol)
		{
			PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) element;
			String commonName = value instanceof String ? ((String) value).trim() : "";
			commonName = commonName.isEmpty() ? priceInfoProtocol.getLabel() : commonName;
			if(!commonName.equals(priceInfoProtocol.getCommonName()))
			{
				priceInfoProtocol.setCommonName(commonName);
				getViewer().update(element, null);
				dirtyable.setDirty(true);
			}
		}
	}
}
