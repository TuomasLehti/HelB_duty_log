package tuomaan.pdftest;

/**
 * Stores a single item of a drivers' duty. Duty items are collected to a duty, which describes
 * a driver's work for a single day.
 */
/* Version 0.1 (2017-12-09)
 */
public class HelBDutyItem {

    /** Duty item type, for example "Linja-ajoa" or "Ruokatauko". It is stored as it is in the duty
     * log PDF-files. */
    public String dutyType = "";

    /** The starting time of this duty item. */
    public HSLTime startTime = new HSLTime();

    /** The ending time of this duty item. */
    public HSLTime endTime = new HSLTime();

    /** The starting place. It is stored as it is written in the duty log files. */
    public String startPlace = "";

    /** The ending place. It is stored as it is written in the duty log files. It might also be
     * empty. */
    public String endPlace = "";

    /** The routes which are driven in this duty item. They are stored as HSL's route ids separated
     * by a comma and a space. This might also be empty, if no driving is done in this duty type. */
    public String routes = "";

    /** The block (reporting number of the bus) which is driven in this duty item. If this isn't
     * a driving item, block is 0. */
    public int block = -1;

    /** The direction in which the dricing begins. 1 means the from city center outwards or from
     * east to west. 2 means the opposite of 1. Might also be 0, if no direction is defined in
     * duty log PDF file. */
    public int direction = -1;

    @Override
    public String toString() {
        return String.format(
                "%-30s %5s %-30s s %-2d - %5s %-30s (vuoro %d, linjat %s)",
                dutyType,
                startTime.getHhmmS(),
                startPlace,
                direction,
                endTime.getHhmmS(),
                endPlace,
                block,
                routes);
    }

}
