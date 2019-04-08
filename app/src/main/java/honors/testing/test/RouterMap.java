package honors.testing.test;

import java.util.ArrayList;
import java.util.List;

public class RouterMap {

    private double[] cordinates;
    private String BSSID;

    public RouterMap(double[] cord, String bssid)
    {
        this.cordinates = cord;
        this.BSSID = bssid;
    }

    public double[] getCordinates() {
        return this.cordinates;
    }

    public String getBSSID() {
        return this.BSSID;
    }




}
