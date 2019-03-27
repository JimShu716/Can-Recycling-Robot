package ca.mcgill.ecse211.main;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import ca.mcgill.ecse211.model.AssessCanColor;
import ca.mcgill.ecse211.model.AssessCanWeight;
import ca.mcgill.ecse211.model.CanLocator;
import ca.mcgill.ecse211.model.Clamp;
import ca.mcgill.ecse211.model.ColorClassification;
import ca.mcgill.ecse211.model.LightLocalizer;
import ca.mcgill.ecse211.model.Navigation;
import ca.mcgill.ecse211.model.Odometer;
import ca.mcgill.ecse211.model.OdometerExceptions;
import ca.mcgill.ecse211.model.Robot;
import ca.mcgill.ecse211.model.SearchZoneLocator;
import ca.mcgill.ecse211.model.UltrasonicLocalizer;

public class Project {

	// Motor and Sensor Ports
	public static final EV3LargeRegulatedMotor LEFT_MOTOR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final EV3LargeRegulatedMotor RIGHT_MOTOR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor SENSOR_MOTOR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor CLAMP_MOTOR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final TextLCD LCD = LocalEV3.get().getTextLCD();
	private static final Port US_PORT = LocalEV3.get().getPort("S3");
	private static final Port CS_PORT = LocalEV3.get().getPort("S1");
	private static final Port CS_FRONT_PORT = LocalEV3.get().getPort("S4");
	private static final Port TS_PORT = LocalEV3.get().getPort("S2");
	
	public static final int TEAM_NUMBER = 10;
	private static final String SERVER_IP = "192.168.2.8";
	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = false;

	public static void main(String[] args) throws OdometerExceptions {

		int buttonChoice;

		// Odometer related objects
		Odometer odometer = Odometer.getOdometer(LEFT_MOTOR, RIGHT_MOTOR, Robot.TRACK, Robot.WHEEL_RAD);
		Navigation navigator = new Navigation(LEFT_MOTOR,RIGHT_MOTOR);
		Display odometryDisplay = new Display(LCD); 

		// Ultrasonic sensor
		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(US_PORT);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];
		
		// Color Sensor (Localization)
		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes csSensor = new EV3ColorSensor(CS_PORT);
		SampleProvider csLineDetector = csSensor.getMode("Red");
		float[] csData = new float[csLineDetector.sampleSize()];
		
		 // Color Sensor (Color Classification)
        @SuppressWarnings("resource") // Because we don't bother to close this resource
        SensorModes clrSensor = new EV3ColorSensor(CS_FRONT_PORT);
        SampleProvider colorId =  clrSensor.getMode("RGB");
        float[] colorData = new float[colorId.sampleSize()];
        
        // Touch Sensor (Assess Can Weight)
        @SuppressWarnings("resource") // Because we don't bother to close this resource
        SensorModes myTouch = new EV3TouchSensor(TS_PORT);
        SampleProvider myTouchStatus =  myTouch.getMode(0);
        float[] tsData = new float[colorId.sampleSize()];
		
        WifiConnection wifi = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
        ColorClassification ClrClassify= new ColorClassification(colorData, colorId);
        AssessCanColor assessCanColor = new AssessCanColor(SENSOR_MOTOR, ClrClassify);
        AssessCanWeight assessCanWeight = new AssessCanWeight(tsData, myTouchStatus);
        Clamp clamp = new Clamp(CLAMP_MOTOR);
		
        do {
			
			LCD.clear();

			// ask the user whether robot should do Rising Edge or Falling Edge
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString(" Field |Test    ", 0, 2);
			LCD.drawString(" Test  |        ", 0, 3);
			LCD.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

			if (buttonChoice == Button.ID_LEFT) { // do Field Test
				LCD.clear();
				Robot robot = null;
				try {
					robot = new Robot(wifi);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				
				Thread odoThread = new Thread(odometer);
				odoThread.start();
				
				Thread odoDisplayThread = new Thread(odometryDisplay);
				odoDisplayThread.start();
				
				// Localization (Ultrasonic and Light)
				UltrasonicLocalizer ultrasonicLocalizer = new UltrasonicLocalizer(LEFT_MOTOR, RIGHT_MOTOR, usDistance, usData);
				LightLocalizer lightLocalizer = new LightLocalizer(LEFT_MOTOR, RIGHT_MOTOR, csLineDetector, csData, navigator);

				ultrasonicLocalizer.fallingEdge();

				lightLocalizer.moveClose();
				lightLocalizer.lightLocalize(0,0);
				
				Sound.beep();
				
				// Search Zone Locator
				SearchZoneLocator searchZonelocator = new SearchZoneLocator(robot, lightLocalizer, navigator);
				searchZonelocator.goToSearchZone();
				
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
	
				CanLocator canLocator = new CanLocator(robot, assessCanColor,assessCanWeight, clamp, 
														usDistance, usData, navigator,lightLocalizer);
				canLocator.RunLocator();
				
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				
			} else {
				LCD.clear();
				
				// Track Test
				/*
				navigator.turnTo(90);
				navigator.turnTo(-90);
				*/
				
				
				// WiFi Connection Test
				/*
				Robot robot = null;
				try {
					robot = new Robot(wifi);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				*/
				
				
				// Localization Test
				/*
				Thread odoThread = new Thread(odometer);
				odoThread.start();
				//Thread odoDisplayThread = new Thread(odometryDisplay);
				//odoDisplayThread.start();
				UltrasonicLocalizer ultrasonicLocalizer = new UltrasonicLocalizer(LEFT_MOTOR, RIGHT_MOTOR, usDistance, usData);
				// Localization (Ultrasonic and Light)
				LightLocalizer lightLocalizer = new LightLocalizer(LEFT_MOTOR, RIGHT_MOTOR, csLineDetector, csData, navigator);
				ultrasonicLocalizer.fallingEdge();
				lightLocalizer.moveClose();
				lightLocalizer.lightLocalize(3, 3);
				*/
				
				
				// Search Algorithm Test
				/*
				CanLocator canLocator = new CanLocator(robot, assessCanColor, usDistance, usData, 
						navigator,lightLocalizer);
				canLocator.RunLocator();
				*/
				
				
				// Color Classification Test
				/*
				while(true) {
					
					if (ClrClassify.run() !="no object") {	//if there is a can detected
				    LCD.drawString("Object Detected", 1,1);
				    LCD.drawString(ClrClassify.run(), 1, 2);
				    
				    } else {
				    	LCD.clear();
				    }

				    colorId.fetchSample(colorData, 0);
				    LCD.drawString("R: " + colorData[0], 1, 3);
			        LCD.drawString("G: " + colorData[1], 1, 4);
			        LCD.drawString("B: " + colorData[2], 1, 5);
				  
				}
				*/
				
				// Clamp Test
				
				clamp.grabCan();
				//clamp.offloadCan();
				
				
				// Can Color test
				
				assessCanColor.run();
				
			}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
