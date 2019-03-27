package ca.mcgill.ecse211.model;

import lejos.robotics.SampleProvider;

// testing : return the sensor value and print it on the screen

public class ColorClassification {

  // use an array to collect color data
  private float[] colorData;
  private SampleProvider colorID;
  private static final double THRESHOLD = 0.4;

  private final float[][] MEAN_RGB = {// the mean RGB values for cans
	      {0.0351924454f, 0.0049923890f, 0.0039901908f}, // red can
	      {0.0315519090f, 0.0257073221f, 0.0068014234f}, // yellow can
	      {0.0055042342f, 0.0206447724f, 0.0235459889f}, // blue can
	      {0.0057257231f, 0.0232034322f, 0.0075382312f} // green can
	  };

  // constructor
  public ColorClassification(float[] colorData, SampleProvider colorId) {
    this.colorData = colorData;
    this.colorID = colorId;
 
    



  }

  public String run() {
    int a = findColor(sampleData());

    if (a !=4) {
      String[] clrName = {"red      ", "yellow   ", "blue     ", "green    "};
      return clrName[a];
    } else {
      return "no object";
    }


  }


  private int findColor(float[] colorData) {
    float eucDistance = (float) Math.sqrt((Math.pow(colorData[0], 2) + Math.pow(colorData[1], 2) + Math.pow(colorData[2], 2)));

    // normalize R,G,B values
    float nR = colorData[0] / eucDistance;
    float nG = colorData[1] / eucDistance;
    float nB = colorData[2] / eucDistance;
    // use a counter and difference in RGB values to classify the color
    for (int i = 0; i < 4; i++) {
      float deltaR = Math.abs(nR - (MEAN_RGB[i][0]/eucDistance));
      float deltaG = Math.abs(nG - (MEAN_RGB[i][1]/eucDistance));
      float deltaB = Math.abs(nB - (MEAN_RGB[i][2]/eucDistance));
 
      if (deltaR < THRESHOLD && deltaG < THRESHOLD && deltaB < THRESHOLD) {
        
        return i;
      }

    }

    return 4;
  }
  
  private float[] sampleData(){
  colorID.fetchSample(colorData, 0);
  return colorData;
  }

}
