package org.grits.toolbox.lab.servicelab.part.filemanager.editsupport;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;
import org.grits.toolbox.lab.servicelab.util.UniqueSubCategoryValidator;

/**
 * 
 *
 */
public class SubCategoryEditSupport extends EditingSupport
{
	private static Logger logger = Logger.getLogger(SubCategoryEditSupport.class);

	private MDirtyable dirtyable = null;
	private TableViewer tableViewer = null;
	private ComboBoxViewerCellEditor comboBoxViewerCellEditor = null;
	private UniqueSubCategoryValidator subCategoryValidator = null;

	public SubCategoryEditSupport(TableViewer tableViewer, MDirtyable dirtyable)
	{
		super(tableViewer);
		this.tableViewer  = tableViewer;
		this.dirtyable = dirtyable;
		comboBoxViewerCellEditor = new ComboBoxViewerCellEditor(tableViewer.getTable());
		comboBoxViewerCellEditor.setContentProvider(new ArrayContentProvider());
		subCategoryValidator  = new UniqueSubCategoryValidator();
		comboBoxViewerCellEditor.setValidator(subCategoryValidator);

		comboBoxViewerCellEditor.addListener(new ICellEditorListener()
		{
			@Override
			public void editorValueChanged(boolean oldValidState, boolean newValidState)
			{
				logger.debug("cell editor value changed " + oldValidState + ", " + newValidState);
				comboBoxViewerCellEditor.getViewer().refresh();
			}

			@Override
			public void cancelEditor()
			{
				logger.debug("cell editor Canceled");
			}

			@Override
			public void applyEditorValue()
			{
				logger.info("Apply editor value");
				if(!comboBoxViewerCellEditor.isValueValid())
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							"Sub Category Selection", comboBoxViewerCellEditor.getErrorMessage());
				}
			}
		});
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		if(element instanceof FileUpload)
		{
			// set the current value as selected item
			FileUpload fileUpload = (FileUpload) element;
			FileInfoProtocol fileInfoProtocol =
					(FileInfoProtocol) tableViewer.getInput();

			DocumentType docType = CategoryEditSupport
					.getDocumentType(fileUpload.getFileCategory());
			if(docType != null)
			{
				comboBoxViewerCellEditor.setInput(docType.getSubTypes());
				comboBoxViewerCellEditor.setValue(fileUpload.getFileSubCategory());

				// also set the values in the validator for appropriate error message
				subCategoryValidator.setProtocolWithFileUpload(fileInfoProtocol);
				subCategoryValidator.setFileUpload(fileUpload);
				return comboBoxViewerCellEditor;
			}
			else
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Category not found",
						"Category \"" + fileUpload.getFileCategory()
						+ "\" was not found in current category preference list."
						+ " Please first change the category or add this category"
						+ " to category preference list.");
			}
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return element instanceof FileUpload;
	}

	@Override
	protected Object getValue(Object element)
	{
		return element instanceof FileUpload ?
				((FileUpload) element).getFileSubCategory() : null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if(element instanceof FileUpload && value instanceof String)
		{
			FileUpload fileUpload = (FileUpload) element;
			String selectedSubCategory = (String) value;
			if(!selectedSubCategory.equals(fileUpload.getFileSubCategory()))
			{
				fileUpload.setFileSubCategory(selectedSubCategory);
				getViewer().update(element, null);
				dirtyable.setDirty(true);
			}
		}
	}
}
