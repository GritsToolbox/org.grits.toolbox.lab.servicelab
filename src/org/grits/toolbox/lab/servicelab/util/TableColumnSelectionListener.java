package org.grits.toolbox.lab.servicelab.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 *
 */
public class TableColumnSelectionListener implements SelectionListener
{
	private TableViewer tableViewer;

	public TableColumnSelectionListener(TableViewer tableViewer)
	{
		this.tableViewer = tableViewer;
	}

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		TableColumn column = (TableColumn) e.getSource();
		Table table = column.getParent();
		table.setSortColumn(column);
		int nextDirection = table.getSortDirection() == SWT.UP
				? SWT.DOWN : SWT.UP;
		table.setSortDirection(nextDirection);
		tableViewer.refresh();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{

	}

}
