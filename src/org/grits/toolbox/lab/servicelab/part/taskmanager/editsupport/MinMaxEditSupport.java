package org.grits.toolbox.lab.servicelab.part.taskmanager.editsupport;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.sample.utilities.CellEditorWithSpinner;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;

/**
 * 
 *
 */
public class MinMaxEditSupport extends EditingSupport
{
	private static final Logger logger = Logger.getLogger(MinMaxEditSupport.class);

	public enum ProtocolMinMaxSetter
	{
		MINIMUM_PROTOCOL, MAXIMUM_PROTOCOL
	}

	private MDirtyable dirtyable = null;
	private CellEditorWithSpinner cellEditorWithSpinner = null;
	private ProtocolMinMaxSetter protocolMinMax = ProtocolMinMaxSetter.MINIMUM_PROTOCOL;

	public MinMaxEditSupport(TableViewer tableViewer,
			MDirtyable dirtyable, ProtocolMinMaxSetter protocolMinMax)
	{
		super(tableViewer);
		this.dirtyable  = dirtyable;
		cellEditorWithSpinner = new CellEditorWithSpinner(tableViewer.getTable());
		this.protocolMinMax = protocolMinMax;
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return cellEditorWithSpinner;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return element instanceof ServiceLabTask;
	}

	@Override
	protected Object getValue(Object element)
	{
		if(element instanceof ServiceLabTask)
		{
			ServiceLabTask serviceLabTask = (ServiceLabTask) element;
			switch (protocolMinMax)
			{
				case MINIMUM_PROTOCOL:
					return serviceLabTask.getMinProtocols();

				case MAXIMUM_PROTOCOL:
					return serviceLabTask.getMaxProtocols();

				default:
					logger.fatal("neither min nor max protocols to be set : " + protocolMinMax);
			}
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value)
	{
		if (element instanceof ServiceLabTask)
		{
			ServiceLabTask serviceLabTask = (ServiceLabTask) element;
			int intValue = value instanceof Integer ? (int) value : 0;

			// for non-empty protocols minimum allowed value is 1
			if(!serviceLabTask.getProtocolNodes().isEmpty())
			{
				intValue = intValue > 0 ? intValue : 1;
			}
			else if(intValue > 0)
				// for empty protocols range is reset to 0 with a message
			{
				intValue = 0;
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						"No Protocol Added", "No protocols added to the task."
								+ " Please add a protocol first for setting protocol range.");
			}

			switch (protocolMinMax)
			{
				case MINIMUM_PROTOCOL:
					if(serviceLabTask.getMinProtocols() != intValue)
					{
						serviceLabTask.setMinProtocols(intValue);

						// set maximum number of protocols to minimum number of protocols if it is less
						if(serviceLabTask.getMaxProtocols() < serviceLabTask.getMinProtocols())
						{
							serviceLabTask.setMaxProtocols(serviceLabTask.getMinProtocols());
						}
						dirtyable.setDirty(true);
					}
					break;

				case MAXIMUM_PROTOCOL:
					if(serviceLabTask.getMaxProtocols() != intValue)
					{
						serviceLabTask.setMaxProtocols(intValue);

						// set minimum number of protocols to maximum number of protocols if it is more
						if(serviceLabTask.getMinProtocols() > serviceLabTask.getMaxProtocols())
						{
							serviceLabTask.setMinProtocols(serviceLabTask.getMaxProtocols());
						}
						dirtyable.setDirty(true);
					}
					break;

				default:
					logger.fatal("neither min nor max protocols to be set : " + protocolMinMax);
					break;
			}
			getViewer().update(element, null);
		}
	}
}
