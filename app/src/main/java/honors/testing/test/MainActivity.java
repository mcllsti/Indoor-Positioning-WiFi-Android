package honors.testing.test;

/**
 * Daryl McAllister
 * S1222204
 * Indoor Positioning System
 * Honors Project
 * Iain Lambie
 */


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.WifiRttManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.indoorlocation.core.IndoorLocation;
import io.indoorlocation.manual.ManualIndoorLocationProvider;
import io.mapwize.mapwizeformapbox.AccountManager;
import io.mapwize.mapwizeformapbox.api.LatLngFloor;
import io.mapwize.mapwizeformapbox.api.Venue;
import io.mapwize.mapwizeformapbox.map.FollowUserMode;
import io.mapwize.mapwizeformapbox.map.MapOptions;
import io.mapwize.mapwizeformapbox.map.MapwizePlugin;
import io.mapwize.mapwizeformapbox.map.MapwizePluginFactory;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MapwizePlugin mapwizePlugin;
    private ManualIndoorLocationProvider manualIndoorLocationProvider;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    double floor = 0.0;
    private WifiManager wifiManager;
    private List<ScanResult> results;
    private ArrayList<Double> cordinates = new ArrayList<>();
    Venue venue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Mapwize is built on mapbox so must instilize this
        Mapbox.getInstance(this, "pk.mapwize");
        setContentView(R.layout.activity_main);
        requestLocationPermission();

        //Getting our map view
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        //Setting style in acocrdance with mapwize
        mapView.setStyleUrl("http://outdoor.mapwize.io/styles/mapwize/style.json?key=" + AccountManager.getInstance().getApiKey());


        //Setting the venue our indoor map will be based on
        MapOptions opts = new MapOptions.Builder().centerOnVenue("5c5dc21da17ef7002dffd5e3").restrictContentToVenue("5c5dc21da17ef7002dffd5e3").floor(5.0).build();
        mapwizePlugin = MapwizePluginFactory.create(mapView, opts);
        venue = mapwizePlugin.getVenue();

        //Setting up wifimanager for scanning
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }


        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
        scanWifi(); //scan to start positioning

    }

    /**
     * Takes a none round number and coverts it to a rounded state to be used to position a user on a floor
     * Returns the whole floor number depending if the trilaterated number is within certain regions
     * @param currentGuess Non whole number floor
     * @return
     */
    public double ConvertToWholeFloor(double currentGuess){

        if(currentGuess < 5.667){
            return 5.0;
        }
        else if(currentGuess >= 5.667 && currentGuess < 6.6902){
            return 6.0;
        }
        else if(currentGuess >= 6.692){
            return 7.0;
        }
        return 0.0;
    }

    /**
     * Updates map with determined position of a user
     */
    public void loadedmap()
    {

        setupLocationProvider(); //needed for positioning

        //ensuring we have a whole number floor
        if(cordinates.size() > 2)
        {
            if(Arrays.asList(6.0,5.0,7.0).contains(cordinates.get(2))){
                floor = cordinates.get(2);
            }
            else
            {
                floor = ConvertToWholeFloor(cordinates.get(2));
            }
        }



        //set the location from cordinates and floor
        IndoorLocation indoorLocation = new IndoorLocation(manualIndoorLocationProvider.getName(), cordinates.get(0), cordinates.get(1), floor, System.currentTimeMillis());

        manualIndoorLocationProvider.setIndoorLocation(indoorLocation);
        mapwizePlugin.setFollowUserMode(FollowUserMode.FOLLOW_USER);

        //Output used in testing to see cordinates
        String coords = Double.toString(indoorLocation.getLatitude()) + "," + Double.toString(indoorLocation.getLongitude());
        Toast.makeText(this,coords,Toast.LENGTH_LONG).show();

        //Ensure map switches to correct floor user is on
        mapwizePlugin.setFloor(indoorLocation.getFloor());
    }

    /**
     * ScanWifi method will be used almost exclusively to start a wifi scan
     */
    private void scanWifi() {

        //Clearing results to ensure accurate scan
        if(results != null){
            results.clear();
        }

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan(); //Start scan is deprecated and will be eventually removed. Should be changed if application is to continue.
        //TODO: Find another method to scan for future project development. StartScans deprecated status comes with limitations such as scanning frequency limits

    }

    /**
     * Used by Mapwize for location providing and indor locationing
     */
    private void setupLocationProvider() {
        manualIndoorLocationProvider = new ManualIndoorLocationProvider();
        mapwizePlugin.setLocationProvider(manualIndoorLocationProvider);
    }


    /**
     * Proforms trilateration on N points providing within a ArrayList
     *
     * Created with the following resources:
     * https://github.com/lemmingapex/trilateration
     *
     *
     * @param list list of positions and distances
     * @return
     */
    public double[] trilateration(ArrayList<DistancesPositions> list)
    {
        //setting up our distances and positions in a more assesiable format
        double[] distances = new double[list.size()];
        double[][] positions = new double[list.size()][list.get(0).position.length];
        int i = 0;
        for(DistancesPositions e : list)
        {
            distances[i] = e.getDistance();
            positions[i] = e.getPositionArray();
            i++;
        }



        double[] centroid;

        try{ //to ensure program does not break

            //Solver which solves trilateration
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
            LeastSquaresOptimizer.Optimum optimum = solver.solve();
            centroid = optimum.getPoint().toArray();

        }
        catch(Exception e){
            //If trilateration cannot be done then return an averaged position
            centroid = list.get(list.size()-1).position;
        }
        return centroid;

    }

    /**
     * Brocast reciever is an class which is called upon a completed scan
     */
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            unregisterReceiver(this); //unregistering in case of future scans

            results = wifiManager.getScanResults(); //getting results
            ArrayList<RouterMap> allRouters = setUp(); //setting up our Uni router infestructure

            ArrayList<DistancesPositions> listy = new ArrayList<DistancesPositions>();
            double[][] positions = new double[20][2]; //Never be more than 20 posisitions


            String checker = wifiManager.getConnectionInfo().getSSID();
            int i = 0;

            //For each of our results check if it is on our network and if so and is part of our acceptable routers
            //add its position and distance to our list for trilateration
            for (ScanResult scanResult : results) {
                if (checker.equals('"' + scanResult.SSID + '"')) {

                    try {
                        positions[i] = allRouters.stream().filter(item -> item.getBSSID().equals(scanResult.BSSID)).findFirst().get().getCordinates();

                        listy.add(new DistancesPositions((double) scanResult.level, scanResult.frequency, positions[i]));

                    } catch (Exception e) {

                    }

                    if (!(positions[i][0] == 0 && positions[i][1] == 0)) { //used to sync positions and distances
                        i++;
                    }
                }
            }

            if (listy.size() < 2) {
                //If there is less than 2 router points we cannot proceed so we have to scan again
                Toast.makeText(getApplicationContext(), "Not enough router points to determine position " , Toast.LENGTH_SHORT).show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        scanWifi(); //This must change as there is limit on scans due to deprecated
                        return; //get out of method
                    }
                }, 10000);
            }

            else if(listy.size() > 1 && listy.size() < 4){
                listy = BulkResults(listy); //have to bulk our results if we have less than 4 for trilateration to work
            }

            double[] centeroid = trilateration(listy);
            if (centeroid != null) { //as long as not null
                for (int j = 0; j < centeroid.length; j++) {
                    cordinates.add(centeroid[j]);
                }
                loadedmap();
            }
        } };

    /**
     * Used to bulk our results by adding false results that are determined by averages of previous results which should not affect our overall acuracy.
     * @param listy our current list that needs bulking
     * @return
     */
    private ArrayList<DistancesPositions> BulkResults(ArrayList<DistancesPositions> listy) {
        if(listy.size() == 2)
        {
            //If there is 2 then we need to add another 2 for trilateration
            DistancesPositions x = listy.get(0);
            x.position[0] = ((x.position[0] + listy.get(1).position[0])/2);
            x.position[1] = ((x.position[1] + listy.get(1).position[1])/2);
            x.position[2] = ((x.position[2] + listy.get(1).position[2])/2);
            x.distance = ((x.distance + listy.get(1).distance)/2);
            listy.add(x);

            x = listy.get(0);
            x.position[0] = ((x.position[0] + listy.get(1).position[0] + listy.get(2).position[0])/3);
            x.position[1] = ((x.position[1] + listy.get(1).position[1] + listy.get(2).position[1])/3);
            x.position[2] = ((x.position[2] + listy.get(1).position[2] + listy.get(2).position[2])/3);
            x.distance = ((x.distance + listy.get(1).distance + listy.get(2).distance)/3);
            listy.add(x);

        }
        else{
            //if there is three we only need one
            DistancesPositions x = listy.get(0);
            x.position[0] = ((x.position[0] + listy.get(1).position[0])/2);
            x.position[1] = ((x.position[1] + listy.get(1).position[1])/2);
            x.position[2] = ((x.position[2] + listy.get(1).position[2])/2);
            x.distance = ((x.distance + listy.get(1).distance)/2);
            listy.add(x);
        }

        return listy;
    }


    @Override
    protected void onStart() { //LIFECYCLE METHOD
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() { //LIFECYCLE METHOD
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() { //LIFECYCLE METHOD
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() { //LIFECYCLE METHOD
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() { //LIFECYCLE METHOD
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() { //LIFECYCLE METHOD
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { //LIFECYCLE METHOD
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Sets up an Arraylist with our known University Wifi Infistructure
     * @return
     */
    public ArrayList<RouterMap> setUp()
    {
        ArrayList<RouterMap> allRouters = new ArrayList<RouterMap>();

        ///SEVENTH FLOOR
        double[][] Sevenpositions = new double[][] {
                { 55.8675254490689, -4.249418377876283,7},
                { 55.867424613296336,-4.249635636806489,7 },
                { 55.867323024753276,-4.249848872423173,7 },
                { 55.867104044101254, -4.250004440546037,7 },
                { 55.86691140030752, -4.2500822246074685,7 },
                { 55.86677933340469, -4.250219017267228,7 }
        };

        RouterMap SevenOne = new RouterMap(Sevenpositions[0],"00:b7:71:97:dc:ad");
        RouterMap Seventwo = new RouterMap(Sevenpositions[1],"00:b7:71:aa:4d:0d");
        RouterMap Seventhree = new RouterMap(Sevenpositions[2],"00:b7:71:97:8f:ad");
        RouterMap Sevenfour = new RouterMap(Sevenpositions[3],"00:b7:71:a5:e4:6d");
        RouterMap Sevenfive = new RouterMap(Sevenpositions[4],"00:b7:71:ac:02:ad");
        RouterMap Sevensix = new RouterMap(Sevenpositions[5],"00:b7:71:a3:9d:c2");

        allRouters.add(SevenOne);
        allRouters.add(Seventwo);
        allRouters.add(Seventhree);
        allRouters.add(Sevenfour);
        allRouters.add(Sevenfive);
        allRouters.add(Sevensix);

        //////////////////////////////

        //SIXTH FLOOR
        double[][] Sixpositions = new double[][] {
                { 55.867542756675164, -4.249380826950074,6},
                { 55.86751566650539,-4.24961417913437,6 },
                { 55.86737833410411,-4.249572604894639,6 },
                { 55.86714806608404, -4.249978959560395,6 },
                { 55.86695542250868, -4.250056743621827,6 },
                { 55.86678911615361, -4.250126481056214,6 }
        };

        RouterMap SixOne = new RouterMap(Sixpositions[0],"00:b7:71:99:14:ed");
        RouterMap Sixtwo = new RouterMap(Sixpositions[1],"00:b7:71:97:dd:8d");
        RouterMap Sixthree = new RouterMap(Sixpositions[2],"00:b7:71:96:cd:8d");
        RouterMap Sixfour = new RouterMap(Sixpositions[3],"00:b7:71:97:ca:22");
        RouterMap Sixfive = new RouterMap(Sixpositions[4],"00:b7:71:97:ca:82");
        RouterMap Sixsix = new RouterMap(Sixpositions[5],"00:b7:71:99:2f:2d");

        allRouters.add(SixOne);
        allRouters.add(Sixtwo);
        allRouters.add(Sixthree);
        allRouters.add(Sixfour);
        allRouters.add(Sixfive);
        allRouters.add(Sixsix);

        //////////////////////////////

        //FIFTH FLOOR
        double[][] FivePositions = new double[][] {
                { 55.86751566650539, -4.249429106712342,5},
                { 55.86742386078959,-4.249628931283952,5 },
                { 55.867293676902484,-4.249779134988786,5 },
                { 55.867114579281164, -4.250004440546037,5 },
                { 55.866948273607605, -4.250066131353379,5 },
                { 55.86676164919841, -4.250141233205796,5 }
        };

        RouterMap FiveOne = new RouterMap(FivePositions[0],"00:b7:71:aa:42:2d");
        RouterMap Fivetwo = new RouterMap(FivePositions[1],"00:b7:71:aa:41:8d");
        RouterMap Fivethree = new RouterMap(FivePositions[2],"00:b7:71:aa:6f:82");
        RouterMap Fivefour = new RouterMap(FivePositions[3],"00:b7:71:99:13:cd");
        RouterMap Fivefive = new RouterMap(FivePositions[4],"00:b7:71:99:15:4d");
        RouterMap Fivesix = new RouterMap(FivePositions[5],"00:b7:71:97:de:ad");

        allRouters.add(FiveOne);
        allRouters.add(Fivetwo);
        allRouters.add(Fivethree);
        allRouters.add(Fivefour);
        allRouters.add(Fivefive);
        allRouters.add(Fivesix);


        return allRouters;
    }

    /**
     * Overriding permissions to allow us to acess WIFI and locations
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Used to request location permissions on newer devices for our positioning and wifi
     */
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }



}
