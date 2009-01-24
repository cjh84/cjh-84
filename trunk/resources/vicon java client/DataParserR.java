/*
I've attached the class. Each time you call it, it will give you a double array the length of the number of data points that you have requested.

You can request the array by calling getData.
You will need to change the array (I've marked it)chanFinder to match the names of the data fields that you want.

I don't know what frame does. I may give the number of frames per second but it doesn't give the number of frame of data. I think it is only given on the initial handshake. 
*/

package projectorFirst1;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.io.IOException;
import java.net.Socket;
import java.nio.*;

public class DataParserR {
	final static int ERequest=0;
	final static int EReply=1;
	final static byte[] EReqInfo = {1, 0, 0, 0, 0, 0, 0, 0};

	final static int EInfo=1;
	final static int EData=2;
	final static byte[] EReqData = {2, 0, 0, 0, 0, 0, 0, 0};
	
	DataInputStream is;
	DataOutputStream os;
	
	//FileOutputStream out = new FileOutputStream("trial6_drawing.txt");
	
	int[] varMatch;
//CC -- changes these names to match whatever you have named your objects
	String[] channels;
	String[] chanFinder = {
			"HipP1:HipP1 <A-X>", "HipP1:HipP1 <A-Y>", "HipP1:HipP1 <A-Z>",
			"HipP2:HipP2 <A-X>", "HipP2:HipP2 <A-Y>", "HipP2:HipP2 <A-Z>",
			"HipP3:HipP3 <A-X>", "HipP3:HipP3 <A-Y>", "HipP3:HipP3 <A-Z>",
			"HipP1:HipP1 <T-X>", "HipP1:HipP1 <T-Y>",
			"HipP2:HipP2 <T-X>", "HipP2:HipP2 <T-Y>",
			"HipP3:HipP3 <T-X>", "HipP3:HipP3 <T-Y>",  
			"HandP1:HandP1 <A-X>", "HandP1:HandP1 <A-Y>", "HandP1:HandP1 <A-Z>",
			"HandP2:HandP2 <A-X>", "HandP2:HandP2 <A-Y>", "HandP2:HandP2 <A-Z>",
			"HandP3:HandP3 <A-X>", "HandP3:HandP3 <A-Y>", "HandP3:HandP3 <A-Z>",
			};
	
//Constructor opens the socket and creates a DataInputStream and a DataOutputStream
	public DataParserR() throws IOException{
		Socket socket = null;
	    try {
	          socket = new Socket("128.232.14.205", 800);

	          try {
	        	  is = new DataInputStream(socket.getInputStream());
	        	  os = new DataOutputStream(socket.getOutputStream());
	          } catch (Throwable e) {
	        	  System.err.println(e);
	        	  e.printStackTrace();
	          }
	          try{
	        	  channels = parseInfoPacket();  
	        	  matchStrings(channels);
	        	//  System.out.println("Done match");
	          } catch (Throwable e){};
	    }finally{}
	    
	}//End Constructor

	public double[] getData(){
		double[] data = new double[varMatch.length];
		
		try{
				parseDataPacket();

			double [] fulldata = parseDataPacket();
			//  System.out.println("Parsed Data Packet");
			
			for (int ii = 0; ii < varMatch.length; ii++){
				data[ii] = fulldata[varMatch[ii]]; 
			}//end for
			
		} catch(Throwable e){};
		return data;
	}
	
	public String[] parseInfoPacket()throws IOException, wrongException {
// Send request
        os.write(EReqInfo, 0, 8);
        
//Test Packet Header   	
      	 if (readNum(is) != EInfo || readNum(is) != EReply )
      	  	throw new wrongException("Bad Packet");
//Number of Channels
		int numString = readNum(is);  
//String array of Channel Names
		String[] channelNames = new String[numString];  
//Fills and returns a string array of Channel Names
		for (int a=0; a<numString; a++){
		  int numChar = readNum(is);         
		  char[] thisString = new char[numChar];
		  
		  for (int b=0; b<numChar; b++){
			  char charc = (char) is.readByte();
			  thisString[b] = charc;
		  }
		  
		  channelNames[a] = new String(thisString);
	  }
	  
	  return channelNames;
	}
	
	public void matchStrings(String[] channels){

		varMatch = new int[chanFinder.length];
		
		for (int a = 0; a < chanFinder.length; a++){
			for (int b = 0; b < channels.length; b++){
				if (chanFinder[a].compareTo(channels[b]) == 0){
					varMatch[a]= b;
				}//end if
			}//end for
		}//end for
		
		/*for (int c = 0; c < chanFinder.length; c++){
			for (int d = 0; d < channels.length; d++){
				if (chanFinder[c].compareTo(channels[d]) == 0){
					varMatch[c]= d;
				}//end if
			}//end for
		}//end for
*/	}// end matchStrings
	

	  public double[] parseDataPacket () throws IOException, wrongException {
//Request Data
		  os.write(EReqData, 0, 8);     
//Test Packet Header   	
		  if (readNum(is) != EData || readNum(is) != EReply )
		      throw new wrongException("Bad Packet");
//Number of doubles expected
		  int numDoubles = readNum(is);  
//array of double data
		  double[] data = new double[numDoubles];
		  for (int c=0; c<data.length; c++){
			  data[c] = readDoub(is);
		  }//end FOR
		  return data;
	  }//end parseDataPacket
	  
	  public static int readNum(DataInputStream is)throws IOException {
		  byte[] array = new byte[4];
    	  is.read(array, 0, 4);
    	  byte [] array1 = new byte[4];
    	  	array1[0] = (byte)(0x000000FF & (array[3]));
    	  	array1[1] = (byte)(0x000000FF & (array[2]));
    	  	array1[2] = (byte)(0x000000FF & (array[1]));
    	  	array1[3] = (byte)(0x000000FF & (array[0]));
    	  	
    	  	ByteBuffer bb = ByteBuffer.wrap(array1);
      	  	
    		int theInt = bb.getInt();
		return theInt;
	}
	
	public static double readDoub (DataInputStream is) throws IOException {
		
		byte[] array = new byte[8];
  	  	is.read(array, 0, 8);
  	  	byte [] array1 = new byte[8];
  	  	
  	  	array1[0] = (byte)(0x000000FF & (array[7]));
  	  	array1[1] = (byte)(0x000000FF & (array[6]));
  	  	array1[2] = (byte)(0x000000FF & (array[5]));
  	  	array1[3] = (byte)(0x000000FF & (array[4]));
  	  	array1[4] = (byte)(0x000000FF & (array[3]));
	  	array1[5] = (byte)(0x000000FF & (array[2]));
	  	array1[6] = (byte)(0x000000FF & (array[1]));
	  	array1[7] = (byte)(0x000000FF & (array[0]));
   
  	  	ByteBuffer bb = ByteBuffer.wrap(array1);
  	  	
		double theDoub = bb.getDouble();
		
		return theDoub;
		
	}
}//end of class DataParserR

class wrongException extends Exception {
	
	public wrongException(String msg){
		super(msg);
	}
}
