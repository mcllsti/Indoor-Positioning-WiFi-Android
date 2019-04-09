package honors.testing.test;
/**
 * Daryl McAllister
 * S1222204
 * Indoor Positioning System
 * Honors Project
 * Iain Lambie
 */

/**
 * Class used to map a distance to a cordinate position
 */
public class DistancesPositions {

    /**
     * Overloaded Constructor
     * @param Distance distance to position
     * @param Position latLon position
     */
    public DistancesPositions(double Distance, double[] Position){
        distance = Distance;
        position = Position;
    }

    /**
     * Overloaded Constructor that determines a position upon creation
     * @param level level of signal to router point
     * @param frequency frequence in int Mhz of signal
     * @param Position LatLon position
     */
    public DistancesPositions(double level, int frequency, double[] Position){
        position = Position;
        distance = calculateDistance(level,frequency);
    }

    double distance;
    double[] position;


    /**
     * Calcualtes the distance using Signal level and Frequency
     * Generally inaccurate as many factors can affect the outcome.
     *
     * Code created using help from the following resources:
     * https://stackoverflow.com/questions/42512697/wifi-rssi-signal-to-distance-calculation-method-with-javascript
     * https://gist.github.com/eklimcz/446b56c0cb9cfe61d575
     * https://www.electronicdesign.com/communications/understanding-wireless-range-calculations
     *
     * @param signalLevelInDb Signal level of the RSSI
     * @param freqInMHz Frequency of the signal
     * @return
     */
    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return (Math.pow(10.0, exp)) / 100000;
    }




    public double getDistance() {
        return distance;
    }

    public double[] getPositionArray() {
        return position;
    }



}
