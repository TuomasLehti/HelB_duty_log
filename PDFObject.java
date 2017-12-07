package tuomaan.pdftest;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.Inflater;

/**
 * Reads an object from a PDF file. This version is meant to read objects from HelB's duty log
 * files, and all relevant object (data) types are included.
 *
 * An object is read by readFromByteArray-method, and the contents of the object can be
 * fetched through various getXxx-methods depending on the object type.
 *
 * TODO: Lots of error correction.
 * TODO: Include object types, which are not needed by HelB's duty logs.
 * TODO: Should we throw exceptions, if the object is not the type which is asked for?
 * TODO: Lots of documentation.
 *
 * @author Tuomas Lehti
 * @version 0.1
 */
public class PDFObject {

    public static final int OT_NULL = 0;
    public static final int OT_BOOLEAN = 1;
    public static final int OT_INTEGER = 2;
    public static final int OT_REAL = 3;
    public static final int OT_STRING = 4;
    public static final int OT_NAME = 5;
    public static final int OT_DICTIONARY = 6;
    public static final int OT_ARRAY = 7;
    public static final int OT_INDIRECT_REFERENCE = 8;

    private int objType = OT_NULL;

    private int objNumber = -1;
    private int objGeneration = 0;

    private boolean booleanValue;
    private String stringValue;
    private Integer integerValue;
    private Float realValue;
    private ArrayList<PDFObject> anArray;
    private HashMap<String, PDFObject> aDictionary;
    private PDFByteArray stream = null;

    /**
     * Returns the type of the object. Different types are represented by integer constants (OT_XXX).
     * @return The type of the object.
     */
    public int getObjType() {
        return objType;
    }

    private void setObjType(int oType) { objType = oType; }

    /**
     * If this object is an indirect object, returns the number of the object.
     * @return The number of the object.
     */
    public int getObjNumber() {
        return objNumber;
    }

    /**
     * If this object is an indirect object, returns the generation number of the object.
     * @return The generation number of the object.
     */
    public int getObjGeneration() {
        return objGeneration;
    }

    /**
     * Reads an object from a byte array.
     * @param pdfArray The array from which to read the object.
     * @param start The position from which to read the object.
     */
    public void readFromByteArray(PDFByteArray pdfArray, int start) {
//        Log.i("pdftesteri", "readFromByteArray(" + start + ")");
        pdfArray.setPosition(start);

        // Read object and generation number, if this is an indirect object.
        if (isIndirectObject(pdfArray)) {
            readIndirectObject(pdfArray);
        }

        // Read the actual object. Indirect reference consists of two integers and the letter R,
        // which is why it is checked before Integer. Real must also be checked be before integer,
        // because the only difference between them is the dot, which is checked by real.
        // The order of other type checks doesn't matter.
        if (isBoolean(pdfArray)) {
            readBoolean(pdfArray);
        } else if (isIndirectReference(pdfArray)) {
            readIndirectReference(pdfArray);
        } else if (isDictionary(pdfArray)) {
            readDictionary(pdfArray);
        } else if (isReal(pdfArray)) {
            readReal(pdfArray);
        } else if (isInteger(pdfArray)) {
            readInteger(pdfArray);
        } else if (isName(pdfArray)) {
            readName(pdfArray);
        } else if (isArray(pdfArray)) {
            readArray(pdfArray);
        } else {
            setObjType(OT_NULL);
        }

        // Read the stream attached to this object, if a stream exists.
        if (hasStream(pdfArray)) {
            readStream(pdfArray);
        }

        // Skip past the endobj-keyword of an indirect object, if necessary.
        if (hasEndobj(pdfArray)) {
            skipEndobj(pdfArray);
        }

    }

    /**
     * Reads an object from a byte array, starting from the current position in the array.
     * Moves the current position to the first character after the object.
     * @param pdfArray The array from which to read the object.
     */
    public void readFromByteArray(PDFByteArray pdfArray) {
        readFromByteArray(pdfArray, pdfArray.getPosition());
    }


    public boolean getBoolean() {
        if (getObjType() == OT_BOOLEAN) {
            return booleanValue;
        } else {
            return false;
        }
    }

    public String getString() {
        if (getObjType() == OT_STRING) {
            return stringValue;
        } else {
            return "";
        }
    }

    public String getIndirectReference() {
        if (getObjType() == OT_INDIRECT_REFERENCE) {
            return stringValue;
        } else {
            return "";
        }
    }

    public int getIndirRefObjNum() {
        if (getObjType() == OT_INDIRECT_REFERENCE) {
            return Integer.parseInt(stringValue.substring(0, stringValue.indexOf(" ")));
        } else {
            return -1;
        }

    }

    public Integer getInteger() {
        if (getObjType() == OT_INTEGER) {
            return integerValue;
        } else {
            return new Integer(0);
        }
    }

    public Float getReal() {
        if (getObjType() == OT_REAL) {
            return realValue;
        } else {
            return new Float(0);
        }
    }

    public String getName() {
        if (getObjType() == OT_NAME) {
            return stringValue;
        } else {
            return "";
        }
    }

    public ArrayList<PDFObject> getArray() {
        if (getObjType() == OT_ARRAY) {
            return anArray;
        } else {
            return null;
        }
    }

    public HashMap<String, PDFObject> getDictionary() {
        if (getObjType() == OT_DICTIONARY) {
            return aDictionary;
        } else {
            return null;
        }
    }

    public PDFByteArray getStream() {
        if (!(stream == null)) {
            return stream;
        } else {
            return null;
        }
    }

    public PDFByteArray getDecodedStream() {
        if (!(stream == null)) {
            byte[] encodedData = stream.getBytes(0, stream.getLength());
            Inflater decompresser = new Inflater();
            decompresser.setInput(encodedData);
            byte[] decodedBuffer = new byte[65536];
            int decodedLength = 0;
            try {
                decodedLength = decompresser.inflate(decodedBuffer);
            } catch (Exception e) {
                System.out.println("decodeStream exception");
            }
            byte[] decodedData = new byte[decodedLength-1];
            try {
                decodedData = Arrays.copyOfRange(decodedBuffer, 0, decodedLength-1);
            } catch (Exception e) {
                System.out.println("decodeStream exception");
            }
            PDFByteArray result = new PDFByteArray();
            result.setBytes(decodedData);
            return result;
        } else {
            return null;
        }
    }

    private boolean isBoolean(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isBoolean(" + pdfArray.getPosition() + ")");
        int start = pdfArray.searchNotChar(pdfArray.getPosition(), pdfArray.WHITESPACE);
        String s = pdfArray.getString(pdfArray.getPosition(), pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);

        return (s.equals("true") || s.equals("false"));
    }

    private void readBoolean(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readBoolean(" + pdfArray.getPosition() + ")");
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        String s = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        if (s.equals("true")) {
            booleanValue = true;
        } else if (s.equals("false")) {
            booleanValue = false;
        }
        setObjType(OT_BOOLEAN);
    }

    private boolean isIndirectReference(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isIndirectReference(" + pdfArray.getPosition() + ")");
        int originalPosition = pdfArray.getPosition();
        String objNumber = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE);
        String objGeneration = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE);
        String r = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        pdfArray.setPosition(originalPosition);
        return r.equals("R");
    }

    private void readIndirectReference(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readIndirectReference(" + pdfArray.getPosition() + ")");
        stringValue =
                pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE) + " " +
                        pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE) + " " +
                        pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        setObjType(OT_INDIRECT_REFERENCE);
    }

    private boolean isIndirectObject(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isIndirectObject(" + pdfArray.getPosition() + ")");
        int originalPosition = pdfArray.getPosition();
        String objNumber = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE);
        String objGeneration = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE);
        String r = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        pdfArray.setPosition(originalPosition);
        return r.equals("obj");
    }

    private void readIndirectObject(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readIndirectObject(" + pdfArray.getPosition() + ")");
        readInteger(pdfArray);
        objNumber = getInteger();
        readInteger(pdfArray);
        objGeneration = getInteger();
        setObjType(OT_NULL);
        pdfArray.setPosition(pdfArray.getPosition()+4);
    }

    private boolean isInteger(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isInteger(" + pdfArray.getPosition() + ")");
        String numChars = new String("-+0123456789");
        String s = pdfArray.getString(pdfArray.getPosition(), pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        return !s.isEmpty() && numChars.indexOf(s.charAt(0)) > -1 && (s.indexOf(".") == -1);
    }

    private void readInteger(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readInteger(" + pdfArray.getPosition() + ")");
        String s = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        try {
            integerValue = new Integer(s);
        } catch (NumberFormatException e) {
            integerValue = new Integer(0);
        }
        setObjType(OT_INTEGER);
    }

    private boolean isReal(PDFByteArray pdfArray) {
        String numChars = new String("-+.0123456789");
        String s = pdfArray.getString(pdfArray.getPosition(), pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        return !s.isEmpty() && numChars.indexOf(s.charAt(0)) > -1 && (s.indexOf(".") > -1);
    }

    private void readReal(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readReal(" + pdfArray.getPosition() + ")");
        String s = pdfArray.getString(pdfArray.WHITESPACE, pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        try {
            realValue = new Float(s);
        } catch (NumberFormatException e) {
            realValue = new Float(0);
        }
        setObjType(OT_REAL);
    }


    private boolean isName(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isName(" + pdfArray.getPosition() + ")");
        int start = pdfArray.searchNotChar(pdfArray.getPosition(), pdfArray.WHITESPACE);
        return pdfArray.isChar(start, "/");
    }

    private void readName(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readName(" + pdfArray.getPosition() + ")");
        stringValue = pdfArray.getString(pdfArray.WHITESPACE+"/", pdfArray.WHITESPACE+pdfArray.DELIMITERS);
        setObjType(OT_NAME);
    }

    private boolean isArray(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isArray(" + pdfArray.getPosition() + ")");
        int start = pdfArray.searchNotChar(pdfArray.getPosition(), pdfArray.WHITESPACE);
        return pdfArray.isChar(start, "[");
    }

    private void readArray(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readArray(" + pdfArray.getPosition() + ")");
        anArray = new ArrayList<PDFObject>();
        // Move to the starting bracket of the array.
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        // Move to the next character, which is the start of the first object of the array or whitespace.
        pdfArray.setPosition(pdfArray.getPosition()+1);
        // Skip whitespace characters.
        pdfArray.searchChar(pdfArray.WHITESPACE);
        // Loop until we find the closing bracket of the array.
        while (!pdfArray.isChar("]")) {
            PDFObject anObj = new PDFObject();
            // Read the next object, whichever type it is. This should leave the current position
            // of pdfArray to the character right after the object, which might be whitespace or
            // the closing bracket.
            anObj.readFromByteArray(pdfArray);
            anArray.add(anObj);
            // Skip whitespace characters.
            pdfArray.searchNotChar(pdfArray.WHITESPACE);
        }
        // After the loop the current position is at the closing bracket. Skip one char further.
        pdfArray.setPosition(pdfArray.getPosition()+1);
        setObjType(OT_ARRAY);
    }

    private boolean isDictionary(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "isDictionary(" + pdfArray.getPosition() + ")");
        int startPos = pdfArray.searchNotChar(pdfArray.getPosition(), pdfArray.WHITESPACE);
        return pdfArray.isChar(startPos, "<") &&
                pdfArray.isChar(startPos+1, "<");
    }

    private void readDictionary(PDFByteArray pdfArray) {
//        Log.i("pdftesteri", "readDictionary(" + pdfArray.getPosition() + ")");
        aDictionary = new HashMap<String, PDFObject>();
        // Move to the starting characters of the dictionary.
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        // Move two characters forward to the start of the dictionary itself.
        pdfArray.setPosition(pdfArray.getPosition()+2);
        // Find the position of the next not-whitespace character
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        // Get two characters to detect the end of the dictionary.
        String nextTwoChars = new String(pdfArray.getBytes(pdfArray.getPosition(), pdfArray.getPosition()+2));
        while (!nextTwoChars.equals(">>")) {
            PDFObject name = new PDFObject();
            name.readFromByteArray(pdfArray, pdfArray.getPosition());
            PDFObject anObj = new PDFObject();
            anObj.readFromByteArray(pdfArray, pdfArray.getPosition());
            aDictionary.put(name.getName(), anObj);
            // Find the position of the next not-whitespace character
            pdfArray.searchNotChar(pdfArray.WHITESPACE);
            // Get two characters to detect the end of the dictionary.
            nextTwoChars = new String(pdfArray.getBytes(pdfArray.getPosition(), pdfArray.getPosition()+2));
        }
        pdfArray.setPosition(pdfArray.getPosition()+2);
        setObjType(OT_DICTIONARY);
    }

    private boolean hasStream(PDFByteArray pdfArray) {
        // The keyword stream that follows the stream dictionary shall be followed by an end-of-line
        // marker consisting of either a CARRIAGE RETURN and a LINE FEED or just a LINE FEED, and
        // not by a CARRIAGE RETURN alone.
        String s = pdfArray.getString(pdfArray.getPosition(), pdfArray.WHITESPACE, pdfArray.ENDOFLINE);
        return s.equals("stream");
    }

    private void readStream(PDFByteArray pdfArray) {
        // Move to the start of the actual stream.
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        pdfArray.setPosition(pdfArray.getPosition()+"stream".length());
        pdfArray.searchNotChar(pdfArray.ENDOFLINE);
        // Find the length of the stream from the dictionary, which should preceed the stream.
        int streamLength = getDictionary().get("Length").getInteger();
        // Read the stream
        stream = new PDFByteArray();
        stream.setBytes(pdfArray.getBytes(pdfArray.getPosition(), pdfArray.getPosition()+streamLength));
        // Move to the end of the endstream-keyword. There should be an EOL-marker between the
        // actual stream and endstream-keyword. There should be only whitespace between endstream-
        // keyword and endobj-keyword.
        pdfArray.setPosition(pdfArray.getPosition()+streamLength);
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        pdfArray.searchChar(pdfArray.WHITESPACE);
    }

    private boolean hasEndobj(PDFByteArray pdfArray) {
        return pdfArray.getString(pdfArray.getPosition(), pdfArray.WHITESPACE, pdfArray.WHITESPACE).equals("endobj");
    }

    private void skipEndobj(PDFByteArray pdfArray) {
        pdfArray.searchNotChar(pdfArray.WHITESPACE);
        pdfArray.setPosition(pdfArray.getPosition()+"endobj".length());
    }
}
