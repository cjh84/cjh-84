// MultiListener.java - DMI - 26-12-02

/* Usage: java MultiListener [ <source-one> <source-two> ]
   (default sources are "news" and "updates") */

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MultiListener
{
	public static void main(String[] args) throws IOException
	{
		SCOP[] scop = new SCOP[2];
		SocketChannel[] sc = new SocketChannel[2];
		SelectionKey[] s_key = new SelectionKey[2];
		String msg;
		Selector sel = null;
		Set ready_keys;
		int num_keys;
		
		sel = Selector.open();
		for(int i = 0; i < 2; i++)
		{
			scop[i] = new SCOP("localhost", "MultiListener");
		}
		scop[0].listen(args.length == 2 ? args[0] : "news");
		scop[1].listen(args.length == 2 ? args[1] : "updates");
		for(int i = 0; i < 2; i++)
		{
			sc[i] = scop[i].channel();
		}

		while(true)
		{
			// Select:
			for(int i = 0; i < 2; i++)
			{
				sc[i].configureBlocking(false);
				s_key[i] = sc[i].register(sel, SelectionKey.OP_READ);
			}
			num_keys = sel.select();
			ready_keys = sel.selectedKeys();
			
			for(int i = 0; i < 2; i++)
			{
				s_key[i].cancel();
				sc[i].configureBlocking(true);
				
				if(ready_keys.contains(s_key[i]))
				{
					msg = scop[i].get_message();
					System.out.println("Received <" + msg + "> from " +
							(i == 1 ? "updates" : "news"));
				}
			}
			sel.selectNow(); // Deregister all the cancelled keys
		}
	}
}
