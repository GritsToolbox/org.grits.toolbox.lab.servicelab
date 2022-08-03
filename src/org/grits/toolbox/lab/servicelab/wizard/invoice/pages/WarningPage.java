/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.invoice.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectDetailsHandler;
import org.grits.toolbox.core.datamodel.property.project.ProjectCollaborator;
import org.grits.toolbox.core.datamodel.property.project.ProjectDetails;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.property.ExperimentProperty;
import org.grits.toolbox.entry.sample.property.SampleProperty;
import org.grits.toolbox.lab.servicelab.invoice.InaccurateDoubleValue;
import org.grits.toolbox.lab.servicelab.invoice.ProtocolPriceCalculator;
import org.grits.toolbox.lab.servicelab.model.CostParameter;
import org.grits.toolbox.lab.servicelab.model.InvoiceComponent;
import org.grits.toolbox.lab.servicelab.model.InvoiceComponentComparator;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocolList;
import org.grits.toolbox.lab.servicelab.model.ServiceLabInvoice;
import org.grits.toolbox.lab.servicelab.preference.ServiceLabPreferenceStore.BillingPriceType;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.util.ProtocolManagerUtil;
import org.grits.toolbox.lab.servicelab.wizard.invoice.provider.MissingPriceProtocolContentProvider;
import org.grits.toolbox.lab.servicelab.wizard.invoice.provider.MissingPriceProtocolLabelProvider;

/**
 * 
 *
 */
public class WarningPage extends WizardPage
{
	private static final Logger logger = Logger.getLogger(WarningPage.class);
	private static final String PAGE_NAME = "Invoice Information";

	public WarningPage(Entry projectEntry, PriceInfoProtocolList priceInfoProtocolList)
	{
		super(PAGE_NAME);
		this.projectEntry = projectEntry;
		this.priceInfoProtocolList = priceInfoProtocolList;
	}

	private Entry projectEntry = null;
	private PriceInfoProtocolList priceInfoProtocolList = null;

	// any warning message added while loading different files and values
	private String warningMessages = "";
	// service lab invoice that is created from all available information
	private ServiceLabInvoice serviceLabInvoice = null;

	// a mapping of sample entry to list of all of its protocol nodes
	private HashMap<Entry, List<ProtocolNode>> sampleProtocolsMap = null;
	// a mapping of protocol node to service lab protocol containing price information
	private HashMap<ProtocolNode, PriceInfoProtocol> protocolServiceProtocolMap = null;
	// a mapping of sample entry to list of protocol nodes which have no price information
	private HashMap<Entry, List<ProtocolNode>> sampleMissingPriceProtocolsMap = null;

	private Button industryTypeButton = null;
	private Button nonProfitTypeButton = null;
	private Composite warningComposite = null;
	private Text warningText = null;
	private Composite missingPriceComposite = null;
	private TableViewer tableViewer = null;

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		// if navigating to this page initialize the values
		if(visible)
		{
			initialize();
		}
	}

	@Override
	public void setPageComplete(boolean complete)
	{
		// set the service lab invoice for the next preview page
		if(complete)
		{
			((InvoicePreviewPage) getNextPage()).setServiceLabInvoice(serviceLabInvoice);
		}

		super.setPageComplete(complete);
	}

	public void initialize()
	{
		logger.info("loading project : " + projectEntry.getDisplayName());

		// loading project information for adding billing info and 
		// loading experiments that were performed on samples in this project
		// and loading price information for those protocols

		serviceLabInvoice = new ServiceLabInvoice();
		warningMessages = "";
		try
		{
			ProjectDetails projectDetails = ProjectDetailsHandler.getProjectDetails(projectEntry);
			String billTo = getCollaboratorInfo(projectDetails.getCollaborators());
			serviceLabInvoice.setBillTo(billTo);
			if(billTo == null || billTo.isEmpty())
			{
				warningMessages += "Collaborator's billing information was not found for this project.\n";
			}

			serviceLabInvoice.setAdditionalNote(getAdditionalNote(projectDetails.getCollaborators()));
		} catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			warningMessages += "Project collaborator information could not be loaded.\n";
		}
		serviceLabInvoice.setInvoiceDate(new Date());

		// for due date add a month to the invoice date by default
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(serviceLabInvoice.getInvoiceDate());
		calendar.add(Calendar.MONTH, 1);
		serviceLabInvoice.setDueDate(new Date(calendar.getTime().getTime()));

		// load all protocols from experiments and create required maps
		initProtocolMaps();
		serviceLabInvoice.setInvoiceComponents(getInvoiceComponents());

		// service lab invoice part done till here
		// now adding warning texts that from warning messages
		// that were added while loading various information
		// and also setting list of protocols with missing price information etc.

		warningText.setText(warningMessages);

		// make warning message box visible or disappear
		boolean visible = !warningMessages.isEmpty();
		warningComposite.setVisible(visible);
		((GridData) warningComposite.getLayoutData()).exclude = !visible;

		tableViewer.setInput(sampleMissingPriceProtocolsMap);

		// make missing price table visible or disappear
		visible = !sampleMissingPriceProtocolsMap.isEmpty();
		missingPriceComposite.setVisible(visible);

		// re-pack the parent composite
		missingPriceComposite.getParent().pack();

		setPageComplete(true);
	}

	private TreeSet<InvoiceComponent> getInvoiceComponents()
	{
		TreeSet<InvoiceComponent> invoiceComponents = new TreeSet<InvoiceComponent>(
				new InvoiceComponentComparator());
		Map<String, InvoiceComponent> protocolUriLabelInvoiceComponentMap =
				new HashMap<String, InvoiceComponent>();
		String mapKey = null;
		InvoiceComponent invoiceComponent = null;
		InaccurateDoubleValue inaccuratePrice = null;
		PriceInfoProtocol priceInfoProtocol = null;
		int industryOrNonProfit = industryTypeButton.getSelection()
				? CostParameter.INDUSTRY_TYPE : CostParameter.NON_PROFIT_TYPE;
		for(Entry sampleEntry : sampleProtocolsMap.keySet())
		{
			for(ProtocolNode protocolNode : sampleProtocolsMap.get(sampleEntry))
			{
				priceInfoProtocol = protocolServiceProtocolMap.get(protocolNode);
				if(priceInfoProtocol == null) // no price information for calculating bill
				{
					logger.error("Missing price information for protocol : "
							+ protocolNode.getLabel().toUpperCase());
					if(sampleMissingPriceProtocolsMap.get(sampleEntry) == null
							|| !sampleMissingPriceProtocolsMap.get(sampleEntry).contains(protocolNode))
					{
						warningMessages += "Error retrieving price for protocol \"" + protocolNode.getLabel() +
								"\" in sample \"" + sampleEntry.getDisplayName() + "\"\n";
					}
					continue;
				}

				mapKey = protocolNode.getTemplateUri() == null
						? protocolNode.getLabel() : protocolNode.getTemplateUri();
				invoiceComponent = protocolUriLabelInvoiceComponentMap.get(mapKey);
				if(invoiceComponent == null)
				{
					// create a new invoice component for this protocol uri / label
					invoiceComponent = new InvoiceComponent();
					invoiceComponent.setProtocolName(priceInfoProtocol.getCommonName());

					// add this invoice component to the map for this protocol uri / label
					protocolUriLabelInvoiceComponentMap.put(mapKey, invoiceComponent);

					// add this invoice component as it is newly created
					invoiceComponents.add(invoiceComponent);
				}

				invoiceComponent.getSampleNames().add(sampleEntry.getDisplayName());
				invoiceComponent.setQuantity(invoiceComponent.getQuantity() + 1);

				// calculate pricing for this protocol
				inaccuratePrice = ProtocolPriceCalculator.calculatePriceWithInaccuracy(
						protocolNode, priceInfoProtocol, industryOrNonProfit);
				invoiceComponent.setAmount(invoiceComponent.getAmount() + inaccuratePrice.getValue());
				// check and add inaccuracy message for this sample
				if(inaccuratePrice.getInaccuracyMessage() != null)
				{
					if(invoiceComponent.getInaccuracyMessage() == null)
					{
						// no previous inaccuracy messages
						invoiceComponent.setInaccuracyMessage(sampleEntry.getDisplayName() +
								"\n" + inaccuratePrice.getInaccuracyMessage());
					}
					else // add to previous inaccuracy messages
					{
						invoiceComponent.setInaccuracyMessage(invoiceComponent.getInaccuracyMessage() +
								"\n" + sampleEntry.getDisplayName() +
								"\n" + inaccuratePrice.getInaccuracyMessage());
					}
				}

				invoiceComponent.setRate(ProtocolPriceCalculator
						.getConstantRate(priceInfoProtocol, industryOrNonProfit));
			}
		}
		return invoiceComponents ;
	}

	private void initProtocolMaps()
	{
		// a map containing template-uri / label of all the service lab protocols saved in file
		Map<String, PriceInfoProtocol> uriLabelServiceLabProtocolMap = getServiceLabProtocolMap();

		sampleProtocolsMap = new HashMap<Entry, List<ProtocolNode>>();
		protocolServiceProtocolMap = new HashMap<ProtocolNode, PriceInfoProtocol>();
		sampleMissingPriceProtocolsMap = new HashMap<Entry, List<ProtocolNode>>();

		List<ProtocolNode> protocolNodes = null;
		List<ProtocolNode> missingPriceProtocolNodes = null;
		int numOfExpDesignInSample = 0;
		int numOfProtocolInExpDesign = 0;

		// read all child entries of project
		for(Entry projectChildEntry : projectEntry.getChildren())
		{
			logger.info("project child entry : " + projectChildEntry.getDisplayName().toUpperCase());
			logger.info("type of entry : " + projectChildEntry.getProperty().getType());

			if(SampleProperty.TYPE.equals(projectChildEntry.getProperty().getType()))
			{
				protocolNodes = new ArrayList<ProtocolNode>();
				missingPriceProtocolNodes = new ArrayList<ProtocolNode>();
				PriceInfoProtocol priceInfoProtocol = null;
				numOfExpDesignInSample = 0;

				// read all child entries of sample
				for(Entry sampleChildEntry : projectChildEntry.getChildren())
				{
					logger.info("sample child entry : " + sampleChildEntry.getDisplayName().toUpperCase());
					logger.info("type of entry : " + sampleChildEntry.getProperty().getType());

					if(numOfExpDesignInSample < 1 &&
							ExperimentProperty.TYPE.equals(sampleChildEntry.getProperty().getType()))
					{
						try
						{
							numOfProtocolInExpDesign = 0;
							for(ProtocolNode protocolNode : ProtocolManagerUtil
									.getProtocolNodesForExperiment(sampleChildEntry))
							{
								logger.info("Protocol node : " + protocolNode.getLabel().toUpperCase());

								priceInfoProtocol = null;
								// protocols created from a template
								if(uriLabelServiceLabProtocolMap.containsKey(protocolNode.getTemplateUri()))
								{
									priceInfoProtocol = uriLabelServiceLabProtocolMap.get(protocolNode.getTemplateUri());
								}
								// check if protocol is one that has no template but was created by user 
								else if(uriLabelServiceLabProtocolMap.containsKey(protocolNode.getLabel()))
								{
									priceInfoProtocol = uriLabelServiceLabProtocolMap.get(protocolNode.getLabel());								
								}

								if(priceInfoProtocol == null)
								{
									missingPriceProtocolNodes.add(protocolNode);
								}
								else // no service protocol found for this protocol 
								{
									if(priceInfoProtocol.isKnownProtocol()
											|| !priceInfoProtocol.getCostParameters().isEmpty())
										protocolServiceProtocolMap.put(protocolNode, priceInfoProtocol);
									else
										missingPriceProtocolNodes.add(protocolNode);
								}

								// add this protocol to list of protocols for a sample
								protocolNodes.add(protocolNode);
								numOfProtocolInExpDesign++;
							}
						} catch (Exception e)
						{
							logger.error("Error loading protocols for : " + sampleChildEntry.getDisplayName());
							warningMessages += "Error loading protocols for experiment \""
									+ sampleChildEntry.getDisplayName() + "\" in sample \""
									+ projectChildEntry + "\"\n";
						}

						numOfExpDesignInSample++;

						// check for at least one protocol in an experiment design
						if(numOfProtocolInExpDesign == 0)
							warningMessages += "No Protocol found in Experiment Design \""
									+ sampleChildEntry.getDisplayName() + "\"\n";
					}
				}
				sampleProtocolsMap.put(projectChildEntry, protocolNodes);

				if(!missingPriceProtocolNodes.isEmpty())
					sampleMissingPriceProtocolsMap.put(projectChildEntry, missingPriceProtocolNodes);

				// no experiment design found for sample
				if(numOfExpDesignInSample == 0)
					warningMessages += "No Experiment Design found for sample \""
							+ projectChildEntry.getDisplayName() + "\"\n";
			}
		}
	}

	private Map<String, PriceInfoProtocol> getServiceLabProtocolMap()
	{
		// a map using template uri or label as the key and service lab protocols as its value
		Map<String, PriceInfoProtocol> uriLabelServiceLabProtocolMap =
				new HashMap<String, PriceInfoProtocol>();
		for(PriceInfoProtocol priceInfoProtocol : priceInfoProtocolList.getPriceInfoProtocols())
		{
			if(priceInfoProtocol.getTemplateUri() != null)
			{
				uriLabelServiceLabProtocolMap.put(priceInfoProtocol.getTemplateUri(), priceInfoProtocol);
			}
			else // protocols that are not based on any template
			{
				uriLabelServiceLabProtocolMap.put(priceInfoProtocol.getLabel(), priceInfoProtocol);
			}
		}
		return uriLabelServiceLabProtocolMap;
	}

	private String getCollaboratorInfo(List<ProjectCollaborator> collaborators)
	{
		String collabInfo = "";
		for(ProjectCollaborator collaborator : collaborators)
		{
			collabInfo += collabInfo.isEmpty() ? "" : "-----\n";
			collabInfo += getCollabInfo(collaborator);
		}
		return collabInfo;
	}

	private String getAdditionalNote(List<ProjectCollaborator> collaborators)
	{
		String additionalNote = "";
		for(ProjectCollaborator collaborator : collaborators)
		{ 
			additionalNote += getAdditionalNote(collaborator) + "\n";
		}
		return additionalNote.trim();
	}

	public static String getCollabInfo(ProjectCollaborator collaborator)
	{
		String collabInfo = "";
		if(collaborator != null)
		{
			collabInfo += collaborator.getDepartment() == null ? "" : collaborator.getDepartment() + "\n";
			collabInfo += collaborator.getInstitution() == null ? "" : collaborator.getInstitution() + "\n";
			collabInfo += collaborator.getAddress() == null ? "" : collaborator.getAddress() + "\n";
			collabInfo += collaborator.getCountry() == null ? "" : collaborator.getCountry() + "\n";
		}
		return collabInfo.trim();
	}

	public static String getAdditionalNote(ProjectCollaborator collaborator)
	{
		String additionalNote = "";
		if(collaborator != null)
		{
			additionalNote += collaborator.getName();
			additionalNote += collaborator.getEmail() == null
					|| collaborator.getEmail().isEmpty()
					? "" : "\t - " + collaborator.getEmail();
		}
		return additionalNote;
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.info("creating warning page for the wizard");

		setTitle(PAGE_NAME + " ( " + projectEntry.getDisplayName() +" )");
		setDescription("General information, error/warning messages etc.");

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);
		addUIParts(container);

		setControl(container);
		setPageComplete(false);

		logger.info("warning page created");
	}

	private void addUIParts(Composite container)
	{
		// a wrapper composite around radio buttons
		Composite radioComposite = new Composite(container, SWT.NONE);
		radioComposite.setLayout(new GridLayout(2, false));
		addLabel(radioComposite, "Pricing Type ", 2);
		industryTypeButton = getRadioButton(radioComposite, "Industry Price");
		industryTypeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				serviceLabInvoice.setInvoiceComponents(getInvoiceComponents());
			}
		});
		nonProfitTypeButton = getRadioButton(radioComposite, "Non-Profit Price");
		nonProfitTypeButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				serviceLabInvoice.setInvoiceComponents(getInvoiceComponents());
			}
		});

		GridData radioCompositeData = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1);
		radioCompositeData.heightHint = 70;
		radioComposite.setLayoutData(radioCompositeData);

		// select the default radio button
		switch(BillingPriceType.getDefaultType())
		{
			case INDUSTRY_PRICE:
				industryTypeButton.setSelection(true);
				break;
			case NON_PROFIT_PRICE:
				nonProfitTypeButton.setSelection(true);
				break;
			default:
				industryTypeButton.setSelection(true);
				break;
		}

		warningComposite = new Composite(container, SWT.NONE);
		warningComposite.setLayout(new GridLayout(4, false));
		addLabel(warningComposite, "Warnings ", 4);
		warningText = addMultiLineText(warningComposite);
		warningText.setEditable(false);
		warningComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2));

		missingPriceComposite = new Composite(container, SWT.NONE);
		missingPriceComposite.setLayout(new GridLayout(4, false));
		addLabel(missingPriceComposite, "Unknown Protocols / Missing Price Information", 4);

		createTreeViewerPart(missingPriceComposite);
		missingPriceComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2));
	}

	private Button getRadioButton(Composite container,
			String label)
	{
		Button button = new Button(container, SWT.RADIO);
		button.setText(label);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.verticalAlignment = SWT.TOP;
		layoutData.horizontalSpan = 1;
		layoutData.verticalSpan = 1;
		button.setLayoutData(layoutData);
		return button;
	}

	private Text addMultiLineText(Composite container)
	{
		Text text = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.verticalAlignment = SWT.TOP;
		layoutData.horizontalSpan = 3;
		layoutData.verticalSpan = 1;
		layoutData.heightHint = 100;
		text.setLayoutData(layoutData);
		return text;
	}

	private void addLabel(Composite container, String labelTitle, int horizontalSpan)
	{
		logger.info("adding label : " + labelTitle);
		Label label = new Label(container, SWT.NONE);
		label.setText(labelTitle);
		GridData gd = new GridData();
		gd.verticalAlignment = SWT.BEGINNING;
		gd.horizontalSpan = horizontalSpan;
		gd.verticalSpan = 1;
		label.setLayoutData(gd);
	}

	private void createTreeViewerPart(Composite container)
	{
		logger.info("creating parameter selection tree");

		tableViewer = new TableViewer(container,
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Table table = tableViewer.getTable();
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = 4;
		tableLayoutData.verticalSpan = 1;
		tableLayoutData.heightHint = 200;
		table.setLayoutData(tableLayoutData);

		TableViewerColumn columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Sample");
		columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Protocol");
		columnViewer = new TableViewerColumn(tableViewer, SWT.FILL, 2);
		columnViewer.getColumn().setText("Rate");

		table.setHeaderVisible(true);

		tableViewer.setContentProvider(new MissingPriceProtocolContentProvider());
		tableViewer.setLabelProvider(new MissingPriceProtocolLabelProvider());
		tableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(550, 2, 2, 1));
	}

	protected void okPressed()
	{
		logger.info("ok pressed");
	}
}
