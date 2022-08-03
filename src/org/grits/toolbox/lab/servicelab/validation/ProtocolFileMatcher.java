/**
 * 
 */
package org.grits.toolbox.lab.servicelab.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.archive.model.Archive;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;
import org.grits.toolbox.lab.servicelab.model.FileUpload;
import org.grits.toolbox.lab.servicelab.model.MinInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocolList;
import org.grits.toolbox.lab.servicelab.model.validation.FileUploadError;
import org.grits.toolbox.lab.servicelab.model.validation.SampleExpValidation;
import org.grits.toolbox.lab.servicelab.util.ProtocolFileUploadUtil;

/**
 * This class matches existing files to protocols in a sample. The input to this class is a
 * list of project entry, protocol file upload info file and sampleExpValidations.
 * For each sampleExpValidation object in the list, there is a sample and a list protocols.
 * This class matches/verifies that for each protocol there is enough uploaded files.
 * <p>
 * <b>matchProtocols()</b> is supposed to be called immediately after initialization. 
 * The method <b>getProjectError()</b> can be called before <b>matchProtocols()</b>
 * as errors during initialization is stored as a project error. The method <b>getFileUploadErrors()</b>
 * is available only after matching is done and so is method <b>getFileToProtocolMap()</b>.
 * 
 * 
 *
 */
public class ProtocolFileMatcher
{
	private static Logger logger = Logger.getLogger(ProtocolFileMatcher.class);

	private static final String SUBTYPE_SEPARATOR = "::";

	private List<SampleExpValidation> sampleExpValidations =
			new ArrayList<SampleExpValidation>();

	// a map containing file to its protocol assignment
	private Map<ArchivedFile, MinInfoProtocol> fileToProtocolMap =
			new HashMap<ArchivedFile, MinInfoProtocol>();

	private String projectError = null;
	// a list containing error for protocols
	private List<FileUploadError> fileUploadErrors = new ArrayList<FileUploadError>();

	private Map<String, FileInfoProtocol> protocolUniqueIdToProtocolCacheMap =
			new HashMap<String, FileInfoProtocol>();
	private Map<String, List<ArchivedFile>> similarCategoryFilesCacheMap =
			new HashMap<String, List<ArchivedFile>>();

	public ProtocolFileMatcher(Entry projectEntry, File fileUploadInfoFile,
			List<SampleExpValidation> sampleExpValidations)
	{
		logger.info("Initializing Protocol File Matcher");
		this.sampleExpValidations = sampleExpValidations;
		try
		{
			// load all protocol information for file uploads
			FileInfoProtocolList protocolFileUploadList =
					ProtocolFileUploadUtil.getFileInfoProtocolList(fileUploadInfoFile);
			String errorMessage = null;

			for(FileInfoProtocol protocolFileUpload : protocolFileUploadList.getFileInfoProtocols())
			{
				// this list should contain unique protocols
				if(protocolUniqueIdToProtocolCacheMap.containsKey(protocolFileUpload.getUniqueKey()))
				{
					errorMessage = "Protocol : " + protocolFileUpload.getUniqueKey() +
							" [Duplicate protocols in Protocol File Manager]";
					logger.error(errorMessage);
					throw new Exception(errorMessage);
				}

				protocolUniqueIdToProtocolCacheMap.put(protocolFileUpload.getUniqueKey(), protocolFileUpload);
			}

			// load archive from project
			Archive archive = initProjectArchive(projectEntry);
			String categoryType = null;
			List<ArchivedFile> similarCategoryFiles = null;
			// create a map index of all similar category archived files
			for(ArchivedFile archivedFile : archive.getArchivedFiles())
			{
				categoryType = getCategoryString(archivedFile);
				similarCategoryFiles = similarCategoryFilesCacheMap.get(categoryType);

				if(similarCategoryFiles == null)
				{
					similarCategoryFiles = new ArrayList<ArchivedFile>();
					similarCategoryFilesCacheMap.put(categoryType, similarCategoryFiles);
				}
				similarCategoryFiles.add(archivedFile);
			}
		} catch (Exception ex)
		{
			logger.fatal(ex.getMessage(), ex);
			projectError = "Error initializing file matcher : " + ex.getMessage();
		}
	}

	private String getCategoryString(ArchivedFile archivedFile)
	{
		// initially unknown for uploaded files
		String categoryString = "unknown";
		DocumentType documentType = archivedFile.getTypicalArchivedFile().getDocumentType();
		if(documentType != null &&
				(documentType.getLabel() != null && !documentType.getLabel().isEmpty()))
		{
			// set a non-null( or non-empty) category
			categoryString = documentType.getLabel() + SUBTYPE_SEPARATOR;

			// add its subType
			categoryString += documentType.getSelectedSubType() == null ?
					"" : documentType.getSelectedSubType();
		}
		return categoryString;
	}

	private String getCategoryString(FileUpload fileUpload)
	{
		// initially empty for manager file descriptions
		String categoryString = "";
		if(fileUpload != null &&
				fileUpload.getFileCategory() != null && !fileUpload.getFileCategory().isEmpty())
		{
			// set a non-null( or non-empty) category
			categoryString = fileUpload.getFileCategory() + SUBTYPE_SEPARATOR;

			// add its subType
			categoryString += fileUpload.getFileSubCategory() == null ?
					"" : fileUpload.getFileSubCategory();
		}
		return categoryString;
	}

	public void matchFiles()
	{
		fileToProtocolMap.clear();
		fileUploadErrors.clear();

		List<ArchivedFile> similarCategoryFiles = null;
		List<ArchivedFile> allAssignedFiles = new ArrayList<ArchivedFile>();
		ArchivedFile assignedFile = null;
		String errorMessage = null;
		String categoryType = null;
		FileInfoProtocol fileInfoProtocol = null;

		for(SampleExpValidation sampleExpValidation : sampleExpValidations)
		{
			for(MinInfoProtocol protocol : sampleExpValidation.getProtocols())
			{
				fileInfoProtocol = protocolUniqueIdToProtocolCacheMap.get(protocol.getUniqueKey());

				// add errors that cannot be fixed by re-assignments
				if(fileInfoProtocol == null)
				{
					errorMessage = "[Missing file information]";
					logger.error(errorMessage);
					fileUploadErrors.add(new FileUploadError(
							sampleExpValidation.getSampleName(), protocol, errorMessage));
					continue;
				}

				similarCategoryFiles = new ArrayList<ArchivedFile>();
				for(FileUpload fileUpload : fileInfoProtocol.getFileUploads())
				{
					categoryType = getCategoryString(fileUpload);
					similarCategoryFiles = similarCategoryFilesCacheMap.get(categoryType);

					// check for all errors that cannot be fixed by re-assignments
					if(similarCategoryFiles == null)
					{
						errorMessage = "[Missing file type : " + categoryType + "]";
						logger.error(errorMessage);
						fileUploadErrors.add(new FileUploadError(
								sampleExpValidation.getSampleName(), protocol, errorMessage));
						continue;
					}

					if(similarCategoryFiles.size() < fileUpload.getNumberOfFiles())
					{
						errorMessage = fileUpload.getNumberOfFiles() == 1 ?
								"[Needs file of type : " : "[Needs more files of type : ";
						errorMessage += categoryType + "]";
						logger.error(errorMessage);
						fileUploadErrors.add(new FileUploadError(
								sampleExpValidation.getSampleName(), protocol, errorMessage));
						continue;
					}

					for(int i = 0; i < fileUpload.getNumberOfFiles(); i++)
					{
						assignedFile = null;
						for(ArchivedFile similarCategoryFile : similarCategoryFiles)
						{
							if(!allAssignedFiles.contains(similarCategoryFile))
							{
								// double check to remove error, crept in while matching string names
								// for files that could have different categories,
								// e.g. categ."A::" with subCateg."B" and categ."A" with subCateg."::B"
								if(matchCategory(similarCategoryFile, fileUpload))
								{
									assignedFile = similarCategoryFile;
									break;
								}
							}
						}

						// needs more file to be uploaded for this protocol
						if(assignedFile == null)
						{
							errorMessage = "[Needs file of type : " + categoryType + "]";
							logger.error(errorMessage);
							fileUploadErrors.add(new FileUploadError(
									sampleExpValidation.getSampleName(), protocol, errorMessage));
							continue;
						}

						allAssignedFiles.add(assignedFile);
						fileToProtocolMap.put(assignedFile, protocol);
					}
				}
			}
		}
	}

	private boolean matchCategory(ArchivedFile archivedFile, FileUpload fileUpload)
	{
		boolean matched = false;

		// only matches for not-null document categories
		if(archivedFile != null && fileUpload != null
				&& archivedFile.getTypicalArchivedFile().getDocumentType() != null)
		{
			DocumentType documentType = archivedFile.getTypicalArchivedFile().getDocumentType();
			if(fileUpload.getFileCategory() != null
					&& documentType != null && documentType.getLabel() != null)
			{
				if(fileUpload.getFileCategory().equals(documentType.getLabel()))
				{
					matched = Objects.equals(fileUpload.getFileSubCategory(),
							documentType.getSelectedSubType());
				}
			}
		}
		return matched;
	}

	private static Archive initProjectArchive(Entry entry) throws Exception
	{
		Entry archiveEntry = null;
		for(Entry childEntry : entry.getChildren())
		{
			if(ArchiveProperty.TYPE.equals(childEntry.getProperty().getType()))
			{
				archiveEntry = childEntry;
				break;
			}
		}
		File archiveFolder = ArchiveProperty.getFolderForArchiveEntry(archiveEntry);

		if(archiveFolder == null || !archiveFolder.exists() || !archiveFolder.isDirectory())
		{
			logger.error("Could not find archive folder for the project.");
			throw new Exception("Could not find archive folder for the project.");
		}

		File archiveXmlFile = null;
		for(File file : archiveFolder.listFiles())
		{
			if(file.getName().equals("archive.xml") 
					&& file.isFile())
			{
				archiveXmlFile = file;
				break;
			}
		}

		if(archiveXmlFile == null || !archiveXmlFile.exists() || !archiveXmlFile.isFile())
		{
			logger.error("Could not find archive xml file.");
			throw new Exception("Could not find archive xml file.");
		}

		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(archiveXmlFile.getAbsolutePath());
			InputStreamReader reader = new InputStreamReader(inputStream,
					PropertyHandler.GRITS_CHARACTER_ENCODING);
			JAXBContext context = JAXBContext.newInstance(Archive.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (Archive) unmarshaller.unmarshal(reader);
		} catch (Exception ex) {
			logger.error("Error parsing the archive xml file." + ex.getMessage(), ex);
			throw new Exception("Error parsing the archive xml file.");
		}
		finally
		{
			if(inputStream != null)
				inputStream.close();
		}
	}

	public Map<ArchivedFile, MinInfoProtocol> getFileToProtocolMap()
	{
		return fileToProtocolMap;
	}

	public List<FileUploadError> getFileUploadErrors()
	{
		return fileUploadErrors;
	}

	public String getProjectError()
	{
		return projectError;
	}
}
