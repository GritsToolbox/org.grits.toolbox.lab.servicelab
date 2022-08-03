/**
 * 
 */
package org.grits.toolbox.lab.servicelab.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.lab.servicelab.dialog.provider.ProtocolTreeContentProvider;
import org.grits.toolbox.lab.servicelab.dialog.provider.ProtocolTreeLabelProvider;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class SelectParametersDialog extends Dialog
{
	private static final Logger logger = Logger.getLogger(SelectParametersDialog.class);

	private PriceInfoProtocol priceInfoProtocol = null;
	private List<Parameter> selectedParameters = null;

	private CheckboxTreeViewer checkboxTreeViewer = null;

	public SelectParametersDialog(Shell parentShell)
	{
		super(parentShell);
	}

	public void setServiceLabProtocol(PriceInfoProtocol priceInfoProtocol)
	{
		this.priceInfoProtocol = priceInfoProtocol;
	}

	@Override
	public void create()
	{
		super.create();
		initialize();
	}

	private void initialize()
	{
		selectedParameters = new ArrayList<>();
		// add all not-null parameters of its cost parameters as selected parameters
		for(CostParameter costParameter : priceInfoProtocol.getCostParameters())
		{
			if(costParameter.getAssociatedParameter() != null)
				selectedParameters.add(costParameter.getAssociatedParameter());
		}

		checkboxTreeViewer.setInput(priceInfoProtocol);
	}

	public List<Parameter> getSelectedParameters()
	{
		return selectedParameters ;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.info("creating parameter selection dialog");
		getShell().setText("Select Parameters");
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		createCheckboxTreeViewer(container);

		logger.info("dialog area created");
		return parent;
	}

	private void createCheckboxTreeViewer(Composite container)
	{
		logger.info("creating parameter checkbox tree");
		checkboxTreeViewer = new CheckboxTreeViewer(container,
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		Tree tree = checkboxTreeViewer.getTree();
		GridData treeLayouData = new GridData(GridData.FILL_BOTH);
		treeLayouData.horizontalSpan = 1;
		treeLayouData.verticalSpan = 1;
		treeLayouData.heightHint = 400;
		tree.setLayoutData(treeLayouData);

		TreeColumn column0 = new TreeColumn(tree, SWT.LEFT, 0);
		column0.setText("Selection");
		column0.setWidth(80);

		TreeViewerColumn columnViewer = new TreeViewerColumn(checkboxTreeViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Parameter Group / Parameter");
		columnViewer.getColumn().setWidth(300);

		tree.setHeaderVisible(true);

		checkboxTreeViewer.setContentProvider(new ProtocolTreeContentProvider());
		checkboxTreeViewer.setLabelProvider(new ProtocolTreeLabelProvider());
		checkboxTreeViewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isGrayed(Object element)
			{
				return element instanceof ParameterGroup;
			}

			@Override
			public boolean isChecked(Object element)
			{
				return element instanceof Parameter
						&& selectedParameters.contains(element);
			}
		});

		checkboxTreeViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				if(event.getElement() instanceof Parameter)
				{
					Parameter parameter = (Parameter) event.getElement();
					if(event.getChecked())
					{
						selectedParameters.add(parameter);
					}
					else
					{
						selectedParameters.remove(parameter);
					}
				}
			}
		});
	}
}
