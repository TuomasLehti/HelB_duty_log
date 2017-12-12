package tuomaan.pdftest;

import java.util.ArrayList;
import java.util.Date;

/**
 * Stores a duty, which describes the work a driver does on a given day. It contains a list of
 * duty items.
 * <p>
 * HelB arranges all its driving to abstract duties. Each duty is daily assigned to a driver.
 * This class looks things from the drivers' viewpoint, and has a date, on which the duty is to
 * be driven. The driver might have the same duty many times in his duty list.
 */
/* Version 0.1. */
public class HelBDuty {

    public ArrayList<HelBDutyItem> duty = new ArrayList<>();

    public Date date = new Date();

    public String dutyId;

}
