package ca.mcgill.ecse211.model;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class CanLocator {

	private Odometer odo;
	private Navigation navigator;
	private LightLocalizer lightLocalizer;
	
	private AssessCanColor assessCanColor;
	private AssessCanWeight assessCanWeight;
	
	private SampleProvider usDistance;
	private float[] usData;
	
	private static double OFFSET = 0.5;
	private static final double TILE_SIZE = 30.48;
	private static final double CAN_DISTANCE_ON_BORDER = 18.5;
	private static final double CAN_DISTANCE_FROM_OUT = 11.75;
	private static final double ANGLE_ERROR = 5.0;
	private static final double DISTANCE_ERROR = 7.0;
	private int ENDX = 0, ENDY = 0;
	private int Cx = 0,Cy = 0;
	
	private int TR;  //this variable stores the integer defining the target can color.
	private int searchZoneLLX, searchZoneLLY, searchZoneURX, searchZoneURY;
	private int islandLLX, islandLLY, islandURX, islandURY;
	
	public CanLocator(Robot robot, AssessCanColor assessCanColor, SampleProvider usDistance, float[] usData, 
			Navigation navigator, LightLocalizer lightLocalizer) throws OdometerExceptions {
		odo = Odometer.getOdometer();
		this.usDistance = usDistance;
		this.usData = usData;
		this.assessCanColor = assessCanColor;
		this.navigator = navigator;
		TR = robot.getGreenTeam();
		searchZoneLLX = robot.getSearchZoneLLX();
		searchZoneLLY = robot.getSearchZoneLLY();
		searchZoneURX = robot.getSearchZoneURX();
		searchZoneURY = robot.getSearchZoneURY();
		islandLLX = robot.getIslandLLX();
		islandLLY = robot.getIslandLLY();
		islandURX = robot.getIslandURX();
		islandURY = robot.getIslandURY();
		this.Cy = searchZoneLLY;
		this.Cx = searchZoneLLX;
		this.ENDX = searchZoneLLX+1;
		this.ENDY = searchZoneLLY;
		this.lightLocalizer = lightLocalizer; 
	}
	
	public void RunLocator(){
		
		while (true) {	
			
			//when EV3 goes full circle with the algorithm
			//and ends where it started, break the loop.
			if(Cx == ENDX && Cy == ENDY) break;
			
			//System.out.println("US"+readUSDistance());
			//checks a can in front of it
			else if(checkCan()){
				
				searchProcess();
				
			}
			
			else{
				System.out.println("else odo x:"+odo.getXYT()[0]);
				System.out.println("else odo y:"+odo.getXYT()[1]);
				if((odo.getXYT()[0] > (searchZoneLLX*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[0] < (searchZoneLLX*TILE_SIZE+DISTANCE_ERROR) && odo.getXYT()[1] > (searchZoneLLY*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[1] < (searchZoneLLY*TILE_SIZE+DISTANCE_ERROR))
						|| (odo.getXYT()[0] > (searchZoneLLX*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[0] < (searchZoneLLX*TILE_SIZE+DISTANCE_ERROR) && odo.getXYT()[1] > (searchZoneURY*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[1] < (searchZoneURY*TILE_SIZE+DISTANCE_ERROR))
						|| (odo.getXYT()[0] > (searchZoneURX*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[0] < (searchZoneURX*TILE_SIZE+DISTANCE_ERROR) && odo.getXYT()[1] > (searchZoneURY*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[1] < (searchZoneURY*TILE_SIZE+DISTANCE_ERROR))
						 || (odo.getXYT()[0] > (searchZoneURX*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[0] < (searchZoneURX*TILE_SIZE+DISTANCE_ERROR) && odo.getXYT()[1] > (searchZoneLLY*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[1] < (searchZoneLLY*TILE_SIZE+DISTANCE_ERROR))){
					
					goToNext();
					
				}
				
				else {
					
					navigator.turnTo(-90);
					if(checkCan()){
						
						searchProcess();
						
					}
					
					else goToNext();
					
				}
			}
			
		}
		
		//If no can was found once algorithm is finished, go to Upper Right
		navigator.travelTo(odo.getXYT()[0]/TILE_SIZE,searchZoneLLY-OFFSET);
		navigator.travelTo(searchZoneURX+OFFSET,searchZoneLLY-OFFSET);
		navigator.travelTo(searchZoneURX+OFFSET,searchZoneURY);
		navigator.travelTo(searchZoneURX,searchZoneURY);
		
	}	
	
	/**
	 * big method
	 */
	
	private void searchProcess(){
		
		double distanceToCan = 0.0;
		boolean inside = false;
		
		//System.out.println("inside check can");
		Sound.beep();
		//outside the border
		if(odo.getXYT()[0] < searchZoneLLX*TILE_SIZE-DISTANCE_ERROR || odo.getXYT()[1] > searchZoneURY*TILE_SIZE+DISTANCE_ERROR 
				|| odo.getXYT()[0] > searchZoneURX*TILE_SIZE+DISTANCE_ERROR || odo.getXYT()[1] < searchZoneLLY*TILE_SIZE-DISTANCE_ERROR){
			System.out.println(odo.getXYT()[0]);
			System.out.println(odo.getXYT()[1]);

			distanceToCan = CAN_DISTANCE_FROM_OUT;
			inside = false;
		}
		
		//on the border
		else{
			distanceToCan = CAN_DISTANCE_ON_BORDER;
			inside = true;
		}
	
		//System.out.println("distance to can" + distanceToCan);
		if(checkColor(distanceToCan)){
			
			System.out.println("inside:"+inside);
			if(inside){

				
				if((odo.getXYT()[0] > (searchZoneLLX*TILE_SIZE+10) && (odo.getXYT()[2] > (90-ANGLE_ERROR) && odo.getXYT()[2] < (90+ANGLE_ERROR)))
						|| (odo.getXYT()[1] < (searchZoneURY*TILE_SIZE-10) && (odo.getXYT()[2] > (180-ANGLE_ERROR) && odo.getXYT()[2] < (180+ANGLE_ERROR)))
						|| (odo.getXYT()[0] < (searchZoneURX*TILE_SIZE-10) && (odo.getXYT()[2] > (270-ANGLE_ERROR) && odo.getXYT()[2] < (270+ANGLE_ERROR)))
						 || odo.getXYT()[1] > (searchZoneLLY*TILE_SIZE+10) && (odo.getXYT()[2] > (0-ANGLE_ERROR) && odo.getXYT()[2] < (0+ANGLE_ERROR))){
					System.out.println("odo t"+ odo.getXYT()[2]);
					System.out.println("odo x"+ odo.getXYT()[0]);
					System.out.println("odo y"+ odo.getXYT()[1]);
					travelToURInside();
				}
				
				else{ 
					System.out.println("travel to UR border happens");
					travelToURBorder();
				}
			}
		
			else{
				travelToUROutside();
			}
			
		}
		
		else{
			//System.out.println("inside:" + inside);
			if(inside){
				
				if((odo.getXYT()[0] > (searchZoneLLX*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[0] < (searchZoneLLX*TILE_SIZE+DISTANCE_ERROR) && (odo.getXYT()[2] > (0-ANGLE_ERROR) && odo.getXYT()[2] < (0+ANGLE_ERROR)))
						|| (odo.getXYT()[1] < (searchZoneURY*TILE_SIZE+DISTANCE_ERROR) && odo.getXYT()[1] > (searchZoneURY*TILE_SIZE-DISTANCE_ERROR) && (odo.getXYT()[2] > (90-ANGLE_ERROR) && odo.getXYT()[2] < (90+ANGLE_ERROR)))
						|| (odo.getXYT()[0] < (searchZoneURX*TILE_SIZE+DISTANCE_ERROR) && odo.getXYT()[0] > (searchZoneURX*TILE_SIZE-DISTANCE_ERROR) && (odo.getXYT()[2] > (180-ANGLE_ERROR) && odo.getXYT()[2] < (180+ANGLE_ERROR)))
						 || odo.getXYT()[1] > (searchZoneLLY*TILE_SIZE-DISTANCE_ERROR) && odo.getXYT()[1] < (searchZoneLLY*TILE_SIZE+DISTANCE_ERROR) && (odo.getXYT()[2] > (270-ANGLE_ERROR) && odo.getXYT()[2] < (270+ANGLE_ERROR))){
					System.out.println("border dodge");
//					System.out.println("odo t"+ odo.getXYT()[2]);
//					System.out.println("odo x"+odo.getXYT()[0]);
//					System.out.println("odo y"+ odo.getXYT()[1]);
					borderDodge();
				}
				
				else{
					System.out.println("inside dodge");
					insideDodge();
				}
			}
			
			else{
				outsideDodge();
			}
		}
		
	}
	
	/**
	*checkCan() returns true if a can was spotten by the ultrasonic sensor within the
	*range of a tile. Otherwise, it returns false.
	*/
	
	//robot is facing the can
	private boolean checkCan(){
	
		//read sensor and see if a can is detected in range
		if(readUSDistance() <= TILE_SIZE+DISTANCE_ERROR) return true;
		else return false;
		
	}
	
	/**
	*checkColor() is a method that is called after checkCan(). It 
	*makes the EV3 beep once and return true if the can scanned is the target can
	*Otherwise, it will beep twice and return false.
	*/
	
	private boolean checkColor(double distance){

		navigator.moveToCan(distance);
		System.out.println("moved to " + distance);
		
		//if the can color is the target color, beep once
		if (TR == assessCanColor.run()) {
			Sound.beep();
			return true;
		}
		
		//otherwise, beep twice
		else {
			Sound.beep(); 
			Sound.beep();
			return false;
		}
	} 
	
	/**
	*goToNext() moves the EV3 forward to the next position when no cans are detected.
	*/

	private void goToNext() { 
		
		if(odo.getXYT()[0] < searchZoneLLX*TILE_SIZE-DISTANCE_ERROR || odo.getXYT()[1] > searchZoneURY*TILE_SIZE+DISTANCE_ERROR || 
				odo.getXYT()[0] > searchZoneURX*TILE_SIZE+DISTANCE_ERROR || odo.getXYT()[1] < searchZoneLLY*TILE_SIZE-DISTANCE_ERROR){
			navigator.moveToCan(0.5*TILE_SIZE);
		}
		
		else{
			if((odo.getXYT()[0] > searchZoneLLX-DISTANCE_ERROR && odo.getXYT()[0] < searchZoneLLX+DISTANCE_ERROR) || 
					(odo.getXYT()[1] > searchZoneLLY-DISTANCE_ERROR && odo.getXYT()[1] < searchZoneLLY+DISTANCE_ERROR) ||
					(odo.getXYT()[0] > searchZoneURX-DISTANCE_ERROR && odo.getXYT()[0] < searchZoneURX+DISTANCE_ERROR) ||
					(odo.getXYT()[1] > searchZoneURY-DISTANCE_ERROR && odo.getXYT()[1] < searchZoneURY+DISTANCE_ERROR)){
				
				navigator.moveToCan(TILE_SIZE);
				navigator.turnTo(90);
			}
		}
		
		//keeps coordinate values in check to update odo if needed
		if(Cy < searchZoneURY) {
			Cy++;
			odo.setY(Cy*TILE_SIZE);
		}
		else if(Cx < searchZoneURX) { 
			Cx++;
			odo.setX(Cx*TILE_SIZE);
		}
		else if(Cy > searchZoneLLY) {
			Cy--;
			odo.setY(Cy*TILE_SIZE);
		}
		
		//ENDX is the x coordinate of the final position of the EV3
		else if(Cx > ENDX) {
			Cx--;
			odo.setX(Cx*TILE_SIZE);
		}
		
	}
	
	/**
	*travelToURBorder() is called when the correct can is found on the edge of the search zone. This
	*method will use travelTo() from the Navigator class to get the EV3 to the upper right corner.
	*/
	
	private void travelToURBorder() {
		
		if ( (odo.getXYT()[2] >= 0-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 0+ANGLE_ERROR)){
		
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			System.out.println("before travel to");
			System.out.println("searchZoneLLX-offset" + (searchZoneLLX-OFFSET)*TILE_SIZE);
			navigator.travelTo(searchZoneLLX-OFFSET, odo.getXYT()[1]/TILE_SIZE);
			System.out.println("searchZoneURY+OFFSET" + (searchZoneURY+OFFSET)*TILE_SIZE);
			navigator.travelTo(odo.getXYT()[0]/TILE_SIZE,searchZoneURY+OFFSET);
			System.out.println("odo.getXYT()[1]" + odo.getXYT()[1]);
			navigator.travelTo(searchZoneURX,odo.getXYT()[1]/TILE_SIZE);
			navigator.travelTo(searchZoneURX,searchZoneURY);		
		}
		
		else if ( (odo.getXYT()[2] >= 90-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 90+ANGLE_ERROR) ){
			
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);

			navigator.travelTo(odo.getXYT()[0],searchZoneURY+OFFSET);
			navigator.travelTo(odo.getXYT()[0]/TILE_SIZE,searchZoneURY+OFFSET);
			navigator.travelTo(searchZoneURX,searchZoneURY+OFFSET);
			navigator.travelTo(searchZoneURX,searchZoneURY);
		}
		
		else if ( (odo.getXYT()[2] >= 180-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 180+ANGLE_ERROR) ){
		
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			navigator.travelTo(searchZoneURX+OFFSET,odo.getXYT()[1]/TILE_SIZE);
			navigator.travelTo(searchZoneURX+OFFSET,searchZoneURY);
			navigator.travelTo(searchZoneURX,searchZoneURY);
		}
		
		else if ( (odo.getXYT()[2] >= 270-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 270+ANGLE_ERROR) ){
		
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			navigator.travelTo(odo.getXYT()[0]/TILE_SIZE,searchZoneLLY-OFFSET);
			navigator.travelTo(searchZoneURX+OFFSET,searchZoneLLY-OFFSET);
			navigator.travelTo(searchZoneURX+OFFSET,searchZoneURY);
			navigator.travelTo(searchZoneURX,searchZoneURY);
		}
	}
	
		

	/**
	*travelToUROutside() is called when the correct can is found from the outside of
	*the search zone. This method will use travelTo() from the Navigator class
	*to get the EV3 to the upper right corner.
	*/
					   
	private void travelToUROutside() {
		
		if ( (odo.getXYT()[2] >= 0-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 0+ANGLE_ERROR)){
		
			navigator.driveBack(CAN_DISTANCE_FROM_OUT);
			navigator.travelTo(odo.getXYT()[0]/TILE_SIZE, searchZoneURY+OFFSET);
			navigator.travelTo(searchZoneURX,searchZoneURY+OFFSET);
			navigator.travelTo(searchZoneURX,searchZoneURY);	
		}
		
		else if ( (odo.getXYT()[2] >= 90-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 90+ANGLE_ERROR) ){
			
			navigator.driveBack(CAN_DISTANCE_FROM_OUT);
			navigator.travelTo(searchZoneURX,searchZoneURY+OFFSET);
			navigator.travelTo(searchZoneURX,searchZoneURY);
		}
		
		else if ( (odo.getXYT()[2] >= 180-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 180+ANGLE_ERROR) ){
		
			navigator.driveBack(CAN_DISTANCE_FROM_OUT);
			navigator.travelTo(searchZoneURX+OFFSET,searchZoneURY);
			navigator.travelTo(searchZoneURX,searchZoneURY);
		}
		
		else if ( (odo.getXYT()[2] >= 270-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 270+ANGLE_ERROR) ){
		
			navigator.driveBack(CAN_DISTANCE_FROM_OUT);
			navigator.travelTo(searchZoneURX+OFFSET,odo.getXYT()[1]/TILE_SIZE);
			navigator.travelTo(searchZoneURX+OFFSET,searchZoneURY);
			navigator.travelTo(searchZoneURX,searchZoneURY);
		}
		
	}
	
	private void travelToURInside(){
		
		navigator.driveBack(CAN_DISTANCE_ON_BORDER+0.5*TILE_SIZE);
		//on the first edge
		if(odo.getXYT()[2] <= Math.abs(0-ANGLE_ERROR)){
			navigator.turnTo(-90);
			navigator.driveForward(searchZoneURY*TILE_SIZE-odo.getXYT()[1]+CAN_DISTANCE_FROM_OUT);
			navigator.turnTo(90);
			navigator.driveForward(searchZoneURX*TILE_SIZE-odo.getXYT()[0]);
			navigator.turnTo(90);
			navigator.driveForward(CAN_DISTANCE_FROM_OUT);
		}
		
		//on the second edge
		else if(odo.getXYT()[2] <= Math.abs(90-ANGLE_ERROR)){
			navigator.turnTo(-90);
			navigator.driveForward(searchZoneURX*TILE_SIZE-odo.getXYT()[0]);
			navigator.turnTo(90);
			navigator.driveForward(CAN_DISTANCE_FROM_OUT);	
		}
		
		//on the third edge
		else if(odo.getXYT()[2] <= Math.abs(180-ANGLE_ERROR)){
			navigator.turnTo(90);
			navigator.driveForward(searchZoneURY*TILE_SIZE-odo.getXYT()[1]);
			navigator.turnTo(-90);
			navigator.driveForward(CAN_DISTANCE_FROM_OUT);	
		}
		
		//on the fourth edge
		else if(odo.getXYT()[2] <= Math.abs(270-ANGLE_ERROR)){
			navigator.turnTo(90);
			navigator.driveForward(searchZoneURX*TILE_SIZE-odo.getXYT()[0]+CAN_DISTANCE_FROM_OUT);
			navigator.turnTo(-90);
			navigator.driveForward(searchZoneURY*TILE_SIZE-odo.getXYT()[0]);
			navigator.turnTo(-90);
			navigator.driveForward(CAN_DISTANCE_FROM_OUT);
		}
		
	}
	
	/**
	*borderDodge() is called when an incorrect color of a can is detected. 
	*The EV3 will dodge the can and continue its trip to look for the correct one.
	*/
	
	private void borderDodge() {
		
		if ( (odo.getXYT()[2] >= 0-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 0+ANGLE_ERROR)){
		
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			
			if(odo.getXYT()[1]/TILE_SIZE >= searchZoneURY-1-DISTANCE_ERROR ||
			   	odo.getXYT()[1]/TILE_SIZE <= searchZoneURY-1+DISTANCE_ERROR){
				
				//the 1.5 added is to make the EV3 dodge 1.5 times a tile
				navigator.travelTo(searchZoneLLX-OFFSET, odo.getXYT()[1]/TILE_SIZE);
				navigator.travelTo(searchZoneLLX-OFFSET,(odo.getXYT()[1]/TILE_SIZE) + 1.5);
				navigator.travelTo(searchZoneLLX-OFFSET+1.5,(odo.getXYT()[1]/TILE_SIZE) + 1.5);
				navigator.turnTo(90);
			}
			
			else{
				//the 2 added is to make the EV3 dodge 2 times a tile
				navigator.travelTo(searchZoneLLX-OFFSET, odo.getXYT()[1]/TILE_SIZE);
				navigator.travelTo(searchZoneLLX-OFFSET,(odo.getXYT()[1]/TILE_SIZE) + 2);
				navigator.turnTo(90);
			}
		}
		
		
		else if ( (odo.getXYT()[2] >= 90-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 90+ANGLE_ERROR) ){
			
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			navigator.travelTo(odo.getXYT()[0]/TILE_SIZE,searchZoneURY+OFFSET);
			navigator.travelTo((odo.getXYT()[0]/TILE_SIZE) + 2, searchZoneURY+OFFSET);
			navigator.turnTo(90);
		}
		
		else if ( (odo.getXYT()[2] >= 180-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 180+ANGLE_ERROR) ){
		
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			
			if(odo.getXYT()[1]/TILE_SIZE <= searchZoneLLY+1+DISTANCE_ERROR ||
			   	odo.getXYT()[1]/TILE_SIZE >= searchZoneLLY+1-DISTANCE_ERROR){
				
				navigator.travelTo(searchZoneURX+OFFSET,odo.getXYT()[1]/TILE_SIZE);
				navigator.travelTo(searchZoneURX+OFFSET,(odo.getXYT()[1]/TILE_SIZE) - 1.5);
				navigator.travelTo(searchZoneURX+OFFSET-1.5,(odo.getXYT()[1]/TILE_SIZE) - 1.5);
				navigator.turnTo(90);
			}
			
			else{
				
				navigator.travelTo(searchZoneURX+OFFSET,odo.getXYT()[1]/TILE_SIZE);
				navigator.travelTo(searchZoneURX+OFFSET,(odo.getXYT()[1]/TILE_SIZE) - 2);
				navigator.turnTo(90);
			}
		}
		
		else if ( (odo.getXYT()[2] >= 270-ANGLE_ERROR) || 
		    	(odo.getXYT()[2] <= 270+ANGLE_ERROR) ){
		
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			navigator.travelTo(odo.getXYT()[0]/TILE_SIZE,searchZoneLLY-OFFSET);
			navigator.travelTo((odo.getXYT()[0]/TILE_SIZE) - 2, searchZoneLLY-OFFSET);
			navigator.turnTo(90);
		}
	}
	
	/**
	*outsideDodge()  
	*
	*/				   
	
	private void outsideDodge() {
		
		//move forward 12.75
		//check for the right color
			//if right color, then travel to UR outside
			//else go back 12.75
		
		navigator.driveBack(CAN_DISTANCE_ON_BORDER);
		navigator.turnTo(-90);
		
		if(odo.getXYT()[1] <= (searchZoneURY*TILE_SIZE+DISTANCE_ERROR) ||
				odo.getXYT()[1] >= (searchZoneURY*TILE_SIZE-DISTANCE_ERROR) ||
				odo.getXYT()[0] <= (searchZoneURX*TILE_SIZE+DISTANCE_ERROR)
				|| odo.getXYT()[0] >= (searchZoneURX*TILE_SIZE-DISTANCE_ERROR)){
			navigator.driveForward(TILE_SIZE*0.5);
			navigator.turnTo(90);
			navigator.driveForward(TILE_SIZE*1.5);
			navigator.turnTo(90);
		}
		
		else{
			navigator.driveForward(TILE_SIZE);
			navigator.turnTo(90);
		}
			
	}
	
	//returns to the next coordinate on the square without turning 90 deg right 
	private void  insideDodge(){
		
		//check if the robot is on the edge
		
		//turn right 90
		
		//check if the can is there
			//if can is there, go straight 27.5 cm 
			//check if the can is right
				//if right, move to UR
				//if not, move back 27.5, turn left
			
				
//			if(odo.getXYT()[2] <= Math.abs(0-ANGLE_ERROR)){
//				navigator.travelTo(odo.getXYT()[0]+TILE_SIZE/CAN_DISTANCE_ON_BORDER, odo.getXYT()[1]);
//			}
//			
//			else if(odo.getXYT()[2] <= Math.abs(90-ANGLE_ERROR)){
//				navigator.travelTo(odo.getXYT()[0], odo.getXYT()[1]-TILE_SIZE/CAN_DISTANCE_ON_BORDER);
//			}
//			
//			else if(odo.getXYT()[2] <= Math.abs(180-ANGLE_ERROR)){
//				navigator.travelTo(odo.getXYT()[0]-TILE_SIZE/CAN_DISTANCE_ON_BORDER, odo.getXYT()[1]);
//			}
//			
//			else if(odo.getXYT()[2] <= Math.abs(270-ANGLE_ERROR)){
//				navigator.travelTo(odo.getXYT()[0], odo.getXYT()[1]+TILE_SIZE/CAN_DISTANCE_ON_BORDER);
//			}

					
			//System.out.println("wrong can found");
			//on the first edge of square
			navigator.driveBack(CAN_DISTANCE_ON_BORDER);
			navigator.turnTo(-90);
			goToNext();
			//navigator.turnTo(90);			
	}
	
	
		
	private int readUSDistance() {
		
		usDistance.fetchSample(usData, 0);
		return (int) (usData[0] * 100);
		
	}
  
}
