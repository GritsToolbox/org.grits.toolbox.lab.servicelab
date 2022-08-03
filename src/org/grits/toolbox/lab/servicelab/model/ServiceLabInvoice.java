/**
 * 
 */
package org.grits.toolbox.lab.servicelab.model;

import java.util.Date;
import java.util.TreeSet;

/**
 * 
 *
 */
public class ServiceLabInvoice
{
	public static final String[] CCRC_ADDRESS = new String[]
			{"University of Georgia",
			"Attn: Analysis Services",
			"315 Riverbend Road",
			"Athens, GA 30602-4712",
			"Tel: (706) 542-4401  Fax: (706) 542-4412"};

	public static final String[] CHECK_INFO = new String[]
			{"Make check payable to: University of Georgia and mail to address above: ",
					"ATTN: Diane Hermosillo (dhermosi@ccrc.uga.edu) at address"
					+ " above or call 706-542-4409 to pay by credit card."};

	private Date invoiceDate = null;
	private String invoiceNumber = null;
	private String billTo = null;
	private Date dueDate = null;
	private String poNumber = null;
	private String additionalNote = null;
	private TreeSet<InvoiceComponent> invoiceComponents =
			new TreeSet<InvoiceComponent>(new InvoiceComponentComparator());
	private TreeSet<InvoiceComponent> selectedInvoiceComponents =
			new TreeSet<InvoiceComponent>(new InvoiceComponentComparator());

	/**
	 * @return the invoiceDate
	 */
	public Date getInvoiceDate()
	{
		return invoiceDate;
	}

	/**
	 * @param invoiceDate the invoiceDate to set
	 */
	public void setInvoiceDate(Date invoiceDate)
	{
		this.invoiceDate = invoiceDate;
	}

	/**
	 * @return the invoiceNumber
	 */
	public String getInvoiceNumber()
	{
		return invoiceNumber;
	}

	/**
	 * @param invoiceNumber the invoiceNumber to set
	 */
	public void setInvoiceNumber(String invoiceNumber)
	{
		this.invoiceNumber = invoiceNumber;
	}

	/**
	 * @return the billTo
	 */
	public String getBillTo()
	{
		return billTo;
	}

	/**
	 * @param billTo the billTo to set
	 */
	public void setBillTo(String billTo)
	{
		this.billTo = billTo;
	}

	/**
	 * @return the dueDate
	 */
	public Date getDueDate()
	{
		return dueDate;
	}

	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate)
	{
		this.dueDate = dueDate;
	}

	/**
	 * @return the poNumber
	 */
	public String getPoNumber()
	{
		return poNumber;
	}

	/**
	 * @param poNumber the poNumber to set
	 */
	public void setPoNumber(String poNumber)
	{
		this.poNumber = poNumber;
	}

	/**
	 * @return the invoiceComponents
	 */
	public TreeSet<InvoiceComponent> getInvoiceComponents()
	{
		return invoiceComponents;
	}

	/**
	 * @param invoiceComponents the invoiceComponents to set
	 */
	public void setInvoiceComponents(TreeSet<InvoiceComponent> invoiceComponents)
	{
		this.invoiceComponents = invoiceComponents;
	}

	/**
	 * @return the additionalNote
	 */
	public String getAdditionalNote()
	{
		return additionalNote;
	}

	/**
	 * @param additionalNote the additionalNote to set
	 */
	public void setAdditionalNote(String additionalNote)
	{
		this.additionalNote = additionalNote;
	}

	public double getTotalSelectionPrice()
	{
		double total = 0.0;
		for(InvoiceComponent invoiceComponent : selectedInvoiceComponents)
		{
			total += invoiceComponent.getAmount();
		}
		return total;
	}

	public TreeSet<InvoiceComponent> getSelectedInvoiceComponents()
	{
		return selectedInvoiceComponents;
	}

	public void setSelectedInvoiceComponents(TreeSet<InvoiceComponent> selectedInvoiceComponents)
	{
		this.selectedInvoiceComponents = selectedInvoiceComponents;
	}

	/**
	 * select all that has a non-zero amount
	 */
	public void makeDefaultSelections()
	{
		selectedInvoiceComponents.clear();
		for(InvoiceComponent invoiceComponent : invoiceComponents)
		{
			if(invoiceComponent.getAmount() > 0)
			{
				selectedInvoiceComponents.add(invoiceComponent);
			}
		}
	}
}
