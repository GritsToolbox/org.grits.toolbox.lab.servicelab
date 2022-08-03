package org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport;

import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.grits.toolbox.lab.servicelab.model.CostParameter;

/**
 * 
 *
 */
public class CostNameEditSupport extends EditingSupport
{
	private TextCellEditor textCellEditor = null;
	private MDirtyable dirtyable = null;

	public CostNameEditSupport(TableViewer tableViewer, MDirtyable dirtyable)
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
		return element instanceof CostParameter ?
				((CostParameter) element).getCostParameterName() : null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if (element instanceof CostParameter)
		{
			CostParameter costParameter = (CostParameter) element;
			if(value instanceof String)
			{
				String name = ((String) value).trim();

				// for empty name use associated parameter name
				if(name.isEmpty() && costParameter.getAssociatedParameter() != null)
				{
					name = costParameter.getAssociatedParameter().getName();
				}

				if(!name.equals(costParameter.getCostParameterName()))
				{
					costParameter.setCostParameterName(name);
					getViewer().update(element, null);
					dirtyable.setDirty(true);
				}
			}
		}
	}
}
