package org.grits.toolbox.lab.servicelab.part.filemanager.action.sort;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class TableViewerStringComparator extends ViewerComparator
{
	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2)
	{
		if(viewer instanceof TableViewer)
		{
			TableViewer tableViewer = (TableViewer) viewer;
			TableColumn sortColumn = tableViewer.getTable().getSortColumn();

			// set default sortColumn to 0
			int columnIndex = sortColumn == null ?
					0 : tableViewer.getTable().indexOf(sortColumn);

			int compareValue = 0;
			if(tableViewer.getLabelProvider() instanceof ITableLabelProvider)
			{
				ITableLabelProvider labelProvider =
						((ITableLabelProvider) tableViewer.getLabelProvider());
				String text1 = labelProvider.getColumnText(obj1, columnIndex);
				String text2 = labelProvider.getColumnText(obj2, columnIndex);

				// no comparison for either of the values is null
				if(text1 == null)
				{
					compareValue = text2 == null ? 0 : -1;
				}
				else if(text2 == null)
				{
					compareValue = 1;
				}
				else // compare only if neither is null
				{
					compareValue = text1.compareToIgnoreCase(text2);
				}
			}

			return tableViewer.getTable().getSortDirection() == SWT.UP
					? compareValue : -compareValue;
		}

		return 0;
	}
}
