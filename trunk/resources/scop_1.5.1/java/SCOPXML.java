/* SCOPXML.java - DMI - 21-9-2002 - SCOP Level 2 Java bindings

Copyright (C) 2002 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

import java.io.*;
import java.net.*;
import java.util.*;

public class SCOPXML extends SCOP
{
	public SCOPXML(String host, String name)
	{
		super(host, name);
	}
	
	public SCOPXML(String host, String name, boolean unique)
	{
		super(host, name, unique);
	}
	
	Vertex rpc(String endpoint, Vertex args)
	{
		return rpc(endpoint, args, null);
	}
	
	Vertex rpc(String endpoint, Vertex args, String method)
	{
		String request, reply;

		if(method != null)
			request = Vertex.vertex_to_string(args, method);
		else
			request = Vertex.vertex_to_string(args);

		reply = rpc(endpoint, request);
		if(reply == null)
			return null;

		Vertex ans = Vertex.string_to_vertex(reply);
		return ans;
	}
	
	Vertex get_request()
	{
		String buf;
		int rpc_flag;

		buf = get_message();
		if(buf == null || reply_required() == false)
			return null;

		Vertex v = Vertex.string_to_vertex(buf);
		return v;
	}
	
	void send_reply(Vertex reply)
	{
		String s;

		s = Vertex.vertex_to_string(reply);
		send_reply(s);
	}
	
	void send_struct(String endpoint, Vertex args)
	{
		send_struct(endpoint, args, null);
	}
	
	void send_struct(String endpoint, Vertex args, String method)
	{
		String msg;

		if(method != null)
			msg = Vertex.vertex_to_string(args, method);
		else
			msg = Vertex.vertex_to_string(args);

		send_message(endpoint, msg);
	}
	
	Vertex get_struct()
	{
		String buf;

		buf = get_message();
		if(buf == null)
			return null;

		Vertex v = Vertex.string_to_vertex(buf);
		return v;
	}
	
	Vertex get_cookie(String name)
	{
		String s = get_plain_cookie(name);
		if(s == null)
			return null;

		Vertex v = Vertex.string_to_vertex(s);
		return v;
	}
	
	void set_cookie(Vertex data)
	{
		String s = Vertex.vertex_to_string(data);
		set_plain_cookie(s);
	}
}
