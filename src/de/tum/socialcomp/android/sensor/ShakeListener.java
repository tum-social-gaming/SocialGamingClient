package de.tum.socialcomp.android.sensor;


/* The following code was written by Matthew Wiggins & Niklas Klügel
 * and is released under the APACHE 2.0 license
 * 
 * Original version taken from:
 * http://android.hlidskialf.com/blog/code/android-shake-detection-listener
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * This class is used to track the acceleration sensor for
 * shaking motions. If this happened an event will be delivered to
 * instances of a registered OnShakeListener.
 */
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.content.Context;
import java.lang.UnsupportedOperationException;
import java.util.LinkedList;

public class ShakeListener implements SensorEventListener {
	
  private static final int FORCE_THRESHOLD = 550;
  private static final int TIME_THRESHOLD = 100;
  private static final int SHAKE_TIMEOUT = 500;
  private static final int SHAKE_DURATION = 1000;
  private static final int SHAKE_COUNT = 3;

  private float lastX=-1.0f, lastY=-1.0f, lastZ=-1.0f;
  private long 	lastTime;
  private Context context;
  private int 	shakeCount = 0;
  private long 	lastShake;
  private long 	lastForce;
  
  Sensor accelerationSensor;
  SensorManager sensorManager;
  
  private Object OnShakeListenerMutex = new Object();
  
  private LinkedList<OnShakeListener> onShakeListeners = new LinkedList<OnShakeListener>();
  
  public ShakeListener(Context context) {
	  this.context = context;	  
	  sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	  
	  resume();
  }  
  
  /**
   * This will register a OnShakeListener, keep in mind that the behavior is
   * one-shot based, thus the listener will be called only once and then removed
   * from the queue
   * 
   * @param listener
   */

  public void addOnShakeListenerOneShot(OnShakeListener listener) {
	synchronized( this.OnShakeListenerMutex ){
		this.onShakeListeners.add(listener);
	}  
  }
  
  public void resume() {
    this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    
    if (this.sensorManager == null) {
      throw new UnsupportedOperationException("Sensors not supported");
    }
    
    accelerationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    
    sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    
  }

  public void pause() {
    if (sensorManager != null) {
    	sensorManager.unregisterListener(this, accelerationSensor);
    	sensorManager = null;
    }
  }  
  
  @Override
  public void onSensorChanged(SensorEvent event) {
	  if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		    float gravity[] = event.values.clone();
		    float xAcc = gravity[0];
		    float yAcc = gravity[1];
		    float zAcc = gravity[2];
		  
		    long now = System.currentTimeMillis();		    
		    
		    if ((now - lastForce) > SHAKE_TIMEOUT) {
		        shakeCount = 0;
		    }
		    
		    if ((now - lastTime) > TIME_THRESHOLD) {
		        long diff = now - lastTime;		        
		        float speed = Math.abs(xAcc + yAcc + zAcc - lastX - lastY - lastZ) / diff * 10000;
		        
		        if (speed > FORCE_THRESHOLD) {
		          if ((++shakeCount >= SHAKE_COUNT) && (now - lastShake > SHAKE_DURATION)) {
		            lastShake = now;
		            shakeCount = 0;
		            
		            synchronized (this.OnShakeListenerMutex) {
		            	
		            	for(OnShakeListener shakeListener: this.onShakeListeners){
			            	shakeListener.onShake();
			            }
		            	
		            	// after the events have been fired we will remove the listeners (on shot property)
		            	
		            	this.onShakeListeners.clear();
					}
		          }
		          lastForce = now;
		        }
		        
		        lastTime = now;
		        lastX = xAcc;
		        lastY = yAcc;
		        lastZ = zAcc;
		      }
		    }
		    
	  }
  
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
  	
}