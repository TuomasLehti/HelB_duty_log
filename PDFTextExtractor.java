package pdftextextractor;

//import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
//import pdf.PDFObjectList;

/**
 * Extracts text from a PDF file. This class is meant to be used with HelB's duty log files, and
 * it contains a lot of shortcuts. This is by no means a conforming reader. Perhaps thing are
 * some day made the right way, but as of now this works for HelB's files and that's enough
 * for now.
 
 * @author Tuomas Lehti
 * @version 2019-03-21
 *
 * TODO: getPageObjectNumbers should be tested for more compilcated page tree structures.
 * TODO: Multiple items in the page contents array should be supported.
 */
public class PDFTextExtractor {

    PDFObjectList pdf = new PDFObjectList();
    public File path;

    public void openFile(File file) {
        pdf.openFile(file);
//        Log.i("pdftesteri", "Opened file " + file.getName());
//        Log.i("pdftesteri", "Number of objects: " + pdf.numOfObjs());
    }

    public ArrayList<PDFTextItem> getText() {
        // Locate the root object.
        int rootObjNum = pdf.getTrailerObj().getDictionary().get("Root").getIndirRefObjNum();
//        Log.i("pdftesteri", "Root object number: " + rootObjNum);
        // Locate the page tree root object.
        int pagesObjNum = pdf.getObj(rootObjNum).getDictionary().get("Pages").getIndirRefObjNum();
//        Log.i("pdftesteri", "Page tree root object number: " + pagesObjNum);
        // Get a list of page object numbers.
        ArrayList<Integer> pageList = getPageObjectNumbers(pdf.getObj(pagesObjNum));
//        Log.i("pdftesteri", "Page object numbers: " + pageList.toString());
        // Loop through all pages.
        ArrayList<PDFTextItem> textItemsFromAllPages = new ArrayList<>();
        for (int i=0; i < pageList.size(); i++) {
            // Get the actual page object.
//            Log.i("pdftesteri", "Page: " + i);
            int pageObjNum = pageList.get(i);
//            Log.i("pdftesteri", "at object: " + pageObjNum);
            PDFObject pageObj = pdf.getObj(pageObjNum);
            // Get the object in which the contents is. So far all PDF files from HelB have had
            // only one item in the contents array, but this should be futureproofed to handle
            // multiple items.
            int contentsObjNum = pageObj.getDictionary().get("Contents").getArray().get(0).getIndirRefObjNum();
//            Log.i("pdftesteri", "contents at object: " + contentsObjNum);
            // Get the decoded contents.
            ArrayList<PDFTextItem> textItemsFromThisPage = getTextItemsFromAPage(contentsObjNum, i);
            textItemsFromAllPages.addAll(textItemsFromThisPage);

        }
        Collections.sort(textItemsFromAllPages);

        return textItemsFromAllPages;
    }

    /**
     * Gets the numbers of page objects in a recursive manner.
     * <p>
     * A node in a page tree might be an intermediate object (called page tree node) or a page
     * object (called leaf node). Page tree nodes contain a list of either other page tree nodes
     * or leaf nodes. This is why recursion is needed.
     * <p>
     * All PDF files produced by HelB so far have only had a single page tree node containing all
     * leaf nodes. This method is futureproofed despite that.
     * <p>
     * All PDF files produced by HelB so far haven't had duties spread over multiple pages. This
     * means that the ordering of the pages shouldn't matter. This should be looked at in the
     * future versions, however.
     * @param root The page tree object to be listed.
     * @return The list of numbers of page objects.
     */
    private ArrayList<Integer> getPageObjectNumbers(PDFObject root) {
        ArrayList<Integer> pageObjNums = new ArrayList<Integer>();
        // Kids-key is required.
        PDFObject kidsArray = root.getDictionary().get("Kids");
        int numOfPages = kidsArray.getArray().size();
        // Go through all nodes.
        for (int i=0; i < numOfPages; i++) {
            // The kids are in an array containing indirect references.
            // Get the object number of the kid.
            int kidNum = kidsArray.getArray().get(i).getIndirRefObjNum();
            // Get the actual kid object.
            PDFObject kid = pdf.getObj(kidNum);
            // The kid should be a dictionary. Its Type is either Pages for another page tree node
            // or Page for a leaf node. The case in which the kid is a page tree node isn't
            // thoroughly tested because lack of suitable testing material.
            if (kid.getDictionary().get("Type").getName().equals("Pages")) {
                pageObjNums.addAll(getPageObjectNumbers(kid));
            } else if (kid.getDictionary().get("Type").getName().equals("Page")) {
                pageObjNums.add(kidNum);
            }
        }
        return pageObjNums;
    }

    /**
     * Decodes a stream object and returns the text snippets from it. This method takes a lot
     * of shortcuts and should be cleaned in the later versions.
	 * <p>
	 * The main problem obviously is that the stream should be processed sequentially, and
	 * there are a lot of useless operators (commands), which do not relate to extracting
	 * text.
     * @param streamObjNum The number of the stream object. Only single stream object per page is
     *                     supported at this time.
     * @param pageNum The page number of the page to be decoded. This is used in the ordering of the
     *                text snippets.
     * @return The text snippets from this page as a list of PDFTextItems.
     */
    private ArrayList<PDFTextItem> getTextItemsFromAPage(int streamObjNum, int pageNum) {
		ArrayList<String> stream = getStream(streamObjNum);
        ArrayList<PDFTextItem> text = new ArrayList<>();

		String[] parts = null;
		for (int row=0; row < stream.size(); row++) {
			if (stream.get(row).equals("BT")) {
				PDFTextItem textItem = new PDFTextItem();
				textItem.page = pageNum;

				// get the dimensions of the preceeding bounding box by backing
				// up the stream, which should be read sequentially according
				// to the pdf specification
				parts = stream.get(row-10).split(" ");
				textItem.topLeftX = Float.valueOf(parts[0]);
				textItem.bottomRightY = Float.valueOf(parts[1]) * -1;
				parts = stream.get(row-8).split(" ");
				textItem.bottomRightX = Float.valueOf(parts[0]);
				textItem.topLeftY = Float.valueOf(parts[1]) * -1;
				
				// Get the positioning string. In real life this is a matrix,
				// and it should be handled with the appropriate math, but
				// this implementation just takes two numbers from a string.
				// The Y-coordinate is flipped, because in pdf the origin of
				// the coordinate system is in the lower left corner, and us
				// humans in the western world at least read from top to bottom.
				parts = stream.get(row+1).split(" ");
				textItem.x = Float.valueOf(parts[4]);
				textItem.y = Float.valueOf(parts[5]) * -1;
				
				// Read the string and remove parentheses
				String stringStr = stream.get(row+2);
				stringStr = stringStr.replace("\\(", "(");
				stringStr = stringStr.replace("\\)", ")");
				textItem.text = stringStr.substring(1, stringStr.length()-4);
				text.add(textItem);
			}
		}
        return text;
    }

	/**
	 * Returns a stream as an array of strings.
	 * <p>
	 * Every PDF file produced by HelB so far has had each operator on its own
	 * line. 
	 * @param streamObjNum 	The number of the stream object.
	 * @return 				The array of strings.
	 */
	private ArrayList<String> getStream(int streamObjNum) {
        // Get the decoded contents of the stream.
        PDFObject streamObj = pdf.getObj(streamObjNum);
        PDFByteArray decodedContents = streamObj.getDecodedStream();
        // Split the byte array into strings.
		ArrayList<String> strs = new ArrayList<String>();
		while (decodedContents.getPosition() < decodedContents.getLength()) {
			strs.add(decodedContents.getString(PDFByteArray.WHITESPACE, PDFByteArray.ENDOFLINE));
		}
		return strs;
	}

	/**
	 * Writes the text items to a csv-file. If a file already exists, it will
	 * be overwritten.
	 * @param file 		The file.
	 */
	public void writeTextItemsToFile(File file) {
		writeTextItemsToFile(getText(), file);
	}

    private void writeTextItemsToFile(ArrayList<PDFTextItem> items, File file) {
        try {
            FileWriter fw = new FileWriter(file);
            for (PDFTextItem item : items) {
                fw.write(item.toString() + "\n");
//                Log.i("pdftesteri", item.toString());
            }
            fw.close();
        } catch (Exception e) {
//            Log.i("pdftesteri", "Output error.");
        }

    }

}

/*
 * CHANGELOG
 *
 * version 2019-03-21
 * - getTextItemFromAPage now reads the bounding box of a text. This was
 *   motivated by HelB changing the format of it's duty log files. When
 *   investigation the new format it was found out, that the y-coordinates of
 *   the bounding boxes were the same on each text line, and the y-coordinates 
 *   of the text were all over the place.
 *
 * version 0.2 (2017-12-12)
 * - Escaped parentheses are now replaced with normal parentheses.
 *
 * version 0.1
 */
