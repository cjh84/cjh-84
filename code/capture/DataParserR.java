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
	String[] channels;
	
	String[] chanFinder = {
	        "BodyP1:BodyP1 <A-X>", "BodyP1:BodyP1 <A-Y>", "BodyP1:BodyP1 <A-Z>",  
	        "BodyP1:BodyP1 <T-X>", "BodyP1:BodyP1 <T-Y>", "BodyP1:BodyP1 <T-Z>",  
	        "LeftArmP1:LeftArmP1 <A-X>", "LeftArmP1:LeftArmP1 <A-Y>", "LeftArmP1:LeftArmP1 <A-Z>",  
	        "LeftArmP1:LeftArmP1 <T-X>", "LeftArmP1:LeftArmP1 <T-Y>", "LeftArmP1:LeftArmP1 <T-Z>",  
	        "RightArmP1:RightArmP1 <A-X>", "RightArmP1:RightArmP1 <A-Y>", "RightArmP1:RightArmP1 <A-Z>",  
	        "RightArmP1:RightArmP1 <T-X>", "RightArmP1:RightArmP1 <T-Y>", "RightArmP1:RightArmP1 <T-Z>",
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
