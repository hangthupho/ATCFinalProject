package com.ruc.hangthupho.TextMessageGPS;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity implements View.OnClickListener{
    public String strName = "";
    static final int PICK_CONTACT_REQUEST = 0;

    LinearLayout displayLayout;
    RelativeLayout.LayoutParams btnParams;

    ArrayList<Button> arrayButtons = new ArrayList<Button>();
    ArrayList<String> arrayNumbers = new ArrayList<String>();
    EditText txtInput, txtMessage;
    ImageButton btnAdd, btnSearch;
    Button btnSend, btnGPS;
    String getInputNumber;
    String lookedupNumber;
    String getAddressFromGPS;
    String getTypedSMS;
    String oldAddressString;
    double lat, lng;
    float acc;
    LocationListener mlocListener;

    private LocationManager lm, lm1;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    Integer count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayLayout = (LinearLayout) findViewById(R.id.addPanel);

        txtInput = (EditText) findViewById(R.id.txtInput);
        txtMessage = (EditText) findViewById(R.id.txtMessage);

        btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnGPS = (Button) findViewById(R.id.btnGPS);
        btnGPS.setEnabled(false);

        btnGPS.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();}

        }


    @Override
    protected void onResume() {
        super.onResume();
        mlocListener = new MyLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(mlocListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        getInputNumber = txtInput.getText().toString();
        switch (v.getId()) {
            case R.id.btnAdd:
                if(getInputNumber.length() == 0)
                    Toast.makeText(getApplicationContext(),"Please enter a number!",Toast.LENGTH_LONG).show();
                else if(getInputNumber.length() < 8)
                    Toast.makeText(getApplicationContext(),"A valid number has 8 digits!",Toast.LENGTH_LONG).show();

                if (!getInputNumber.equals("") && getInputNumber.length() == 8) {
                    final Button btnRemove = new Button(this);
                    btnParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    btnRemove.setLayoutParams(btnParams);
                    btnRemove.setCompoundDrawablesWithIntrinsicBounds(R.drawable.remove, 0, 0, 0);
                    btnRemove.setBackgroundColor(Color.TRANSPARENT);
                    btnRemove.setTextColor(Color.parseColor("#003399"));

                    if(!getInputNumber.equals(lookedupNumber) || strName.equals(""))
                        btnRemove.setText(" " + getInputNumber);
                    else btnRemove.setText(" " + strName);

                    arrayButtons.add(btnRemove);
                    arrayNumbers.add(getInputNumber);
                    displayLayout.addView(btnRemove);

                    //Remove the button on click
                    btnRemove.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            arrayNumbers.remove(arrayButtons.indexOf(btnRemove));
                            arrayButtons.remove(btnRemove);
                            displayLayout.removeView(btnRemove);
                        }
                    });
                    txtInput.setText("");
                    strName = "";
                }
                break;

            case R.id.btnSearch:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 1);
                break;

            case R.id.btnSend:
                sendSmsByManager();
                break;

            case R.id.btnGPS:
                gpsButton();
                break;
        }
    }

    public void gpsButton(){
        Geocoder coder = new Geocoder(getBaseContext(), Locale.getDefault());
        Address address = null;
        String message = "";

        if(lat == 0 && lng == 0){
            Toast.makeText(getApplicationContext(),"Loading location, please wait...", Toast.LENGTH_LONG).show();
        }
        else {
            try {
                List<Address> addresses = coder.getFromLocation(lat,lng,1);
                if (!addresses.isEmpty())
                    address = addresses.get(0);
            } catch (Exception e) {
            }

            if (address != null) {
                message += "\n";
                String line = null;
                for (int i = 0; true; i++) {
                    line = address.getAddressLine(i);
                    if (line == null)
                        break;
                    message += line + "\n";
                }
            }

            if(message != null && !message.equals("")) {
                oldAddressString = message;
                txtMessage.setText(txtMessage.getText().toString().replace(oldAddressString,"")+message);
            }
        }
    }

    public void showSelectedNumber(int type, String number, String name) {
        getInputNumber = number;
        lookedupNumber = number;
        strName = name;
        txtInput.setText(getInputNumber);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                            null, null, null);
                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        String contactName = c.getString(2);
                        showSelectedNumber(type, number, contactName);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    public void sendSmsByManager() {
        try {
            // Get the default instance of the SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            if(txtMessage.getText().toString().equals(""))
                Toast.makeText(getApplicationContext(),"Your text message is empty. Please write something!", Toast.LENGTH_LONG).show();
            else {
                if (arrayNumbers.size() == 0)
                    Toast.makeText(getApplicationContext(), "Please add the number into your sending list first!", Toast.LENGTH_LONG).show();
                else {
                    for (String c : arrayNumbers) {
                        smsManager.sendTextMessage(c, null, txtMessage.getText().toString(), null, null);
                    }
                    Toast.makeText(getApplicationContext(), "Your SMS has been successfully sent!", Toast.LENGTH_LONG).show();

                }
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"Failed to send your SMS...",
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public class MyLocationListener implements LocationListener{

        public void onLocationChanged(Location location) {


            if (location != null) {

                if (!btnGPS.isEnabled()){
                    btnGPS.setText(" Update location");
                    btnGPS.setEnabled(true);
                }

                lat = location.getLatitude();
                lng = location.getLongitude();
                acc = location.getAccuracy();
            }
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }

    }
