import java.io.*;

class capture
{
    public static void main (String[] args) 
    {
        DataParserR dataparser = null;
		SCOP scop1, scop2;
		double[] data;
		int points;
		
        try
		{
            dataparser = new DataParserR();
        }
        catch(IOException e)
        {
            System.out.println("Cannot open connection to Vicon");
            System.exit(0);
        }

		scop1 = new SCOP("www.srcf.ucam.org", "capturep1");
		scop2 = new SCOP("www.srcf.ucam.org", "capturep2");
		scop1.set_source_hint("p1coords");
		scop2.set_source_hint("p2coords");

		while(true)
		{
            data = dataparser.getData();
            points = data.length;
            if(points != 18 && points != 36)
            {
                System.out.println("Received " + points + " data points; " +
                    "expected 18 or 36.");
                System.exit(0);
            }
            output(data, 0, scop1, "P1");
            if(points == 36)
                output(data, 18, scop2, "P2");
		}
    }
    
    public static void output(double[] data, int startpos, SCOP scop, String label)
    {
        StringBuilder sb = new StringBuilder();
        
        for(int i = startpos; i < startpos + 18; i++)
        {
            sb.append(data[i]);
            if(i != startpos + 18 - 1)
                sb.append(' ');
        }
        String s = sb.toString();
        System.out.println(label + ": " + s);
		scop.emit(s);
    }
}	        

