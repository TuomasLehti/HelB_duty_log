package tuomaan.pdftest;

/**
 * Created by Tuomas on 8.11.2017.
 */
import android.util.Log;

import java.io.*;
import java.util.Arrays;

/**
 * Reads bytes from a file in a PDF fashion. PDF specification 32000_2008 used.
 */
public class PDFByteArray {

    /**
     * Whitespace characters from PDF specification. (null, horizontal tab, line feed, form feed,
     * carriage return, space.
     */
    public static final String WHITESPACE = new String(new byte[] {0, 9, 10, 12, 13, 32});

    /**
     * Delimiter characters from PDF specification.
     */
    public static final String DELIMITERS = new String("()<>[]{}/%");

    /**
     * End-of-line markers from PDF specification.
     */
    public static final String ENDOFLINE = new String(new byte[] {10, 13});

    /**
     * The byte array itself.
     */
    private byte[] pdfArray;

    /**
     * Current position in the array.
     */
    private int position = 0;

    /**
     * Gets the current position in the array.
     * @return The current position in the array.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the current position in the array.
     * @param position The wanted position in the array.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the length of the byte array.
     * @return The length of the array.
     */
    public int getLength() {
        return pdfArray.length;
    }

    /**
     * Tests if the character at a position is a certain character.
     * @param pos The position at which a character is to be tested.
     * @param chars The set of characters (as a string) which are searched.
     * @return Returns true if the character at "pos" is one of characters in "chars".
     */
    public boolean isChar(int pos, String chars) {
        return chars.indexOf(pdfArray[pos]) > -1;
    }

    /**
     * Tests if the character at the current position is a certain character.
     * @param chars The set of characters (as a string) which are searched.
     * @return Returns true if the character at the current position is one of characters in "chars".
     */
    public boolean isChar(String chars) { return isChar(getPosition(), chars); }

    /**
     * Searches for the first occurence of a certain character. This might also be at the position
     * from which the search starts.
     * @param start The position, from which to start the search.
     * @param chars The characters, which are searched.
     * @return The position of the first occurence of a certain character.
     */
    public int searchChar(int start, String chars) {
        int pos = start;
        while (pos < pdfArray.length && !isChar(pos, chars)) {
            pos++;
        }
        return pos;
    }

    /**
     * Moves the current position to the next occurence of a certain character. This might also
     * be at the current position, in which chase the current position doesn't change.
     * @param chars The characters, which are searched.
     */
    public void searchChar(String chars) {
        setPosition(searchChar(getPosition(), chars));
    }

    /**
     * Searches for the first occurence of an other than specified character. This might also
     * be at the position from which the search starts.
     * @param start The position, from which to start the search.
     * @param chars The characters, which are to be skipped.
     * @return The position of the first occurence of an other than specified character.
     */
    public int searchNotChar(int start, String chars) {
        int pos = start;
        while (pos < pdfArray.length && isChar(pos, chars)) {
            pos++;
        }
        return pos;
    }

    /**
     * Moves the current position to the first occurence of an other than specified character.
     * This might also be at the current position, in which case the current position doesn't
     * change.
     * @param chars
     */
    public void searchNotChar(String chars) {
        setPosition(searchNotChar(getPosition(), chars));
    }

    void openFile(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            pdfArray = new byte[(int)file.length()];
            inputStream.read(pdfArray);
        } catch (FileNotFoundException e) {
            Log.i("pdftesteri", "File doesn't exists!");
        } catch (IOException e) {
            Log.i("pdftesteri", "IO error!");
        }
    }

    void openFile(String filename) {
        File file = new File(filename);
        openFile(file);
    }

    void setBytes(byte[] bytes) {
        pdfArray = bytes;
    }

    /**
     * Returns a string from the array. The end of the string is defined by a certain character,
     * and certain characters are trimmed from the start of the string.
     * @param start The position, from which to get the string.
     * @param trimChars The characters, which are trimmed from the front of the string.
     * @param delimiterChars The characters, which terminate the string.
     * @return The string.
     */
    public String getString(int start, String trimChars, String delimiterChars) {
        int s = searchNotChar(start, trimChars);
        int end = searchChar(s, delimiterChars);
        return new String(getBytes(s, end));
    }

    /**
     * Returns a string from the current position of the array. The end of the string is defined by
     * a certain character, and certain characters are trimmed from the start of the string.
     * @param trimChars The characters, which are trimmed from the front of the string.
     * @param delimiterChars The characters, which terminate the string.
     * @return The string.
     */
    public String getString(String trimChars, String delimiterChars) {
        searchNotChar(trimChars);
        int s = getPosition();
        searchChar(delimiterChars);
        int end = getPosition();
        return new String(getBytes(s, end));
    }

    /**
     * Searches for the first occurrence of a string in the byte array. If there is no such string
     * in the byte array, returns -1.
     * @param start The position from which to start the search.
     * @param search The string which is searched.
     * @return The position of the first occurrence of the string, or -1 if not found.
     */
    int searchString(int start, String search) {
        int i = start;
        String found = new String(pdfArray, i, search.length());
        while (i < (pdfArray.length - search.length()) && !found.equals(search)) {
            i++;
            found = new String(pdfArray, i, search.length());
        }
        if (found.equals(search)) {
            return i;
        } else {
            return -1;
        }
    }

    /**
     * Searches for the first occurrence of a string in the byte array after the current position.
     * The current position is moved to the position of the string which is found. If there is no
     * such string in the byte array, the current position doesn't move.
     * @param search The string which is searched.
     */
    void searchString(String search) {
        setPosition(searchString(getPosition(), search));
    }


    /**
     * Returns bytes from the array.
     * @param start The starting index, inclusive.
     * @param end The ending index, exclusive.
     * @return
     */
    byte[] getBytes(int start, int end) {
        byte[] result;
        try {
            result = Arrays.copyOfRange(pdfArray, start, end);
        } catch (Exception e) {
            Log.i("pdftesteri", "getBytes exception!");
            result = new byte[0];
        }
        return result;
    }

}