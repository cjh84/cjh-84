// multiplex.cpp - DMI - 12-9-02

#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>

#include "multiplex.h"

/* Implementation note:

We either need to make a backup of read_fds and write_fds before each
select() call, or recreate them from another source (e.g. an array or
linked list) because select() overwrites them to indicate which FD's
are ready.

I choose the latter approach (recreating them from an array), because
(a) no standard copy function exists for fd_set's, and (b) after the
call we also need to extract bits using only FD_ISSET, and hence need
a way of enumerating possible fd's in any case. */

multiplex::multiplex(int capacity)
{
	lmode = MULTI_READ;
	rlist = new int[capacity];
	wlist = new int[capacity];
	num_read = num_write = 0;
}

multiplex::~multiplex()
{
	delete[] rlist;
	delete[] wlist;
}

multi_mode multiplex::last_mode()
{
	return lmode;
}

void multiplex::add(int fd, multi_mode mode)
{
	if(mode == MULTI_WRITE)
	{
		if(num_write == capacity)
			return;
		wlist[num_write++] = fd;
	}
	else
	{
		if(num_read == capacity)
			return;
		rlist[num_read++] = fd;
	}
}

void multiplex::clear()
{
	num_read = num_write = 0;
}

void multiplex::init_sets()
{
	max_fd = 0;
	FD_ZERO(&read_fds);
	FD_ZERO(&write_fds);
	for(int i = 0; i < num_read; i++)
	{
		FD_SET(rlist[i], &read_fds);
		if(rlist[i] > max_fd)
			max_fd = rlist[i];
	}
	for(int i = 0; i < num_write; i++)
	{
		FD_SET(wlist[i], &write_fds);
		if(wlist[i] > max_fd)
			max_fd = wlist[i];
	}
}

int multiplex::output()
{
	for(int i = 0; i < num_write; i++)
		if(FD_ISSET(wlist[i], &write_fds))
		{
			lmode = MULTI_WRITE;
			return wlist[i];
		}
	for(int i = 0; i < num_read; i++)
		if(FD_ISSET(rlist[i], &read_fds))
		{
			lmode = MULTI_READ;
			return rlist[i];
		}
	return -1;
}

int multiplex::poll()
{
	int ret;
	init_sets();
	tv.tv_sec = tv.tv_usec = 0;
	ret = select(max_fd + 1, &read_fds, &write_fds, NULL, &tv);
	if(ret < 1)
		return -1;
	return output();
}

int multiplex::wait()
{
	int ret;
	init_sets();
	ret = select(max_fd + 1, &read_fds, &write_fds, NULL, NULL);
	if(ret < 1)
		return -1;
	return output();
}

int multiplex::pause(int us)
{
	int ret;
	init_sets();
	tv.tv_sec = us / 1000000;
	tv.tv_usec = us % 1000000;
	ret = select(max_fd + 1, &read_fds, &write_fds, NULL, &tv);
	if(ret < 1)
		return -1;
	return output();
}
