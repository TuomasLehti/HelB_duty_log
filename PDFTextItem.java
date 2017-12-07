package tuomaan.pdftest;

/**
 * A simple class containing the data of a single text snippet from HelB's duty log pdf-files.
 *
 * @author Tuomas Lehti
 * @version 1.0
 */
public class PDFTextItem implements Comparable<PDFTextItem> {
    public Float x;
    public Float y;
    public int page;
    public String text;

    @Override
    public String toString() {
        return "\"" + page + "\";\"" + x + "\";\"" + y + "\";\"" + text + "\"";
    }

    @Override
    public int compareTo(PDFTextItem other) {
        if (!(this.page==other.page)) {
            return this.page - other.page;
        } else if (!(this.y.compareTo(other.y)==0)) {
            return Math.round(this.y) - Math.round(other.y);
        } else if (!(this.x.compareTo(other.x)==0)) {
            return Math.round(this.x) - Math.round(other.x);
        } else {
            return 0;
        }
    }
}
