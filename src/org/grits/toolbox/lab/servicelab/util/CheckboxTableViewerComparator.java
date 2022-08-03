package org.grits.toolbox.lab.servicelab.util;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class CheckboxTableViewerComparator extends ViewerComparator
{
	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2)
	{
		if(viewer instanceof CheckboxTableViewer)
		{
			CheckboxTableViewer checkboxTableViewer = (CheckboxTableViewer) viewer;
			TableColumn sortColumn = checkboxTableViewer.getTable().getSortColumn();

			// initially sortColumn might not be set
			int columnIndex = sortColumn == null ?
					1 : checkboxTableViewer.getTable().indexOf(sortColumn);

			int compareValue = 0;
			if(columnIndex == 0)
			{
				int checked1 = checkboxTableViewer.getChecked(obj1) ? 1 : 0;
				int checked2 = checkboxTableViewer.getChecked(obj2) ? 1 : 0;
				compareValue = checked1 - checked2;
			}
			else if(checkboxTableViewer.getLabelProvider() instanceof ITableLabelProvider)
			{
				ITableLabelProvider labelProvider =
						((ITableLabelProvider) checkboxTableViewer.getLabelProvider());
				String text1 = labelProvider.getColumnText(obj1, columnIndex);
				String text2 = labelProvider.getColumnText(obj2, columnIndex);

				// no direct comparison when either of the values is null
				if(text1 == null)
				{
					compareValue = text2 == null ? 0 : -1;
				}
				else if(text2 == null)
				{
					compareValue = 1;
				}
				else // string comparison for their label
				{
					compareValue = text1.compareToIgnoreCase(text2);
				}
			}

			return checkboxTableViewer.getTable().getSortDirection() == SWT.UP
					? compareValue : -compareValue;
		}

		return 0;
	}
}
