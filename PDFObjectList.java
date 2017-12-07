package tuomaan.pdftest;

import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;

/**
 * This class handles the file structure of a PDF file. Doesn't know how to deal with
 * incremental updates, so only linearized PDF files yield errorless results.
 *
 * A file can be read by giving a file name or a File object. The objects can be accessed through
 * getObject-method. The trailer dictionary is also available via getTrailerObj.
 *
 * TODO: Error handling.
 * TODO: Documentation.
 *
 * @author Tuomas Lehti
 * @version 0.1
 */

public class PDFObjectList {

    private PDFByteArray pdfArray = new PDFByteArray();

    private PDFObject pdfObj = new PDFObject();

    private int xrefPosition;
    private PDFObject trailerObj = new PDFObject();
    private int numOfObjects;
    private int[] objectPositions;
    private ArrayList<PDFObject> objects = new ArrayList<>();

    public void openFile(File file) {
        pdfArray.openFile(file);
        readXRefPosition();
        readTrailerObj();
        readObjectPositions();
        readObjects();
    }

    public void openFile(String filename) {
        openFile(new File(filename));
    }

    PDFObject getObj(int index) {
        return objects.get(index);
    }

    PDFObject getTrailerObj() {
        return trailerObj;
    }

    int numOfObjs() {
        return numOfObjects;
    }

    private void readXRefPosition() {
        pdfArray.setPosition(0);
        pdfArray.searchString("startxref");
        pdfArray.setPosition(pdfArray.getPosition() + "startxref".length());
        String startXRefStr = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE);
        try {
            xrefPosition = Integer.parseInt(startXRefStr);
        } catch (NumberFormatException e) {
            xrefPosition = -1;
        }
    }

    private void readTrailerObj() {
        pdfArray.setPosition(0);
        pdfArray.searchString("trailer");
        pdfArray.setPosition(pdfArray.getPosition()+"trailer".length());
        trailerObj.readFromByteArray(pdfArray);
    }

    private void readObjectPositions() {
        numOfObjects = trailerObj.getDictionary().get("Size").getInteger();
        objectPositions = new int[numOfObjects];
        pdfArray.setPosition(xrefPosition + "xref".length());
        // The line with the number of objects
        String aLine = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.ENDOFLINE);
        for (int i=0; i < numOfObjects; i++) {
            aLine = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.ENDOFLINE);
            objectPositions[i] = Integer.parseInt(aLine.substring(0, 10));
        }
    }

    private void readObjects() {
        // Add a dummy object for the 0th object. Otherwise the object numbers of PDF-file and
        // indexes of this list are off by one.
        objects.add(new PDFObject());
        for (int i=1; i < numOfObjects; i++) {
            PDFObject obj = new PDFObject();
            pdfArray.setPosition(objectPositions[i]);
            obj.readFromByteArray(pdfArray);
            objects.add(obj);
        }
    }

}