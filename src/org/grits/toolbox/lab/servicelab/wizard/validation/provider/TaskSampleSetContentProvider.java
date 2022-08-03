/**
 * 
 */
package org.grits.toolbox.lab.servicelab.wizard.validation.provider;

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.lab.servicelab.model.validation.TaskAssignment;

/**
 * 
 *
 */
public class TaskSampleSetContentProvider implements IStructuredContentProvider
{
	public TaskSampleSetContentProvider()
	{
		
	}

	@Override
	public void dispose()
	{

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{

	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if(inputElement instanceof TaskAssignment)
		{
			TreeSet<Entry> treeSet = new TreeSet<Entry>(entryComparator);
			treeSet.addAll(((TaskAssignment) inputElement).getSampleExpEntries());
			return treeSet.toArray();
		}
		return null;
	}

	private static Comparator<Entry> entryComparator = new Comparator<Entry>()
	{
		@Override
		public int compare(Entry entry1, Entry entry2)
		{
			return entry1.getDisplayName().compareToIgnoreCase(entry2.getDisplayName());
		}
	};
}
