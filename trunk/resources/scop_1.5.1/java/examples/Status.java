// Status.java - DMI - 24-12-02

/* Usage: java Status */

import java.io.*;
import java.util.*;

public class Status
{
	public static void main(String[] args)
	{
		SCOP scop;
		Vector v;
		SCOP.ListNode ln;

		scop = new SCOP("localhost", "Status");
		v = scop.list();
		System.out.println(v.size() + " clients connected.");
		for(int i = 0; i < v.size(); i++)
		{
			ln = (SCOP.ListNode)v.elementAt(i);
			System.out.print("Client connection <" + ln.name);
			System.out.print("> listening to <" + ln.interest);
			System.out.print(">, source hint <" + ln.src_hint);
			System.out.println(">");
		}
		scop.close();
	}
}
