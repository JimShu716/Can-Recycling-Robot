package ca.mcgill.ecse211.model;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import org.json.simple.parser.ParseException;

import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import ca.mcgill.ecse211.lab5.Project;

public class Robot {
	//Robot related parameters
	public static final double WHEEL_RAD = 2.2;
	public static final double TRACK = 13.2;
	
	private final int TEAM_NUMBER = Project.TEAM_NUMBER;
	// Holds the Wifi data
	private Map data;
	
	public Robot(WifiConnection wifi) throws UnknownHostException, IOException, ParseException {
		data = wifi.getData();
	}
	
	public int getRedTeam() {
		int redTeam = ((Long) data.get("RedTeam")).intValue();
		return redTeam;
	}
	
	public int getGreenTeam() {
		int greenTeam = ((Long) data.get("GreenTeam")).intValue();
		return greenTeam;
	}
	
	public int getStartingCorner() {
		int SC = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			SC = ((Long) data.get("RedCorner")).intValue();
		}else {
			SC = ((Long) data.get("GreenCorner")).intValue();
		}
		return SC;
	}
	
	public int getHomeZoneLLX() {
		int homeZoneLLX = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			homeZoneLLX = ((Long) data.get("Red_LL_x")).intValue();
		}else {
			homeZoneLLX = ((Long) data.get("Green_LL_x")).intValue();
		}
		return homeZoneLLX;
	}
	
	public int getHomeZoneLLY() {
		int homeZoneLLY = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			homeZoneLLY = ((Long) data.get("Red_LL_y")).intValue();
		}else {
			homeZoneLLY = ((Long) data.get("Green_LL_y")).intValue();
		}
		return homeZoneLLY;
	}
	
	public int getHomeZoneURX() {
		int homeZoneURX = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			homeZoneURX = ((Long) data.get("Red_UR_x")).intValue();
		}else {
			homeZoneURX = ((Long) data.get("Green_UR_x")).intValue();
		}
		return homeZoneURX;
	}
	
	public int getHomeZoneURY() {
		int homeZoneURY = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			homeZoneURY = ((Long) data.get("Red_UR_y")).intValue();
		}else {
			homeZoneURY = ((Long) data.get("Green_UR_y")).intValue();
		}
		return homeZoneURY;
	}
	
	public int getIslandLLX() {
		int islandLLX = ((Long) data.get("Island_LL_x")).intValue();
		return islandLLX;
	}
	
	public int getIslandLLY() {
		int islandLLY = ((Long) data.get("Island_LL_y")).intValue();
		return islandLLY;
	}
	
	public int getIslandURX() {
		int islandURX = ((Long) data.get("Island_UR_x")).intValue();
		return islandURX;
	}
	
	public int getIslandURY() {
		int islandURY = ((Long) data.get("Island_UR_y")).intValue();
		return islandURY;
	}
	
	public int getTunnelLLX() {
		int tunnelLLX = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			tunnelLLX = ((Long) data.get("TNR_LL_x")).intValue();
		}else {
			tunnelLLX = ((Long) data.get("TNG_LL_x")).intValue();
		}
		return tunnelLLX;
	}
	
	public int getTunnelLLY() {
		int tunnelLLY = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			tunnelLLY = ((Long) data.get("TNR_LL_y")).intValue();
		}else {
			tunnelLLY = ((Long) data.get("TNG_LL_y")).intValue();
		}
		return tunnelLLY;
	}
	
	public int getTunnelURX() {
		int tunnelURX = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			tunnelURX = ((Long) data.get("TNR_UR_x")).intValue();
		}else {
			tunnelURX = ((Long) data.get("TNG_UR_x")).intValue();
		}
		return tunnelURX;
	}
	
	public int getTunnelURY() {
		int tunnelURY = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			tunnelURY = ((Long) data.get("TNR_UR_y")).intValue();
		}else {
			tunnelURY = ((Long) data.get("TNG_UR_y")).intValue();
		}
		return tunnelURY;
	}
	
	public int getSearchZoneLLX() {
		int searchZoneLLX = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			searchZoneLLX = ((Long) data.get("SZR_LL_x")).intValue();
		}else {
			searchZoneLLX = ((Long) data.get("SZG_LL_x")).intValue();
		}
		return searchZoneLLX;
	}
	
	public int getSearchZoneLLY() {
		int searchZoneLLY = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			searchZoneLLY = ((Long) data.get("SZR_LL_y")).intValue();
		}else {
			searchZoneLLY = ((Long) data.get("SZG_LL_y")).intValue();
		}
		return searchZoneLLY;
	}
	
	public int getSearchZoneURX() {
		int searchZoneURX = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			searchZoneURX = ((Long) data.get("SZR_UR_x")).intValue();
		}else {
			searchZoneURX = ((Long) data.get("SZG_UR_x")).intValue();
		}
		return searchZoneURX;
	}
	
	public int getSearchZoneURY() {
		int searchZoneURY = 0;
		if (getRedTeam() == TEAM_NUMBER) {
			searchZoneURY = ((Long) data.get("SZR_UR_y")).intValue();
		}else {
			searchZoneURY = ((Long) data.get("SZG_UR_y")).intValue();
		}
		return searchZoneURY;
	}
}
