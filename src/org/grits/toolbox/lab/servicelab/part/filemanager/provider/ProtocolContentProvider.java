package org.grits.toolbox.lab.servicelab.part.filemanager.provider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocol;

public class ProtocolContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if(inputElement instanceof FileInfoProtocol)
		{
			return ((FileInfoProtocol) inputElement).getFileUploads().toArray();
		}
		return null;
	}
}
