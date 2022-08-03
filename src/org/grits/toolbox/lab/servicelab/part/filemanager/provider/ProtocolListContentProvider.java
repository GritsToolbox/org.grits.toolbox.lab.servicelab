/**
 * 
 */
package org.grits.toolbox.lab.servicelab.part.filemanager.provider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.lab.servicelab.model.FileInfoProtocolList;

/**
 * 
 *
 */
public class ProtocolListContentProvider implements IStructuredContentProvider
{
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement)
	{
		if(inputElement instanceof FileInfoProtocolList)
		{
			return ((FileInfoProtocolList) inputElement).getFileInfoProtocols().toArray();
		}
		return null;
	}
}
