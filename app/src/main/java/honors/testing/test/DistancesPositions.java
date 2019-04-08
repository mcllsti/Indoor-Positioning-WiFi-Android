package honors.testing.test;

public class DistancesPositions {

    public DistancesPositions(double Distance, double[] Position){
        distance = Distance;
        position = Position;
    }

    double distance;
    double[] position;


    public double getDistance() {
        return distance;
    }

    public double[] getPositionArray() {
        return position;
    }

    public double getXPosition() {
        return position[0];
    }

    public double getYPosition() {
        return position[1];
    }

    public double getZPosition() {
        return position[2];
    }




}
