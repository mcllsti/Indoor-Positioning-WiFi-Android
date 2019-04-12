package honors.testing.test;
/**
 * Daryl McAllister
 * S1222204
 * Indoor Positioning System
 * Honors Project
 * Iain Lambie
 */
import java.util.ArrayList;
import java.util.List;

/**
 *RouterMap Class
 * Used to match a set of LatLon cordinates to router using the routers BSSID
 */
public class RouterMap {

    private double[] cordinates;
    private String BSSID;
    /**
     * Overloaded Constructor
     * @param cord array containing cordinates
     * @param bssid String of BSSID
     */
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
