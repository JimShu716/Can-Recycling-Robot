package ca.mcgill.ecse211.model;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import ca.mcgill.ecse211.model.*;

/**
 * This class contains all the methods that contribute to making
 * the robot move
 * 
 * @author Carlo D'Angelo
 *
 */
public class Navigation {

  public final static int SLOW_SPEED = 75;
  public final static int ROTATION_SPEED = 100;
  private final static int FORWARD_SPEED = 200; 
  
  public static final double TILE_SIZE = 30.48;
  
  private final double RADIUS = Robot.WHEEL_RAD;
  private final double TRACK = Robot.TRACK;
  
  private Odometer odo;
  private EV3LargeRegulatedMotor leftMotor, rightMotor;
 
  public static double minAng;

  /**
   * This is the default constructor of this class
   * @param leftMotor left motor of robot
   * @param rightMotor right motor of robot
   * @throws OdometerExceptions
   */
  public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) throws OdometerExceptions {
	odo = Odometer.getOdometer();
	this.leftMotor = leftMotor;
	this.rightMotor = rightMotor;
	}
  
  /**
   * Method that allows robot to travel from its current position to any other point
   * @param x x coordinate of desired destination
   * @param y y coordinate of desired destination
   */
  public void travelTo(double x, double y) {
	double currentX = odo.getXYT()[0];
	double currentY = odo.getXYT()[1];

	double deltaX = x * TILE_SIZE - currentX;
	double deltaY = y * TILE_SIZE - currentY;
	
	double currentA = odo.getXYT()[2];
	
	// find minimum angle
	minAng = -currentA + Math.atan2(deltaX, deltaY) * 180 / Math.PI;
	
	// special cases so that robot does not turn at maximum angle
	if (minAng < 0 && (Math.abs(minAng) >= 180)) {
		minAng += 360;
	}else if (minAng > 0 && (Math.abs(minAng) >= 180)){
		minAng -= 360;
	}
	turnTo(minAng);
	
	double distance = Math.hypot(deltaX, deltaY);

	leftMotor.setSpeed(FORWARD_SPEED);
	rightMotor.setSpeed(FORWARD_SPEED);

	leftMotor.rotate(convertDistance(RADIUS, distance), true);
	rightMotor.rotate(convertDistance(RADIUS, distance), false);
	
  }

  /**
   * Method that allows the robot to rotate in place 
   * @param theta angle amount (in degrees) that you want the robot to rotate in place
   */
  public void turnTo(double theta) {
	leftMotor.setSpeed(ROTATION_SPEED);
	rightMotor.setSpeed(ROTATION_SPEED);
	leftMotor.rotate(convertAngle(RADIUS, TRACK, theta), true);
	rightMotor.rotate(-convertAngle(RADIUS, TRACK, theta), false);

  }
  
  /**
   * This method is only used when scanning for a can in the search zone. It is
   * similar to turnTo(), but the motors rotate at a slower speed, and return
   * instantly rather than wait for the robot to rotate.
   * @param theta angle amount (in degrees) that you want the robot to rotate in place
   */
  public void turnToScan(double theta) {
		leftMotor.setSpeed(SLOW_SPEED);
		rightMotor.setSpeed(SLOW_SPEED);
		leftMotor.rotate(convertAngle(RADIUS, TRACK, theta), true);
		rightMotor.rotate(-convertAngle(RADIUS, TRACK, theta), true);

	  }
  
  /**
   * Method that allows the robot to move backward
   * @param distance distance that you want the robot to travel backward
   */
  public void driveBack(double distance) {
	  leftMotor.setSpeed(FORWARD_SPEED);
	  rightMotor.setSpeed(FORWARD_SPEED);

	  leftMotor.rotate(-convertDistance(RADIUS, distance), true);
	  rightMotor.rotate(-convertDistance(RADIUS, distance), false);
  }
  
  /**
   * Method that allows the robot to move forward
   * @param distance distance that you want the robot to travel forward
   */
  public void driveForward(double distance) {
	  leftMotor.setSpeed(FORWARD_SPEED);
	  rightMotor.setSpeed(FORWARD_SPEED);

	  leftMotor.rotate(convertDistance(RADIUS, distance), true);
	  rightMotor.rotate(convertDistance(RADIUS, distance), false);
  }
  
  /**
   * Method that allows the robot to approach a can
   * @param distance distance that you want the robot to travel forward
   */
  public void moveToCan(double distance) {
	  leftMotor.setSpeed(FORWARD_SPEED);
	  rightMotor.setSpeed(FORWARD_SPEED);

	  leftMotor.rotate(convertDistance(RADIUS, distance), true);
	  rightMotor.rotate(convertDistance(RADIUS, distance), false);
  }
  
	/**
	 * This method converts a distance into the total rotation (in degrees) of 
	 * each wheel needed to cover that distance 
	 * 
	 * @param RADIUS RADIUS of wheel
	 * @param distance distance that you want the robot to move
	 * @return total rotation (in degrees) of each wheel needed to cover a distance
	 */
	public static int convertDistance(double RADIUS, double distance) {
	  return (int) ((180.0 * distance) / (Math.PI * RADIUS));
	}
	
	/**
	 * This method converts a rotation in place into the total 
	 * rotation (in degrees) of each wheel needed to cause that rotation
	 * 
	 * @param RADIUS RADIUS of wheel
	 * @param width distance between centers of wheels
	 * @param angle angle (in place) that you want the robot to rotate
	 * @return total rotation (in degrees) of each wheel needed to cause a rotation in place
	 */
	public static int convertAngle(double RADIUS, double width, double angle) {
	  return convertDistance(RADIUS, Math.PI * width * angle / 360.0);
	}  
  
}
