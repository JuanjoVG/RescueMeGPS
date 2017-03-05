package hack.rescue.me.rescuemegps;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 7;
    public static final long MIN_TIME = 7 * 1000;
    public static final float MIN_DISTANCE = 2;
    private TextView txtLat;
    private TextView txtLon;
    private int gPSLocationNum = 0;

    Handler bluetoothIn;                     //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = "20:15:10:20:04:46";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLat = (TextView) findViewById(R.id.latitud);
        txtLon = (TextView) findViewById(R.id.longitud);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Toast.makeText(getApplicationContext(), latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                sendGPSLocation(latitude, longitude);
                saveLocation(latitude, longitude, false);
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

        bluetoothSetup();
    }

    private void sendGPSLocation(Double lat, Double lon) {
        txtLat.setText(String.valueOf(lat));
        txtLon.setText(String.valueOf(lon));
    }

    private void saveLocation(Double lat, Double lon, boolean alert) {
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> postValues = new HashMap<>();
        postValues.put("lat", lat);
        postValues.put("lon", lon);
        postValues.put("alert", alert);
        postValues.put("time", ServerValue.TIMESTAMP);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/juanjo/positions/" + gPSLocationNum, postValues);

        mFirebaseDatabaseReference.updateChildren(childUpdates);
        ++gPSLocationNum;
    }

    private void bluetoothSetup() {
        bluetoothIn = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String str = (String) msg.obj;
                Log.d("Bluetooth", str);
                if (str.contains("ALERT")) {
                    hayAlert();
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    private void hayAlert() {
        final DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Map<String, Object> mapValues = (Map<String, Object>) snap.getValue();

                    Double lat = (Double) mapValues.get("lat");
                    Double lon = (Double) mapValues.get("lon");
                    mFirebaseDatabaseReference.child("users").child("juanjo").child("positions")
                            .limitToLast(1).removeEventListener(this);
                    mConnectedThread.write(lat + "/" + lon + "#");
                    saveLocation(lat, lon, true);
                    AsyncLocation asyncLocation = new AsyncLocation();
                    asyncLocation.execute("Juanjo needs help");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mFirebaseDatabaseReference.child("users").child("juanjo").child("positions")
                .limitToLast(1).addValueEventListener(listener);
    }

    private void checkBTState() {
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket, bluetoothIn);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

}
