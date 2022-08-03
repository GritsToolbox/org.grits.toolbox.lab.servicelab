/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.pages;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
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
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;
import org.grits.toolbox.lab.servicelab.model.validation.TaskUnit;
import org.grits.toolbox.lab.servicelab.util.MaintainTableColumnRatioListener;
import org.grits.toolbox.lab.servicelab.util.SampleExpValidationComparator;
import org.grits.toolbox.lab.servicelab.validation.SampleProtocolTaskMatcher;
import org.grits.toolbox.lab.servicelab.wizard.validation.ValidationWizard;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.SampleExpTasksContentProvider;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.SampleExpValidationLabelProvider;
import org.grits.toolbox.lab.servicelab.wizard.validation.provider.TaskUnitLabelProvider;

/**
 * 
 *
 */
public class SampleValidationPage extends WizardPage
{
	private static final Logger logger = Logger.getLogger(SampleValidationPage.class);
	private static final String PAGE_NAME = "Sample Validation";

	private SampleProtocolTaskMatcher sampleProtocolTaskMatcher = null;
	private Map<Entry, SampleExpValidation> entryToSampleValidationMap = null;

	private TableViewer sampleExpValidationTableViewer = null;
	private TableViewer sampleTasksTableViewer = null;
	private TableViewer protocolsForTaskTableViewer = null;
	private TableViewer availableProtocolsTableViewer = null;
	private Button downloadErrorButton = null;

	public SampleValidationPage()
	{
		super(PAGE_NAME);
		setTitle(PAGE_NAME);
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);

		// if navigating to this page reload
		if(visible)
		{
			initialize();
		}
	}

	public Map<Entry, SampleExpValidation> getEntryToSampleValidationMap()
	{
		return entryToSampleValidationMap;
	}

	private void initialize()
	{
		this.entryToSampleValidationMap =
				((TaskAssignmentPage) getPreviousPage()).getEntryToSampleValidationMap();

		List<SampleExpValidation> sampleExpValidations =
				new ArrayList<SampleExpValidation>(entryToSampleValidationMap.values());

		Collections.sort(sampleExpValidations, new SampleExpValidationComparator());
		sampleProtocolTaskMatcher = new SampleProtocolTaskMatcher(sampleExpValidations);
		sampleProtocolTaskMatcher.allocateProtocols();

		sampleExpValidationTableViewer.setInput(sampleExpValidations);
		StructuredSelection selection = sampleExpValidations.isEmpty() ?
				null : new StructuredSelection(sampleExpValidations.iterator().next());
		sampleExpValidationTableViewer.setSelection(selection);

		refreshErrorStatus();
	}

	@SuppressWarnings("unchecked")
	protected void refreshErrorStatus()
	{
		String errorMessage = null;
		for(SampleExpValidation sampleExpValidation : (List<SampleExpValidation>)
				sampleExpValidationTableViewer.getInput())
		{
			errorMessage = sampleExpValidation.getErrorMessage();
			if(errorMessage != null)
			{
				// add error message for this sample
				errorMessage = sampleExpValidation.getSampleName()
						+ " - " + errorMessage.trim();

				if(errorMessage.contains("\n"))
				{
					errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n")).trim();
				}
				break;
			}
		}

		sampleExpValidationTableViewer.refresh();

		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
		downloadErrorButton.setEnabled(errorMessage != null);
	}

	@Override
	public void createControl(Composite parent)
	{
		logger.info("creating sample validation page for the wizard");

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 15;
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		addUIParts(container);

		setControl(container);
		setPageComplete(false);

		logger.info("sample validation page created");
	}

	private void addUIParts(Composite container)
	{
		addLabel(container, ((ValidationWizard) getWizard()).getProjectName());

		// add a table for samples
		addSampleExpTablePart(container);

		// a table for tasks in a sample
		addTasksPart(container);

		// a table displaying all protocols that were assigned to selected task
		addTaskProtocolsPart(container);

		// a button to remove/add protocols from/to a task
		addButtonsPart(container);

		// a table displaying all available protocols that have
		// not been added but can be added to selected task
		addProtocolsOptionPart(container);

		// add button for downloading errors
		addErrorDownloadButton(container);
	}

	private void addSampleExpTablePart(Composite container)
	{
		// this part displays all the samples that are there in a project
		logger.info("creating sample experiment list table");

		sampleExpValidationTableViewer = createTableViewer(container, SWT.SINGLE| SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 200, 375, 1, 3);

		TableViewerColumn columnViewer = new TableViewerColumn(sampleExpValidationTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Sample");
		columnViewer = new TableViewerColumn(sampleExpValidationTableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Status");

		sampleExpValidationTableViewer.setContentProvider(new GenericListContentProvider());
		sampleExpValidationTableViewer.setLabelProvider(new SampleExpValidationLabelProvider());
		sampleExpValidationTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 5, 2));
		sampleExpValidationTableViewer.getTable().setHeaderVisible(true);

		sampleExpValidationTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(!sampleExpValidationTableViewer.getSelection().isEmpty())
				{
					SampleExpValidation sampleExpValidation = (SampleExpValidation) ((StructuredSelection)
							sampleExpValidationTableViewer.getSelection()).getFirstElement();
					// set this sample validation as input to table displaying tasks
					sampleTasksTableViewer.setInput(sampleExpValidation);

					// select a task for this sample
					StructuredSelection selection = sampleExpValidation.getAssignedTasks().iterator().hasNext() ?
							new StructuredSelection(sampleExpValidation.getAssignedTasks().iterator().next()) : null;

					sampleTasksTableViewer.setSelection(selection);
					sampleTasksTableViewer.refresh();
				}
			}
		});
	}

	private void addTasksPart(Composite container)
	{
		// this part displays a table with all the tasks added to a sample
		logger.info("creating sample tasks table");

		sampleTasksTableViewer = createTableViewer(container, SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 200, 375, 1, 3);

		TableViewerColumn columnViewer = new TableViewerColumn(
				sampleTasksTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Tasks");
		columnViewer = new TableViewerColumn(sampleTasksTableViewer, SWT.FILL, 1);
		columnViewer.getColumn().setText("Person");

		sampleTasksTableViewer.setContentProvider(new SampleExpTasksContentProvider());
		sampleTasksTableViewer.setLabelProvider(new TaskUnitLabelProvider());

		sampleTasksTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 3, 2));
		sampleTasksTableViewer.getTable().setHeaderVisible(true);

		sampleTasksTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				List<MinInfoProtocol> matchingProtocols = new ArrayList<MinInfoProtocol>();
				if(!sampleTasksTableViewer.getSelection().isEmpty())
				{
					// set this task as input to table displaying available protocols
					TaskUnit taskUnit = (TaskUnit) ((StructuredSelection)
							sampleTasksTableViewer.getSelection()).getFirstElement();

					// get all matching protocols for this task
					SampleExpValidation sampleExpValidation = (SampleExpValidation)
							sampleTasksTableViewer.getInput();
					matchingProtocols =
							sampleExpValidation.getMatchingProtocols(taskUnit.getServiceLabTask());
				}

				// set all the matching protocols but table will only show that were assigned
				protocolsForTaskTableViewer.setInput(matchingProtocols);
				// set all matching protocols
				availableProtocolsTableViewer.setInput(matchingProtocols);
			}
		});
	}

	private void addTaskProtocolsPart(Composite container)
	{
		// this part displays a table with all the protocols set to the project
		logger.info("creating protocols for task table");

		protocolsForTaskTableViewer = createTableViewer(container, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 200, 145, 2, 1);

		TableViewerColumn columnViewer = new TableViewerColumn(protocolsForTaskTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Set Protocols");

		protocolsForTaskTableViewer.getTable().setHeaderVisible(true);

		protocolsForTaskTableViewer.setContentProvider(new GenericListContentProvider());
		protocolsForTaskTableViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof MinInfoProtocol ?
						((MinInfoProtocol) element).getLabel() : null;
			}
		});
		protocolsForTaskTableViewer.getTable().addControlListener(
				new MaintainTableColumnRatioListener(200, 1));

		protocolsForTaskTableViewer.addFilter(new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				if(element instanceof MinInfoProtocol)
				{
					SampleExpValidation sampleExpValidation =
							(SampleExpValidation) sampleTasksTableViewer.getInput();
					if(!sampleTasksTableViewer.getSelection().isEmpty())
					{
						TaskUnit taskUnit = (TaskUnit) ((StructuredSelection)
								sampleTasksTableViewer.getSelection()).getFirstElement();
						// if this protocol is assigned to selected task in sampleTasksTableViewer
						return sampleExpValidation.getProtocolToTaskMap().get(element) == taskUnit;
					}
				}
				return false;
			}
		});
	}

	private void addButtonsPart(Composite container)
	{
		logger.info("adding \"Remove/Add\" button");
		// this part shows a button for removing or adding protocols for a task
		Button addButton = createButton(container, "Add");
		addButton.setImage(ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.UP_ARROW).createImage());
		addButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(!sampleTasksTableViewer.getSelection().isEmpty() &&
						!availableProtocolsTableViewer.getSelection().isEmpty())
				{
					TaskUnit taskUnit = (TaskUnit) ((StructuredSelection)
							sampleTasksTableViewer.getSelection()).getFirstElement();

					@SuppressWarnings("unchecked")
					Iterator<MinInfoProtocol> protocolSelectionIterator = ((StructuredSelection)
							availableProtocolsTableViewer.getSelection()).iterator();

					SampleExpValidation sampleExpValidation =
							(SampleExpValidation) sampleTasksTableViewer.getInput();
					MinInfoProtocol selectedProtocol = null;
					// allocate current task to all selected protocols
					while(protocolSelectionIterator.hasNext())
					{
						selectedProtocol = (MinInfoProtocol) protocolSelectionIterator.next();
						sampleExpValidation.getProtocolToTaskMap().put(selectedProtocol, taskUnit);
					}

					protocolsForTaskTableViewer.refresh();
					availableProtocolsTableViewer.refresh();
					sampleTasksTableViewer.refresh();
					// resetting error refreshes sample table
					refreshErrorStatus();
				}

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
				if(!sampleTasksTableViewer.getSelection().isEmpty() &&
						!protocolsForTaskTableViewer.getSelection().isEmpty())
				{
					@SuppressWarnings("unchecked")
					Iterator<MinInfoProtocol> protocolSelectionIterator = ((StructuredSelection)
							protocolsForTaskTableViewer.getSelection()).iterator();

					SampleExpValidation sampleExpValidation =
							(SampleExpValidation) sampleTasksTableViewer.getInput();
					// remove all selected protocols from task map
					while(protocolSelectionIterator.hasNext())
					{
						sampleExpValidation.getProtocolToTaskMap().remove(protocolSelectionIterator.next());
					}

					protocolsForTaskTableViewer.refresh();
					availableProtocolsTableViewer.refresh();
					sampleTasksTableViewer.refresh();
					// resetting error refreshes sample table
					refreshErrorStatus();

				}
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

	private void addProtocolsOptionPart(Composite container)
	{
		// this part displays a table with all the protocols that are available for a task
		// that were not added to this task
		logger.info("creating protocols option table");

		availableProtocolsTableViewer = createTableViewer(container, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 200, 145, 2, 1);

		TableViewerColumn columnViewer = new TableViewerColumn(availableProtocolsTableViewer, SWT.FILL, 0);
		columnViewer.getColumn().setText("Other Possible Match");
		columnViewer.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return element instanceof MinInfoProtocol ?
						((MinInfoProtocol) element).getLabel() : null;
			}

			@Override
			public String getToolTipText(Object element)
			{
				if(!sampleTasksTableViewer.getSelection().isEmpty() &&
						element instanceof MinInfoProtocol)
				{
					SampleExpValidation sampleExpValidation =
							(SampleExpValidation) sampleTasksTableViewer.getInput();
					TaskUnit currentAssignedTask =
							sampleExpValidation.getProtocolToTaskMap().get(element);
					StringBuilder tooltipBuilder = new StringBuilder().append("Currently assigned to ");

					// get current assigned task
					if(currentAssignedTask != null)
					{
						tooltipBuilder.append(currentAssignedTask.getServiceLabTask().getTaskName());
						if(currentAssignedTask.getAssignedPerson() != null)
						{
							tooltipBuilder.append(" (").append(
									currentAssignedTask.getAssignedPerson()).append(")"); 
						}
					}
					else
					{
						tooltipBuilder.append("none");
					}

					return tooltipBuilder.toString();
				}
				return null;
			}
		});

		ColumnViewerToolTipSupport.enableFor(columnViewer.getViewer());
		availableProtocolsTableViewer.setContentProvider(new GenericListContentProvider());
		availableProtocolsTableViewer.getTable().setHeaderVisible(true);


		availableProtocolsTableViewer.addFilter(new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				if(element instanceof MinInfoProtocol)
				{
					TaskUnit taskUnit = (TaskUnit) ((StructuredSelection)
							sampleTasksTableViewer.getSelection()).getFirstElement();
					SampleExpValidation sampleExpValidation =
							(SampleExpValidation) sampleTasksTableViewer.getInput();

					// input list only contains protocols that are possible for selected task
					// if this protocol is not assigned to selected task in sampleTasksTableViewer
					return sampleExpValidation.getProtocolToTaskMap().get(element) != taskUnit;
				}

				return false;
			}
		});
		availableProtocolsTableViewer.getTable().addControlListener(new MaintainTableColumnRatioListener(200, 1));
	}

	private void addErrorDownloadButton(Composite container)
	{
		logger.info("creating download error button");
		// this part shows a button for removing or adding samples for a task
		downloadErrorButton = new Button(container, SWT.PUSH);
		downloadErrorButton.setText("Download Errors");
		downloadErrorButton.setImage(ImageRegistry.getImageDescriptor(
				ImageRegistry.PluginIcon.ERROR_ICON).createImage());
		GridData gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.END;
		gd.verticalAlignment = SWT.BEGINNING;
		downloadErrorButton.setLayoutData(gd);
		final String initialFileName = ((ValidationWizard) getWizard()).getProjectName()
				+ " Protocol Errors";
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

	@SuppressWarnings("unchecked")
	private void downloadErrors(String fileLocation)
	{
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(fileLocation);

			fileWriter.append("*****Errors Matching Task with Protocols*****").append(System.lineSeparator())
			.append("PROJECT - ").append(((ValidationWizard) getWizard()).getProjectName())
			.append(System.lineSeparator()).append("" + new Date())
			.append(System.lineSeparator()).append("---------------------------------------------")
			.append(System.lineSeparator()).append(System.lineSeparator());

			String errorMessage = null;
			for(SampleExpValidation sampleExpValidation : (List<SampleExpValidation>)
					sampleExpValidationTableViewer.getInput())
			{
				errorMessage = sampleExpValidation.getErrorMessage();
				if(errorMessage != null)
				{
					// add error message for this sample
					fileWriter.append("=> ")
					.append(sampleExpValidation.getSampleName()).append(System.lineSeparator())
					.append(errorMessage).append(System.lineSeparator());
				}
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
					logger.error(ex.getMessage(), ex);
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
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1);
		label.setLayoutData(gd);
	}

	protected void okPressed()
	{
		logger.info("ok pressed");
	}
}
