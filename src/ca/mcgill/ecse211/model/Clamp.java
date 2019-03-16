package ca.mcgill.ecse211.model;

import lejos.robotics.SampleProvider;

public class Clamp {
	private static final int ROTATE_AMOUNT = 90;
	private static final int ROTATION_SPEED = 50;
	private static final int TURN_ERROR = 10;
	private Navigation navigation;
	
	public Clamp(Navigation navigation) {
		this.navigation = navigation;
	}
	
	public void grabCan() {
	}
	
	public void offloadCan() {
	}
}
