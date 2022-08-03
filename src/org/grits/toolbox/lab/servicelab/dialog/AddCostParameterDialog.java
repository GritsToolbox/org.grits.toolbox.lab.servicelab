/**
 * 
 */
package org.grits.toolbox.lab.servicelab.dialog;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.grits.toolbox.editor.experimentdesigner.model.Parameter;
import org.grits.toolbox.editor.experimentdesigner.model.ParameterGroup;
import org.grits.toolbox.lab.servicelab.dialog.provider.ProtocolTreeContentProvider;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;

/**
 * 
 *
 */
public class AddCostParameterDialog extends Dialog
{
	private static final Logger logger = Logger.getLogger(AddCostParameterDialog.class);

	private PriceInfoProtocol priceInfoProtocol = null;
	private CostParameter costParameter = null;

	private Text costNameText = null;
	private Spinner industryPriceSpinner = null;
	private Spinner nonProfitPriceSpinner = null;
	private TreeViewer treeViewer = null;

	public AddCostParameterDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	public void create()
	{
		super.create();
		initialize();
	}

	public void setServiceLabProtocol(PriceInfoProtocol priceInfoProtocol)
	{
		this.priceInfoProtocol = priceInfoProtocol;
	}

	public CostParameter getCostParameter()
	{
		return costParameter;
	}

	private void initialize()
	{
		treeViewer.setInput(priceInfoProtocol);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.info("creating add cost parameter dialog");

		getShell().setText("Cost Parameter");
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);
		createLabelPart(container);
		createTreeViewerPart(container);

		logger.info("dialog area created");
		return parent;
	}

	private void createLabelPart(Composite container)
	{
		addLabel(container, "Name");
		costNameText = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 1;
		layoutData.minimumWidth = 300;
		costNameText.setLayoutData(layoutData);

		addLabel(container, "Industry Price ($)");
		industryPriceSpinner = new Spinner(container, SWT.BORDER);
		industryPriceSpinner.setDigits(2);
		industryPriceSpinner.setIncrement(10);
		industryPriceSpinner.setMaximum(Integer.MAX_VALUE);
		layoutData = new GridData();
		layoutData.horizontalSpan = 1;
		industryPriceSpinner.setLayoutData(layoutData);

		addLabel(container, "Non-Profit Price ($)");
		nonProfitPriceSpinner = new Spinner(container, SWT.BORDER);
		nonProfitPriceSpinner.setDigits(2);
		nonProfitPriceSpinner.setIncrement(10);
		nonProfitPriceSpinner.setMaximum(Integer.MAX_VALUE);
		layoutData = new GridData();
		layoutData.horizontalSpan = 1;
		nonProfitPriceSpinner.setLayoutData(layoutData);
	}

	private void addLabel(Composite container, String labelTitle)
	{
		logger.info("adding label : " + labelTitle);
		Label label = new Label(container, SWT.NONE);
		label.setText(labelTitle);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
	}

	private void createTreeViewerPart(Composite container)
	{
		logger.info("creating parameter selection tree");

		Button noParameterButton = new Button(container, SWT.RADIO);
		noParameterButton.setText("No Parameter");
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		noParameterButton.setLayoutData(layoutData);
		noParameterButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// clear any selection on tree
				treeViewer.setSelection(null);

				// disable the tree
				treeViewer.getTree().setEnabled(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});

		Button setParameterButton = new Button(container, SWT.RADIO);
		setParameterButton.setText("Set Parameter");
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		setParameterButton.setLayoutData(layoutData);
		setParameterButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// enable the tree for selection
				treeViewer.getTree().setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// TODO Auto-generated method stub
				
			}
		});

		treeViewer = new TreeViewer(container,
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Tree tree = treeViewer.getTree();
		GridData treeLayoutData = new GridData(GridData.FILL_BOTH);
		treeLayoutData.horizontalSpan = 2;
		treeLayoutData.verticalSpan = 1;
		treeLayoutData.heightHint = 300;
		tree.setLayoutData(treeLayoutData);

		TreeViewerColumn columnViewer = new TreeViewerColumn(treeViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Parameter Group / Parameter");
		columnViewer.getColumn().setWidth(400);

		tree.setHeaderVisible(true);

		treeViewer.setContentProvider(new ProtocolTreeContentProvider());
		treeViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if(element instanceof ParameterGroup)
					return ((ParameterGroup) element).getLabel();
				if(element instanceof Parameter)
					return ((Parameter) element).getName();
				return null;
			}
		});
	}

	@Override
	protected void okPressed()
	{
		costParameter = new CostParameter();
		costParameter.setCostParameterName(costNameText.getText().trim());
		costParameter.setIndustryPrice(getDoubleValue(industryPriceSpinner));
		costParameter.setNonProfitPrice(getDoubleValue(nonProfitPriceSpinner));
		if(!treeViewer.getSelection().isEmpty())
		{
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			if(selection.getFirstElement() instanceof Parameter)
			{
				Parameter parameter = (Parameter) selection.getFirstElement();
				costParameter.setAssociatedParameter(parameter);
				if(priceInfoProtocol.getParameterGroups() != null)
				{
					for(ParameterGroup parameterGroup : priceInfoProtocol.getParameterGroups())
					{
						if(parameterGroup.getId().equals(parameter.getGroupId()))
						{
							costParameter.setAssociatedParameterGroup(parameterGroup);
						}
					}
				}
			}
		}
		super.okPressed();
	}

	private double getDoubleValue(Spinner priceSpinner)
	{
		return priceSpinner.getSelection() /
				Math.pow(10, priceSpinner.getDigits());
	}
}
