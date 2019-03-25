package ca.mcgill.ecse211.model;

import ca.mcgill.ecse211.main.Project;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class CanLocator {

	private Odometer odo;
	private Navigation navigator;
	private LightLocalizer lightLocalizer;
	
	private AssessCanColor assessCanColor;
	private AssessCanWeight assessCanWeight;
	private Clamp clamp;
	
	private SampleProvider usDistance;
	private float[] usData;
	
	private static int FORWARD_SPEED = 100;
	private static double OFFSET = 0.5;
	private static final double TILE_SIZE = 30.48;
	private static final double CAN_DISTANCE_ON_BORDER = 18.5;
	private static final double ANGLE_ERROR = 10.0;
	private static final double DISTANCE_ERROR = 4.0;
	private static final double TEST_VALUE = 6;
	private static final double TEST_ANGLE = 15.0;
	private double canAngle = 0;
	private double canDistance = 0;
	private int ENDX = 0, ENDY = 0;
	private double Cx = 0,Cy = 0;
	private int count = 0;
	private static boolean loopStop = false;
	
	private int LLx, LLy, URx, URy;
	private int islandLLX, islandLLY, islandURX, islandURY;
	
	/**
	 * This class allows the EV3 to search for cans and identify their colors and weights.
	 * @author Mohamed Samee
	 */
	
	public CanLocator(Robot robot, AssessCanColor assessCanColor, SampleProvider usDistance, float[] usData, 
			Navigation navigator, LightLocalizer lightLocalizer) throws OdometerExceptions {
		odo = Odometer.getOdometer();
		this.usDistance = usDistance;
		this.usData = usData;
		this.assessCanColor = assessCanColor;
		this.navigator = navigator;
		LLx = robot.getSearchZoneLLX();
		LLy = robot.getSearchZoneLLY();
		URx = robot.getSearchZoneURX();
		URy = robot.getSearchZoneURY();
		islandLLX = robot.getIslandLLX();
		islandLLY = robot.getIslandLLY();
		islandURX = robot.getIslandURX();
		islandURY = robot.getIslandURY();
		this.Cy = LLy;
		this.Cx = LLx;
		this.ENDX = LLx+1;
		this.ENDY = LLy;
		this.lightLocalizer = lightLocalizer; 
	}
	
	/**
	 * RunLocator() is the method that runs the algorithm for searching for the correct can.
	 * It drives the EV3 forward and in a square around teh search zone and looks for cans.
	 * If a can is detected, it calls for the searchProcess(), otherwise it calls goToNext().
	 * Once it has traveled around the whole zone without finding the correct can it then travels
	 * to the upper right corner.
	 */
	
	public void RunLocator(){
		
		while (true && !loopStop) {	
			
			//when EV3 goes full circle with the algorithm
			//and ends where it started, break the loop.
			if(Cx == ENDX && Cy == ENDY) {
				
				navigator.turnTo(135);
				lightLocalizer.lightLocalize(Cx,Cy);
				
				//If no can was found once search algorithm is finished, go to Upper Right
				navigator.travelTo(Cx,LLy-OFFSET);
				navigator.travelTo(URx+OFFSET,LLy-OFFSET);
				navigator.travelTo(URx+OFFSET,URy);
				navigator.travelTo(URx,URy);
				navigator.turnTo(135);
				lightLocalizer.lightLocalize(URx,URy);
				break;
			}			
			
			//checkCan takes 90 degrees as initial input(i.e this is assuming the tile
			//has not been scanned yet, so a full 90 degree turn is required)
			else if(!checkCan(90)){
					
					goToNext();
				
			}
			
			//checks a can in front of it
			else{
				
				searchProcess();
				
			}
			
		}
		
	}
	
	
	/**
	 * searchProcess() runs when the EV3 detects a can. When detected, it drives to it and checks its color.
	 * If the color is correct, it beeps once and travels to the upper right corner. Otherwise it
	 * reverses and calls one of the dodge methods depending on where the can was spotted.
	 * For instance, If an incorrect colored can is placed on the border
	 * the EV3 dodges outwards, and then sets the distanceToCan to CAN_DISTANCE_FROM_OUT so that
	 * the EV3 knows how far to move towards a can if it spots one.
	 */
	
	private void searchProcess(){
		
		boolean inside = false;
		
		Sound.beep();
		//outside the border
		if(Cx*TILE_SIZE < LLx*TILE_SIZE-DISTANCE_ERROR || Cy*TILE_SIZE > URy*TILE_SIZE+DISTANCE_ERROR 
				|| Cx*TILE_SIZE > URx*TILE_SIZE+DISTANCE_ERROR || Cy*TILE_SIZE < LLy*TILE_SIZE-DISTANCE_ERROR){

			inside = false;
		}                           
		
		//on the border
		else{			
			if(Cx == LLx && (odo.getXYT()[2] > (90-ANGLE_ERROR) && odo.getXYT()[2] < (90+ANGLE_ERROR))){
				
				Cx = (Cx*TILE_SIZE+CAN_DISTANCE_ON_BORDER)/TILE_SIZE;
				
			}
			else if (Cy == URy && (odo.getXYT()[2] > (180-ANGLE_ERROR) && odo.getXYT()[2] < (180+ANGLE_ERROR))) {
				
				Cy = (Cy*TILE_SIZE-CAN_DISTANCE_ON_BORDER)/TILE_SIZE;
				
			}
			else if ( Cx == URx && odo.getXYT()[2] > (270-ANGLE_ERROR) && odo.getXYT()[2] < (270+ANGLE_ERROR)){
				
				Cx = (Cx*TILE_SIZE-CAN_DISTANCE_ON_BORDER)/TILE_SIZE;
				
			}
			else if( Cy == LLy && odo.getXYT()[2] > (360-ANGLE_ERROR) || odo.getXYT()[2] < (0+ANGLE_ERROR)){
				
				Cx = (Cx*TILE_SIZE+CAN_DISTANCE_ON_BORDER)/TILE_SIZE;
				
			}
			
			inside = true;
		}                      
		
		if(assessCan(canDistance = (readUSDistance()-(TEST_VALUE))) ){
			
			if(inside){
				navigator.driveBack(canDistance);
		        travelToURBorder();
			}
		
			else{
			    navigator.driveBack(canDistance);
				travelToUROutside(); //NEED CHECK
			}
			
		}
		
		else{ 				//NEEDS INTENSE CHECK
		    
			if(inside){
			    
				navigator.driveBack(canDistance);
				
				if(canAngle < ANGLE_ERROR) borderDodge();
		        
				else {
				    
				    navigator.driveBack(canDistance);
		        	navigator.turnTo(-canAngle);
		        	goToNext(); 
				    
				}
			}
			
			else{
				outsideDodge(); //NEED CHECK
			}
		}
	}
	
	/**
	*checkCan() returns true if a can was spotted by the ultrasonic sensor within the
	*range of a tile. Otherwise, it returns false.
	*/
	
	private boolean checkCan(double angle){
	
		canAngle = 0;
	    double currentAngle = odo.getXYT()[2];
	    
	    System.out.println("currentAng:"+currentAngle);
	    
		//begin rotating to scan for cans 
		navigator.turnToScan(angle);
        
        while (readUSDistance() > TILE_SIZE*Math.sqrt(2.0)) {
            
            //keep turning until the distance of the US is less than a tile (i.e a can is detected)
            
            //if the motors finish the 90 degree turn, and no can is found, the method returns false
            if(!(Project.LEFT_MOTOR.isMoving()) || !(Project.RIGHT_MOTOR.isMoving())) {
                
                navigator.turnTo(-90);
                return false;
            }
            
        }
        
        //if can is found, stop motors and record the angle the can was detected at
        Project.LEFT_MOTOR.stop(true);
        Project.RIGHT_MOTOR.stop();
        navigator.turnTo(TEST_ANGLE);
//        System.out.println("firstAng:"+odo.getXYT()[2]);
        canAngle = odo.getXYT()[2] - currentAngle;
        
//        System.out.println("canAngle:"+canAngle);
        
       if(canAngle < -110){
            
            canAngle = 360+canAngle-(ANGLE_ERROR/2);
            
        }
        
        return true;
		
	}
	
	/**
	*assessCan() is a method that is called after checkCan(). It 
	*makes the EV3 beep depending on the color of the can scanned.
	*@param distance
	*/
	
	private void assessCan(double distance){

		int heavy = 0;
		navigator.driveForward(distance);
				
		while(Project.LEFT_MOTOR.isMoving() && Project.RIGHT_MOTOR.isMoving()){
			heavy = heavy | assessCanWeight.run();
		}
		
		clamp.grabCan();
		
		//Beeps depending on the color and weight of the can
		if(heavy == 1){
			switch (assessCanColor.run()) {

				case 1: Sound.playTone(500, 1000);
						break;

				case 2: Sound.playTone(500, 1000);
						Sound.playTone(500, 1000);
						break;

				case 3: Sound.playTone(500, 1000);
						Sound.playTone(500, 1000);
						Sound.playTone(500, 1000);
						break;

				case 4: Sound.playTone(500, 1000);
						Sound.playTone(500, 1000);
						Sound.playTone(500, 1000);
						Sound.playTone(500, 1000);
						break;
				default: Sound.buzz(); //this means incorrect identification 
						 break;
			}
		}
		
		else{
			switch (assessCanColor.run()) {

				case 1: Sound.playTone(500, 500);
						break;

				case 2: Sound.playTone(500, 500);
						Sound.playTone(500, 500);
						break;

				case 3: Sound.playTone(500, 500);
						Sound.playTone(500, 500);
						Sound.playTone(500, 500);
						break;

				case 4: Sound.playTone(500, 500);
						Sound.playTone(500, 500);
						Sound.playTone(500, 500);
						Sound.playTone(500, 500);
						break;
				default: Sound.buzz(); //this means incorrect identification 
						 break;
			}
			
		}
	} 
	
	/**
	*goToNext() moves the EV3 forward to the next position when no cans are detected.
	*/

	private void goToNext() { 

		navigator.driveForward(TILE_SIZE);

		//keeps coordinate values in check to update odo if needed
		if(Cy < URy && Cx==LLx) {
			
			Cy = Cy+1;
			odo.setY(Cy*TILE_SIZE);
		}
		else if(Cx < URx && Cy==URy) { 
			
			Cx = Cx+1;
			odo.setX(Cx*TILE_SIZE);
		}
		else if(Cy > LLy && Cx==URx) {
			
			Cy=Cy-1;
			odo.setY(Cy*TILE_SIZE);
		}
		
		//ENDX is the x coordinate of the final position of the EV3
		else if(Cx > ENDX && Cy==LLy) {
			
			Cx=Cx-1;
			odo.setX(Cx*TILE_SIZE);
		}
		
		//if the EV3 is at one of the 3 corners of the search zone, localize then turn right to stay on the zone border
		if( (Cx*TILE_SIZE > (LLx*TILE_SIZE-DISTANCE_ERROR) && Cx*TILE_SIZE < (LLx*TILE_SIZE+DISTANCE_ERROR) 
		    && Cy*TILE_SIZE > (URy*TILE_SIZE-DISTANCE_ERROR) && Cy*TILE_SIZE < (URy*TILE_SIZE+DISTANCE_ERROR))){
		    
		    navigator.turnTo(45);
		    lightLocalizer.lightLocalize(Cx,Cy);
		    navigator.turnTo(90);
			goToNext();
		    
		}
			  
	    else if ((Cx*TILE_SIZE > (URx*TILE_SIZE-DISTANCE_ERROR) && Cx*TILE_SIZE < (URx*TILE_SIZE+DISTANCE_ERROR) 
	            && Cy*TILE_SIZE > (URy*TILE_SIZE-DISTANCE_ERROR) && Cy*TILE_SIZE < (URy*TILE_SIZE+DISTANCE_ERROR))){
	        
	        navigator.turnTo(-45);
		    lightLocalizer.lightLocalize(Cx,Cy);
		    navigator.turnTo(180);
			goToNext();
	        
	    }
		else if((Cx*TILE_SIZE > (URx*TILE_SIZE-DISTANCE_ERROR) && Cx*TILE_SIZE < (URx*TILE_SIZE+DISTANCE_ERROR) 
		        && Cy*TILE_SIZE > (LLy*TILE_SIZE-DISTANCE_ERROR) && Cy*TILE_SIZE < (LLy*TILE_SIZE+DISTANCE_ERROR))){
					
			navigator.turnTo(-135);
		    lightLocalizer.lightLocalize(Cx,Cy);
		    navigator.turnTo(-90);
			goToNext();
					
		}
		canDistance = 0;
	}
	
	/**
	*travelToURBorder() is called when the correct can is found on the edge of the search zone. This
	*method will use travelTo() from the Navigator class to get the EV3 to the upper right corner.
	*/
	
	private void travelToURBorder() {
		
		navigator.turnTo(-canAngle);
		
		if ( (odo.getXYT()[2] >= 360-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 0+ANGLE_ERROR)){
			
			navigator.turnTo(45);
			lightLocalizer.lightLocalize(Cx,Cy);
			navigator.travelTo(LLx-OFFSET, Cy);
			navigator.travelTo(LLx-OFFSET,URy+OFFSET);
			navigator.travelTo(URx,URy+OFFSET);
			navigator.travelTo(URx,URy);
			navigator.turnTo(-135);
			lightLocalizer.lightLocalize(Cx,Cy);
		}
		
		else if ( (odo.getXYT()[2] >= 90-ANGLE_ERROR) && 
		    	(odo.getXYT()[2] <= 90+ANGLE_ERROR) ){

			navigator.turnTo(-45);
			lightLocalizer.lightLocalize(Cx,Cy);
			navigator.travelTo(Cx,URy+OFFSET);
			navigator.travelTo(URx,URy+OFFSET);
			navigator.travelTo(URx,URy);
			navigator.turnTo(-135);
			lightLocalizer.lightLocalize(Cx,Cy);
		}
		
		else if ( (odo.getXYT()[2] >= 180-ANGLE_ERROR) && 
		    	(odo.getXYT()[2] <= 180+ANGLE_ERROR) ){
		
			navigator.turnTo(-135);
			lightLocalizer.lightLocalize(Cx,Cy);
			navigator.travelTo(URx+OFFSET,Cy);
			navigator.travelTo(URx+OFFSET,URy);
			navigator.travelTo(URx,URy);
			navigator.turnTo(135);
			lightLocalizer.lightLocalize(Cx,Cy);
		}
		
		else if ( (odo.getXYT()[2] >= 270-ANGLE_ERROR) &&
		    	(odo.getXYT()[2] <= 270+ANGLE_ERROR) ){
		
			navigator.turnTo(135);
			lightLocalizer.lightLocalize(Cx,Cy);
			navigator.travelTo(Cx,LLy-OFFSET);
			navigator.travelTo(URx+OFFSET,LLy-OFFSET);
			navigator.travelTo(URx+OFFSET,URy);
			navigator.travelTo(URx,URy);
			navigator.turnTo(135);
			lightLocalizer.lightLocalize(Cx,Cy);
		}
		
		loopStop = true;
	}
	
	/**
	 * This method fetches the distance from the Ultrasonic sensor.
	 * @return distance
	 */
	
	private int readUSDistance() {
		//this method returns the ultrasonic distance read.
		usDistance.fetchSample(usData, 0);
		return (int) (usData[0] * 100);
		
	}

  
}	

