// multiplex.h - DMI - 14-9-02

#include <sys/time.h>

enum multi_mode { MULTI_READ, MULTI_WRITE };

class multiplex
{
	public:
		
		multiplex(int capacity = 10);
		~multiplex();
		
		void add(int fd, multi_mode mode = MULTI_READ);
		void clear();

		// These functions return a FD, or -1 if none ready before timeout:
		int poll();
		int wait();
		int pause(int us);
		
		multi_mode last_mode();
		
	private:
		
		fd_set read_fds, write_fds;
		int max_fd;
		struct timeval tv;
		multi_mode lmode;
		
		int *rlist, *wlist;
		int num_read, num_write, capacity;
		
		void init_sets();
		int output();
};
