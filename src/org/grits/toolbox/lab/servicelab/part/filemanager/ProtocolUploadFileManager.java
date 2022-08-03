
package org.grits.toolbox.lab.servicelab.part.filemanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.ontology.ExperimentDesignOntologyAPI;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocolList;
import org.grits.toolbox.lab.servicelab.part.filemanager.action.AddFileUploadAction;
import org.grits.toolbox.lab.servicelab.part.filemanager.action.DeleteFileUploadAction;
import org.grits.toolbox.lab.servicelab.part.filemanager.action.sort.TableViewerStringComparator;
import org.grits.toolbox.lab.servicelab.part.filemanager.editsupport.CategoryEditSupport;
import org.grits.toolbox.lab.servicelab.part.filemanager.editsupport.NumberOfFilesEditSupport;
import org.grits.toolbox.lab.servicelab.part.filemanager.editsupport.SubCategoryEditSupport;
import org.grits.toolbox.lab.servicelab.part.filemanager.provider.FileUploadLabelProvider;
import org.grits.toolbox.lab.servicelab.part.filemanager.provider.ProtocolContentProvider;
import org.grits.toolbox.lab.servicelab.part.filemanager.provider.ProtocolListContentProvider;
import org.grits.toolbox.lab.servicelab.part.filemanager.provider.ProtocolListLabelProvider;
import org.grits.toolbox.lab.servicelab.part.pricemanager.action.sort.TableViewerDoubleComparator;
import org.grits.toolbox.lab.servicelab.part.taskmanager.TaskProtocolManager;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.util.ProtocolFileUploadUtil;
import org.grits.toolbox.lab.servicelab.util.ProtocolManagerUtil;
import org.grits.toolbox.lab.servicelab.util.TableColumnSelectionListener;

public class ProtocolUploadFileManager
{
	private static final Logger logger = Logger.getLogger(ProtocolUploadFileManager.class);

	public static final String PART_ID =
			"org.grits.toolbox.lab.servicelab.partdescriptor.protocolfilemanager";

	@Inject private MDirtyable dirtyable;
	@Inject private @Named (IGritsDataModelService.WORKSPACE_ENTRY) Entry workspaceEntry;

	private File protocolListFile = null;
	private FileInfoProtocolList protocolList = null;
	private ExperimentDesignOntologyAPI expApi = null;

	private TableViewer protocolListTableViewer = null;
	private TableViewer fileUploadTableViewer = null;

	@Inject
	public ProtocolUploadFileManager(@Named(
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
		protocolListFile = new File(serviceLabFolder, IConfig.PROTOCOL_FILE_UPLOAD_INFO_FILE_NAME);

		// initialize experiment design api
		expApi  = new ExperimentDesignOntologyAPI();
	}

	@PostConstruct
	public void postConstruct(Composite parent)
	{
		logger.info("Creating Protocol File Manager Part");

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
		logger.info("Protocol File Manager Part created");
	}

	private void createLeftSideTable(Composite parent)
	{
		logger.info("Creating left side table");

		Section section = createSectionTable(parent, "Protocols", 150);
		protocolListTableViewer = new TableViewer(section,
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		section.setClient(protocolListTableViewer.getTable());

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		TableViewerColumn columnViewer = new TableViewerColumn(protocolListTableViewer, SWT.LEFT, 0);
		columnViewer.getColumn().setText("Protocol");

		protocolListTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(150, 1));
		protocolListTableViewer.getTable().setHeaderVisible(true);
		protocolListTableViewer.getTable().setLinesVisible(true);

		protocolListTableViewer.setContentProvider(new ProtocolListContentProvider());
		protocolListTableViewer.setLabelProvider(new ProtocolListLabelProvider());

		// add comparator and column selection listeners
		protocolListTableViewer.setComparator(new TableViewerStringComparator());
		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(protocolListTableViewer);
		for (int i = 0; i < protocolListTableViewer.getTable().getColumnCount(); i++)
		{
			protocolListTableViewer.getTable().getColumn(i).addSelectionListener(selectionListener);
		}
		protocolListTableViewer.getTable().setSortColumn(columnViewer.getColumn());
		protocolListTableViewer.getTable().setSortDirection(SWT.UP);

		protocolListTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				fileUploadTableViewer.setInput(selection.getFirstElement());
			}
		});
	}

	private void createRightSideTable(Composite generalPartComposite)
	{
		logger.info("Creating right side table");

		Section section = createSectionTable(generalPartComposite, "File Uploads", 400);
		fileUploadTableViewer  = new TableViewer(section,
				SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL|SWT.SINGLE|SWT.FULL_SELECTION);
		section.setClient(fileUploadTableViewer.getTable());

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		final AddFileUploadAction addFileUploadAction =
				new AddFileUploadAction(fileUploadTableViewer, dirtyable);
		final DeleteFileUploadAction deleteFileUploadAction =
				new DeleteFileUploadAction(fileUploadTableViewer, dirtyable);
		toolBarManager.add(addFileUploadAction);
		toolBarManager.add(deleteFileUploadAction);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);

		TableViewerColumn columnViewer = new TableViewerColumn(fileUploadTableViewer, SWT.LEFT, 0);
		columnViewer.getColumn().setText("# Files");
		columnViewer.setEditingSupport(new NumberOfFilesEditSupport(
				fileUploadTableViewer, dirtyable));

		columnViewer = new TableViewerColumn(fileUploadTableViewer, SWT.LEFT, 1);
		columnViewer.getColumn().setText("Category");
		columnViewer.setEditingSupport(new CategoryEditSupport(fileUploadTableViewer, dirtyable));

		columnViewer = new TableViewerColumn(fileUploadTableViewer, SWT.LEFT, 2);
		columnViewer.getColumn().setText("Sub Category");
		columnViewer.setEditingSupport(new SubCategoryEditSupport(fileUploadTableViewer, dirtyable));

		fileUploadTableViewer.setContentProvider(new ProtocolContentProvider());
		fileUploadTableViewer.setLabelProvider(new FileUploadLabelProvider());

		fileUploadTableViewer.setComparator(new TableViewerDoubleComparator());
		TableColumnSelectionListener selectionListener =
				new TableColumnSelectionListener(fileUploadTableViewer);
		for (int i = 0; i < fileUploadTableViewer.getTable().getColumnCount(); i++)
		{
			fileUploadTableViewer.getTable().getColumn(i).addSelectionListener(selectionListener);
		}
		fileUploadTableViewer.getTable().setSortColumn(
				fileUploadTableViewer.getTable().getColumn(1));
		fileUploadTableViewer.getTable().setSortDirection(SWT.UP);

		fileUploadTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(400, 1, 3, 3));

		fileUploadTableViewer.getTable().setHeaderVisible(true);
		fileUploadTableViewer.getTable().setLinesVisible(true);
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
	private ProgressBar progressBar = null;
	private Label progressLabel = null;
	private Label nextTaskLabel = null;

	private void initializeValues()
	{
		logger.info("initializing protocol list table");

		display = Display.getCurrent();
		shell = new Shell(display, SWT.ON_TOP);
		shell.setText("Loading Protocols");
		progressBar  = new ProgressBar(shell, SWT.SMOOTH);
		progressBar.setBounds(10, 10, 300, 20);
		shell.open();
		progressBar.setMaximum(100);

		// label displaying percentage of work done
		progressLabel = new Label(shell, SWT.FILL);
		progressLabel.setText("0%");
		progressLabel.setAlignment(SWT.LEFT);
		progressLabel.setBounds(320, 10, 40, 20);

		// label displaying current type of work being done
		nextTaskLabel = new Label(shell, SWT.NULL);
		nextTaskLabel.setAlignment(SWT.LEFT);
		nextTaskLabel.setText("Reading Protocols From File ..");
		nextTaskLabel.setBounds(10, 40, 230, 30);

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
				protocolList = null;
				boolean needToSave = false;
				try
				{
					// load initial protocols from the file
					// adds empty list if not loaded properly
					needToSave = loadProtocolListFromFile();
				} catch (LoadingException loadingException)
				{
					logger.error(loadingException.getErrorTitle() + "\n" + loadingException.getErrorMessage());
					showError(loadingException);

					// initialize with empty value
					protocolList = new FileInfoProtocolList(new ArrayList<FileInfoProtocol>());
					protocolList.setLastModifiedDate(new Date());
					needToSave = true; 
				}

				// update progress bar as 33% with next task as updating list from workspace
				updateProgress(33, "Updating list from workspace ..");

				needToSave =
						// update the list with the current protocols and variants in workspace
						// replaces old protocols with new ones
						updateProtocolsList() || needToSave;
				// update progress bar as 66% with next task as saving protocols
				updateProgress(66, "Saving new protocols to file ..");

				// mark it dirty if anything has changed
				dirtyable.setDirty(needToSave);

				// save method checks if the editor is dirty or else does not save anything
				save();
				// update progress bar as 100% with done status
				updateProgress(100, "Done");
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
						// set input for refresh
						protocolListTableViewer.setInput(protocolList);

						// select first element
						if(!protocolList.getFileInfoProtocols().isEmpty())
						{
							protocolListTableViewer.setSelection(
									new StructuredSelection(protocolListTableViewer.getElementAt(0)));
						}

						// close progress bar
						if(!shell.isDisposed())
							shell.dispose();
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
				progressLabel.setText((currentProgress) + "%");
				nextTaskLabel.setText(nextTask);
			}
		});
	}

	private boolean updateProtocolsList()
	{
		logger.info("updating protocols list");

		Map<String, FileInfoProtocol> protocolUploadMap = new HashMap<String, FileInfoProtocol>();

		boolean someProtocolsRemoved = false;
		for(FileInfoProtocol fileInfoProtocol : protocolList.getFileInfoProtocols())
		{
			logger.debug("File protocol : " + fileInfoProtocol.getUniqueKey());
			if(protocolUploadMap.containsKey(fileInfoProtocol.getUniqueKey()))
			{
				logger.info("Protocol already exists with this key : " + fileInfoProtocol.getUniqueKey());
				someProtocolsRemoved = true;
			}
			else
				protocolUploadMap.put(fileInfoProtocol.getUniqueKey(), fileInfoProtocol);
		}

		boolean newProtocolsAdded = false;
		String mapKey = null;
		FileInfoProtocol fileInfoProtocol = null;
		FileInfoProtocol prevProtocolWithFileUpload = null;

		// for updating protocols from ontology if any of the version number has changed
		boolean needsUpdate = !expApi.getStandardOntologyVersion().equals(protocolList.getStandardOntologyVersion())
						|| !expApi.getLocalOntologyVersion().equals(protocolList.getLocalOntologyVersion());

		// replace previous protocols with new ones
		// and check for additional protocols from ontology 
		for(ProtocolNode protocolNode : ProtocolManagerUtil.getAllProtocolNodesFromOntology(expApi))
		{
			// all protocols in the ontology should have a template uri
			if(protocolNode.getTemplateUri() == null)
			{
				logger.error("Protocol does not have a template uri in ontology : " + protocolNode.getLabel());
				continue;
			}

			mapKey = protocolNode.getTemplateUri();

			if(protocolUploadMap.containsKey(mapKey))
			{
				prevProtocolWithFileUpload = protocolUploadMap.get(mapKey);
				if(needsUpdate)
				{
					fileInfoProtocol = new FileInfoProtocol(protocolNode);
					// set its previous file upload values after reloading
					for(FileUpload fileUpload : prevProtocolWithFileUpload.getFileUploads())
					{
						fileInfoProtocol.getFileUploads().add(fileUpload);
					}
				}
				else
				{
					fileInfoProtocol = prevProtocolWithFileUpload;
				}
			}
			else
			{
				fileInfoProtocol = new FileInfoProtocol(protocolNode);
				newProtocolsAdded = true;
			}
			protocolUploadMap.put(mapKey, fileInfoProtocol);
		}

		// get other protocols from workspace that do not follow any template
		// and are not in the list
		// i.e. protocol variants that are not in the ontology
		// but are used in experiment design
		for(ProtocolNode protocolNode : ProtocolManagerUtil.getAllProtocolNodesFromWorkspace(workspaceEntry))
		{
			logger.debug("Workspace protocol : " + protocolNode.getLabel());
			mapKey = protocolNode.getTemplateUri() == null ? protocolNode.getLabel()
					: protocolNode.getTemplateUri();
			if(!protocolUploadMap.containsKey(mapKey))
			{
				fileInfoProtocol = new FileInfoProtocol(protocolNode);
				protocolUploadMap.put(mapKey, fileInfoProtocol);
				newProtocolsAdded = true;
			}
		}

		// reset the list from the file
		protocolList.getFileInfoProtocols().clear();
		for(String uploadMapKey : protocolUploadMap.keySet())
		{
			// add the updated object of the map
			protocolList.getFileInfoProtocols().add(protocolUploadMap.get(uploadMapKey));
		}

		return someProtocolsRemoved || newProtocolsAdded;
	}

	private boolean loadProtocolListFromFile() throws LoadingException
	{
		boolean needToSave = false;
		if(protocolListFile.exists())
		{
			try
			{
				protocolList = ProtocolFileUploadUtil.getFileInfoProtocolList(protocolListFile);
			} catch (FileNotFoundException e)
			{
				logger.error("Upload File Manager : protocol list file was not found.\n" + e.getMessage(), e);
				throw new LoadingException("Missing Protocols File",
						"File containing list of protcols and upload information was not found.");
			} catch (UnsupportedEncodingException e)
			{
				logger.error("Upload File Manager : FileInfoProtocolList object could not be read from xml.\n"
						+ e.getMessage(), e);
				throw new LoadingException("Error Reading File",
						"File containing list of protcols and upload information could not be loaded as it has"
								+ " an unsupported character encoding.");
			} catch (JAXBException e)
			{
				logger.error("Upload File Manager : The object could not be read from xml.\n" + e.getMessage(), e);
				throw new LoadingException("Error Reading File",
						"File containing list of protcols and upload information could not be parsed from xml.");
			} catch (IOException e)
			{
				logger.fatal("Upload File Manager : Something unexpected went wrong while loading protocols from file.\n"
						+ e.getMessage(), e);
				throw new LoadingException("Error Reading File",
						"Upload File Manager : Something unexpected went wrong while loading protocols from file.");
			}
		}

		if(protocolList == null)
		{
			protocolList = new FileInfoProtocolList(new ArrayList<FileInfoProtocol>());
			protocolList.setLastModifiedDate(new Date());

			// for saving an empty list
			needToSave = true;
		}

		return needToSave;
	}

	@PreDestroy
	public void preDestroy()
	{
		logger.info("Destroying Protocol File Manager Part");
	}

	@Focus
	public void onFocus()
	{
		logger.info("Creating Protocol File Manager Part");
	}

	@Persist
	public void save()
	{
		logger.info("Saving Protocol File Manager Part");
		if(dirtyable.isDirty())
		{
			boolean changesSaved = false;
			try
			{
				Marshaller marshaller = JAXBContext.newInstance(
						FileInfoProtocolList.class).createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.setProperty(Marshaller.JAXB_ENCODING,
						PropertyHandler.GRITS_CHARACTER_ENCODING);
				FileOutputStream fileOutputStream = null;
				try
				{
					fileOutputStream = new FileOutputStream(protocolListFile);
					protocolList.setLastModifiedDate(new Date());
					marshaller.marshal(protocolList, fileOutputStream);
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

			// set it to false if changes were saved and vice-versa
			dirtyable.setDirty(!changesSaved);
		}
	}
}