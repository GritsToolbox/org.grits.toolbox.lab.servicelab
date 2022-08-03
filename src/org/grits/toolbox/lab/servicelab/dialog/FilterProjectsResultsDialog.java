package org.grits.toolbox.lab.servicelab.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;

public class FilterProjectsResultsDialog extends TitleAreaDialog {

	List<Entry> projects;
	
	public FilterProjectsResultsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Matched Projects");
		
		if (projects.isEmpty()) {
			setMessage ("No projects match the given timeframe");
			return parent;
		}
		else {
			setMessage("The projects that match the given timeframe");
		}
		
		Composite tableComposite = new Composite(parent, SWT.NONE);
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout(tableLayout);
		TableViewer xslTable = new TableViewer(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		xslTable.getTable().setLinesVisible(true);
		xslTable.getTable().setHeaderVisible(true);
		
		TableViewerColumn projectName = new TableViewerColumn(xslTable, SWT.NONE);
		projectName.getColumn().setText("Project");
		projectName.getColumn().setWidth(150);
		
		projectName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Entry) {
					return ((Entry) element).getDisplayName();
				}
				return null;
			}
		});
		
		int projectColumnWidth = projectName.getColumn().getWidth();
		tableLayout.setColumnData(projectName.getColumn(), new ColumnWeightData(100, projectColumnWidth));
		
		xslTable.setContentProvider(new ArrayContentProvider());
		xslTable.setInput(projects);
		
		return parent;
	}

	public void setResults(List<Entry> projects) {
		this.projects = projects;
	}

}
