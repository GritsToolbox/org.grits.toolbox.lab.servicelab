package org.grits.toolbox.lab.servicelab.part.pricemanager.action.sort;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class TableViewerDoubleComparator extends ViewerComparator
{
	private static final Logger logger = Logger.getLogger(TableViewerDoubleComparator.class);

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2)
	{
		if(viewer instanceof TableViewer)
		{
			TableViewer tableViewer = (TableViewer) viewer;
			TableColumn sortColumn = tableViewer.getTable().getSortColumn();

			// initially sortColumn might not be set
			int columnIndex = sortColumn == null ?
					0 : tableViewer.getTable().indexOf(sortColumn);

			int compareValue = 0;
			if(tableViewer.getLabelProvider() instanceof ITableLabelProvider)
			{
				ITableLabelProvider labelProvider =
						((ITableLabelProvider) tableViewer.getLabelProvider());
				String text1 = labelProvider.getColumnText(obj1, columnIndex);
				String text2 = labelProvider.getColumnText(obj2, columnIndex);

				// no comparison for both null value
				if(text1 == null && text2 == null)
				{
					compareValue = 0;
				}
				else
				{
					try
					{
						// for one of them null, treat it as 0
						double num1 = text1 == null ? 0.0 : Double.parseDouble(text1);
						double num2 = text1 == null ? 0.0 : Double.parseDouble(text2);
						compareValue = num1 > num2 ? 1 : -1;
						// for equal case assign it 0 else leave the compared value
						compareValue = num1 == num2 ? 0 : compareValue;
					} catch (NumberFormatException ex)
					{
						// if even one of them is not double, do text comparison 
						logger.error("Not double values : " + text1 + ", " + text2);
						compareValue = text1.compareToIgnoreCase(text2);
					}
				}
			}

			return tableViewer.getTable().getSortDirection() == SWT.UP
					? compareValue : -compareValue;
		}

		return 0;
	}
}
