package projectorFirst;


import java.awt.Color;

import javax.vecmath.*;
import java.util.regex.*;

public class DrawLine4 {
	static String[] channels;
	DataParserR engine;
	static double[] data;
	final int YLENGTH = 3000; final int XWIDTH = 2000;
	final int X = 0; final int Y = 1; final int Z = 2;
/*Person 1 controls the angle of the movement.  The angle is calculated subtracting the global 
rotation of the Z coordinate of the hand from the hip. The right hand takes precedent. Person 1
also controls the thickness of the line with the X coordinate of the global translation of the hip.
Person 2 controls the color with the Z coordinate of Global translation. Controls whether and 
in what direction the program draws. */
	
	public DrawLine4(){
		try{
			engine = new DataParserR();
		}catch(Throwable e){};
	}
//Pulls one frame of data

	
	 public double[] chooseData(){	
		int num = ProduceScreen.speed;
		
		 for (int ak = 0; ak < 40 - num; ak++){
		 	data = engine.getData();
		 }
		 return data;		 
	 }
//axis determines stimulates which axis the angle is rotated around. 
//0 = x, 1 = y, 2 = z
	 public Vector3d calcVec(double angle, Vector3d axis, int vector){
//Translate from Axis-Angle to Matrix form 
		 AxisAngle4d aa = new AxisAngle4d(axis.x, axis.y, axis.z, angle);
		 Matrix3d mat = new Matrix3d();
		 mat.set(aa);
		 
//Take choosen direction vector (same coding as axis)
		 double xx = 0; double yy = 0; double zz = 0; 
		 if (vector == 0) xx = 1;   
		 if (vector == 1) yy = 1; 
		 if (vector == 2) zz = 1; 
		 
		 Tuple3d tup = new Vector3d(xx, yy, zz);
//multiply matrix by directional vector choosen 
		 mat.transform(tup);
//Project to the x, y plane, normalize and change to vector form 
		 double[] holder = new double[3];
		 tup.get(holder);
		 holder[2] = 0;
		 tup.set(holder);
		 Vector3d vec = new Vector3d(tup);
		 vec.normalize();
		 
		 return vec;	
	 }
	 
	 public double doAngleMap(Vector3d bodyVec, Vector3d handVec){
//find the angle between the two vectors
		 double mapped; 
		 
		Vector3d sign = new Vector3d();
		sign.cross(bodyVec, handVec);
		double[] lookForZ = new double[3];
		
		sign.get(lookForZ);
		/*if (lookForZ[2] < 1.5) mapped = 0;
		else {mapped = lookForZ[2]*-1;}*/
		//System.out.println(lookForZ[2]);
		mapped = lookForZ[2]*-1;
		 return mapped;
		 
	 }
//maps and x or y coordinate to a number between 1 and -1. axis = 0 is X, axis = 1 is y
	 public double doCorMap(double cor, int axis){
		 double mapped;
		 
		 int choice = 0;
		 if (axis == 0) choice = XWIDTH;
		 if (axis == 1) choice = YLENGTH;
		 
		 if (cor > choice) cor = choice;
		 if (cor < 0) cor = 0;
		 
		  if (choice == 0) mapped = 0;
		  else mapped = cor/choice;
		  
		 return mapped;
		 
	 }	 
	 
	 public int doChangeMap(double cor, int axis){
		 int mapped = 2;
		 
		 int choice = 0;
		 if (axis == 0) choice = XWIDTH;
		 if (axis == 1) choice = YLENGTH;
		 
		 if (cor < (choice/3)*2) mapped = 1;
		 if (cor < choice/3) mapped = 0;
		 
		 return mapped; 
	 }
}
	 
	 
	
	
	

