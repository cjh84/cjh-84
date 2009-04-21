class Utils
{
	public static void error(String msg)
	{
		System.out.println(msg);
		System.exit(0);
	}

	static double square(double d)
	{
		return d * d;
	}
	
	static void delay(int ms)
	{
		try { Thread.sleep(ms); }
		catch (InterruptedException e) {}
	}
};

class CircularBuffer
{
	Frame[] circ;
	int ptr; // Position for next incoming item
	int size;
	
	CircularBuffer(int size, Frame init)
	{
		this.size = size;
		circ = new Frame[size];
		for(int i = 0; i < size; i++)
			circ[i] = init;
		ptr = 0;
	}
	
	void add(Frame f)
	{
		circ[ptr] = f;
		ptr++;
		if(ptr >= size)
			ptr -= size;
	}
	
	Frame get(int windowsize, int index)
	{
		int pos;
		
		if(windowsize < 0 || windowsize > size ||
				index < 0 || index >= windowsize)
			return null;
		pos = ptr - windowsize + index;
		if(pos < 0)
			pos += size;
		return circ[pos];
	}
	
	void test()
	{
		Frame f;
		int windowsize;
				
		for(int i = 0; i < size + 100; i++)
		{
			f = new Frame();
			f.body = new SixDOF();
			f.body.tx = (double)i;
			add(f);
		}
		/* If size == 500, data layout should now be:
			500...599,100..499 */
		ptr = size / 2;
		windowsize = 2 * size / 5;
		for(int i = 0; i < windowsize; i++)
		{
			f = get(windowsize, i);
			System.out.print(f.body.tx + ", ");
		}
		System.out.print("\n\n");
		/* Should have displayed 550...599,100...249 */
		windowsize = 3 * size / 5;
		for(int i = 0; i < windowsize; i++)
		{
			f = get(windowsize, i);
			System.out.print(f.body.tx + ", ");
		}
		/* Should have displayed 450...499,500...599,100...249 */
	}
};
