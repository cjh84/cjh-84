from Tkinter import *
import scop


class Controls(Frame):
    """This class provides mousebutton and arrow key support for emitting basic controls (a,d,l,r,s) to an SCOP socket."""

    def __init__(self, parent, sock, player, width=800, height=800, **options):
        Frame.__init__(self, parent, **options)
        self.width, self.height = width, height
        parent.title("Player " + str(player))
        self.sock = sock     
        
        Button(self, text='accel', command=self.Accelerate).grid(row=0, column=1)
        Button(self, text='decel', command=self.Decelerate).grid(row=2, column=1)
        Button(self, text='left', command=self.TurnLeft).grid(row=1, column=0)
        Button(self, text='right', command=self.TurnRight).grid(row=1, column=2)
        Button(self, text='startstop', command=self.Pause).grid(row=1, column=1)

        self.running = 0
        self.period = 20 # milliseconds

        parent.bind('<KeyPress-Left>', self.TurnLeft)
        parent.bind('<KeyPress-Right>', self.TurnRight)
        parent.bind('<KeyPress-Up>', self.Accelerate)
        parent.bind('<KeyPress-Down>', self.Decelerate)
        parent.bind('<KeyPress-space>', self.Pause)

    def Accelerate(self, event=None):
        print "accel"
        scop.scop_emit(self.sock, "a")

    def Decelerate(self, event=None):
        print "decel"
        scop.scop_emit(self.sock, "d")

    def TurnLeft(self, event=None):
        print "turn left"
        scop.scop_emit(self.sock, "l")

    def TurnRight(self, event=None):
        print "turn right pressed"
        scop.scop_emit(self.sock, "r")
        
    def Pause(self, event=None):
	if self.running:
	    self.running = 0
	    print "startstop"
	    scop.scop_emit(self.sock, "s")
	else:
	    self.running = 1
	    print "startstop"
	    scop.scop_emit(self.sock, "s")
