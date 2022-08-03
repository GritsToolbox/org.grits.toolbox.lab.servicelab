/**
 * 
 */
package org.grits.toolbox.lab.servicelab.invoice;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableCell.XWPFVertAlign;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.grits.toolbox.lab.servicelab.config.IConfig;
import org.grits.toolbox.lab.servicelab.model.InvoiceComponent;
import org.grits.toolbox.lab.servicelab.model.ServiceLabInvoice;
import org.grits.toolbox.lab.servicelab.wizard.invoice.provider.InvoiceComponentContentProvider;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

/**
 * 
 *
 */
public class MSWordInvoiceGenerator
{
	private static Logger logger = Logger.getLogger(MSWordInvoiceGenerator.class);

	public static final String DEFAULT_FONT = "Arial";
	public static final String INVOICE_FILL_FONT = "Courier New";
	public static final int INVOICE_FILL_FONT_SIZE = 11;

	private static DateFormat dateFormat = new SimpleDateFormat(IConfig.DATE_FORMAT);

	public static XWPFDocument getInvoiceDocument(ServiceLabInvoice serviceLabInvoice)
	{
		logger.info("Generating MS Word document Bill for ");
		logger.info("Invoice number : " + serviceLabInvoice.getInvoiceNumber());
		logger.info("Invoice Date : " + serviceLabInvoice.getInvoiceDate());
		logger.info("P.O. Number : " + serviceLabInvoice.getPoNumber());

		// creating an empty word document for the invoice
		XWPFDocument document = new XWPFDocument();
		try
		{
			createInvoice(document, serviceLabInvoice);
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return document;
	}

	private static XWPFDocument createInvoice(XWPFDocument document,
			ServiceLabInvoice serviceLabInvoice)
	{
		logger.info("Adding invoice template with invoice information");
		logger.info("Default Font : " + DEFAULT_FONT);
		logger.info("Invoice Filling Font : " + INVOICE_FILL_FONT);
		logger.info("Invoice Filling Size : " + INVOICE_FILL_FONT_SIZE);

		// first row layout contains uga address and invoice info
		createFirstRowLayout(document, serviceLabInvoice.getInvoiceDate(),
				serviceLabInvoice.getInvoiceNumber());

		// second row layout contains bill to address and due date info
		createSecondRowLayout(document, serviceLabInvoice.getBillTo(),
				serviceLabInvoice.getDueDate(), serviceLabInvoice.getPoNumber());
		// add a break for spacing
		document.createParagraph().createRun();

		// create pricing table
		createPricingTable(document, serviceLabInvoice.getSelectedInvoiceComponents(),
				serviceLabInvoice.getAdditionalNote());

		// create last row with check information and total price
		createLastRowLayout(document, serviceLabInvoice.getTotalSelectionPrice());

		logger.info("document with invoice information created");
		return document;
	}

	private static void createPricingTable(XWPFDocument document,
			Set<InvoiceComponent> invoiceComponents, String additionalNote)
	{
		XWPFTable pricingTable = document.createTable(2, 4);
		pricingTable.setCellMargins(40, 40, 40, 40);
		// make table fixed width
		pricingTable.getCTTbl().getTblPr().addNewTblLayout().setType(STTblLayoutType.FIXED);

		pricingTable.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf(5700));
		pricingTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf(900));
		pricingTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf(1150));
		pricingTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf(1600));
		pricingTable.getCTTbl().getTblPr().addNewJc().setVal(STJc.LEFT);
		pricingTable.getCTTbl().getTblPr().addNewTblW().setW(BigInteger.valueOf(9350));
		pricingTable.getCTTbl().getTblPr().getTblW().setType(STTblWidth.PCT);

		createHeaderCell("DESCRIPTION", pricingTable.getRow(0).getCell(0), 5700);
		createHeaderCell("QTY", pricingTable.getRow(0).getCell(1), 900);
		createHeaderCell("RATE", pricingTable.getRow(0).getCell(2), 1150);
		createHeaderCell("AMOUNT", pricingTable.getRow(0).getCell(3), 1600);
		squeezeRow(pricingTable.getRow(0), 300);

		XWPFTableRow pricingTableRow1 = pricingTable.getRow(1);
		XWPFTableCell pricingTableSecondRowCell0 = pricingTableRow1.getCell(0);
		XWPFTableCell pricingTableSecondRowCell1 = pricingTableRow1.getCell(1);
		XWPFTableCell pricingTableSecondRowCell2 = pricingTableRow1.getCell(2);
		XWPFTableCell pricingTableSecondRowCell3 = pricingTableRow1.getCell(3);

		XWPFParagraph paragraph0 = pricingTableSecondRowCell0.addParagraph();
		pricingTableSecondRowCell0.removeParagraph(0);
		paragraph0.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun cell0Run = createFillingRun(paragraph0);

		XWPFParagraph paragraph1 = pricingTableSecondRowCell1.addParagraph();
		pricingTableSecondRowCell1.removeParagraph(0);
		paragraph1.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun cell1Run = createFillingRun(paragraph1);

		XWPFParagraph paragraph2 = pricingTableSecondRowCell2.addParagraph();
		pricingTableSecondRowCell2.removeParagraph(0);
		paragraph2.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun cell2Run = createFillingRun(paragraph2);

		XWPFParagraph paragraph3 = pricingTableSecondRowCell3.addParagraph();
		pricingTableSecondRowCell3.removeParagraph(0);
		paragraph3.setAlignment(ParagraphAlignment.RIGHT);
		XWPFRun cell3Run = createFillingRun(paragraph3);

		int lineCount = 0;
		String rate = "";
		String csvSampleNames = "";
		for(InvoiceComponent invoiceComponent : invoiceComponents)
		{
			cell0Run.setText(invoiceComponent.getProtocolName());
			cell0Run.addBreak();
			csvSampleNames = InvoiceComponentContentProvider.getCSVSampleNames(invoiceComponent);
			cell0Run.setText(csvSampleNames);
			cell0Run.addBreak();

			cell1Run.setText(invoiceComponent.getQuantity() + "");
			cell1Run.addBreak();

			rate = invoiceComponent.getRate() == 0
					? "-" : invoiceComponent.getRate() + "";
			cell2Run.setText(rate);
			cell2Run.addBreak();

			cell3Run.setText(String.format("%.2f", invoiceComponent.getAmount()));
			cell3Run.addBreak();

			lineCount = estimateNumberOfLines(csvSampleNames, 42);
			logger.info(lineCount);
			for(int numOfBreaks = 1
					// add breaks for sample names to cell1, cell2 and cell3
					; numOfBreaks <= lineCount; numOfBreaks++)
			{
				cell1Run.addBreak();
				cell2Run.addBreak();
				cell3Run.addBreak();
			}
			cell0Run.addBreak();
			cell1Run.addBreak();
			cell2Run.addBreak();
			cell3Run.addBreak();
		}

		// add additional notes in first column
		cell0Run.addBreak(); cell0Run.addBreak(); cell0Run.addBreak();
		setMultilineTextToRun(cell0Run, additionalNote);
		pricingTable.getRow(1).setHeight(6500);
	}

	private static void createLastRowLayout(XWPFDocument document, double totalAmount)
	{
		// creating a bottom table for placing check info, total and EIN
		XWPFTable bottomTable = document.createTable(3, 2);
		bottomTable.setCellMargins(80, 80, 40, 80);
		bottomTable.getCTTbl().getTblPr().addNewJc().setVal(STJc.LEFT);
		bottomTable.getCTTbl().getTblPr().getTblW().setType(STTblWidth.PCT);
		bottomTable.getCTTbl().addNewTblGrid().addNewGridCol().setW(BigInteger.valueOf(5700));
		bottomTable.getCTTbl().getTblGrid().addNewGridCol().setW(BigInteger.valueOf(3650));

		// set width for left column
		XWPFTableCell bottomTableLeftCell = bottomTable.getRow(0).getCell(0);
		bottomTableLeftCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5700));

		//merge left column cells vertically
		bottomTableLeftCell.getCTTc().getTcPr().addNewVMerge().setVal(STMerge.RESTART);
		bottomTable.getRow(1).getCell(0).getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
		bottomTable.getRow(2).getCell(0).getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);

		// add check info
		XWPFParagraph paragraph = bottomTable.getRow(0).getCell(0).addParagraph();
		bottomTableLeftCell.removeParagraph(0);
		paragraph.setVerticalAlignment(TextAlignment.CENTER);
		XWPFRun run = null;
		for(String line : ServiceLabInvoice.CHECK_INFO)
		{
			run = paragraph.createRun();
			run.setFontFamily(DEFAULT_FONT);
			run.setFontSize(8);
			run.setText(line);
			run.addBreak();
		}

		// set appropriate heights for three rows
		squeezeRow(bottomTable.getRow(0), 600);
		squeezeRow(bottomTable.getRow(1), 350);
		squeezeRow(bottomTable.getRow(2), 200);

		XWPFTableCell bottomTableRightCellFirstRow = bottomTable.getRow(0).getCell(1);
		bottomTableRightCellFirstRow.getCTTc().addNewTcPr().addNewVAlign().setVal(STVerticalJc.CENTER);
		// add text "Total" to first row
		paragraph = bottomTableRightCellFirstRow.addParagraph();
		bottomTableRightCellFirstRow.removeParagraph(0);
		run = paragraph.createRun();
		run.setFontSize(15);
		run.setFontFamily(DEFAULT_FONT);
		run.setBold(true);
		run.setText("Total"); 
		// add 3 tabs as separator
		run.addTab(); run.addTab(); run.addTab();
		paragraph.setAlignment(ParagraphAlignment.LEFT);

		// add the invoice amount
		createFillingRun(paragraph).setText("  " + String.format("%.2f", totalAmount));

		// set second row of right column for EIN
		XWPFTableCell bottomTableRightCellSecondRow = bottomTable.getRow(1).getCell(1);
		bottomTableRightCellSecondRow.getCTTc().addNewTcPr().addNewVAlign().setVal(STVerticalJc.TOP);
		// add EIN paragraph
		paragraph = bottomTableRightCellSecondRow.addParagraph();
		bottomTableRightCellSecondRow.removeParagraph(0);
		run = paragraph.createRun();
		run.setFontSize(11);
		run.setFontFamily(DEFAULT_FONT);
		run.setBold(true);
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run.setText("EIN #58-6001998");
	}

	private static void createFirstRowLayout(XWPFDocument document, Date invoiceDate, String invoiceNumber)
	{
		// creating a layout table for placing objects in side by side columns 
		XWPFTable layoutTable = document.createTable(1, 2);
		layoutTable.setCellMargins(40, 40, 40, 40);
		// make layout table invisible
		layoutTable.getCTTbl().getTblPr().unsetTblBorders();

		// 'layoutTableLeftCell' displays ccrc address
		XWPFTableCell layoutTableLeftCell = layoutTable.getRow(0).getCell(0);
		layoutTableLeftCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(7000));
		XWPFParagraph paragraph = layoutTableLeftCell.addParagraph();
		layoutTableLeftCell.removeParagraph(0);
		XWPFRun run = paragraph.createRun();
		run.setFontSize(12);
		run.setFontFamily(DEFAULT_FONT);
		for(int index = 0; index < ServiceLabInvoice.CCRC_ADDRESS.length ; index++)
		{
			run.setText(ServiceLabInvoice.CCRC_ADDRESS[index]);

			// do not add break on last line
			if(index != ServiceLabInvoice.CCRC_ADDRESS.length - 1)
				run.addBreak();
		}

		// set alignment, width etc. for 'layoutTableRightCell'
		XWPFTableCell layoutTableRightCell = layoutTable.getRow(0).getCell(1);
		layoutTableRightCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(3000));
		layoutTableRightCell.setVerticalAlignment(XWPFVertAlign.TOP);

		// create another table 'layoutTableRightCellInsideTable' inside
		// this cell of 'layoutTableRightCell'
		// layoutTableRightCell contains another layout table with two rows 
		// first row displaying title "Invoice" and
		// second row containing table for invoice date and number
		XWPFTable layoutTableRightCellInsideTable = new XWPFTable(
				layoutTableRightCell.insertNewTbl(
						layoutTableRightCell.getCTTc().addNewTcPr().newCursor()).getCTTbl(),
				layoutTableRightCell, 2, 1);
		layoutTableRightCellInsideTable.getCTTbl().addNewTblPr().addNewJc().setVal(STJc.RIGHT);
		layoutTableRightCellInsideTable.setCellMargins(40, 40, 40, 40);
		// make this inside layout table invisible
		layoutTableRightCellInsideTable.getCTTbl().getTblPr().unsetTblBorders();

		// first row of 'layoutTableRightCellInsideTable' contains the heading text "Invoice"
		XWPFTableCell layoutTableRightCellFirstRow = layoutTableRightCellInsideTable.getRow(0).getCell(0);
		layoutTableRightCellFirstRow.getCTTc().addNewTcPr().addNewVAlign().setVal(STVerticalJc.BOTTOM);
		// add a paragraph containing text
		paragraph = layoutTableRightCellFirstRow.addParagraph();
		layoutTableRightCellFirstRow.removeParagraph(0);
		run = paragraph.createRun();
		run.setFontSize(20);
		run.setFontFamily(DEFAULT_FONT);
		run.setBold(true);
		run.setText("Invoice");
		paragraph.setAlignment(ParagraphAlignment.RIGHT);
		paragraph.setVerticalAlignment(TextAlignment.BOTTOM);
		paragraph.getCTP().addNewPPr().addNewSpacing().setAfter(BigInteger.ZERO);

		// second row of 'layoutTableRightCellInsideTable' calling it 'layoutTableRightCellSecondRow'
		// contains the table for invoice number and date
		XWPFTableCell layoutTableRightCellSecondRow = layoutTableRightCellInsideTable.getRow(1).getCell(0);
		layoutTableRightCellSecondRow.getCTTc().addNewTcPr().addNewVAlign().setVal(STVerticalJc.TOP);
		layoutTableRightCellInsideTable.getRow(1).setHeight(800);

		// add 'dateInvoiceTable' to this second row 'layoutTableRightCellSecondRow'
		XWPFTable dateInvoiceTable = new XWPFTable(
				layoutTableRightCellSecondRow.insertNewTbl(
						layoutTableRightCellSecondRow.getCTTc().addNewTcPr().newCursor()).getCTTbl(),
				layoutTableRightCellSecondRow, 2, 2);
		dateInvoiceTable.setCellMargins(40, 40, 40, 40);
		squeezeRow(dateInvoiceTable.getRow(0), 300);
		squeezeRow(dateInvoiceTable.getRow(1), 300);
		dateInvoiceTable.getCTTbl().getTblPr().addNewJc().setVal(STJc.RIGHT);
		dateInvoiceTable.getCTTbl().getTblPr().addNewTblW().setW(BigInteger.valueOf(3200));
		createHeaderCell("DATE", dateInvoiceTable.getRow(0).getCell(0), 1400);
		createHeaderCell("INVOICE#", dateInvoiceTable.getRow(0).getCell(1), 1800);

		fillCell(dateInvoiceTable.getRow(1).getCell(0), invoiceDate);
		fillCell(dateInvoiceTable.getRow(1).getCell(1), invoiceNumber);
	}

	private static void createSecondRowLayout(XWPFDocument document, String billTo,
			Date dueDate, String poNumber)
	{
		// creating a layout table for placing objects in side by side columns 
		XWPFTable layoutTable = document.createTable(1, 2);
		layoutTable.setCellMargins(40, 400, 40, 40);
		layoutTable.getRow(0).setHeight(2200);
		// make layout table invisible
		layoutTable.getCTTbl().getTblPr().unsetTblBorders();

		// 'layoutTableLeftCell' displays bill to table
		XWPFTableCell layoutTableLeftCell = layoutTable.getRow(0).getCell(0);
//		layoutTableLeftCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5500));
		layoutTableLeftCell.setVerticalAlignment(XWPFVertAlign.TOP);

		// add 'billToTable' to left side cell 'layoutTableLeftCell'
		XWPFTable billToTable = new XWPFTable(
				layoutTableLeftCell.insertNewTbl(
						layoutTableLeftCell.getCTTc().addNewTcPr().newCursor()).getCTTbl(),
				layoutTableLeftCell, 2, 1);
		billToTable.setCellMargins(40, 40, 40, 40);
		billToTable.getRow(1).setHeight(1300);
		billToTable.getCTTbl().addNewTblPr().addNewJc().setVal(STJc.LEFT);
		billToTable.getCTTbl().getTblPr().addNewTblW().setW(BigInteger.valueOf(4500));
		createHeaderCell("BILL TO", billToTable.getRow(0).getCell(0), 4500);
		squeezeRow(billToTable.getRow(0), 300);

		fillCell(billToTable.getRow(1).getCell(0), billTo);

		// 'layoutTableRightCell' displays due date and P.O. number
		XWPFTableCell layoutTableRightCell = layoutTable.getRow(0).getCell(1);
//		layoutTableRightCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(4600));
		layoutTableRightCell.setVerticalAlignment(XWPFVertAlign.BOTTOM);
		// add 'poDueDateTable' to right side cell 'layoutTableRightCell'
		XWPFTable poDueDateTable = new XWPFTable(
				layoutTableRightCell.insertNewTbl(
						layoutTableRightCell.getCTTc().addNewTcPr().newCursor()).getCTTbl(),
				layoutTableRightCell, 2, 2);
		poDueDateTable.setCellMargins(40, 40, 40, 40);
		poDueDateTable.getCTTbl().getTblPr().addNewJc().setVal(STJc.RIGHT);
		poDueDateTable.getCTTbl().getTblPr().addNewTblW().setW(BigInteger.valueOf(3500));
		createHeaderCell("DUE DATE", poDueDateTable.getRow(0).getCell(0), 1400);
		createHeaderCell("P.O. NUMBER", poDueDateTable.getRow(0).getCell(1), 2100);
		squeezeRow(poDueDateTable.getRow(0), 300);
		squeezeRow(poDueDateTable.getRow(1), 300);

		fillCell(poDueDateTable.getRow(1).getCell(0), dueDate);
		fillCell(poDueDateTable.getRow(1).getCell(1), poNumber);
	}

	private static XWPFTableCell createHeaderCell(String headerValue, XWPFTableCell cell, int width)
	{
		cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(width));
		cell.setColor("C8C8C8");

		XWPFParagraph paragraph = cell.addParagraph();
		cell.removeParagraph(0);

		paragraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = paragraph.createRun();
		run.setFontFamily(DEFAULT_FONT);
		run.setText(headerValue);
		return cell;
	}

	private static void fillCell(XWPFTableCell cell, Date date)
	{
		String dateString = date == null ? null : dateFormat.format(date);
		fillCell(cell, dateString);
	}

	private static void fillCell(XWPFTableCell cell, String value)
	{
		XWPFParagraph paragraph = cell.addParagraph();
		cell.removeParagraph(0);

		paragraph.setAlignment(ParagraphAlignment.LEFT);
		paragraph.setWordWrapped(true);
		XWPFRun run = createFillingRun(paragraph);

		setMultilineTextToRun(run, value);
	}

	private static XWPFRun createFillingRun(XWPFParagraph paragraph)
	{
		XWPFRun fillingRun = paragraph.createRun();
		fillingRun.setFontFamily(INVOICE_FILL_FONT);
		fillingRun.setFontSize(INVOICE_FILL_FONT_SIZE);
		return fillingRun;
	}

	private static void setMultilineTextToRun(XWPFRun run, String value)
	{
		if(value == null)
		{
			run.setText(null);
			return;
		}

		String[] lines = value.split("\n");
		for(int i = 0; i < lines.length ;i++)
		{
			run.setText(lines[i]);
			if(i < lines.length - 1)
			{
				run.addBreak();
			}
		}
	}

	private static void squeezeRow(XWPFTableRow row, int height)
	{
		// squeezes the row to this height irrespective of fonts used
		CTHeight trHeight = row.getCtRow().addNewTrPr().addNewTrHeight();
		trHeight.setHRule(STHeightRule.EXACT);
		trHeight.setVal(BigInteger.valueOf(height));
	}

	private static int estimateNumberOfLines(String fullText, int wordsPerline)
	{
		if(fullText == null || wordsPerline < 0)
		{
			return 0;
		}

		int lineCount = 1; // default line count is 1

		String[] allWords = fullText.split(" ");
		int index = 0;
		String nextWord = "";
		int wordLength = 0;

		int thisLineWordLength = 0; // length of total words in current line

		while(index < allWords.length)
		{
			nextWord = allWords[index];
			wordLength = nextWord.length() + 1;

			// last word does not have any space
			if(index == allWords.length - 1)
			{
				wordLength--;
			}

			// start with new line if next word can't fit
			if(thisLineWordLength + nextWord.length() > wordsPerline)
			{
				lineCount++;
				thisLineWordLength = 0;
			}

			thisLineWordLength += wordLength;
			index++;
		}

		return lineCount;
	}
}
