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

    public void setNumber(double[] cord) {
        this.cordinates = cord;
    }

    public String getBSSID() {
        return this.BSSID;
    }

    public void setNumber(String bssid) {
        this.BSSID = bssid;
    }

    public List<Double> convertGpsToECEF(double lat, double longi, float alt) {

        double a=6378.1;
        double b=6356.8;
        double N;
        double e= 1-(Math.pow(b, 2)/Math.pow(a, 2));
        N= a/(Math.sqrt(1.0-(e*Math.pow(Math.sin(Math.toRadians(lat)), 2))));
        double cosLatRad=Math.cos(Math.toRadians(lat));
        double cosLongiRad=Math.cos(Math.toRadians(longi));
        double sinLatRad=Math.sin(Math.toRadians(lat));
        double sinLongiRad=Math.sin(Math.toRadians(longi));
        double x =(N+0.001*alt)*cosLatRad*cosLongiRad;
        double y =(N+0.001*alt)*cosLatRad*sinLongiRad;
        double z =((Math.pow(b, 2)/Math.pow(a, 2))*N+0.001*alt)*sinLatRad;

        List<Double> ecef= new ArrayList<>();
        ecef.add(x);
        ecef.add(y);
        ecef.add(z);

        return ecef;


    }

}
