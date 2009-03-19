/* SCOP.java - DMI - 15-9-2002 - SCOP Level 1 Java bindings

Copyright (C) 2002 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;

public class SCOP
{
	static final int FALLBACK_PORT = 51234;
	static final int MAJOR_VERSION = 1;
	static final int MINOR_VERSION = 2;
	static final int RELEASE_NUMBER = 0;
	static final int VERIFY_SYNTAX_ERROR = -1;

	private SocketChannel sc;
	private Socket socket;
	private InputStream input;
	private PrintStream output;
	private boolean last_rpc;
	private boolean valid;
	
	// Connection setup:

	public SCOP(String host, String name)
	{
		this(host, name, false);
	}
	
	boolean connection_ok()
	{
		return valid;
	}

	public SocketChannel channel()
	{
		return sc;
	}
		
	public SCOP(String host, String name, boolean unique)
	{
		int port = FALLBACK_PORT;
		
		valid = true;
		try
		{
			InetSocketAddress addr;
			addr = new InetSocketAddress(host, port);
			sc = SocketChannel.open(addr);
			
			socket = sc.socket();
			socket.setTcpNoDelay(true);

			input = socket.getInputStream();
			output = new PrintStream(socket.getOutputStream());

			if(name != null)
			{
				if(unique == true)
					clear(name);
				
				transmit("register " + name);
			}
		}
		catch(IOException e)
		{
			log("Can't connect to scopserver");
			valid = false;
		}
	}
	
	private void transmit(String s)
	{
		write_header(s.length());
		output.print(s);
		output.flush();
	}
	
	private byte hex(int d)
	{
		if(d >= 0 && d <= 9) return (byte)(d + '0');
		if(d >= 10 && d <= 15) return (byte)(d - 10 + 'A');
		return '0';
	}

	private int dec(byte c)
	{
		if(c >= '0' && c <= '9') return c - '0';
		if(c >= 'A' && c <= 'F') return c - 'A' + 10;
		return 0;
	}

	private void write_header(int len)
	{
		String s;
		int version, mask, shift;
		byte[] vbuf;
		byte[] lbuf;
		
		version = MAJOR_VERSION << 16;
		version += MINOR_VERSION << 8;
		version += RELEASE_NUMBER;
		
		vbuf = new byte[6];
		mask = 0xF00000;
		shift = 20;
		for(int i = 0; i < 6; i++)
		{
			vbuf[i] = hex((version & mask) >>> shift);
			mask >>>= 4;
			shift -= 4;
		}
	
		lbuf = new byte[8];
		mask = 0xF0000000;
		shift = 28;
		for(int i = 0; i < 8; i++)
		{
			lbuf[i] = hex((len & mask) >>> shift);
			mask >>>= 4;
			shift -= 4;
		}

		String s1 = new String(vbuf);
		String s2 = new String(lbuf);
		output.print("sCoP " + s1 + " " + s2 + " ");
	}
	
	private int fixed_read(InputStream is, byte[] buf, int nbytes)
	{
		int remain = nbytes;
		int pos = 0;
		int amount;

		while(remain > 0)
		{
			try
			{
				amount = is.read(buf, pos, remain);
			}
			catch(IOException e)
			{
				return -1;
			}
			if(amount <= 0)
				return -1; // EOF or error
			remain -= amount;
			pos += amount;
		}
		return 0;
	}
	
	private int read_int()
	{
		byte[] buf = new byte[8];
		int n = 0;
		
		if(fixed_read(input, buf, 8) == -1)
			return -1;
		
		int shift = 28;
		for(int i = 0; i < 8; i++)
		{
			n += dec(buf[i]) << shift;
			shift -= 4;
		}	
		return n;
	}
	
	private String read_protocol()
	{
		byte[] header, body;
		int len = 0, version = 0, shift;
		
		header = new byte[21];
		if(fixed_read(input, header, 21) == -1)
			return null;
		
		String intro = new String(header, 0, 4);
		if(intro.equals("sCoP") == false)
		{
			log("Protocol version mismatch");
			return null;
		}
		
		shift = 20;
		for(int i = 0; i < 6; i++)
		{
			version += dec(header[5 + i]) << shift;
			shift -= 4;
		}
		int major_version = (version & 0xFF0000) >>> 16;
		int minor_version = (version & 0x00FF00) >>> 8;
		if(major_version != MAJOR_VERSION ||
				minor_version != MINOR_VERSION)
		{
			log("Protocol version mismatch");
			return null;
		}

		shift = 28;
		for(int i = 0; i < 8; i++)
		{
			len += dec(header[12 + i]) << shift;
			shift -= 4;
		}
		body = new byte[len];
		if(fixed_read(input, body, len) == -1)
		{
			return null;
		}
		return new String(body);
	}
	
	private void log(String msg)
	{
		String homedir = System.getProperty("user.home");
		String logfile = homedir + "/.scoplog";

		try
		{
			OutputStream fout;
			PrintStream ps;
			
			fout = new FileOutputStream(logfile, true);
			ps = new PrintStream(fout);
			ps.println(msg);
			fout.close();
		}
		catch(IOException e) {}
	}
	
	void close()
	{
		try
		{
			socket.close();
		}
		catch(IOException e) {}
	}
	
	void listen(String interest)
	{
		listen(interest, false);
	}

	void listen(String interest, boolean unique)
	{
		if(unique == true)
			clear(interest);
		
		transmit("listen " + interest);
	}

	// Messaging:

	void send_message(String endpoint, String message)
	{
		send_message(endpoint, message, false);
	}
	
	int send_message(String endpoint, String message, boolean verify)
	{
		if(verify == true)
			transmit("verify " + endpoint + "! " + message);
		else
			transmit("message " + endpoint + "! " + message);
		
		if(verify == true)
		{
			int status = read_int();
			return status;
		}
		else
			return -1;
	}
	
	private static final String SCOP_RPC_CALL = "scop-rpc-call ";
	
	String get_message() // Also used for getting RPC's
	{
		String s = read_protocol();
		if(s == null) return null;
		
		if(s.startsWith(SCOP_RPC_CALL))
		{
			last_rpc = true;
			return s.substring(SCOP_RPC_CALL.length());
		}
		else
		{
			last_rpc = false;
			return s;
		}
	}
	
	// RPC:
	
	boolean reply_required()
	{
		return last_rpc;
	}

	String rpc(String endpoint, String args)
	{
		transmit("call " + endpoint + "! " + args);
		
		String reply = get_message();
		if(reply.equals("scop-rpc-error"))
			return null;
		
		return reply;
	}
	
	void send_reply(String reply)
	{
		transmit("reply " + reply);
	}
	
	// Predefined event sources:

	void set_source_hint(String endpoint)
	{
		transmit("set-source-hint " + endpoint);
	}
	
	void emit(String message)
	{
		emit(message, false);
	}

	int emit(String message, boolean verify)
	{
		transmit("emit " + message);
		int status = read_int();
		return verify == true ? status : 0;
	}

	// Admin:

	int query(String endpoint)
	{
		transmit("query " + endpoint);
		int answer = read_int();
		return answer;
	}
	
	void clear(String endpoint)
	{
		transmit("clear " + endpoint);
	}
	
	void set_log(int log_level)
	{
		transmit("log " + log_level);
	}
	
	void terminate()
	{
		transmit("terminate");
	}
	
	void reconfigure()
	{
		transmit("reconfigure");
	}
	
	void set_plain_cookie(String text)
	{
		transmit("set-cookie " + text);
	}

	String get_plain_cookie(String name)
	{
		transmit("get-cookie " + name);
		String s = read_protocol();
		if(s == null) return null;
		
		return s;
	}
	
	Vector list()
	{
		Vector v;
		ListNode ln;
		String name = "foo";
		String interest = "bar";
		String src_hint = "zoo";
		
		transmit("list");
		String s = read_protocol();
		String[] sarr = s.split("!", -1);
		int n = sarr.length;
		if(n % 3 != 1)
		{
			log("List parsing sanity check failed!");
			return null;
		}
		
		v = new Vector();
		for(int i = 0; i < (n - 1) / 3; i++)
		{
			name = sarr[i * 3];
			interest = sarr[i * 3 + 1];
			src_hint = sarr[i * 3 + 2];
			ln = new ListNode(name, interest, src_hint);
			v.addElement(ln);
		}
		return v;
	}

	class ListNode
	{
		String name;
		String interest;
		String src_hint;

		public ListNode(String name, String interest, String src_hint)
		{
			this.name = name;
			this.interest = interest;
			this.src_hint = src_hint;
		}
	}
}
