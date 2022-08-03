/**
 * 
 */
package org.grits.toolbox.lab.servicelab.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.lab.servicelab.dialog.provider.CheckboxProtocolLabelProvider;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.util.CheckboxTableViewerComparator;
import org.grits.toolbox.lab.servicelab.util.TableColumnSelectionListener;

/**
 * 
 *
 */
public class SelectProtocolsDialog extends Dialog
{
	private static final Logger logger = Logger.getLogger(SelectProtocolsDialog.class);

	private Map<String, MinInfoProtocol> labelUriProtocolMap =
			new HashMap<String, MinInfoProtocol>();
	private List<MinInfoProtocol> selectedNodes =
			new ArrayList<MinInfoProtocol>();

	private CheckboxTableViewer checkboxTableViewer = null;

	public SelectProtocolsDialog(Shell parentShell,
			Map<String, MinInfoProtocol> labelUriProtocolMap)
	{
		super(parentShell);
		this.labelUriProtocolMap = labelUriProtocolMap;
	}

	public void setSelectedNodes(List<MinInfoProtocol> selectedNodes)
	{
		this.selectedNodes = selectedNodes;
	}

	public List<MinInfoProtocol> getSelectedNodes()
	{
		return selectedNodes;
	}

	@Override
	public void create()
	{
		logger.info("creating dialog");
		super.create();
		loadValues();
	}

	private void loadValues()
	{
		logger.info("loading values for table from protocol map");

		checkboxTableViewer.setInput(
				new ArrayList<MinInfoProtocol>(labelUriProtocolMap.values()));
		for(MinInfoProtocol protocolNode : selectedNodes)
		{
			checkboxTableViewer.setChecked(
					labelUriProtocolMap.get(protocolNode.getUniqueKey()), true);
		}

		logger.info("protocol map loaded to table");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.info("creating protocol selection dialog");
		getShell().setText("Select Protocols");
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		createCheckboxTableViewer(container);

		logger.info("dialog area created");
		return parent;
	}

	private void createCheckboxTableViewer(Composite container)
	{
		logger.info("creating protocol checkbox table");

		checkboxTableViewer = CheckboxTableViewer.newCheckList(container,
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		Table table = checkboxTableViewer.getTable();
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 1;
		tableLayouData.verticalSpan = 1;
		tableLayouData.heightHint = 400;
		table.setLayoutData(tableLayouData);

		TableColumn column0 = new TableColumn(table, SWT.LEFT, 0);
		column0.setText(" ");
		column0.setWidth(30);

		TableViewerColumn columnViewer = new TableViewerColumn(checkboxTableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Protocol");
		columnViewer.getColumn().setWidth(300);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		checkboxTableViewer.setContentProvider(new GenericListContentProvider());
		checkboxTableViewer.setLabelProvider(new CheckboxProtocolLabelProvider());

		// set comparator for sorting in each column
		checkboxTableViewer.setComparator(new CheckboxTableViewerComparator());
		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(checkboxTableViewer);
		column0.addSelectionListener(selectionListener);
		columnViewer.getColumn().addSelectionListener(selectionListener);
		checkboxTableViewer.getTable().setSortColumn(columnViewer.getColumn());
		checkboxTableViewer.getTable().setSortDirection(SWT.UP);

		logger.info("protocol checkbox table created");
	}

	@Override
	protected void okPressed()
	{
		logger.info("Adding selected nodes");
		selectedNodes.clear();
		for(Object object : checkboxTableViewer.getCheckedElements())
		{
			if(object instanceof MinInfoProtocol)
			{
				selectedNodes.add(((MinInfoProtocol) object).getACopy());
			}
		}
		super.okPressed();
	}
}
