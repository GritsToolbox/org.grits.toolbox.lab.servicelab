
package org.grits.toolbox.lab.servicelab.part.taskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore.Preference;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.ontology.ExperimentDesignOntologyAPI;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTask;
import org.grits.toolbox.lab.servicelab.model.ServiceLabTasklist;
import org.grits.toolbox.lab.servicelab.part.filemanager.action.sort.TableViewerStringComparator;
import org.grits.toolbox.lab.servicelab.part.filemanager.editsupport.CategoryEditSupport;
import org.grits.toolbox.lab.servicelab.part.pricemanager.action.sort.TableViewerDoubleComparator;
import org.grits.toolbox.lab.servicelab.part.taskmanager.action.AddProtocolAction;
import org.grits.toolbox.lab.servicelab.part.taskmanager.action.DeleteProtocolAction;
import org.grits.toolbox.lab.servicelab.part.taskmanager.editsupport.MinMaxEditSupport;
import org.grits.toolbox.lab.servicelab.part.taskmanager.editsupport.MinMaxEditSupport.ProtocolMinMaxSetter;
import org.grits.toolbox.lab.servicelab.part.taskmanager.provider.ProtocolNodeLabelProvider;
import org.grits.toolbox.lab.servicelab.part.taskmanager.provider.ServiceLabTaskContentProvider;
import org.grits.toolbox.lab.servicelab.part.taskmanager.provider.TasklistContentProvider;
import org.grits.toolbox.lab.servicelab.part.taskmanager.provider.TasklistLabelProvider;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.util.ProtocolManagerUtil;
import org.grits.toolbox.lab.servicelab.util.TableColumnSelectionListener;

public class TaskProtocolManager
{
	private static final Logger logger = Logger.getLogger(TaskProtocolManager.class);

	public static final String PART_ID =
			"org.grits.toolbox.lab.servicelab.partdescriptor.taskprotocolmanager";
	public static final String KEY_LEFT_SIDE_TABLE = "task_manager_left_side_table";

	@Inject private MDirtyable dirtyable;
	@Inject private @Named (IGritsDataModelService.WORKSPACE_ENTRY) Entry workspaceEntry;

	private File taskInfoFile = null;
	private ServiceLabTasklist serviceLabTasklist = null;
	private ExperimentDesignOntologyAPI expApi = null;

	private HashMap<String, MinInfoProtocol> protocolUriLabelNodeMap =
			new HashMap<String, MinInfoProtocol>();

	private TableViewer tasklistTableViewer = null;
	private TableViewer protocolTableViewer = null;

	@Inject
	public TaskProtocolManager(@Named(
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
		taskInfoFile = new File(serviceLabFolder, IConfig.TASK_PROTOCOL_INFO_FILE_NAME);

		// initialize experiment design api
		expApi  = new ExperimentDesignOntologyAPI();
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.info("Creating Task Protocol Manager Part");
		parent.setLayout(new FillLayout());

		ScrolledComposite scrolledComposite = new ScrolledComposite(
				parent, SWT.H_SCROLL | SWT.V_SCROLL| SWT.BORDER);
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
		logger.info("Task Protocol Manager Part created");
	}

	private void createLeftSideTable(Composite parent)
	{
		logger.info("Creating left side table");

		Section section = createSectionTable(parent, "Tasklist", 400);
		tasklistTableViewer = new TableViewer(section,
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		section.setClient(tasklistTableViewer.getTable());

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		TableViewerColumn columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.LEFT, 0);
		columnViewer.getColumn().setText("Task");

		columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.LEFT, 1);
		columnViewer.getColumn().setText("Min. Protocols");
		columnViewer.setEditingSupport(new MinMaxEditSupport(tasklistTableViewer, dirtyable,
				ProtocolMinMaxSetter.MINIMUM_PROTOCOL));

		columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.LEFT, 2);
		columnViewer.getColumn().setText("Max. Protocols");
		columnViewer.setEditingSupport(new MinMaxEditSupport(tasklistTableViewer, dirtyable,
				ProtocolMinMaxSetter.MAXIMUM_PROTOCOL));

		tasklistTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(400, 3, 1, 1));
		tasklistTableViewer.getTable().setHeaderVisible(true);
		tasklistTableViewer.getTable().setLinesVisible(true);

		tasklistTableViewer.setContentProvider(new TasklistContentProvider());
		tasklistTableViewer.setLabelProvider(new TasklistLabelProvider());

		// add comparator and column selection listeners
		tasklistTableViewer.setComparator(new TableViewerStringComparator());
		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(tasklistTableViewer);
		for (int i = 0; i < tasklistTableViewer.getTable().getColumnCount(); i++)
		{
			tasklistTableViewer.getTable().getColumn(i).addSelectionListener(selectionListener);
		}
		tasklistTableViewer.getTable().setSortColumn(tasklistTableViewer.getTable().getColumn(0));
		tasklistTableViewer.getTable().setSortDirection(SWT.UP);

		tasklistTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				protocolTableViewer.setInput(selection.getFirstElement());
			}
		});
	}

	private void createRightSideTable(Composite generalPartComposite)
	{
		logger.info("Creating right side table");

		Section section = createSectionTable(generalPartComposite, "Protocols", 200);
		protocolTableViewer  = new TableViewer(section,
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		section.setClient(protocolTableViewer.getTable());

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		final AddProtocolAction addProtocolAction =
				new AddProtocolAction(protocolTableViewer, protocolUriLabelNodeMap, dirtyable);
		final DeleteProtocolAction deleteProtocolAction =
				new DeleteProtocolAction(protocolTableViewer, dirtyable);
		toolBarManager.add(addProtocolAction);
		toolBarManager.add(deleteProtocolAction);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		TableViewerColumn columnViewer = new TableViewerColumn(protocolTableViewer, SWT.LEFT, 0);
		columnViewer.getColumn().setText("Protocols");
		columnViewer.setEditingSupport(new CategoryEditSupport(protocolTableViewer, dirtyable));

		protocolTableViewer.setContentProvider(new ServiceLabTaskContentProvider());
		protocolTableViewer.setLabelProvider(new ProtocolNodeLabelProvider());

		protocolTableViewer.setComparator(new TableViewerDoubleComparator());

		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(protocolTableViewer);
		columnViewer.getColumn().addSelectionListener(selectionListener);
		protocolTableViewer.getTable().setSortColumn(columnViewer.getColumn());
		protocolTableViewer.getTable().setSortDirection(SWT.UP);

		protocolTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 1));

		protocolTableViewer.getTable().setHeaderVisible(true);
		protocolTableViewer.getTable().setLinesVisible(true);
		protocolTableViewer.setData(KEY_LEFT_SIDE_TABLE, tasklistTableViewer);
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

	// progress bar related ui variables
	private Display display = null;
	private Shell shell = null;
	// progress bar for displaying progress
	private ProgressBar progressBar = null;
	// label displaying percentage of work done
	private Label progressLabel = null;
	// label displaying current type of work being done
	private Label nextTaskLabel = null;

	private void initializeValues()
	{
		logger.info("initializing task list table");

		display = Display.getCurrent();
		shell = new Shell(display, SWT.ON_TOP);
		shell.setText("Loading Task list");
		progressBar  = new ProgressBar(shell, SWT.SMOOTH);
		progressBar.setBounds(10, 10, 350, 20);
		shell.open();
		progressBar.setMaximum(100);

		progressLabel = new Label(shell, SWT.FILL);
		progressLabel.setText("0%");
		progressLabel.setAlignment(SWT.LEFT);
		progressLabel.setBounds(370, 10, 40, 20);

		nextTaskLabel = new Label(shell, SWT.NULL);
		nextTaskLabel.setAlignment(SWT.LEFT);
		nextTaskLabel.setText("Reading and Updating Task list from file ..");
		nextTaskLabel.setBounds(10, 40, 340, 30);

		logger.info("setting progress bar location and opening it");
		shell.setLocation(Display.getCurrent().getClientArea().width/4,
				Display.getCurrent().getClientArea().height/2);
		shell.pack();
		shell.open();

		Thread loadTaskAndProtocolThread = new Thread()
		{
			@Override
			public void run()
			{
				serviceLabTasklist = null;

				boolean needToSave =
						// load initial task list from the file
						// update this list from preference list
						loadUpdatedTasklistFromFile();
				// update progress bar as 33% with next task as updating list from workspace
				updateProgress(33, "Loading protocols from ontology and workspace ..");

				// load a map of protocol template uri, label to protocols in ontology/workspace
				loadProtocolList();
				// update progress bar as 66% with next task as updating protocols from ontology
				updateProgress(66, "Saving to file ..");

				// mark it dirty if anything has changed
				dirtyable.setDirty(needToSave);

				// save method checks if the editor is dirty or else does not save anything
				save();
				// update progress bar as 100% with done status
				updateProgress(100, "Done");
			}
		};

		logger.info("starting a thread for loading tasklist and protocol");
		loadTaskAndProtocolThread.start();

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
					loadTaskAndProtocolThread.join();
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
						// set input for refresh
						tasklistTableViewer.setInput(serviceLabTasklist);

						// select first element
						if(!serviceLabTasklist.getServiceLabTasks().isEmpty())
						{
							tasklistTableViewer.setSelection(
									new StructuredSelection(tasklistTableViewer.getElementAt(0)));
						}

						// close progress bar
						if(!shell.isDisposed())
							shell.dispose();
					}
				});
			}
		}).start();
	}

	protected void loadProtocolList()
	{
		logger.info("loading  protocol list from ontology and workspace");

		// clear the map containing the protocols
		protocolUriLabelNodeMap.clear();

		// load all protocols from ontology and add it to the map
		addToMap(ProtocolManagerUtil.getAllProtocolNodesFromOntology(expApi));

		// load all protocols from workspace (that are not in the ontology) and add it to the map
		addToMap(ProtocolManagerUtil.getAllProtocolNodesFromWorkspace(workspaceEntry));

		logger.info("protocol list loaded from ontology and workspace");
	}

	private void addToMap(List<ProtocolNode> allProtocolNodes)
	{
		String mapKey = null;
		for(ProtocolNode protocolNode : allProtocolNodes)
		{
			if(protocolNode.getTemplateUri() == null)
			{
				logger.info("No protocol template found for this protocol : "
						+ protocolNode.getLabel());
				mapKey = protocolNode.getLabel();
			}
			else
			{
				mapKey = protocolNode.getTemplateUri();
			}

			// for protocols not in the map create a new protocol
			if(!protocolUriLabelNodeMap.containsKey(mapKey))
			{
				protocolUriLabelNodeMap.put(mapKey, new MinInfoProtocol(protocolNode));
			}
		}
	}

	protected boolean loadUpdatedTasklistFromFile()
	{
		logger.info("load tasklist from the file and update it with current preference");
		boolean needToSave = false;

		if(taskInfoFile.exists())
		{
			try
			{
				// load tasklist from file
				FileInputStream inputStream = null;
				try
				{
					logger.info("Task Protocol manager : reading task list from file");
					inputStream = new FileInputStream(taskInfoFile);
					InputStreamReader reader = new InputStreamReader(inputStream,
							PropertyHandler.GRITS_CHARACTER_ENCODING);
					Unmarshaller unmarshaller = JAXBContext.newInstance(
							ServiceLabTasklist.class).createUnmarshaller();

					serviceLabTasklist = (ServiceLabTasklist) unmarshaller.unmarshal(reader);
				} finally
				{
					if(inputStream != null)
					{
						inputStream.close();
					}
				}
			} catch (FileNotFoundException e)
			{
				logger.error("Task Protocol Manager : protocol list file was not found.\n" + e.getMessage(), e);
				showError(new LoadingException("Missing Protocols File",
						"File containing task and protocol information was not found."));
			} catch (UnsupportedEncodingException e)
			{
				logger.error("Task Protocol Manager : ServiceLabTasklist object could not be read from xml.\n"
						+ e.getMessage(), e);
				showError(new LoadingException("Error Reading File",
						"File containing task and protocol information could not be loaded as it has"
								+ " an unsupported character encoding."));
			} catch (JAXBException e)
			{
				logger.error("Task Protocol Manager : The object could not be read from xml.\n" + e.getMessage(), e);
				showError(new LoadingException("Error Reading File",
						"File containing task and protocol information could not be parsed from xml."));
			} catch (IOException e)
			{
				logger.fatal("Task Protocol Manager : Something unexpected went wrong while loading protocols from file.\n"
						+ e.getMessage(), e);
				showError(new LoadingException("Error Reading File",
						"Task Protocol Manager : Something unexpected went wrong while loading protocols from file."));
			}
		}
		else // no file was found
		{
			logger.error("task list file does not exist");
		}

		if(serviceLabTasklist == null)
		{
			logger.info("creating empty tasklist for saving an empty list");
			serviceLabTasklist = new ServiceLabTasklist(new ArrayList<ServiceLabTask>());
			// for saving an empty list
			needToSave = true;
		}

		logger.info("loading task preference");
		// loading task prefernce from preference store
		SingleChoicePreference taskPreference =
				ProjectPreferenceStore.getSingleChoicePreference(Preference.TASK);
		Set<String> preferenceTasklist = taskPreference.getAllValues();

		logger.info("updating task list from current task preference");

		Map<String, ServiceLabTask> taskServiceLabTaskMap = new HashMap<String, ServiceLabTask>();
		for(ServiceLabTask serviceLabTask : serviceLabTasklist.getServiceLabTasks())
		{
			// if current preference has this task then keep it
			if(preferenceTasklist.contains(serviceLabTask.getTaskName()))
			{
				taskServiceLabTaskMap.put(serviceLabTask.getTaskName(), serviceLabTask);
			}
		}

		ServiceLabTask serviceLabTask = null;
		List<ServiceLabTask> updatedTasklist = new ArrayList<ServiceLabTask>();
		for(String preferenceTask : preferenceTasklist)
		{
			logger.debug("task : " + preferenceTask);
			serviceLabTask = taskServiceLabTaskMap.get(preferenceTask);
			if(serviceLabTask == null)
			{
				// new serviceLabTask was created for this new task
				logger.info("adding new task to the list : " + preferenceTask);
				serviceLabTask = new ServiceLabTask(preferenceTask);
				needToSave = true;
			}
			updatedTasklist.add(serviceLabTask);
		}

		// set the updated task list
		serviceLabTasklist.setServiceLabTasks(updatedTasklist);
		return needToSave;
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
				progressLabel.setText(currentProgress + "%");
				nextTaskLabel.setText(nextTask);
			}
		});
	}

	@PreDestroy
	public void preDestroy()
	{
		logger.info("Destroying Task Protocol Manager Part");
	}

	@Focus
	public void onFocus()
	{
		logger.info("Creating Task Protocol Manager Part");

		// set focus on the left side table
		tasklistTableViewer.getTable().setFocus();
	}

	@Persist
	public void save()
	{
		logger.info("Saving Task Protocol Manager Part");
		if(dirtyable.isDirty())
		{
			boolean changesSaved = false;
			try
			{
				Marshaller marshaller = JAXBContext.newInstance(ServiceLabTasklist.class).createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
				FileOutputStream fileOutputStream = null;
				try
				{
					fileOutputStream = new FileOutputStream(taskInfoFile);
					serviceLabTasklist.setLastModifiedDate(new Date());
					marshaller.marshal(serviceLabTasklist, fileOutputStream);
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