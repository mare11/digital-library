package org.udd.digitallibrary.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.DateTools;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.udd.digitallibrary.model.IndexUnit;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Slf4j
public class PDFHandler implements DocumentHandler {

    @Override
    public IndexUnit getIndexUnit(File file) {
        IndexUnit retVal = new IndexUnit();
        try {
            PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
            parser.parse();

            String text = getText(parser);
            retVal.setText(text);

            // metadata extraction
            PDDocument pdf = parser.getPDDocument();
            PDDocumentInformation info = pdf.getDocumentInformation();

            retVal.setTitle(info.getTitle());
            retVal.setKeywords(info.getKeywords());
            retVal.setFilename(file.getCanonicalPath());

            String modificationDate = DateTools.dateToString(new Date(file.lastModified()), DateTools.Resolution.DAY);
            retVal.setFileDate(modificationDate);

            pdf.close();
        } catch (IOException e) {
            log.error("Greksa pri konvertovanju dokumenta u pdf");
        }

        return retVal;
    }

    public String getText(PDFParser parser) {
        try {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(parser.getPDDocument());
        } catch (IOException e) {
            log.error("Greksa pri konvertovanju dokumenta u pdf");
        }
        return null;
    }

    @Override
    public String getText(File file) {
        try {
            PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
            parser.parse();
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(parser.getPDDocument());
        } catch (IOException e) {
            log.error("Greksa pri konvertovanju dokumenta u pdf");
        }
        return null;
    }

}
