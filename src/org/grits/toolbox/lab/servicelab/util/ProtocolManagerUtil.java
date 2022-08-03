/**
 * 
 */
package org.grits.toolbox.lab.servicelab.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.editor.experimentdesigner.model.ExperimentGraph;
import org.grits.toolbox.editor.experimentdesigner.model.GraphNode;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolCategory;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolNode;
import org.grits.toolbox.editor.experimentdesigner.model.ProtocolPaletteEntry;
import org.grits.toolbox.editor.experimentdesigner.ontology.ExperimentDesignOntologyAPI;
import org.grits.toolbox.editor.experimentdesigner.property.ExperimentProperty;
import org.grits.toolbox.lab.servicelab.model.PriceInfoProtocolList;

/**
 * 
 *
 */
public class ProtocolManagerUtil
{
	private static Logger logger = Logger.getLogger(ProtocolManagerUtil.class);

	public static List<ProtocolNode> getAllProtocolNodesFromWorkspace(Entry workspaceEntry)
	{
		logger.info("Adding other protocol variants from workspace");
		List<ProtocolNode> protocolNodes = new ArrayList<ProtocolNode>();

		if(workspaceEntry == null)
		{
			logger.error("workspace entry is null : " + workspaceEntry);
			return null;
		}

		if(!WorkspaceProperty.TYPE.equals(workspaceEntry.getProperty().getType()))
		{
			logger.error("given entry is not a workspace entry but is of type : "
					+ workspaceEntry.getProperty().getType());
			return null;
		}

		for(Entry projectEntry : workspaceEntry.getChildren())
		{
			logger.debug(projectEntry.getDisplayName().toUpperCase());
			for(Entry sampleEntry : projectEntry.getChildren())
			{
				logger.debug(sampleEntry.getDisplayName().toUpperCase());
				for(Entry sampleChildEntry : sampleEntry.getChildren())
				{
					logger.debug(sampleChildEntry.getDisplayName().toUpperCase());
					try
					{
						if(sampleChildEntry.getProperty() instanceof ExperimentProperty)
						{
							protocolNodes.addAll(getProtocolNodesForExperiment(sampleChildEntry));
						}
					} catch (Exception ex)
					{
						logger.error("error reading experiment entry : " +
								"(project " + projectEntry.getDisplayName() + " -> "
								+ "sample " + sampleEntry.getDisplayName() + " -> "
								+ "experiment " + sampleChildEntry.getDisplayName()
								+ ")\n" + ex.getMessage(), ex);
					}
				}
			}
		}

		return protocolNodes;
	}

	public static List<ProtocolNode> getProtocolNodesForExperiment(Entry experimentDesignEntry) throws Exception
	{
		if(experimentDesignEntry == null)
		{
			logger.error("null experiment entry " + experimentDesignEntry);
			return null;
		}

		if(!(experimentDesignEntry.getProperty() instanceof ExperimentProperty))
		{
			logger.error("not an experiment entry " + experimentDesignEntry.getProperty().getClass());
			return null;
		}

		List<ProtocolNode> protocolNodes = new ArrayList<ProtocolNode>();

		ExperimentProperty experimentProperty = ((ExperimentProperty) experimentDesignEntry.getProperty());
		String experimentFolderLocation = ExperimentProperty.getExperimentDesignLocation(experimentDesignEntry);
		String fileName = experimentProperty.getExperimentFile().getName();
		File experimentFile = new File(experimentFolderLocation + File.separator + fileName);
		logger.info("reading experiment file : " + experimentFile.getAbsolutePath());

		FileInputStream inputStream = null;
		try
		{
			inputStream  = new FileInputStream(experimentFile);
			JAXBContext context = JAXBContext.newInstance(ExperimentGraph.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			ExperimentGraph graph = (ExperimentGraph) unmarshaller.unmarshal(
					new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING));
			for(GraphNode node : graph.getNodes())
			{
				if(node instanceof ProtocolNode)
				{
					protocolNodes.add((ProtocolNode) node);
				}
			}
		} finally
		{
			if(inputStream != null)
				inputStream.close();
		}
		return protocolNodes;
	}

	public static List<ProtocolNode> getAllProtocolNodesFromOntology(ExperimentDesignOntologyAPI expApi)
	{
		logger.info("Adding other protocol variants from workspace");

		if(expApi == null)
		{
			logger.error("parameter experiment api object is null : " + expApi);
			return null;
		}

		logger.info("Adding protocols from ontology");
		List<ProtocolNode> protocolNodes = new ArrayList<ProtocolNode>();
		ProtocolNode protocolNode = null;
		for(ProtocolCategory topCategory : expApi.getTopLevelCategories())
		{
			logger.debug(topCategory.getName().toUpperCase());
			for(ProtocolCategory category : expApi.getProtocolCategoriesByTopLevelCategory(topCategory))
			{
				logger.debug(category.getName().toUpperCase());
				for(ProtocolPaletteEntry paletteEntry : expApi.getProtocolsForCategory(category))
				{
					logger.debug(paletteEntry.getLabel().toUpperCase());
					protocolNode = expApi.getProtocolByUri(paletteEntry.getUri());
					if(protocolNode == null)
					{
						logger.error("missing protocol node : " + paletteEntry.getUri());
						continue;
					}
					protocolNodes.add(protocolNode);
				}
			}
		}

		return protocolNodes;
	}

	public static PriceInfoProtocolList getPriceInfoProtocolList(File protocolListFile) throws IOException, JAXBException
	{
		if(protocolListFile == null)
		{
			logger.fatal("protocol list file is null");
			return null;
		}

		if(!protocolListFile.exists())
		{
			logger.fatal("protocol list file does not exist");
			return null;
		}

		PriceInfoProtocolList priceInfoProtocolList = null;
		FileInputStream inputStream = null;
		try
		{
			logger.info("reading protocols list from file");
			inputStream = new FileInputStream(protocolListFile);
			InputStreamReader reader = new InputStreamReader(inputStream, PropertyHandler.GRITS_CHARACTER_ENCODING);
			Unmarshaller unmarshaller = JAXBContext.newInstance(PriceInfoProtocolList.class).createUnmarshaller();
			priceInfoProtocolList  = (PriceInfoProtocolList) unmarshaller.unmarshal(reader);
		} finally
		{
			if(inputStream != null)
			{
				inputStream.close();
			}
		}
		return priceInfoProtocolList;
	}
}
