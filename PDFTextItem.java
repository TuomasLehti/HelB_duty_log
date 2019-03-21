package pdftextextractor;
/**
 * A simple class containing the data of a single text snippet from HelB's duty log pdf-files.
 *
 * @author Tuomas Lehti
 * @version 2019-03-21
 */
public class PDFTextItem implements Comparable<PDFTextItem> {

	// the coordinates for the surrounding box
	public Float topLeftX;
	public Float topLeftY;
	public Float bottomRightX;
	public Float bottomRightY;

    public Float x;
    public Float y;
    public int page;
    public String text;

    @Override
    public String toString() {
		String separator = "\";\"";
		StringBuilder sb = new StringBuilder();
		sb.append("\"");
		sb.append(page+"");
		sb.append(separator);
		sb.append(topLeftX+"");
		sb.append(separator);
		sb.append(topLeftY+"");
		sb.append(separator);
		sb.append(bottomRightX+"");
		sb.append(separator);
		sb.append(bottomRightY+"");
		sb.append(separator);
		sb.append(x+"");
		sb.append(separator);
		sb.append(y+"");
		sb.append(separator);
		sb.append(text);
		sb.append("\"");
        return sb.toString();
    }

    @Override
    public int compareTo(PDFTextItem other) {
        if (!(this.page==other.page)) {
            return this.page - other.page;
        } else if (this.topLeftY.compareTo(other.topLeftY)!=0) {
            return Math.round(this.topLeftY) - Math.round(other.topLeftY);
        } else if (this.topLeftX.compareTo(other.topLeftX)!=0) {
            return Math.round(this.topLeftX) - Math.round(other.topLeftX);
        } else {
            return 0;
        }
    }
}
/* 
CHANGELOG

2019-03-19: 
- Includes the coordinates for the surrounding box.
- Objects are compared by the coordinates of the surrounding box.
*/
