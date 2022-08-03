
package org.grits.toolbox.lab.servicelab.handler;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartStackImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.lab.servicelab.part.filemanager.ProtocolUploadFileManager;

@SuppressWarnings("restriction")
public class OpenFileManagerHandler
{
	private static final Logger logger = Logger.getLogger(OpenFileManagerHandler.class);

	@Execute
	public void execute(EModelService modelService, EPartService partService,
			MApplication application, IGritsUIService gritsUiService)
	{
		logger.info("Opening File Manager part");
		MPart managerPart = partService.findPart(ProtocolUploadFileManager.PART_ID);

		if(managerPart == null)
		{
			logger.info("Protocol file manager part not found. Creating protocol file manager");
			managerPart = partService.createPart(ProtocolUploadFileManager.PART_ID);

			logger.info("Adding protocol file manager to partstack - e4.primaryDataStack");
			PartStackImpl partStackImpl = (PartStackImpl) modelService.find(
					IGritsUIService.PARTSTACK_PRIMARY_DATA, application);
			partStackImpl.getChildren().add(managerPart);
		}

		partService.showPart(managerPart, PartState.ACTIVATE);
		gritsUiService.selectPerspective(IGritsConstants.ID_DEFAULT_PERSPECTIVE);

		logger.info("File Manager part opened");
	}
}