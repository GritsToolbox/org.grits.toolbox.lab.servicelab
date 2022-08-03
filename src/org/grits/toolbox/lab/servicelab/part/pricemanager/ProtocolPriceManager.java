
package org.grits.toolbox.lab.servicelab.part.pricemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.ontology.ExperimentDesignOntologyAPI;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocolList;
import org.grits.toolbox.lab.servicelab.part.pricemanager.action.AddCostParameterAction;
import org.grits.toolbox.lab.servicelab.part.pricemanager.action.DeleteCostParameterAction;
import org.grits.toolbox.lab.servicelab.part.pricemanager.action.SelectCostParametersAction;
import org.grits.toolbox.lab.servicelab.part.pricemanager.action.sort.TableViewerDoubleComparator;
import org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport.CommonNameEditSupport;
import org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport.CostNameEditSupport;
import org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport.ParameterEditSupport;
import org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport.PriceEditSupport;
import org.grits.toolbox.lab.servicelab.part.pricemanager.editsupport.PriceEditSupport.PriceCategory;
import org.grits.toolbox.lab.servicelab.part.pricemanager.provider.CostParameterLabelProvider;
import org.grits.toolbox.lab.servicelab.part.pricemanager.provider.ProtocolContentProvider;
import org.grits.toolbox.lab.servicelab.part.pricemanager.provider.ProtocolListContentProvider;
import org.grits.toolbox.lab.servicelab.part.pricemanager.provider.ProtocolListLabelProvider;
import org.grits.toolbox.lab.servicelab.util.CheckboxTableViewerComparator;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.util.ProtocolManagerUtil;
import org.grits.toolbox.lab.servicelab.util.TableColumnSelectionListener;

public class ProtocolPriceManager
{
	private static final Logger logger = Logger.getLogger(ProtocolPriceManager.class);

	public static final String PART_ID = "org.grits.toolbox.lab.servicelab.partdescriptor.protocolpricemanager";

	@Inject private MDirtyable dirtyable;
	@Inject private @Named (IGritsDataModelService.WORKSPACE_ENTRY) Entry workspaceEntry;

	private File protocolListFile = null;
	private PriceInfoProtocolList priceInfoProtocolList = null;
	private ExperimentDesignOntologyAPI expApi = null;

	private CheckboxTableViewer protocolListTableViewer = null;
	private TableViewer protocolInfoTableViewer = null;

	@Inject
	public ProtocolPriceManager(@Named(
			IGritsConstants.WORKSPACE_LOCATION) String workspaceLocation) throws Exception
	{
		File serviceLabFolder = new File(workspaceLocation + File.separator
				+ IConfig.SERVICE_LAB_FOLDER_NAME);
		if(!serviceLabFolder.exists())
		{
			logger.info("creating \"" + IConfig.SERVICE_LAB_FOLDER_NAME +
					"\" folder in the workspace - " + serviceLabFolder.getAbsolutePath());
			serviceLabFolder.mkdir();
		}
		protocolListFile = new File(serviceLabFolder, IConfig.PROTOCOL_PRICE_INFO_FILE_NAME);

		// initialize experiment design api
		expApi  = new ExperimentDesignOntologyAPI();
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.info("Creating Protocol Manager Part");
		parent.setLayout(new FillLayout());

		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, 
				SWT.H_SCROLL | SWT.V_SCROLL| SWT.BORDER);
		scrolledComposite.setLayout(new FillLayout());

		Composite generalPartComposite  = new Composite(scrolledComposite, SWT.FILL);
		GridLayout generalPartLayout = new GridLayout();
		generalPartLayout.marginWidth = 20;
		generalPartLayout.marginHeight = 30;
		generalPartLayout.horizontalSpacing = 30;
		generalPartLayout.verticalSpacing = 10;
		generalPartLayout.numColumns = 2;
		generalPartLayout.makeColumnsEqualWidth = false;
		generalPartComposite.setLayout(generalPartLayout);

		generalPartComposite.setLayoutData(new GridData());
		createLeftSideTable(generalPartComposite);
		createRightSideTable(generalPartComposite);

		scrolledComposite.setContent(generalPartComposite);
		scrolledComposite.setMinSize(generalPartComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		initializeValues();

		logger.info("Protocol Manager Part created");
	}

	private void createLeftSideTable(Composite parent)
	{
		logger.info("Creating left side table");

		Section section = createSectionTable(parent, "Protocols", 150);
		protocolListTableViewer = CheckboxTableViewer.newCheckList(section,
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		section.setClient(protocolListTableViewer.getTable());

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		TableColumn column0=new TableColumn(protocolListTableViewer.getTable(), SWT.LEFT, 0);
		column0.setText("Known");

		TableViewerColumn columnViewer = new TableViewerColumn(protocolListTableViewer, SWT.LEFT, 1);
		columnViewer.getColumn().setText("Protocol");

		columnViewer = new TableViewerColumn(protocolListTableViewer, SWT.LEFT, 2);
		columnViewer.getColumn().setText("Common Name");
		columnViewer.setEditingSupport(new CommonNameEditSupport(protocolListTableViewer, dirtyable));

		protocolListTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(150, 1, 3, 3));
		protocolListTableViewer.getTable().setHeaderVisible(true);
		protocolListTableViewer.getTable().setLinesVisible(true);

		protocolListTableViewer.setContentProvider(new ProtocolListContentProvider());
		protocolListTableViewer.setLabelProvider(new ProtocolListLabelProvider());

		// add comparator and column selection listeners
		protocolListTableViewer.setComparator(new CheckboxTableViewerComparator());
		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(protocolListTableViewer);
		for (int i = 0; i < protocolListTableViewer.getTable().getColumnCount(); i++)
		{
			protocolListTableViewer.getTable().getColumn(i).addSelectionListener(selectionListener);
		}
		protocolListTableViewer.getTable().setSortColumn(protocolListTableViewer.getTable().getColumn(1));
		protocolListTableViewer.getTable().setSortDirection(SWT.UP);

		protocolListTableViewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isGrayed(Object element)
			{
				return false;
			}

			@Override
			public boolean isChecked(Object element)
			{
				return element instanceof PriceInfoProtocol
						&& ((PriceInfoProtocol) element).isKnownProtocol();
			}
		});

		// switch protocol's known status on checking/unchecking of the checkbox
		protocolListTableViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				if(event.getElement() instanceof PriceInfoProtocol)
				{
					((PriceInfoProtocol) event.getElement()
							).setKnownProtocol(event.getChecked());
					dirtyable.setDirty(true);
				}
			}
		});

		protocolListTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				StructuredSelection selection = (StructuredSelection) event.getSelection();
					protocolInfoTableViewer.setInput(selection.getFirstElement());
			}
		});
	}

	private Section createSectionTable(Composite parent, String tableTitle, int widthHint)
	{
		logger.info("creating table section for : " + tableTitle);
		Composite sectionParentComposite = new Composite(parent, SWT.FILL);
		sectionParentComposite.setLayout(new FillLayout());
		Section section = new Section(sectionParentComposite, Section.EXPANDED | Section.TITLE_BAR);
		section.setText(tableTitle);
		section.setTitleBarBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		section.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		GridData compositeLayoutData = new GridData(GridData.FILL_BOTH);
		compositeLayoutData.verticalSpan = 1;
		compositeLayoutData.horizontalSpan = 1;
		compositeLayoutData.widthHint = widthHint;
		compositeLayoutData.minimumHeight = 200;
		sectionParentComposite.setLayoutData(compositeLayoutData);
		return section;
	}

	private void createRightSideTable(Composite generalPartComposite)
	{
		logger.info("Creating right side table");

		Section section = createSectionTable(generalPartComposite, "Cost Parameters", 400);
		protocolInfoTableViewer = new TableViewer(section,
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		section.setClient(protocolInfoTableViewer.getTable());

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		final AddCostParameterAction addCostParameterAction =
				new AddCostParameterAction(protocolInfoTableViewer, dirtyable);
		final SelectCostParametersAction selectCostParametersAction =
				new SelectCostParametersAction(protocolInfoTableViewer, dirtyable);
		final DeleteCostParameterAction deleteCostParameterAction =
				new DeleteCostParameterAction(protocolInfoTableViewer, dirtyable);
		toolBarManager.add(addCostParameterAction);
		toolBarManager.add(selectCostParametersAction);
		toolBarManager.add(deleteCostParameterAction);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		TableViewerColumn columnViewer = new TableViewerColumn(protocolInfoTableViewer, SWT.LEFT, 0);
		columnViewer.getColumn().setText("Name");
		columnViewer.setEditingSupport(new CostNameEditSupport(protocolInfoTableViewer, dirtyable));

		columnViewer = new TableViewerColumn(protocolInfoTableViewer, SWT.LEFT, 1);
		columnViewer.getColumn().setText("Industry Price ($)");
		columnViewer.setEditingSupport(new PriceEditSupport(protocolInfoTableViewer,
				dirtyable, PriceCategory.INDUSTRY_PRICE));

		columnViewer = new TableViewerColumn(protocolInfoTableViewer, SWT.LEFT, 2);
		columnViewer.getColumn().setText("Non-Profit Price ($)");
		columnViewer.setEditingSupport(new PriceEditSupport(protocolInfoTableViewer,
				dirtyable, PriceCategory.NON_PROFIT_PRICE));

		columnViewer = new TableViewerColumn(protocolInfoTableViewer, SWT.LEFT, 3);
		columnViewer.getColumn().setText("Parameter");
		columnViewer.setEditingSupport(new ParameterEditSupport(protocolInfoTableViewer, dirtyable));

		protocolInfoTableViewer.setContentProvider(new ProtocolContentProvider());
		protocolInfoTableViewer.setLabelProvider(new CostParameterLabelProvider());

		protocolInfoTableViewer.setComparator(new TableViewerDoubleComparator());
		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(protocolInfoTableViewer);
		for (int i = 0; i < protocolInfoTableViewer.getTable().getColumnCount(); i++)
		{
			protocolInfoTableViewer.getTable().getColumn(i).addSelectionListener(selectionListener);
		}
		protocolInfoTableViewer.getTable().setSortColumn(
				protocolInfoTableViewer.getTable().getColumn(0));
		protocolInfoTableViewer.getTable().setSortDirection(SWT.UP);

		protocolInfoTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(400, 4, 3, 3, 4));

		protocolInfoTableViewer.getTable().setHeaderVisible(true);
		protocolInfoTableViewer.getTable().setLinesVisible(true);
	}

	// progress bar related ui variables
	private Display display = Display.getCurrent();
	private Shell shell = null;
	private ProgressBar progressBar = null;
	private Label label1 = null;
	private Label label2 = null;

	private void initializeValues()
	{
		logger.info("initializing protocol list table");

		display = Display.getCurrent();
		shell = new Shell(display, SWT.ON_TOP);
		shell.setText("Loading Protocols");
		progressBar  = new ProgressBar(shell, SWT.SMOOTH);
		progressBar.setBounds(10, 10, 300, 20);
		shell.open();
		progressBar.setMaximum(4);

		// label displaying percentage of work done
		label1 = new Label(shell, SWT.FILL);
		label1.setText("0%");
		label1.setAlignment(SWT.LEFT);
		label1.setBounds(320, 10, 40, 20);

		// label displaying current work being done
		label2 = new Label(shell, SWT.NULL);
		label2.setAlignment(SWT.LEFT);
		label2.setText("Reading Protocols From File ..");
		label2.setBounds(10, 40, 230, 30);

		logger.info("setting progress bar location and opening it");
		shell.setLocation(Display.getCurrent().getClientArea().width/4,
				Display.getCurrent().getClientArea().height/2);
		shell.pack();
		shell.open();

		Thread loadProtocolThread = new Thread()
		{
			@Override
			public void run()
			{
				priceInfoProtocolList = null;
				boolean needToSave = false;
				try
				{
						// load initial protocols from the file
						// adds empty list if not loaded properly
						needToSave = loadProtocolsFromFile();
				} catch (LoadingException loadingException)
				{
					logger.error(loadingException.getErrorTitle() + "\n" + loadingException.getErrorMessage());
					showError(loadingException);

					// initialize with empty value
					priceInfoProtocolList = new PriceInfoProtocolList();
					priceInfoProtocolList.setLastModifiedDate(new Date());
					needToSave = true; 
				}

				// update progress bar with current state and next task
				updateProgress(1, "Updating list from workspace ..");

				needToSave =
						// update the list with the current protocols and variants in workspace
						// only adds new protocols but does not remove non-existing protocols
						updateProtocolsList() || needToSave;
				// update progress bar with current state and next task
				updateProgress(2, "Updating protocols from ontology ..");

				needToSave =
						// also update protocol objects from ontology (only if ontology has changed)
						updateObjectsFromOntology() || needToSave;
				// update progress bar with current state and next task
				updateProgress(3, "Saving new protocols to file ..");

				// mark it dirty if anything has changed
				dirtyable.setDirty(needToSave);

				// save method checks if the editor is dirty or else does not save anything
				save();
				// update progress bar with current state and next task
				updateProgress(4, "Done");
			}
		};

		logger.info("starting a thread for loading protocol");
		loadProtocolThread.start();

		// start a new thread to wait for refreshing list in table once loaded
		logger.info("starting a thread for waiting and updating table");
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// wait for load protocol thread to end
				try
				{
					loadProtocolThread.join();
				}
				catch (InterruptedException e)
				{
					logger.error(e.getMessage(), e);
				}

				// refresh table
				display.syncExec(new Runnable()
				{
					@Override
					public void run()
					{
						// first close progress bar
						if(!shell.isDisposed())
							shell.dispose();

						protocolListTableViewer.setInput(priceInfoProtocolList);
					}
				});
			}
		}).start();
	}

	private void showError(LoadingException loadingException)
	{
		display.syncExec(new Runnable()
		{
			public void run()
			{
				// let the user know about loading error
				MessageDialog.openError(display.getActiveShell(),
						loadingException.getErrorTitle(), loadingException.getErrorMessage());
			}
		});
	}

	private void updateProgress(int currentProgress, String nextTask)
	{
		logger.info("update progress bar with " + currentProgress
				+ "% and set next task : " + nextTask);
		display.syncExec(new Runnable()
		{
			public void run()
			{
				if(progressBar.isDisposed())
					return;

				progressBar.setSelection(currentProgress);
				label1.setText((currentProgress*25) + "%");
				label2.setText(nextTask);
			}
		});
	}

	private boolean loadProtocolsFromFile() throws LoadingException
	{
		boolean needToSave = false;
		if(protocolListFile.exists())
		{
			try
			{
				priceInfoProtocolList = ProtocolManagerUtil.getPriceInfoProtocolList(protocolListFile);
			} catch (FileNotFoundException e)
			{
				logger.error("Protocol Price Manager : protocol list file was not found.\n" + e.getMessage(), e);
				throw new LoadingException("Missing Protocols File",
						"File containing list of protcols and price information was not found.");
			} catch (UnsupportedEncodingException e)
			{
				logger.error("Protocol Price Manager : PriceInfoProtocolList object could not be read from xml.\n"
						+ e.getMessage(), e);
				throw new LoadingException("Error Reading File",
						"File containing list of protcols and price information could not be loaded as it has"
								+ " an unsupported character encoding.");
			} catch (JAXBException e)
			{
				logger.error("Protocol Price Manager : The object could not be read from xml.\n" + e.getMessage(), e);
				throw new LoadingException("Error Reading File",
						"File containing list of protcols and price information could not be parsed from xml.");
			} catch (IOException e)
			{
				logger.fatal("Protocol Price Manager : Something unexpected went wrong while loading protocols from file.\n"
						+ e.getMessage(), e);
				throw new LoadingException("Error Reading File",
						"Protocol Price Manager : Something unexpected went wrong while loading protocols from file.");
			}
		}

		if(priceInfoProtocolList == null)
		{
			priceInfoProtocolList = new PriceInfoProtocolList();
			priceInfoProtocolList.setLastModifiedDate(new Date());

			// for saving an empty list
			needToSave = true;
		}

		return needToSave;
	}

	private boolean updateProtocolsList()
	{
		logger.info("updating protocols list");

		// make a set of protocol template uris from the current file for filtering
		Set<String> protocolUris = new HashSet<String>();
		// make a set of protocol labels from current file for filtering
		Set<String> protocolLabels = new HashSet<String>();

		boolean knownStatusChanged = false;
		for(PriceInfoProtocol priceInfoProtocol : priceInfoProtocolList.getPriceInfoProtocols())
		{
			if(priceInfoProtocol.getTemplateUri() != null)
			{
				protocolUris.add(priceInfoProtocol.getTemplateUri());
			}
			protocolLabels.add(priceInfoProtocol.getLabel());

			// make protocol with non-empty cost parameter as known protocol
			if(!priceInfoProtocol.isKnownProtocol() &&
					!priceInfoProtocol.getCostParameters().isEmpty())
			{
				priceInfoProtocol.setKnownProtocol(true);
				knownStatusChanged = true;
			}
		}

		// check for additional protocols from ontology
		PriceInfoProtocol priceInfoProtocol = null;
		boolean newProtocolsAdded = false;
		for(ProtocolNode protocolNode : ProtocolManagerUtil.getAllProtocolNodesFromOntology(expApi))
		{
			if(!protocolUris.contains(protocolNode.getTemplateUri()))
			{
				priceInfoProtocol = new PriceInfoProtocol(protocolNode);
				priceInfoProtocolList.getPriceInfoProtocols().add(priceInfoProtocol);
				protocolUris.add(protocolNode.getTemplateUri());
				protocolLabels.add(protocolNode.getLabel());
				newProtocolsAdded = true;
			}
		}

		// get other protocols from workspace that do not follow any template
		// i.e. protocol variants that are not in the ontology
		// but are used in experiment design
		for(ProtocolNode protocolNode : ProtocolManagerUtil.getAllProtocolNodesFromWorkspace(workspaceEntry))
		{
			logger.debug(protocolNode.getLabel());
			if((protocolNode.getTemplateUri() == null
					&& !protocolLabels.contains(protocolNode.getLabel())
					// also add protocols from old templates that are no longer in the ontology
					|| (protocolNode.getTemplateUri() != null
					&& !protocolUris.contains(protocolNode.getTemplateUri()))))
			{
				logger.debug(protocolNode.getLabel());
				priceInfoProtocol = new PriceInfoProtocol(protocolNode);
				priceInfoProtocolList.getPriceInfoProtocols().add(priceInfoProtocol);
				protocolLabels.add(protocolNode.getLabel());
				if(protocolNode.getTemplateUri() != null)
					protocolUris.add(protocolNode.getTemplateUri());
				newProtocolsAdded = true;
			}
		}

		return knownStatusChanged || newProtocolsAdded;
	}

	private boolean updateObjectsFromOntology()
	{
		logger.info("updating protocols from ontology");

		// check if ontology has changed
		// update when at least one of the version number is different
		if(!expApi.getStandardOntologyVersion().equals(
				priceInfoProtocolList.getStandardOntologyVersion())
				|| !expApi.getLocalOntologyVersion().equals(
						priceInfoProtocolList.getLocalOntologyVersion()))
		{
			logger.info("standard ontology version in owl file : " + expApi.getStandardOntologyVersion());
			logger.info("standard ontology version in protocols file : " + priceInfoProtocolList.getStandardOntologyVersion());
			logger.info("local ontology version in owl file : " + expApi.getLocalOntologyVersion());
			logger.info("local ontology version in protocols file : " + priceInfoProtocolList.getLocalOntologyVersion());

			List<PriceInfoProtocol> updatedProtocols = new ArrayList<PriceInfoProtocol>();
			ProtocolNode protocolNode = null;
			PriceInfoProtocol priceInfoProtocol = null;
			for(PriceInfoProtocol protocol : priceInfoProtocolList.getPriceInfoProtocols())
			{
				if(protocol.getTemplateUri() != null)
				{
					protocolNode = expApi.getProtocolByUri(protocol.getTemplateUri());
					if(protocolNode == null)
					{
						logger.error("protocol not found : " + protocol.getTemplateUri());
						continue;
					}

					// create a new service lab protocol from this protocol node
					logger.info("updating protocol from ontology : " + protocol.getUri());
					priceInfoProtocol = new PriceInfoProtocol(protocolNode);
					// set its previous set values after reloading
					priceInfoProtocol.setCommonName(protocol.getCommonName());
					priceInfoProtocol.setCostParameters(protocol.getCostParameters());
					priceInfoProtocol.setKnownProtocol(protocol.isKnownProtocol());
					updatedProtocols.add(priceInfoProtocol);
				}
				else
				{
					// a protocol variant with no protocol template
					logger.info("no template uri for this protocol node : " + protocol.getLabel());
					updatedProtocols.add(protocol);
				}
			}

			priceInfoProtocolList.setPriceInfoProtocols(updatedProtocols);
			priceInfoProtocolList.setStandardOntologyVersion(expApi.getStandardOntologyVersion());
			priceInfoProtocolList.setLocalOntologyVersion(expApi.getLocalOntologyVersion());
			priceInfoProtocolList.setLastModifiedDate(new Date());
			return true;
		}

		return false;
	}

	@PreDestroy
	public void preDestroy()
	{
		logger.info("destroying protocol manager part");
	}

	@Focus
	public void onFocus()
	{

	}

	@Persist
	public void save()
	{
		logger.info("saving protocol manager part");
		if(dirtyable.isDirty())
		{
			boolean changesSaved = false;
			try
			{
				Marshaller marshaller = JAXBContext.newInstance(PriceInfoProtocolList.class).createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
				FileOutputStream fileOutputStream = null;
				try
				{
					fileOutputStream = new FileOutputStream(protocolListFile);
					priceInfoProtocolList.setLastModifiedDate(new Date());
					marshaller.marshal(priceInfoProtocolList, fileOutputStream);
					changesSaved = true;
				} catch (IOException e)
				{
					logger.error("The changes made could not be written to the file.\n" + e.getMessage(), e);
				} finally
				{
					if(fileOutputStream != null)
					{
						fileOutputStream.close();
					}
				}
			} catch (JAXBException e)
			{
				logger.error("The changes made could not be serialized as xml.\n" + e.getMessage(), e);
			} catch (Exception e)
			{
				logger.fatal(e.getMessage(), e);
			}

			// set it to false only if changes were saved
			dirtyable.setDirty(!changesSaved);
		}
	}
}