package tuomaan.pdftest;

/**
 * Stores a time in 30 hour clock format.
 */
/*
 * Version 0.1 (2017-12-19)
 */

public class HSLTime {

    int minsAfterMidnight = 0;

    /** Sets the time. It should be given in a string format so that it is formatted
     * "hh:mm" or "h:mm". 30 hour clock is used. */
    public void setHhmmS(String startTime) {
        int hours = Integer.parseInt(startTime.substring(0, startTime.indexOf(":")));
        int mins = Integer.parseInt(startTime.substring(startTime.indexOf(":")+1));
        this.minsAfterMidnight = hours*60 + mins;
    }

    /** Gets the starting time. It is returned as a string, which is formatted either "h:mm" or
     * "hh:mm". 30 hour clock is used. */
    public String getHhmmS() {
        int hours = this.minsAfterMidnight / 60;
        int mins = this.minsAfterMidnight % 60;
        return String.format("%d:%02d", hours, mins);
    }


}
