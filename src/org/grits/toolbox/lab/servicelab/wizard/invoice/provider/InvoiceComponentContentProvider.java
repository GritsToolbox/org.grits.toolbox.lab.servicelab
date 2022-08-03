package org.grits.toolbox.lab.servicelab.wizard.invoice.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.lab.servicelab.model.InvoiceComponent;
import org.grits.toolbox.lab.servicelab.model.ServiceLabInvoice;

public class InvoiceComponentContentProvider implements IStructuredContentProvider
{
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
		if(inputElement instanceof ServiceLabInvoice)
		{
			return ((ServiceLabInvoice) inputElement).getInvoiceComponents().toArray();
		}
		return null;
	}

	public static String getCSVSampleNames(InvoiceComponent invoiceComponent)
	{
		if(invoiceComponent == null)
			return null;

		StringBuilder stringBuilder = new StringBuilder();
		String value = "";
		if(!invoiceComponent.getSampleNames().isEmpty())
		{
			stringBuilder.append("(Samples: ");
			List<String> sortedList = new ArrayList<String>(invoiceComponent.getSampleNames());
			Collections.sort(sortedList, String.CASE_INSENSITIVE_ORDER); 
			for(String sampleName : sortedList)
			{
				stringBuilder.append(sampleName).append(", ");
			}
			value = stringBuilder.toString().trim();
			value = value.endsWith(",")
					? value.substring(0, value.lastIndexOf(",")) : value;
			value += ")";
		}
		return value;
	}
}
