package com.employee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import com.employee.ShakeDetector.OnShakeListener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;
	String h="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector();
		mShakeDetector.setOnShakeListener(new OnShakeListener() {
 
			@Override
			public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
				final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				 int counts=0;
			
	            wifi.setWifiEnabled(true);
	            int state = wifi.getWifiState();
	            if(state == WifiManager.WIFI_STATE_ENABLED) {
	                List<ScanResult> results = wifi.getScanResults();
	              
	     
	                for (ScanResult result : results) {
	                    if(result.BSSID.equals(wifi.getConnectionInfo().getBSSID())) {
	                    	h=result.BSSID.toString();
	                    	counts++;
	                     // fference + " signal state:" + signalStrangth);
	                    		//Log.d("[ad00]",""+result+"addd"+signalStrangth);
	                    }

	                }
	              
	            }
	            if(counts==0)
                {
                	String coordinate="12.9733°N,77.6197°E";
                	 AsyncTCPSend tcpSend= new AsyncTCPSend(coordinate);
                     tcpSend.execute();
                     return;
                	
                	
                }
	            else
	            {
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo wInfo = wifiManager.getConnectionInfo();
			//	String macAddress = wInfo.getMacAddress();
				String macAddress=getMacAddr();
				Log.d("[SUPER_TAG]", "[mac] = "+macAddress);
				 AsyncTCPSend tcpSend= new AsyncTCPSend(macAddress+","+h);
                 tcpSend.execute();
				Toast.makeText(MainActivity.this,"Working"+" "+macAddress,Toast.LENGTH_SHORT).show();
			}
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
	public void onResume() {
		super.onResume();
		// Add the following line to register the Session Manager Listener onResume
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
	}
 
	@Override
	public void onPause() {
		// Add the following line to unregister the Sensor Manager onPause
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}
	 public static String getMacAddr() {
	        try {
	            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
	            for (NetworkInterface nif : all) {
	                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
	 
	                byte[] macBytes = nif.getHardwareAddress();
	                if (macBytes == null) {
	                    return "";
	                }
	 
	                StringBuilder res1 = new StringBuilder();
	                for (byte b : macBytes) {
	                	res1.append(String.format("%02X:",b));
	                	
	                  //  res1.append(Integer.toHexString(b & 0xFF) + ":");
	                }
	 
	                if (res1.length() > 0) {
	                    res1.deleteCharAt(res1.length() - 1);
	                }
	                return res1.toString();
	            }
	        } catch (Exception ex) {
	        }
	        return "02:00:00:00:00:00";
	    }
	 public class AsyncTCPSend extends AsyncTask<Void, Void, Void> {
         String address;
         int port;
         String message;
         String response="";
         AsyncTCPSend(String mes) {
           //  address = h;
             //port = p;
             message = mes;
         }

         @Override
         protected Void doInBackground(Void... params) {
             Socket socket = null;
             try {
               //  socket = new Socket("10.42.0.67",6000);
            	 socket = new Socket("139.59.23.220",6000);
                 DataOutputStream writeOut = new DataOutputStream(socket.getOutputStream());
                 writeOut.writeUTF(message);
                 writeOut.flush();
                

               /*  ByteArrayOutputStream writeBuffer = new ByteArrayOutputStream(1024);
                 byte[] buffer = new byte[1024];

                 int bytesRead;
                 InputStream writeIn = socket.getInputStream();

                 while((bytesRead = writeIn.read(buffer)) != -1) {
                     writeBuffer.write(buffer,0,bytesRead);
                     response += writeBuffer.toString("UTF-8");
                 }
                 response = response.substring(4);
                 */
                    //Server sends extra "Null" string in front of data. This cuts it out
             } catch (UnknownHostException e){
                 e.printStackTrace();
                 response = "Unknown HostException: " + e.toString();
                 System.out.println(response);
             } catch (IOException e) {
                 response = "IOException: " + e.toString();
                 System.out.println(response);
             } finally {
                 if (socket != null) {
                     try {
                         socket.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
             return null;
         }

         @Override
         protected void onPostExecute(Void result) {
            // recieve.setText(response);
             super.onPostExecute(result);
         }
     }
}

