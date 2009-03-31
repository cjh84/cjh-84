 a 

package projectorFirst;


import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.vecmath.*;
import java.lang.Number;
import java.awt.color.*;


import javax.swing.*;
import java.io.*;
import javax.imageio.*;

public class ProduceScreen extends JFrame {	
//the dimensions of the frame
	final static int FRAMEX = 890; final static int FRAMEY = 960; 
	final static int FRAMEXT = 1400; final static int FRAMEYT = 1400; 
	static int sWid = 1; static int sHig = 1;  
	static int speed = 39;
	static int clean;
	static double[] Point = {FRAMEX/2, FRAMEY/2};
	static double[] sendData = {0, 0, 0, 0, 0, 0, 0, 0, 0};
	//static double Xcursor = Point[0];
	//static double Ycursor = Point[1];
//DCursor indicates whether to draw or not. 0= don't draw, 1 = draw
	static int Dcursor = 1;
//Ecursos indicates whether to Erase. 0 = don't erase, 1 = erase. Can only erase in draw mode
	static int Ecursor = 0;
	static Color Ccolor = Color.RED;
	static BufferedImage buffer;
	
	public static void main(String[] a)  {
//Creates the JFrame   
		   JFrame frame = new JFrame();
		   frame.setBounds(0, 0, FRAMEXT, FRAMEYT);
		   frame.addWindowListener(new WindowAdapter(){
			   public void windowClosing(WindowEvent we){
				   try {
				   File outpicture = new File("projectorFirstPicture.jpg");
				   ImageIO.write(buffer, "jpg", outpicture);
				   

					FileOutputStream fout = new FileOutputStream ("projectorFirstEndPoint.txt");
	
					PrintStream p = new PrintStream(fout); 
					for (int jk = 0; jk < sendData.length; jk++){
					    p.println (sendData[jk]);
					
					}//end for
					    // Close our output stream
					    fout.close();		
				
				   System.exit(0);
				   }catch(Exception e){};
			   }
		   });

//Sets up the container
		   
		   Container contents = frame.getContentPane();
		
		   MyPanel panel = new MyPanel();
		   contents.add(panel);
		 
		   frame.pack();
		   frame.setVisible(true);
		   MyRenderer mr = new MyRenderer(panel);
		   (new Thread(mr)).start();    	
	}
	
	
	static class MyPanel extends JPanel{
		
		Graphics2D g2d;
		DrawLine4 drawer = new DrawLine4();
		double[] data;
		
		public MyPanel(){
			setPreferredSize(new Dimension(FRAMEXT, FRAMEYT));
			buffer = new BufferedImage(FRAMEXT, FRAMEYT,BufferedImage.TYPE_INT_RGB);
			g2d = buffer.createGraphics();
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, FRAMEXT, FRAMEYT);
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, FRAMEX, FRAMEY);
			
		}
			
		public void paintComponent(Graphics g2d){
			super.paintComponent(g2d);
			g2d.drawImage(buffer,0,0,this);
			if (Dcursor == 1 || Dcursor == 2){
				g2d.setColor(Color.BLACK);
				g2d.fillOval((int) Point[0], (int)Point[1],sWid+2, sHig+2);
			}
			g2d.setColor(Ccolor);
			g2d.fillOval((int) Point[0], (int)Point[1],sWid, sHig);
			if (Dcursor == 2){
				g2d.setColor(Color.BLACK);
				g2d.fillRect((int) Point[0]+ sWid/2, (int)Point[1] + sHig/2,3, 3);
			}
		}
		public void render(){
//			 HipP1 - A (0, 1, 2) HipP2 - A (3, 4, 5) HipP3 - A (6, 7, 8) 
//			 HipP1 - T (9, 10) HipP2 - T (11, 12) HipP3 T (13, 14) 
//			 HandP1-A(15, 16, 17) HandP2 (18, 19, 20) HandP3 (21, 22, 23) 
			
			data = drawer.chooseData();
			if (dataIsClean(data) == 0){
				
				sendData[0]=0; 
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0,FRAMEY-15, FRAMEX, FRAMEY);
				
				int X = 0; int Y = 1; int Z = 2; 

//X point from first person
				
				double angleHip = Math.sqrt(
						data[0]*data[0] + data[1]*data[1] + data[2]*data[2]);
				Vector3d hipAxis = new Vector3d(data[0], data[1], data[2]);	
				hipAxis.normalize();
				Vector3d hip1 = drawer.calcVec(angleHip, hipAxis, X);

				double angleHand1 = Math.sqrt(
						data[15]*data[15] + data[16]*data[16] + data[17]*data[17]);
				Vector3d handAxis = new Vector3d(data[15], data[16], data[17]);
				handAxis.normalize();
				Vector3d hand1 = drawer.calcVec(angleHand1, handAxis, X);
				
				double xPoint = drawer.doAngleMap(hip1, hand1);
				//Xcursor = xPoint;
		
//Y point from second person
				double angleHip2 = Math.sqrt(
						data[3]*data[3] + data[4]*data[4] + data[5]*data[5]);
				Vector3d hipAxis2 = new Vector3d(data[3], data[4], data[5]);	
				hipAxis.normalize();
				Vector3d hip2 = drawer.calcVec(angleHip2, hipAxis2, X);
				double angleHand2 = Math.sqrt(
						data[18]*data[18] + data[19]*data[19] + data[20]*data[20]);
				Vector3d hand2Axis = new Vector3d(data[18], data[19], data[20]);
				hand2Axis.normalize();
				Vector3d hand2 = drawer.calcVec(angleHand2, hand2Axis, X);
				
				double yPoint = drawer.doAngleMap(hip2, hand2);
				//Ycursor = yPoint;
				
//Length Vector from 3rd person
				double angleHip3 = Math.sqrt(
						data[6]*data[6] + data[7]*data[7] + data[8]*data[8]);
				Vector3d hipAxis3 = new Vector3d(data[6], data[7], data[8]);	
				hipAxis.normalize();
				Vector3d hip3 = drawer.calcVec(angleHip3, hipAxis3, X);
				double angleHand3 = Math.sqrt(
						data[21]*data[21] + data[22]*data[22] + data[23]*data[23]);
				Vector3d hand3Axis = new Vector3d(data[21], data[22], data[23]);
				hand2Axis.normalize();
				Vector3d hand3 = drawer.calcVec(angleHand3, hand3Axis, X);
				
				double zPoint = drawer.doAngleMap(hip3, hand3);
				
//??Might have trouble with NANs
				speed = (int) ((zPoint+1)*20);	
				if(speed == 0) speed = 1;
				//System.out.println(speed);

//Color 
/*P1 X hue, P1 Y saturation, P3 Y brightness*/
				float red = (float) drawer.doCorMap(data[10], Y);
				float green = (float) drawer.doCorMap(data[12], Y);
				float blue = (float) drawer.doCorMap(data[14], Y);
				
				sendData[1]= red;
				sendData[2]= green;
				sendData[3]= blue;
				
				Ccolor = new Color (red, green, blue);
				g2d.setColor(Ccolor);
				
				
//Height Person1
				setHeight(drawer.doCorMap(data[9], X));
				sendData[6] = sHig;
				
//Width - Person2
				setWidth(drawer.doCorMap(data[11], X));
				sendData[7] = sWid;
				
				Dcursor = drawer.doChangeMap(data[13], X);
				//System.out.println(Dcursor);                       
				sendData[8] = Dcursor;
				
//Normalize vector
				Vector3d pointVec = new Vector3d(xPoint, (yPoint*-1), 0);
			//	pointVec.normalize();
				//System.out.println("x, " + pointVec.x);
				Double d = new Double(pointVec.x); 
				if (d.isNaN()) Point[0] = Point[0];
				else {Point[0] = Point[0] + pointVec.x;}
				
				Double dd = new Double(pointVec.y); 
				if (dd.isNaN()) Point[1] = Point[1];
				else {Point[1] = Point[1] + pointVec.y;}
			
				if (Point[0] > FRAMEX)Point[0] = 0;
				if (Point[0] < 0)Point[0] = FRAMEX;
				if (Point[1] > FRAMEY)Point[1] = 0;
				if (Point[1] < 0)Point[1] = FRAMEY;
				
				sendData[4] = Point[0];
				sendData[5] = Point[1];
///Dc = 0don't draw, = 1 draw =2 erase
				//ps.toSend(sendData);
				
				if(Dcursor == 2)  {
					g2d.setColor(Color.WHITE);
					g2d.fillOval((int) Point[0], (int)Point[1],sWid, sHig);
				}	
					
				if (Dcursor == 1)	{
					g2d.fillOval((int) Point[0], (int)Point[1],sWid, sHig);
				}
				
				
			} else 		{
				if(clean == 1 || clean == 2 || clean ==3){
					if (clean == 1) g2d.setColor(Color.RED);
					if (clean == 2) g2d.setColor(Color.GREEN);
					if (clean == 3) g2d.setColor(Color.BLUE);
					g2d.fillRect(0, FRAMEY-15, FRAMEX, FRAMEY);
					g2d.setColor(Color.BLACK);
					g2d.fillRect(0, FRAMEY-5, FRAMEX, FRAMEY);
				}else {
					if (clean == 4) g2d.setColor(Color.RED);
					if (clean == 5) g2d.setColor(Color.GREEN);
					if (clean == 6) g2d.setColor(Color.BLUE);
					g2d.fillRect(0, FRAMEY-15, FRAMEX, FRAMEY);
					g2d.setColor(Color.BLACK);
					g2d.fillRect(0, FRAMEY-5, FRAMEX, FRAMEY);
				}
				sendData[0] = clean;
			}
			
		}
	}

//Clean = 0; HipP1 = 1; HipP2 = 2; HipP3 = 3; HandP1 = 4; HandP2 = 5; HandP3 = 6;  	
	public static int dataIsClean(double[] data){
		 clean = 0;
		
		 for (int qr = 0; qr < 3; qr++){
			 if (data[qr] == 0) clean = 1;
		 }//end FOR
		 
		 for (int qr = 3; qr < 6; qr++){
			 if (data[qr] == 0) clean = 2;
		 }//end FOR
		 
		 for (int qr = 6; qr < 9; qr++){
			 if (data[qr] == 0) clean = 3;
		 }//end FOR
		 
		 for (int qr = 9; qr < 11; qr++){
			 if (data[qr] == 0) clean = 1;
		 }//end FOR
		 
		 for (int qr = 11; qr < 13; qr++){
			 if (data[qr] == 0) clean = 2;
		 }//end FOR
		 
		 for (int qr = 13; qr < 15; qr++){
			 if (data[qr] == 0) clean = 3;
		 }//end FOR
		 
		 for (int qr = 15; qr < 18; qr++){
			 if (data[qr] == 0) clean = 4;
		 }//end FOR
		 
		 for (int qr = 18; qr < 21; qr++){
			 if (data[qr] == 0) clean = 5;
		 }//end FOR
		 
		 for (int qr = 21; qr < 24; qr++){
			 if (data[qr] == 0) clean = 6;
		 }//end FOR
		 return clean;
		 
	}//end dataIsClean
	
		public static void setWidth(double mapped){
	
	
				if (mapped < .1) 
			{if (sWid != 1) sWid--;}
		else if (mapped < .2) 
			{if(sWid < 5) sWid++; if (sWid > 5) sWid--;}
		else if (mapped < .3) 
			{if(sWid < 10) sWid++; if(sWid > 10) sWid--;}
		else if (mapped < .4) 
			{if(sWid < 15) sWid++; if (sWid > 15) sWid--;}
		else if (mapped < .5) 
			{if(sWid < 20) sWid++; if (sWid > 20) sWid--;}
		else if (mapped < .6) 
			{if(sWid < 25) sWid++; if (sWid > 25) sWid--;;}
		else if (mapped < .7) 
			{if(sWid < 30) sWid++; if (sWid > 30) sWid--;;}
		else if (mapped < .8) 
			{if(sWid < 35) sWid++; if (sWid > 35) sWid--;}
		else if (mapped < .9) 
			{if(sWid < 40) sWid++; if (sWid > 40) sWid--;}
		else if (mapped < 1) 
			{if (sWid != 45) sWid++;}
			//System.out.println(mapped + ", " + sWid);
		}
		
		public static void setHeight(double mapped){
			
			if (mapped < .1) 
				{if (sHig != 1) sHig--;}
			else if (mapped < .2) 
				{if(sHig < 5) sHig++; if (sHig > 5) sHig--;}
			else if (mapped < .3) 
				{if(sHig < 10) sHig++; if(sHig > 10) sHig--;}
			else if (mapped < .4) 
				{if(sHig < 15) sHig++; if (sHig > 15) sHig--;}
			else if (mapped < .5) 
				{if(sHig < 20) sHig++; if (sHig > 20) sHig--;}
			else if (mapped < .6) 
				{if(sHig < 25) sHig++; if (sHig > 25) sHig--;;}
			else if (mapped < .7) 
				{if(sHig < 30) sHig++; if (sHig > 30) sHig--;;}
			else if (mapped < .8) 
				{if(sHig < 35) sHig++; if (sHig > 35) sHig--;}
			else if (mapped < .9) 
				{if(sHig < 40) sHig++; if (sHig > 40) sHig--;}
			else if (mapped < 1) 
				{if (sHig != 45) sHig++;}
				/*System.out.println(mapped + ", " + sWid);*/
		}
		
		public static double[] sentData(){
			return sendData;
		}

	static class MyRenderer implements Runnable {
		MyPanel panel;
		
		public MyRenderer(MyPanel panel) {
			this.panel = panel;
		}
		
		public void run()
		{
			while (true){
				panel.render();
				panel.repaint();
				
				try{
					Thread.sleep(100);	
				}
				catch (Exception ex){}
			}
		}
	}
}
	


