package ca.mcgill.ecse211.model;

import lejos.hardware.Sound;

public class ReturnHome {
	private final double TILE_SIZE = Navigation.TILE_SIZE;
	private static final int CANDROP_DISTANCE = 6;
	private int startingCorner, homeZoneLLX, homeZoneLLY, homeZoneURX, homeZoneURY,
					tunnelLLX, tunnelLLY, tunnelURX, tunnelURY;
	private int islandLLX, islandLLY, islandURX, islandURY,
					searchZoneLLX, searchZoneLLY, searchZoneURX, searchZoneURY;
	private LightLocalizer lightLocalizer;
	private Navigation navigator;
	private Clamp clamp;
	private Odometer odo;
	
	public ReturnHome(Robot robot, LightLocalizer lightLocalizer, 
						Clamp clamp, Navigation navigator) throws OdometerExceptions {
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
		islandLLX = robot.getIslandLLX();
		islandLLY = robot.getIslandLLY();
		islandURX = robot.getIslandURX();
		islandURY = robot.getIslandURY();
		searchZoneLLX = robot.getSearchZoneLLX();
		searchZoneLLY = robot.getSearchZoneLLY();
		searchZoneURX = robot.getSearchZoneURX();
		searchZoneURY = robot.getSearchZoneURY();
		this.lightLocalizer = lightLocalizer;
		this.navigator = navigator;
		this.clamp = clamp;
	}
	
	public void goHome() {
		switch(startingCorner){
			case 0: 
				// corner 0
				if (homeZoneURX < islandLLX) { // horizontal tunnel
					double tunnelY;
					if(islandLLY == tunnelLLY) {
						navigator.travelTo(tunnelURX + 1, tunnelY = tunnelURY);
					} else {
						navigator.travelTo(tunnelURX + 1, tunnelY = tunnelLLY);
					}
					if (searchZoneLLY == tunnelY) {
						navigator.turnTo(135);
						lightLocalizer.lightLocalize(tunnelURX + 1, tunnelY);
					} else if (searchZoneLLY < tunnelY) {
						navigator.turnTo(90);
						lightLocalizer.lightLocalize(tunnelURX + 1, tunnelY);
					} else if (searchZoneLLY > tunnelY) {
						navigator.turnTo(180);
						lightLocalizer.lightLocalize(tunnelURX + 1, tunnelY);
					}	
					if (tunnelY == tunnelURY) {
						navigator.turnTo(180);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(90);
					}else if (tunnelY == tunnelLLY) {
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(90);
					}
					navigator.driveForward(((tunnelURX - tunnelLLX) + 2) * TILE_SIZE);
					if(homeZoneLLY == tunnelLLY) {
						navigator.turnTo(90);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(45);
						lightLocalizer.lightLocalize(tunnelLLX - 1, tunnelURY);
						navigator.travelTo(homeZoneLLX + 1, homeZoneLLY + 1);
						navigator.turnTo(135);
						lightLocalizer.lightLocalize(homeZoneLLX + 1, homeZoneLLY + 1);
					} else {
						navigator.turnTo(-90);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(-135);
						lightLocalizer.lightLocalize(tunnelLLX - 1, tunnelLLY);
						navigator.travelTo(homeZoneLLX + 1, homeZoneLLY + 1);
						if ((homeZoneLLY + 1) == tunnelLLY) {
							navigator.turnTo(135);
						}else {
							navigator.turnTo(180);
						}
						lightLocalizer.lightLocalize(homeZoneLLX + 1, homeZoneLLY + 1);	
					}
					navigator.turnTo(-135);
					navigator.driveForward(CANDROP_DISTANCE);
					clamp.offloadCan();
					issueOffloadBeeps();
					navigator.driveBack(CANDROP_DISTANCE);
					navigator.turnTo(180);
					lightLocalizer.lightLocalize(homeZoneLLX + 1, homeZoneLLY + 1);
					
				} else if (homeZoneURY < islandLLY){ // vertical tunnel
					double tunnelX;
					if(islandLLX == tunnelLLX) {
						navigator.travelTo(tunnelX = tunnelURX, tunnelURY + 1);
					} else {
						navigator.travelTo(tunnelX = tunnelLLX, tunnelURY + 1);
					}
					if (searchZoneLLX == tunnelX) {
						navigator.turnTo(-135);
						lightLocalizer.lightLocalize(tunnelX, tunnelURY + 1);
					} else if (searchZoneLLX < tunnelX) {
						navigator.turnTo(-90);
						lightLocalizer.lightLocalize(tunnelX, tunnelURY + 1);
					} else if (searchZoneLLX > tunnelX) {
						navigator.turnTo(-180);
						lightLocalizer.lightLocalize(tunnelX, tunnelURY + 1);
					}	
					if (tunnelX == tunnelURX) {
						navigator.turnTo(-90);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(-90);
					}else if (tunnelX == tunnelLLX) {
						navigator.turnTo(90);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(90);
					}
					navigator.driveForward(((tunnelURY - tunnelLLY) + 2) * TILE_SIZE);
					if(homeZoneLLX == tunnelLLX) {
						navigator.turnTo(-90);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(-45);
						lightLocalizer.lightLocalize(tunnelURX, tunnelLLY - 1);
						navigator.travelTo(homeZoneLLX + 1, homeZoneLLY + 1);
						navigator.turnTo(-135);
						lightLocalizer.lightLocalize(homeZoneLLX + 1, homeZoneLLY + 1);
					} else {
						navigator.turnTo(90);
						navigator.driveForward(0.5 * TILE_SIZE);
						navigator.turnTo(135);
						lightLocalizer.lightLocalize(tunnelLLX, tunnelLLY - 1);
						navigator.travelTo(homeZoneLLX + 1, homeZoneLLY + 1);
						if ((homeZoneLLX + 1) == tunnelLLX) {
							navigator.turnTo(-135);
						}else {
							navigator.turnTo(180);
						}
						lightLocalizer.lightLocalize(homeZoneLLX + 1, homeZoneLLY + 1);	
					}
					navigator.turnTo(-135);
					navigator.driveForward(CANDROP_DISTANCE);
					clamp.offloadCan();
					issueOffloadBeeps();
					navigator.driveBack(CANDROP_DISTANCE);
					navigator.turnTo(180);
					lightLocalizer.lightLocalize(homeZoneLLX + 1, homeZoneLLY + 1);
				}
				break;
			case 1: 
				// corner 1
				break;
			case 2: 
				// corner 2
				break;
			case 3:	
				// corner 3
				break;
		    default:
		    	System.out.println("Error - invalid button"); // None of the above - abort
		        System.exit(-1);
		        break;
		}
		
	}
	
	private void issueOffloadBeeps() {
		Sound.beep();
		Sound.pause(100);
		Sound.beep();
		Sound.pause(100);
		Sound.beep();
		Sound.pause(100);
		Sound.beep();
		Sound.pause(100);
		Sound.beep();
	}

}
