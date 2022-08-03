package org.grits.toolbox.lab.servicelab.part.filemanager.editsupport;

import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.grits.toolbox.entry.sample.utilities.CellEditorWithSpinner;
import org.grits.toolbox.lab.servicelab.model.FileUpload;

/**
 * 
 *
 */
public class NumberOfFilesEditSupport extends EditingSupport
{
	private MDirtyable dirtyable = null;
	private CellEditorWithSpinner cellEditorWithSpinner = null;

	public NumberOfFilesEditSupport(TableViewer tableViewer,
			MDirtyable dirtyable)
	{
		super(tableViewer);
		this.dirtyable  = dirtyable;
		cellEditorWithSpinner = new CellEditorWithSpinner(tableViewer.getTable());
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return cellEditorWithSpinner;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return element instanceof FileUpload;
	}

	@Override
	protected Object getValue(Object element)
	{
		if(element instanceof FileUpload)
		{
			return ((FileUpload) element).getNumberOfFiles();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if (element instanceof FileUpload)
		{
			if(value == null)
				return;

			int intValue = (int) value;
			FileUpload fileUpload = (FileUpload) element;
			if(fileUpload.getNumberOfFiles() != intValue)
			{
				fileUpload.setNumberOfFiles(intValue);
				getViewer().update(element, null);
				dirtyable.setDirty(true);
			}
		}
	}
}
