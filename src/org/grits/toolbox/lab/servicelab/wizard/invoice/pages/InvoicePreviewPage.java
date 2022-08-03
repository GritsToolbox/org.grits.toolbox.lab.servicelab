/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.invoice.pages;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.InvoiceComponent;
import org.grits.toolbox.lab.servicelab.model.ServiceLabInvoice;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.wizard.invoice.InvoiceWizard;
import org.grits.toolbox.lab.servicelab.wizard.invoice.provider.InvoiceComponentContentProvider;

/**
 * 
 *
 */
public class InvoicePreviewPage extends WizardPage
{
	private static final Logger logger = Logger.getLogger(InvoicePreviewPage.class);
	private static final String PAGE_NAME = "Invoice";

	public InvoicePreviewPage()
	{
		super(PAGE_NAME);
	}

	private ServiceLabInvoice serviceLabInvoice = null;

	private Text billToText = null;
	private CDateTime invoiceDateTime = null;
	private Text invoiceNumberText = null;
	private CDateTime dueDateTime = null;
	private Text poNumberText = null;
	private Text totalText = null;
	private Text additionalNoteText = null;
	private CheckboxTableViewer tableViewer = null;

	// boolean for enabling/disabling auto check invoice components
	private boolean autoCheckEnabled = true;

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if(visible) // if navigating to this page refresh all values
		{
			initialize();
		}
	}

	@Override
	public void setPageComplete(boolean complete)
	{
		if(complete)
		{
			// set the edited invoice for download page
			((DownloadPage) getNextPage()).setServiceLabInvoice(serviceLabInvoice);
		}
		super.setPageComplete(complete);
	}

	public void setServiceLabInvoice(ServiceLabInvoice serviceLabInvoice)
	{
		this.serviceLabInvoice = serviceLabInvoice;
	}

	private void initialize()
	{
		String value = serviceLabInvoice.getBillTo() == null
				? "" : serviceLabInvoice.getBillTo();
		billToText.setText(value);
		invoiceDateTime.setSelection(serviceLabInvoice.getInvoiceDate());

		value = serviceLabInvoice.getInvoiceNumber() == null
				? "" : serviceLabInvoice.getInvoiceNumber();
		invoiceNumberText.setText(value);
		dueDateTime.setSelection(serviceLabInvoice.getDueDate());

		value = serviceLabInvoice.getPoNumber() == null
				? "" : serviceLabInvoice.getPoNumber();
		poNumberText.setText(value);

		value = serviceLabInvoice.getAdditionalNote() == null
				? "" : serviceLabInvoice.getAdditionalNote();
		additionalNoteText.setText(value);

		tableViewer.setInput(serviceLabInvoice);
		if(autoCheckEnabled)
		{
			serviceLabInvoice.makeDefaultSelections();
		}
		tableViewer.setCheckedElements(serviceLabInvoice.getSelectedInvoiceComponents().toArray());

		totalText.setText(serviceLabInvoice.getTotalSelectionPrice() + "");
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.info("Creating invoice page for the wizard");

		setTitle("Invoice Summary ( " + ((InvoiceWizard) getWizard()).getProjectName() +" )");
		setDescription("Review Invoice information before download");

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);
		addUIParts(container);

		setControl(container);
		setPageComplete(false);

		logger.info("invoice page created");
	}

	private void addUIParts(Composite container)
	{
		addLabel(container, "Bill To");
		billToText = addMultiLineText(container);
		billToText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				serviceLabInvoice.setBillTo(billToText.getText().trim());
			}
		});

		addLabel(container, "Invoice Date");
		invoiceDateTime = new CDateTime(container, CDT.BORDER | CDT.DROP_DOWN);
		invoiceDateTime.setPattern(IConfig.DATE_FORMAT);
		GridData calendarComboData = new GridData();
		calendarComboData.horizontalSpan = 1;
		calendarComboData.verticalSpan = 1;
		calendarComboData.widthHint = 200;
		invoiceDateTime.setLayoutData(calendarComboData);
		invoiceDateTime.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				serviceLabInvoice.setInvoiceDate(invoiceDateTime.getSelection());
			}
		});

		addLabel(container, "Invoice #");
		invoiceNumberText = addText(container, 2);
		invoiceNumberText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				serviceLabInvoice.setInvoiceNumber(invoiceNumberText.getText().trim());
			}
		});

		addLabel(container, "Due Date");
		dueDateTime = new CDateTime(container, CDT.BORDER | CDT.DROP_DOWN);
		dueDateTime.setPattern(IConfig.DATE_FORMAT);
		calendarComboData = new GridData();
		calendarComboData.horizontalSpan = 1;
		calendarComboData.verticalSpan = 1;
		calendarComboData.widthHint = 200;
		dueDateTime.setLayoutData(calendarComboData);
		dueDateTime.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				serviceLabInvoice.setDueDate(dueDateTime.getSelection());
			}
		});

		addLabel(container, "P.O. Number");
		poNumberText = addText(container, 2);
		poNumberText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				serviceLabInvoice.setPoNumber(poNumberText.getText().trim());
			}
		});

		addLabel(container, "Addn. Notes");
		additionalNoteText = addMultiLineText(container);
		additionalNoteText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				serviceLabInvoice.setAdditionalNote(additionalNoteText.getText().trim());
			}
		});

		createTreeViewerPart(container);

		addLabel(container, "");
		addLabel(container, "");

		addLabel(container, "Total");
		totalText = addText(container, 2);
		totalText.setEnabled(false);
	}

	private Text addMultiLineText(Composite container)
	{
		Text text = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 4;
		layoutData.minimumWidth = 500;
		layoutData.heightHint = 100;
		text.setLayoutData(layoutData);
		return text;
	}

	private Text addText(Composite container, int horizontalSpan)
	{
		Text text = new Text(container, SWT.BORDER);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = horizontalSpan;
		layoutData.minimumWidth = 200;
		text.setLayoutData(layoutData);
		return text;
	}

	private void addLabel(Composite container, String labelTitle)
	{
		logger.info("adding label : " + labelTitle);
		Label label = new Label(container, SWT.NONE);
		label.setText(labelTitle);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		gd.verticalAlignment = SWT.BEGINNING;
		label.setLayoutData(gd);
	}

	private void createTreeViewerPart(Composite container)
	{
		logger.info("creating parameter selection tree");

		tableViewer = CheckboxTableViewer.newCheckList(container,
				SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Table table = tableViewer.getTable();
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = 5;
		tableLayoutData.verticalSpan = 1;
		tableLayoutData.heightHint = 250;
		table.setLayoutData(tableLayoutData);

		TableViewerColumn columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Select");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return null;
			}
		});
		columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Description");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				InvoiceComponent invoiceComponent = (InvoiceComponent) element;
				String value = invoiceComponent.getProtocolName();
				if(value != null)
				{
					if(!invoiceComponent.getSampleNames().isEmpty())
					{
						value += " " + InvoiceComponentContentProvider.getCSVSampleNames(invoiceComponent);
					}
				}
				return value;
			}

			@Override
			public String getToolTipText(Object element)
			{
				return element instanceof InvoiceComponent ?
						((InvoiceComponent) element).getInaccuracyMessage() : null;
			}
		});
		columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 2);
		columnViewer.getColumn().setText("Qty");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof InvoiceComponent ?
						((InvoiceComponent) element).getQuantity() + "" : null;
			}
		});
		columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 3);
		columnViewer.getColumn().setText("Rate");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if(element instanceof InvoiceComponent)
				{
					double rate = ((InvoiceComponent) element).getRate();
					return rate > 0.0 ? rate + "" : "-";
				}
				return null;
			}
		});
		columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 4);
		columnViewer.getColumn().setText("Amount");
		final Image warningIcon = ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.WARNING_ICON).createImage();
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof InvoiceComponent ?
						((InvoiceComponent) element).getAmount() + "" : null;
			}
			@Override
			public Image getImage(Object element)
			{
				return element instanceof InvoiceComponent &&
						((InvoiceComponent) element).getInaccuracyMessage() != null
						? warningIcon : null;
			}
			@Override
			public String getToolTipText(Object element)
			{
				return element instanceof InvoiceComponent ?
						((InvoiceComponent) element).getInaccuracyMessage() : null;
			}
		});
		ColumnViewerToolTipSupport.enableFor(columnViewer.getViewer());

		table.setHeaderVisible(true);

		tableViewer.setContentProvider(new InvoiceComponentContentProvider());
		tableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(550, 1, 3, 1, 1, 1));
		tableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				if(event.getElement() instanceof InvoiceComponent)
				{
					if(event.getChecked())
					{
						if(!serviceLabInvoice.getSelectedInvoiceComponents().contains(event.getElement()))
						{
							serviceLabInvoice.getSelectedInvoiceComponents().add((InvoiceComponent) event.getElement());
						}
					}
					else
					{
						if(serviceLabInvoice.getSelectedInvoiceComponents().contains(event.getElement()))
						{
							serviceLabInvoice.getSelectedInvoiceComponents().remove(event.getElement());
						}
					}

					totalText.setText(serviceLabInvoice.getTotalSelectionPrice() + "");
					autoCheckEnabled = false;
				}
			}
		});
	}

	protected void okPressed()
	{
		logger.info("ok pressed");
	}
}
