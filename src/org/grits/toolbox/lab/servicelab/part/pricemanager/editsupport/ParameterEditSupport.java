package org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;
import org.grits.toolbox.lab.servicelab.part.pricemanager.provider.ParameterListContentProvider;
import org.grits.toolbox.lab.servicelab.util.UniqueParameterValidator;

/**
 * 
 *
 */
public class ParameterEditSupport extends EditingSupport
{
	private static Logger logger = Logger.getLogger(ParameterEditSupport.class);

	private ComboBoxViewerCellEditor comboBoxViewerCellEditor = null;
	private TableViewer tableViewer = null;
	private MDirtyable dirtyable = null;
	private Map<Parameter, String> parameterLabelMap = new HashMap<Parameter, String>();
	private UniqueParameterValidator validator = new UniqueParameterValidator();

	public ParameterEditSupport(TableViewer tableViewer, MDirtyable dirtyable)
	{
		super(tableViewer);
		this.tableViewer  = tableViewer;
		comboBoxViewerCellEditor = new ComboBoxViewerCellEditor(tableViewer.getTable());
		this.dirtyable  = dirtyable;
		comboBoxViewerCellEditor.setContentProvider(new ParameterListContentProvider(parameterLabelMap));
		comboBoxViewerCellEditor.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return parameterLabelMap.get(element);
			}
		});

		comboBoxViewerCellEditor.setValidator(validator);
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
							"Parameter Added", comboBoxViewerCellEditor.getErrorMessage());
				}
			}
		});
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		if(element instanceof CostParameter)
		{
			PriceInfoProtocol priceInfoProtocol = (PriceInfoProtocol) tableViewer.getInput();

			// for loading the list of parameters set the current protocol as input
			comboBoxViewerCellEditor.setInput(priceInfoProtocol);

			// set the current value as selected item
			CostParameter costParameter = (CostParameter) element;

			// some cost parameters might not have an associated parameter
			// check if it has an associated parameter else do not select anything
			if(costParameter.getAssociatedParameter() != null)
			{
				comboBoxViewerCellEditor.setValue(costParameter.getAssociatedParameter());
			}

			// also set the values in the validator for appropriate error message
			validator.setServiceLabProtocol(priceInfoProtocol);
			validator.setCostParameter(costParameter);
			return comboBoxViewerCellEditor;
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return element instanceof CostParameter;
	}

	@Override
	protected Object getValue(Object element)
	{
		return element instanceof CostParameter ?
				((CostParameter) element).getAssociatedParameter() : null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if(element instanceof CostParameter && value instanceof Parameter)
		{
			CostParameter costParameter = (CostParameter) element;
			Parameter selectedParameter = (Parameter) value;
			if(!selectedParameter.equals(costParameter.getAssociatedParameter()))
			{
				costParameter.setAssociatedParameter(selectedParameter);
				// check if a group contains it
				for(ParameterGroup parameterGroup : ((PriceInfoProtocol)
						tableViewer.getInput()).getParameterGroups())
				{
					if(parameterGroup.getId().equals(selectedParameter.getGroupId()))
					{
						costParameter.setAssociatedParameterGroup(parameterGroup);
						break;
					}
				}
				getViewer().update(element, null);
				dirtyable.setDirty(true);
			}
		}
	}
}
