import java.util.*;
import java.io.*;

class Config
{
	String homedir;
	String slash;
	private HashMap<String,String> dict;
	
	static Config singleton = null;
	
	public static String lookup(String key)
	{
		if(singleton == null)
			singleton = new Config();
		return singleton.do_lookup(key);
	}

	public static String set(String key, String value)
	{
		if(singleton == null)
			singleton = new Config();
		return singleton.do_set(key, value);
	}

	public static void dump_options()
	{
		if(singleton == null)
			singleton = new Config();
		singleton.do_dump();
	}
		
	//All private from here

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
		check_add("verbose", "false");
		check_add("neuralthreshold", "0.5");
		check_add("n_learner", "0");
		check_add("n_epochs", "1000");
		check_add("n_hidden_nodes", "20");
		check_add("n_learning_rate", "0.8");
		check_add("n_momentum", "0.3");
		check_add("n_train_on_negs", "false");
		check_add("m_learner", "0");
		check_add("m_hidden_states", "5");
		check_add("m_iterations", "10");
		check_add("index_filename", "training.dat");
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
	
	private String do_set(String key, String value)
	{
		return dict.put(key, value);
	}
	
	private void do_dump()
	{
		for (Map.Entry<String,String> pair : dict.entrySet())
		{
			Utils.log(pair.getKey() + " = " + pair.getValue());
		}
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
