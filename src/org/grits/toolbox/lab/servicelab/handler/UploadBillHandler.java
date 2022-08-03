
package org.grits.toolbox.lab.servicelab.handler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.entry.archive.model.Archive;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;
import org.grits.toolbox.entry.archive.utility.UtilityFileName;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.dialog.UploadBillDialog;
import org.grits.toolbox.lab.servicelab.exception.LoadingException;

/**
 * 
 *
 */
public class UploadBillHandler
{
	private static final Logger logger = Logger.getLogger(UploadBillHandler.class);
	public static final String COMMAND_ID =
			"org.grits.toolbox.lab.servicelab.command.generatebill";

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry,
			IGritsDataModelService gritsDataModelService,
			Shell shell)
	{
		logger.info("Uploading bill for a project");
		if(entry == null)
		{
			logger.info("try getting selected entry from data model service's last selection");
			// for case when eclipse context has changed and selection has been reset
			// try getting the last selection from grits data model service.
			if(gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().size() == 1
					&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
			{
				logger.info("retrieved selected entry from data model service last selection");
				entry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
			}
		}

		if(entry != null && entry.getProperty() != null
				&& ProjectProperty.TYPE.equals(entry.getProperty().getType()))
		{
			logger.info(entry.getDisplayName());
			try
			{
				// check for ".servicelab" folder inside project
				String workspaceLocation = ((WorkspaceProperty)
						entry.getParent().getProperty()).getLocation();

				File projectServiceLabFolder = new File(workspaceLocation
						+ File.separator + entry.getDisplayName()
						+ File.separator + IConfig.SERVICE_LAB_FOLDER_NAME);

				// try creating this folder, if it does not exist
				if(!projectServiceLabFolder.exists())
				{
					logger.info("creating .servicelab folder inside project");
					if(!projectServiceLabFolder.mkdirs())
					{
						throw new LoadingException("Error creating Bill folder",
								"A folder could not be created inside the project to"
										+ " store billing information. Try closing any open window"
										+ " in workspace and re-uploading it."
										+ " If the problem persists contact GRITS developers.");
					}
				}

				Entry archiveEntry = null;
				for(Entry projectChildEntry : entry.getChildren())
				{
					if(ArchiveProperty.TYPE.equals(projectChildEntry.getProperty().getType()))
					{
						archiveEntry = projectChildEntry;
						break;
					}
				}

				if(archiveEntry == null)
				{
					logger.error("archive entry not found inside project");
					throw new LoadingException("Archive not found",
							"No Archive created for this project. Please create an archive"
									+ " in the project first then upload the bill.");
				}

				// get project archive folder
				File archiveFolder = ArchiveProperty.getFolderForArchiveEntry(archiveEntry);
				File archiveXmlFile = getArchiveXmlFile(archiveFolder);
				Archive projectArchive = getArchive(archiveXmlFile);
				List<ArchivedFile> archivedFiles = projectArchive.getArchivedFiles();
				Set<String> existingNames =
						UtilityFileName.getExistingNames(archivedFiles, archiveFolder);

				UploadBillDialog uploadBillDialog = new UploadBillDialog(shell,
						entry.getDisplayName(), existingNames);
				if(uploadBillDialog.open() == Window.OK) 
				{
					logger.info("add bill to archive");
					ArchivedFile newArchivedFile = uploadBillDialog.getArchivedFile();
					String archiveFileName = newArchivedFile.getTypicalArchivedFile().getFileName();
					File fileToArchive = uploadBillDialog.getSourceFile();
					FileOutputStream archiveFileOutputStream = null;
					try
					{
						logger.info("copying bill to archive");
						archiveFileOutputStream = new FileOutputStream(
								archiveFolder.getAbsolutePath() + File.separator + archiveFileName);
						Files.copy(fileToArchive.toPath(), archiveFileOutputStream);
						archivedFiles.add(newArchivedFile);
						saveArchive(archiveXmlFile, projectArchive);

						// add the bill amount to stats file
						logger.info("set bill amount to project stats");
						addBillAmount(new File(projectServiceLabFolder,
								IConfig.PROJECT_STATS_FILE_NAME), uploadBillDialog.getBillAmount());
					} catch (Exception ex)
					{
						logger.error("error copying bill file to archive");
						logger.error(ex.getMessage(), ex);
						throw new Exception("Bill file could not be copied to archive folder.\n" + ex.getMessage());
					}
					finally
					{
						if(archiveFileOutputStream != null)
							archiveFileOutputStream.close();
					}
				}
			} catch (LoadingException e)
			{
				logger.error(e.getMessage(), e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						e.getErrorTitle(), e.getErrorMessage());
			} catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						"Error Uploading Bill", "Error in bill upload :\n" + e.getMessage());
			}
		}
	}

	private File getArchiveXmlFile(File archiveFolder) throws Exception
	{
		logger.info("load archive xml file");

		// validate archive folder
		if(archiveFolder == null || !archiveFolder.exists() || !archiveFolder.isDirectory())
		{
			logger.error("Could not find archive folder for the project.");
			throw new Exception("Could not find archive folder for the project.");
		}

		File archiveXmlFile = null;
		for(File file : archiveFolder.listFiles())
		{
			if(org.grits.toolbox.entry.archive.config
					.IConfig.ARCHIVE_FILE_NAME.equals(file.getName()) && file.isFile())
			{
				archiveXmlFile = file;
				break;
			}
		}

		// validate archive xml file
		if(archiveXmlFile == null || !archiveXmlFile.exists() || !archiveXmlFile.isFile())
		{
			logger.error("Could not find archive xml file in the project archive.");
			throw new Exception("Could not find archive xml file in the project archive.");
		}

		return archiveXmlFile;
	}

	private Archive getArchive(File archiveXmlFile) throws Exception
	{
		logger.info("load archive from archive xml file");
		try
		{
			FileInputStream inputStream = new FileInputStream(archiveXmlFile.getAbsolutePath());
			InputStreamReader reader = new InputStreamReader(inputStream, 
					PropertyHandler.GRITS_CHARACTER_ENCODING);
			try
			{
				JAXBContext context = JAXBContext.newInstance(Archive.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				return (Archive) unmarshaller.unmarshal(reader);
			} catch (JAXBException ex)
			{
				logger.error("Error parsing the archive xml file.\n" + ex.getMessage(), ex);
				throw ex;
			}
		} catch (FileNotFoundException | UnsupportedEncodingException ex)
		{
			logger.error("Error loading the archive xml from file location.\n" + ex.getMessage(), ex);
			throw ex;
		}
	}

	private void addBillAmount(File projectStatsFile, double billAmount) throws IOException
	{
		logger.info("add bill amount to stats file");
		Map<String, String> statsMap = new HashMap<String, String>();

		// read existing values from map
		if(projectStatsFile.exists())
		{
			BufferedReader bufferedReader = null;
			try
			{
				logger.info("reading stats file from project");
				bufferedReader = new BufferedReader(new FileReader(projectStatsFile));
				String line = bufferedReader.readLine();
				String[] keyValue = null;
				int lineNumber = 1;
				while(line != null)
				{
					keyValue = line.split("=");
					if(keyValue.length != 2)
					{
						logger.error("Error in line : " + lineNumber);
					}
					else
					{
						if(keyValue[0] == null)
						{
							logger.error("Error in line : " + lineNumber
									+ " No stats key was found");
						}
						else
						{
							statsMap.put(keyValue[0], keyValue[1]);
						}
					}
					line = bufferedReader.readLine();
					lineNumber++;
				}
			} catch (IOException ex)
			{
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
			finally
			{
				if(bufferedReader != null)
					bufferedReader.close();
			}
		}

		// modify bill amount in the map
		statsMap.put(IConfig.PROJECT_STATS_BILL_AMOUNT, "" + billAmount);

		// save the file
		FileWriter fileWriter = null;
		try
		{
			logger.info("writing project stats file");
			fileWriter = new FileWriter(projectStatsFile);
			for(String key : statsMap.keySet())
			{
				fileWriter.append(key).append('=');
				if(statsMap.get(key) != null)
					fileWriter.append(statsMap.get(key));
				fileWriter.append(System.lineSeparator());
			}
		} catch(IOException ex)
		{
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
		finally
		{
			if(fileWriter != null)
				fileWriter.close();
		}
	}

	private void saveArchive(File archiveXmlFile, Archive projectArchive) throws Exception
	{
		logger.info("save archive xml file");
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try
			{
				JAXBContext context = JAXBContext.newInstance(Archive.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
				marshaller.marshal(projectArchive, os);
				try
				{
					FileWriter fileWriter = new FileWriter(archiveXmlFile);
					fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
					fileWriter.close();
					os.close();
				} catch (IOException e)
				{
					logger.error("The bill could not be added to the archive file.\n" + e.getMessage(), e);
					throw e;
				}
			} catch (JAXBException e)
			{
				logger.error("The bill archive could not be serialized as xml.\n" + e.getMessage(), e);
				throw e;
			}
		} catch (Exception e)
		{
			logger.fatal(e.getMessage(), e);
			throw e;
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IGritsDataModelService gritsDataModelService)
	{
		if(object == null)
		{
			// for case when eclipse context has changed and selection has been reset
			// try getting the last selection from grits data model service.
			if(gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().size() == 1)
			{
				object = gritsDataModelService.getLastSelection().getFirstElement();
			}
		}

		if(object instanceof Entry)
		{
			Entry entry = (Entry) object;
			return entry.getProperty() instanceof ProjectProperty
					&& ((ProjectProperty) entry.getProperty()).isOpen();
		}
		return false;
	}
}