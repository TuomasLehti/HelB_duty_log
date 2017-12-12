package tuomaan.pdftest;

import java.util.ArrayList;

/**
 * Stores a list of duties, which a driver is to drive.
 */
/* Version 0.1 (2017-12-08)
 */

public class HelBDutyList {

    private ArrayList<HelBDuty> list = new ArrayList<>();

    public void add(HelBDuty duty) {
        list.add(duty);
    }

    public void clear() {
        list.clear();
    }

}
