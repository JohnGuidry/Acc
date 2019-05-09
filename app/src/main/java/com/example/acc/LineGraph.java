package com.example.acc;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class LineGraph {
	
	static final int HUGE = 700;
	private double x[] = new double[HUGE];
	private double unSmoothY[] = new double[HUGE];
	private double smoothY[] = new double[HUGE];
	private double threshHold = 0.0;
	private double peakMean[] = new double[HUGE];
	
	public void setNoSmoothY(double[] a) {
		for(int i = 0; i < HUGE; i++) {
		unSmoothY[i] = a[i];
		}
	}
	
	public void setSmoothY(double[] a) {
		for(int i = 0; i < HUGE; i++) {
		smoothY[i] = a[i];
		}
	}
	
	public void setThreshHold(double thresh) {
		threshHold = thresh;
	}
	
	public void setPeakMean(double[] a) {
		for(int i = 0; i < HUGE; i++) {
		peakMean[i] = a[i];
		}
	}
	
	public Intent displayGraph(Context context) {
		
		// Our first data
		//int[] x = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }; // x values!
		//int[] y = { 30, 34, 45, 57, 77, 89, 100, 111 ,123 ,145 }; // y values!
		TimeSeries series = new TimeSeries("Magnitude(Raw data)"); 
		for( int i = 5; i < HUGE; i++) {
			x[i] = i;
			series.add(x[i], unSmoothY[i]);
		}
		
		// Our second data
		//int[] x2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }; // x values!
		//int[] y2 =  { 145, 123, 111, 100, 89, 77, 57, 45, 34, 30}; // y values!
		TimeSeries series2 = new TimeSeries("Smoothed data"); 
		for( int i = 0; i < HUGE; i++)
		{
			series2.add(x[i], smoothY[i]);
		}
		
		TimeSeries series3 = new TimeSeries("Peak Average");
		for(int i = 0; i < HUGE; i++) {
			series3.add(x[i], peakMean[i]);
		}
	
		
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph
		XYSeriesRenderer renderer = new XYSeriesRenderer(); // This will be used to customize line 1
		XYSeriesRenderer renderer2 = new XYSeriesRenderer(); // This will be used to customize line 2
		XYSeriesRenderer renderer3 = new XYSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
		mRenderer.addSeriesRenderer(renderer2);
		mRenderer.addSeriesRenderer(renderer3);
		
		//Raw data
		renderer.setColor(Color.WHITE);
		renderer.setPointStyle(PointStyle.SQUARE);
		renderer.setFillPoints(true);
		//Smoothed data
		renderer2.setColor(Color.YELLOW);
		renderer2.setPointStyle(PointStyle.DIAMOND);
		renderer2.setFillPoints(true);
		//ThreshHold
		renderer3.setColor(Color.RED);
		renderer3.setFillPoints(true);
		
		Intent intent = ChartFactory.getLineChartIntent(context, dataset, mRenderer, "Line Graph Title");
		return intent;
		
	}
}