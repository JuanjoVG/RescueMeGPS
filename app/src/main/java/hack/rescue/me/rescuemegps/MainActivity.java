package hack.rescue.me.rescuemegps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final long MIN_TIME = 3000;
    public static final float MIN_DISTANCE = 0;
    private TextView txtLat;
    private TextView txtLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLat = (TextView)findViewById(R.id.latitud);
        txtLon = (TextView)findViewById(R.id.longitud);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                sendGPSLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("onStatusChanged", String.valueOf(status));
            }

            public void onProviderEnabled(String provider) {
                Log.d("onProviderEnabled", provider);
            }

            public void onProviderDisabled(String provider) {
                Log.d("onProviderDisabled", provider);
            }
        };
        

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    private void sendGPSLocation(Location location) {
        txtLat.setText(String.valueOf(location.getLatitude()));
        txtLon.setText(String.valueOf(location.getLongitude()));
    }

}
