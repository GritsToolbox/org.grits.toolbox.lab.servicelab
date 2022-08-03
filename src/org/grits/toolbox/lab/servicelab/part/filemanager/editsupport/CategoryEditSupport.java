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
import org.grits.toolbox.entry.archive.preference.doctype.DocTypePreference;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;
import org.grits.toolbox.lab.servicelab.util.UniqueCategoryValidator;
import org.grits.toolbox.lab.servicelab.util.UniqueSubCategoryValidator;

/**
 * 
 *
 */
public class CategoryEditSupport extends EditingSupport
{
	private static Logger logger = Logger.getLogger(CategoryEditSupport.class);

	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;
	private ComboBoxViewerCellEditor comboBoxViewerCellEditor = null;
	private UniqueCategoryValidator categoryValidator = new UniqueCategoryValidator();
	private UniqueSubCategoryValidator subCategoryValidator = new UniqueSubCategoryValidator();

	public CategoryEditSupport(TableViewer tableViewer, MDirtyable dirtyable)
	{
		super(tableViewer);
		this.tableViewer  = tableViewer;
		this.dirtyable  = dirtyable;
		comboBoxViewerCellEditor = new ComboBoxViewerCellEditor(tableViewer.getTable());
		comboBoxViewerCellEditor.setContentProvider(new ArrayContentProvider());
		comboBoxViewerCellEditor.setValidator(categoryValidator);
		// for loading the list of parameters set the current protocol as input
		DocTypePreference.loadPreferences();
		String[] comboInput = new String[DocTypePreference.ALL_DOCUMENT_TYPES.size()];
		int i = 0;
		for(DocumentType docType  : DocTypePreference.ALL_DOCUMENT_TYPES)
		{
			comboInput[i++] = docType.getLabel();
		}
		comboBoxViewerCellEditor.setInput(comboInput);

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
							"Category Selection", comboBoxViewerCellEditor.getErrorMessage());
				}
			}
		});
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		if(element instanceof FileUpload)
		{
			FileInfoProtocol fileInfoProtocol =
					(FileInfoProtocol) tableViewer.getInput();

			// set the current value as selected item
			FileUpload fileUpload = (FileUpload) element;
			DocumentType docType = CategoryEditSupport
					.getDocumentType(fileUpload.getFileCategory());
			String value = docType == null ? null : docType.getLabel();
			comboBoxViewerCellEditor.setValue(value);

			// also set the values in the validator for appropriate error message
			categoryValidator.setProtocolWithFileUpload(fileInfoProtocol);
			categoryValidator.setFileUpload(fileUpload);
			return comboBoxViewerCellEditor;
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
				((FileUpload) element).getFileCategory() : null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if(element instanceof FileUpload && value instanceof String)
		{
			FileUpload fileUpload = (FileUpload) element;
			String selectedCategory = (String) value;
			if(!selectedCategory.equals(fileUpload.getFileCategory()))
			{
				DocumentType selectedDocType = getDocumentType(selectedCategory);
				if(selectedDocType != null)
				{
					String previousCategory = fileUpload.getFileCategory();
					String previousSubCategory = fileUpload.getFileSubCategory();
					String errorMessage = null;

					fileUpload.setFileCategory(selectedCategory);
					subCategoryValidator.setProtocolWithFileUpload(
							(FileInfoProtocol) tableViewer.getInput());
					subCategoryValidator.setFileUpload(fileUpload);

					for(String subType : selectedDocType.getSubTypes())
					{
						if((errorMessage = subCategoryValidator.isValid(subType)) == null)
						{
							fileUpload.setFileSubCategory(subType);
							break;
						}
					}

					if(errorMessage != null) // no sub-category available for this category
					{
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
								"Category Selection", "Selected category \""+ selectedCategory
										+ "\" has been added already with all its subtypes."
										+ " Please select another category or add new to preference!");

						// reset the previous category
						fileUpload.setFileCategory(previousCategory);
						fileUpload.setFileSubCategory(previousSubCategory);
					}

					getViewer().update(element, null);
					dirtyable.setDirty(true);
				}
			}
		}
	}

	public static DocumentType getDocumentType(String selectedCategory)
	{
		if(selectedCategory == null)
			return null;

		DocumentType documentType = null;
		// match with all the categories available in preference
		for(DocumentType docType : DocTypePreference.ALL_DOCUMENT_TYPES)
		{
			if(docType.getLabel().equals(selectedCategory))
			{
				documentType = docType.clone();
				break;
			}
		}
		return documentType;
	}
}
