package ca.mcgill.ecse211.model;

/**
 * This class handles the robot's navigation from the starting corner
 * to the search zone after localization is complete
 * 
 * @author Carlo D'Angelo
 *
 */
public class SearchZoneLocator {

	private Odometer odo;
	private final double TILE_SIZE = Navigation.TILE_SIZE;
	private int startingCorner, homeZoneLLX, homeZoneLLY, homeZoneURX, homeZoneURY,
					tunnelLLX, tunnelLLY, tunnelURX, tunnelURY;
	private LightLocalizer lightLocalizer;
	private Navigation navigator;
	
	/**
	 * 
	 * @param robot
	 * @param lightLocalizer
	 * @param navigator
	 * @throws OdometerExceptions
	 */
	public SearchZoneLocator(Robot robot, LightLocalizer lightLocalizer, 
								Navigation navigator) throws OdometerExceptions {
		odo = Odometer.getOdometer();
		startingCorner = robot.getStartingCorner();	
		homeZoneLLX = robot.getHomeZoneLLX();
		homeZoneLLY = robot.getHomeZoneLLY();
		homeZoneURX = robot.getHomeZoneURX();
		homeZoneURY = robot.getHomeZoneURY();
		tunnelLLX = robot.getTunnelLLX();
		tunnelLLY = robot.getTunnelLLY();
		tunnelURX = robot.getTunnelURX();
		tunnelURY = robot.getTunnelURY();
		this.lightLocalizer = lightLocalizer;
		this.navigator= navigator;
	}
	
	/** 
	 * Method that allows the robot to make its way to the search zone
	 * The path the robot takes will depend on the starting corner (startingCorner) parameter
	 */
	public void goToSearchZone(){
		
		switch(startingCorner){
			case 0: 
				//current position is (1,1)
				odo.setXYT(1.0 * TILE_SIZE, 1.0 * TILE_SIZE, 0.0);

				break;
			case 1: 
				//current position is (7,1)
				odo.setXYT(7.0 * TILE_SIZE, 1.0 * TILE_SIZE, 270.0);

				break;
			case 2: 
				//current position is (7,7)
				odo.setXYT(7.0 * TILE_SIZE, 7.0 * TILE_SIZE, 180.0);

				break;
			case 3:	
				//current position is (1,7)
				odo.setXYT(1.0 * TILE_SIZE, 7.0 * TILE_SIZE, 90.0);

				break;
		    default:
		    	System.out.println("Error - invalid button"); // None of the above - abort
		        System.exit(-1);
		        break;
		}
		
	} 
	
	private void transitTunnel() {
		
	}
  
}
