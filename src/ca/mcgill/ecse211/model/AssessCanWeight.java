package ca.mcgill.ecse211.model;

import lejos.robotics.SampleProvider;

public class AssessCanWeight {

	
	private float[] tsData;
	private SampleProvider tsTouch;
	
	public AssessCanWeight(float[] tsData, SampleProvider tsTouch) {
		this.tsData = tsData;
		this.tsTouch = tsTouch;
	}
	
	public int run() {
		return 0;
	}
	
	private float sampleData(){
		tsTouch.fetchSample(tsData, 0);
		return tsData[0];
	}
}
