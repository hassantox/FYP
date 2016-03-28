package com.hassanmaps.tourismguidepk;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.support.v4.app.FragmentActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	Button search;
	ImageButton back_btn;
	LinearLayout destLayout, locationLayout;
	EditText destination;
	AutoCompleteTextView autoDestination, autoPlace;
	
	private PlacesAutoCompleteAdapter_Dest mPlaceArrayAdapter;
	private PlacesAutoCompleteAdapter_Place mPlaceArrayAdapter_Place;
	GoogleMap mMap;
	
	float defaultZoom = 5;
	double pakLng = 69.345116;
	double pakLat = 28.375321;
	
	public boolean internet (){
    	
    	ConnectivityManager connection = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = connection.getActiveNetworkInfo();
    	
    	if(netInfo != null && connection.getActiveNetworkInfo().isAvailable() && connection.getActiveNetworkInfo().isConnected())
    	{
    		return true;
    	}
    	
    	return false;
    }
    
	public void netPrompt ()
    {
	   AlertDialog.Builder alert = new AlertDialog.Builder(this);   	
   		alert.setTitle("Check Connection");
   		alert.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.");
   		alert.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(Settings.ACTION_SETTINGS);
					startActivity(intent);
				}
			})
			.setNegativeButton("Quit", new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int id) {
					MainActivity.this.finish();
				}
			});
   		alert.create().show();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Button find = (Button) findViewById(R.id.btn_find);
    	search = (Button) findViewById(R.id.searchBtn);
    	back_btn = (ImageButton) findViewById(R.id.back_Btn);
    	//EditText place = (EditText) findViewById(R.id.et_location);
    	destination = (EditText) findViewById(R.id.et_dest);
    	destLayout = (LinearLayout) findViewById(R.id.mainLinear);
        locationLayout = (LinearLayout) findViewById(R.id.location_layout);
        
        autoDestination = (AutoCompleteTextView) findViewById(R.id.et_dest);
        autoDestination.setThreshold(3);
        autoPlace = (AutoCompleteTextView) findViewById(R.id.et_location);
        autoDestination.setThreshold(3);
        
        
        
        if (!internet())
        {
        	netPrompt();
        }
        
        	SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
        	mMap = mapFragment.getMap();
            
            gotoLocation(pakLat, pakLng, defaultZoom);
            
            back_btn.setVisibility(View.GONE);
            
        back_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				destLayout.setVisibility(View.VISIBLE);
				locationLayout.setVisibility(View.GONE);
			}
		});
        
        search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				destLayout.setVisibility(View.GONE);
				locationLayout.setVisibility(View.VISIBLE);
	            back_btn.setVisibility(View.VISIBLE);
				hideSoftKeyboard(v);
				
				String dest = destination.getText().toString();
				searchDest(dest,12);
			}
		});
        
        destination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_SEARCH){
					String dest = destination.getText().toString();
					searchDest(dest,12);
					destLayout.setVisibility(View.GONE);
					locationLayout.setVisibility(View.VISIBLE);
					back_btn.setVisibility(View.VISIBLE);
					hideSoftKeyboard(v);
					return true;
				}
				return false;
			}
		});
        
        mPlaceArrayAdapter = new PlacesAutoCompleteAdapter_Dest(this, android.R.layout.simple_list_item_1);
        autoDestination.setAdapter(mPlaceArrayAdapter);
        
        autoDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
        		hideSoftKeyboard(view);
                String description = (String) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
                searchDest(description,12);
                autoDestination.setText("");
                destLayout.setVisibility(View.GONE);
                locationLayout.setVisibility(View.VISIBLE);
                back_btn.setVisibility(View.VISIBLE);
            }
        	
        });
        
        mPlaceArrayAdapter_Place = new PlacesAutoCompleteAdapter_Place(this, android.R.layout.simple_list_item_1);
        autoPlace.setAdapter(mPlaceArrayAdapter_Place);
        
        autoPlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
        		hideSoftKeyboard(view);
                String description = (String) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
                searchDest(description,18);
                autoPlace.setText("");
                
            }
        	
        });
    }
    
    //@Override
  /*  public void onMapReady(GoogleMap map) {
        // Add a marker in pak and move the camera.
      // LatLng sydney = new LatLng(30.375321, 69.345116);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng pak = new LatLng(pakLat, pakLng);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pak, defaultZoom);
    	map.moveCamera(update);
       // map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       // map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    */
    public void gotoLocation(double lat, double lng, float zoom){
    	
    	LatLng pak = new LatLng(lat, lng);
    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pak, zoom);
    	mMap.moveCamera(update);
    	
    }
    
public void searchDest (String destination, int zoom){

	
	Geocoder gc = new Geocoder(getApplicationContext());
		try {
			List<Address> list = gc.getFromLocationName(destination, 1);
			
			if(list != null && !list.isEmpty()){
			Address add = list.get(0);
			//String locality = add.getLocality();
			//Toast.makeText(getApplicationContext(), locality, Toast.LENGTH_LONG).show();
			double lat = add.getLatitude();
			double lng = add.getLongitude();
			
			gotoLocation(lat, lng, zoom);
			}else
			{
				Toast.makeText(getApplicationContext(), "no address", Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void hideSoftKeyboard(View v){
    	InputMethodManager kb = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	kb.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
