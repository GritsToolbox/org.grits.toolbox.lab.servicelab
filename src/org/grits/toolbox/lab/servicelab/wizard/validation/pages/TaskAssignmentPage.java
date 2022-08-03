/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.pages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.lab.servicelab.config.ImageRegistry;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;
import org.grits.toolbox.lab.servicelab.model.validation.TaskAssignment;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.validation.ProjectTaskAssigner;
import org.grits.toolbox.lab.servicelab.wizard.validation.ValidationWizard;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.TaskAssignmentLabelProvider;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.TaskSampleSetContentProvider;

/**
 * 
 *
 */
public class TaskAssignmentPage extends WizardPage
{
	private static final Logger logger = Logger.getLogger(TaskAssignmentPage.class);
	private static final String PAGE_NAME = "Task Assignment";

	private ProjectTaskAssigner projectTaskAssigner = null;
	private List<TaskAssignment> projectTaskAssignments = new ArrayList<TaskAssignment>();

	// for storing error messages like empty task or samples 
	private Set<String> errorMessages = new HashSet<String>();

	private TableViewer tasklistTableViewer = null;
	private TableViewer sampleSelectionTableViewer = null;
	private TableViewer sampleOptionTableViewer = null;
	private Button downloadErrorButton = null;

	public TaskAssignmentPage(Entry projectEntry, File taskInfoFile)
	{
		super(PAGE_NAME);
		setTitle(PAGE_NAME);
		projectTaskAssigner = new ProjectTaskAssigner(projectEntry, taskInfoFile);
	}

	private void initialize()
	{
		List<Entry> sampleExpEntries = projectTaskAssigner.getSampleExpEntries();
		errorMessages = projectTaskAssigner.getErrorMessages();
		projectTaskAssignments = projectTaskAssigner.loadTaskAssignments();

		tasklistTableViewer.setInput(projectTaskAssignments);
		StructuredSelection selection = projectTaskAssignments.isEmpty() ?
				null : new StructuredSelection(projectTaskAssignments.iterator().next());
		tasklistTableViewer.setSelection(selection);

		sampleOptionTableViewer.setInput(sampleExpEntries);
		refreshErrorStatus();
	}

	@Override
	public IWizardPage getNextPage()
	{
		if(isPageComplete())
		{
			projectTaskAssigner.saveTaskAssignments();
		}
		return super.getNextPage();
	}

	public Map<Entry, SampleExpValidation> getEntryToSampleValidationMap()
	{
		return isPageComplete() ? projectTaskAssigner.generateSampleExpValidations() : null;
	}

	protected void refreshErrorStatus()
	{
		String errorMessage = null;
		for(TaskAssignment taskAssignment : projectTaskAssignments)
		{
			errorMessage = taskAssignment.getErrorMessage();
			if(errorMessage != null)
			{
				// add header for this task to error message and break
				errorMessage = taskAssignment.getTaskHeader()
						+ " - " + errorMessage;
				break;
			}
		}

		// check if other errors exist
		if(errorMessage == null && !errorMessages.isEmpty())
		{
			errorMessage = errorMessages.iterator().next();
			if(errorMessages.size() > 1)
				errorMessage += " ...and " + (errorMessages.size() - 1) + " more error";
			if(errorMessages.size() > 2)
				errorMessage += "s";
		}

		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
		downloadErrorButton.setEnabled(errorMessage != null);
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.info("creating task assignment page for the wizard");

		Composite container = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// add all the ui components
		addUIParts(container);

		setControl(container);
		setPageComplete(false);

		initialize();
		logger.info("task assignment page created");
	}

	private void addUIParts(Composite container)
	{
		addLabel(container, ((ValidationWizard) getWizard()).getProjectName());

		// add a table for task
		addTaskTablePart(container);

		// a table for selected samples for a task
		addSamplePart(container);

		// a button to remove/add samples from/to a task
		addButtonsPart(container);

		// a table displaying all available samples that can be added
		addSampleOptionPart(container);

		// an error download part
		addDownloadErrorButton(container);
	}

	private void addTaskTablePart(Composite container)
	{
		// this part displays all the tasks that were assigned in a project
		logger.info("creating task status table");

		tasklistTableViewer = createTableViewer(container, SWT.SINGLE| SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 320, 375, 1, 3);

		TableViewerColumn columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Task");
		columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Person");
		columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.FILL, 2);
		columnViewer.getColumn().setText("# Tasks");
		columnViewer = new TableViewerColumn(tasklistTableViewer, SWT.FILL, 3);
		columnViewer.getColumn().setText("Status");

		tasklistTableViewer.getTable().setHeaderVisible(true);

		tasklistTableViewer.setContentProvider(new GenericListContentProvider());
		tasklistTableViewer.setLabelProvider(new TaskAssignmentLabelProvider());
		tasklistTableViewer.getTable().addControlListener(new MaintainTableColumnRatioListener(260, 7, 4, 3, 3));

		tasklistTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(!tasklistTableViewer.getSelection().isEmpty())
				{
					// set this task as input to table displaying selected samples
					Object selection = ((StructuredSelection)
							tasklistTableViewer.getSelection()).getFirstElement();
					if(selection instanceof TaskAssignment)
					{
						sampleSelectionTableViewer.setInput(selection);
					}
				}
			}
		});
	}

	private void addSamplePart(Composite container)
	{
		// this part displays a table with all the samples added to a task
		logger.info("creating sample selection table");

		sampleSelectionTableViewer = createTableViewer(container, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 200, 145, 2, 1);

		TableViewerColumn columnViewer = new TableViewerColumn(
				sampleSelectionTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Selected Samples");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof Entry ?
						((Entry) element).getDisplayName() : null;
			}
		});
		columnViewer = new TableViewerColumn(
				sampleSelectionTableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Count");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if(element instanceof Entry &&
						sampleSelectionTableViewer.getInput() instanceof TaskAssignment)
				{
					return Collections.frequency(
							((TaskAssignment) sampleSelectionTableViewer.getInput()
									).getSampleExpEntries(), (Entry) element) + "";
				}
				return "-";
			}
		});

		sampleSelectionTableViewer.getTable().setHeaderVisible(true);

		sampleSelectionTableViewer.setContentProvider(new TaskSampleSetContentProvider());
		sampleSelectionTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 3, 1));
	}

	private void addSampleOptionPart(Composite container)
	{
		// this part displays a table with all the samples available in a project
		logger.info("creating sample option table");

		sampleOptionTableViewer = createTableViewer(container, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 200, 145, 2, 1);

		TableViewerColumn columnViewer = new TableViewerColumn(sampleOptionTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("All Samples");

		sampleOptionTableViewer.getTable().setHeaderVisible(true);

		sampleOptionTableViewer.setContentProvider(new GenericListContentProvider());
		sampleOptionTableViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof Entry ?
						((Entry) element).getDisplayName() : null;
			}
		});

		sampleOptionTableViewer.addFilter(new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				return element instanceof Entry;
			}
		});
		sampleOptionTableViewer.getTable().addControlListener(new MaintainTableColumnRatioListener(200, 1));
	}

	private void addButtonsPart(Composite container)
	{
		// this part shows a button for removing or adding samples for a task
		Button addButton = createButton(container, "Add");
		addButton.setImage(ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.UP_ARROW).createImage());
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(!sampleOptionTableViewer.getSelection().isEmpty())
				{
					TaskAssignment taskAssignment = (TaskAssignment) ((StructuredSelection)
							tasklistTableViewer.getSelection()).getFirstElement();
					@SuppressWarnings("unchecked")
					Iterator<Entry> sampleSelectionIterator = ((StructuredSelection)
							sampleOptionTableViewer.getSelection()).iterator();
					Entry selectedSample = null;
					while(sampleSelectionIterator.hasNext())
					{
						selectedSample = (Entry) sampleSelectionIterator.next();
						taskAssignment.getSampleExpEntries().add(selectedSample);
					}

					sampleSelectionTableViewer.refresh();
					sampleOptionTableViewer.refresh();
				}

				//  reset task status in table
				tasklistTableViewer.refresh();
				refreshErrorStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});

		Button removeButton = createButton(container, "Remove");
		removeButton.setImage(ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.DOWN_ARROW).createImage());
		removeButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(!sampleSelectionTableViewer.getSelection().isEmpty())
				{
					TaskAssignment taskAssignment = (TaskAssignment) ((StructuredSelection)
							tasklistTableViewer.getSelection()).getFirstElement();
					@SuppressWarnings("unchecked")
					Iterator<Entry> sampleSelectionIterator = ((StructuredSelection)
							sampleSelectionTableViewer.getSelection()).iterator();
					Entry selectedSample = null;
					while(sampleSelectionIterator.hasNext())
					{
						selectedSample = (Entry) sampleSelectionIterator.next();
						taskAssignment.getSampleExpEntries().remove(selectedSample);
					}

					// no need to clear the selection to keep this button reusable
					// refresh the table for updating count
					sampleSelectionTableViewer.refresh();
				}

				//  reset task status in table
				tasklistTableViewer.refresh();
				refreshErrorStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
	}

	private Button createButton(Composite container, String buttonText)
	{
		Button button = new Button(container, SWT.PUSH);
		button.setText(buttonText);
		GridData gd = new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 1, 1);
		gd.widthHint = 100;
		button.setLayoutData(gd);
		return button;
	}

	private void addDownloadErrorButton(Composite container)
	{
		logger.info("creating download error button");
		// this part shows a button for removing or adding samples for a task
		downloadErrorButton = new Button(container, SWT.PUSH);
		downloadErrorButton.setText("Download Errors");
		downloadErrorButton.setImage(ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.ERROR_ICON).createImage());
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.END;
		gd.verticalAlignment = SWT.BEGINNING;
		downloadErrorButton.setLayoutData(gd);
		final String initialFileName = ((ValidationWizard) getWizard()).getProjectName()
				+" Task Errors";
		downloadErrorButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fileDialog = new FileDialog(container.getShell(), SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{".txt"});
				fileDialog.setText("Download Errors");
				fileDialog.setOverwrite(true);
				fileDialog.setFileName(initialFileName);
				String fileLocation = null;
				if((fileLocation = fileDialog.open()) != null)
				{
					downloadErrors(fileLocation);
					Program.launch(fileLocation);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
	}

	private void downloadErrors(String fileLocation)
	{
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(fileLocation);

			fileWriter.append("*****Errors Assigning Tasks*****").append(System.lineSeparator())
			.append("PROJECT - ").append(((ValidationWizard) getWizard()).getProjectName())
			.append(System.lineSeparator()).append("" + new Date())
			.append(System.lineSeparator()).append("--------------------------------")
			.append(System.lineSeparator()).append(System.lineSeparator());

			String errorMessage = null;
			for(TaskAssignment taskAssignment : projectTaskAssignments)
			{
				errorMessage = taskAssignment.getErrorMessage();
				if(errorMessage != null)
				{
					// add error message for this task
					fileWriter.append("=> ")
					.append(taskAssignment.getTaskHeader()).append(System.lineSeparator())
					.append(errorMessage).append(System.lineSeparator());
				}
			}

			for(String errorMess : errorMessages)
			{
				fileWriter.append("--").append(errorMess)
				.append(System.lineSeparator()).append(System.lineSeparator());
			}
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
		}
		finally
		{
			if(fileWriter != null)
			{
				try
				{
					fileWriter.close();
				} catch (IOException ex)
				{
					logger.fatal(ex.getMessage(), ex);
				}
			}
		}
	}

	private TableViewer createTableViewer(Composite container, int style,
			int widthHint, int heightHint, int horizontalSpan, int verticalSpan)
	{
		TableViewer tableViewer = new TableViewer(container, style);
		Table table = tableViewer.getTable();
		GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL,
				true, true, horizontalSpan, verticalSpan);
		tableLayoutData.widthHint = widthHint;
		tableLayoutData.heightHint = heightHint;
		table.setLayoutData(tableLayoutData);
		return tableViewer;
	}

	private void addLabel(Composite container, String labelTitle)
	{
		logger.info("adding label : " + labelTitle);
		Label label = new Label(container, SWT.NONE);
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		label.setText(labelTitle);
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
		label.setLayoutData(gd);
	}

	protected void okPressed()
	{
		logger.info("ok pressed");
	}
}
