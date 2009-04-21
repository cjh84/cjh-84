import java.util.*;
import java.io.*;

class Config
{
	String homedir;
	String slash;
	private HashMap<String,String> dict;
	
	static Config singleton = null;
	
	static String lookup(String key)
	{
		if(singleton == null)
			singleton = new Config();
		return singleton.do_lookup(key);
	}
	
	private Config()
	{
		String filename = ".robotwars";
		String pathname;
		String line;
		String[] parts;
		
		dict = new HashMap<String,String>();
		homedir = System.getProperty("user.home");
		slash = System.getProperty("file.separator");
		
		pathname = homedir + slash + filename;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(pathname));
			while((line = in.readLine()) != null)
			{
				if(line.length() == 0 || line.charAt(0) == '#')
					continue;
				parts = line.split("=");
				if(parts.length != 2)
					Utils.error("Invalid config file line: <" + line + ">");
				dict.put(parts[0], parts[1]);
			}
			in.close();
		}
		catch(IOException e)
		{
			System.out.println("Cannot read config file from <" + pathname + ">");
			System.exit(0);
		}
		supply_defaults();
	}

	private void supply_defaults()
	{
		check_add("framerate", "100");
		check_add("closedthreshold", "3000");
		check_add("coordserver", "www.srcf.ucam.org");
		check_add("ctrlserver", "www.srcf.ucam.org");
	}
	
	private void check_add(String key, String defaultvalue)
	{
		if(!dict.containsKey(key))
			dict.put(key, defaultvalue);
	}
	
	private String do_lookup(String key) // Returns null if not found
	{
		return dict.get(key);
	}
		
	public static void main(String[] argv)
	{
		Config c = new Config();
		System.out.println("home dir = " + c.homedir);
		System.out.println("Slash char = " + c.slash);
	}
};

/*
*/
