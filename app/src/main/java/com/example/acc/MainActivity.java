package com.example.acc;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
    /** Called when the activity is first created. */
	
	final Context context = this;
	Sensor accelerometer;
	SensorManager sm;
	TextView thresh;
	TextView acceleration;
	TextView magnitude;
	TextView steps;
	TextView peakAcc;
	TextView peaks;
	TextView peakMeanDis;
	TextView counter;
	int count = 0;
	double threshHold = 1.3;
	int peakCount = 0;
	double peakAccumulate = 0;
	int stepCount = 0;
	int periods = 5;
	double peakMean = 0;
	static final int HUGE = 700;
	double unsmoothed[] = new double[HUGE];
	double smoothed[] = new double[HUGE];
	double peakAverage[] = new double[HUGE];

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sm.registerListener(this, accelerometer,
			SensorManager.SENSOR_DELAY_NORMAL);
		thresh = (TextView) findViewById(R.id.thresh);
		acceleration = (TextView) findViewById(R.id.acceleration);
		steps = (TextView) findViewById(R.id.steps);
		magnitude = (TextView) findViewById(R.id.magnitude);
		peakAcc = (TextView) findViewById(R.id.peakAcc);
		peaks = (TextView) findViewById(R.id.peaks);
		peakMeanDis = (TextView) findViewById(R.id.peakMeanDis);
		counter = (TextView) findViewById(R.id.counter);
    }
    
    public void lineGraphHandler (View view) {
    	LineGraph line = new LineGraph();
    	line.setNoSmoothY(unsmoothed);
    	line.setSmoothY(smoothed);
    	line.setPeakMean(peakAverage);
    	Intent lineIntent = line.displayGraph(this);	
        startActivity(lineIntent);
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {	
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//int tempStepCount = 0;
		thresh.setText("Thresh: " + threshHold);
		acceleration.setText("X: " + event.values[0] + "\nY: "
				+ event.values[1] + "\nZ: " + event.values[2]);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		counter.setText("Counter: " + count);
		
		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		magnitude.setText("Magnitude: " + String.valueOf(accelationSquareRoot)); // display
																					// 3D
																					// acceleration
																					// vector
			unsmoothed[count] = accelationSquareRoot;
			count++;
			
			movingAverage(unsmoothed,periods);//exponentialSmoothing(a, b, 0.2f);

			if (count % 100 == 0  || count == HUGE - 2) {
				// ASSERT: Count is greater than 2, so data can be accessed in a
				// -1 and +1 manner
				peakCount = 0;
				peakAccumulate = 0;
				for (int i = 1; i < count; i++) {
					double forwardSlope = smoothed[i + 1] - smoothed[i];
					double backwardSlope = smoothed[i] - smoothed[i - 1];
					if (forwardSlope < 0 && backwardSlope > 0) {
						//tempPeakCount++;
					//	if(tempPeakCount > peakCount) {
							peakCount++;
							peakAccumulate = peakAccumulate + smoothed[i];
					//	}
					}
				}
				peakAcc.setText("peakAccumulate: " + peakAccumulate);
				peaks.setText("Peaks: " + peakCount);
				
				//TODO: Make function
			        peakMean = peakAccumulate / peakCount;
					for (int i = count - 100; i < count; i++) {
			         peakAverage[i + 1] = peakMean;
					}
				
				peakMeanDis.setText("peakMean: " + peakMean);
				stepCount = 0;
				for (int i = 1; i < count; i++) {
					double forwardSlope = smoothed[i + 1] - smoothed[i];
					double backwardSlope = smoothed[i] - smoothed[i - 1];
					if (forwardSlope < 0 && backwardSlope > 0
							&& smoothed[i] > 0.7 * peakMean && smoothed[i] >= threshHold) {
						//tempStepCount++;
						//if(tempStepCount > stepCount) {
						stepCount = stepCount + 1;  //was peakCount + 1...
						//}
					}
				}
			}
			steps.setText("Steps: " + stepCount);
			
			//TODO: MAKE INTO A FUNCTION
			if (count == HUGE - 2) {
				// ASSERT: Count is equal to 500 or the +1 to the array size
				count = 0;
				for (int i = (HUGE - 2) - periods; i <= HUGE - 2; i++) {
					unsmoothed[count] = unsmoothed[i];
					smoothed[count]= smoothed[i];
					count++;
				}
			}
	}
	
	private void movingAverage(double[] array, int numPeriods) {
		double total = 0;
		double average = 0;
		if(count >= numPeriods) {
		for(int i = count - numPeriods; i <= count; i++) {
			total = total + array[i];
		}
		average = total / numPeriods;
		smoothed[count - numPeriods] = average;
		}
	}
	
	// PRE: input,output,alpha are all defined.
	// POST: smoothes the wave-like graph from accelerometer magnitude data
	private double[] exponentialSmoothing(double[] input, double[] output,
			float alpha) {
		if (output == null) {
			return input;
		}
		for (int i = 0; i < input.length; i++) {
			output[i] = output[i] + alpha * (input[i] - output[i]);
		}
		return output;
	}
	
	// PRE: view is defined
	// POST: The user has hit the enter button to change the threshHold and to
	// reset stepCount,count,peakCount,peakAccumulate to zero
	public void enterMessage(View view) {
		EditText editText = (EditText) findViewById(R.id.edit_message);
		try {
			double message = Double.parseDouble(editText.getText().toString());
			threshHold = message;
		} catch (NumberFormatException nfe) {
			System.out.println("Could not parse " + nfe);
		}
		stepCount = 0;
		count = 0;
		peakCount = 0;
		peakAccumulate = 0;
	}
}
	
// double butterWorth = 1 / (1 + Math.pow((accelationSquareRoot
// /25),40.0));

// double butterWorth = Math.sqrt(20)
// / (2 * 3.14 * 25 * accelationSquareRoot);

// butter.setText("butterWorth: " + butterWorth);
// if(butterWorth > 0.005) {
// unsmoothed[count] = butterWorth;
// count++;

/*
 * accLowPassSignal = lowPass(event.values[1],ALPHA); accThreshold =
 * lowPass(event.values[1],ALPHA_SMALL); if(accLowPassSignal < accThreshold) {
 * numSteps = (int) ((60.0 * 75) / (sample - currentSample)); currentSample =
 * sample; } sample++; //TO-DO: MEDIAN FILTER bpm.setText("BPM: "+numSteps);
 */

// This one was being used last.
// @see
// http://seattlesensor.wordpress.com/2013/01/01/accelerometer-sensor-data-processing/
// Smoothes the data coming in from the accelerometer on the y-axis
/*
 * float lowPass(float y, float freq) { float filteredValue = 0; filteredValue =
 * y * freq + filteredValue * (MAX-freq); return (filteredValue); }
 */

// @see
// http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter
// @see http://en.wikipedia.org/wiki/Low-pass_filter
// Smoothes the data coming from the accelerometer on the y-axis
/*
 * protected float[] lowPass( float[] input, float[] output ) { if ( output ==
 * null ) return input;
 * 
 * for ( int i=0; i<input.length; i++ ) { output[i] = output[i] + ALPHA *
 * (input[i] - output[i]); } return output; }
 */

// Smoothes the data coming frm the accelerometer on the y-axis
/*
 * protected float lowPass(float input, float output, float freq) { output =
 * output + freq * (input - output); return(output); }
 */
