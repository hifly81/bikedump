package org.hifly.bikedump.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PdfReport {
    private PDDocument document;

    public PdfReport(String text) throws Exception {
        //remove html tags
        text = text.replaceAll("\\<.*?>","");
        text = text.replaceAll("&nbsp;","");
        text = text.replaceAll("\\%\\%\\%","");

        document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        PDFont pdfFont = PDType1Font.HELVETICA;
        float fontSize = 20;

        PDRectangle mediabox = page.getArtBox();
        float margin = 72;

        float startX = mediabox.getLowerLeftX() + margin;
        float startY = mediabox.getUpperRightY() - margin;

        float initialStartY = startY;

        String [] lines = text.split("\\$\\$\\$");


        for(String line:lines) {
            if(startY<0) {
                contentStream.close();
                //new page
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                startY = initialStartY;
            }
            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            contentStream.moveTextPositionByAmount(startX, startY);
            contentStream.drawString(line);
            contentStream.endText();
            startY-=25;
        }

        contentStream.close();

    }

    public void saveReport(String fileName) throws Exception {
        document.save(fileName);
        document.close();
    }

}
