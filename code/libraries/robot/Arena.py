from Tkinter import *
from math import sin, cos, pi
from Vector import *

class Arena(Frame):
    """This class provides the user interface for an arena of turtles."""

    def __init__(self, parent, width=800, height=800, **options):
        Frame.__init__(self, parent, **options)
        self.width, self.height = width, height
        self.canvas = Canvas(self, width=width, height=height)
        self.canvas.pack()
        parent.title("UC Bereley CS9H Turtle Arena")

        Button(self, text='accel', command=self.Accelerate).pack(side=LEFT)
        Button(self, text='decel', command=self.Decelerate).pack(side=LEFT)
        Button(self, text='left', command=self.TurnLeft).pack(side=LEFT)
        Button(self, text='right', command=self.TurnRight).pack(side=LEFT)
        Button(self, text='startstop', command=self.Pause).pack(side=LEFT)

        self.turtles = []
        self.items = {}
        self.running = 0
        self.period = 20 # milliseconds
	
        self.canvas.bind('<ButtonPress>', self.press)
        self.canvas.bind('<Motion>', self.motion)
        self.canvas.bind('<ButtonRelease>', self.release)
	
	#Player 1
	parent.bind('a', lambda x : self.TurnLeft(0))
	parent.bind('d', lambda x : self.TurnRight(0))
	parent.bind('w', lambda x : self.Accelerate(0))
	parent.bind('s', lambda x : self.Decelerate(0))

	#Player 2
	parent.bind('<Left>', lambda x : self.TurnLeft(1))
	parent.bind('<Right>', lambda x : self.TurnRight(1))
	parent.bind('<Up>', lambda x : self.Accelerate(1))
	parent.bind('<Down>', lambda x : self.Decelerate(1))

	parent.bind('<space>', self.Pause)

        self.dragging = None

    def Accelerate(self, playerno=0):
        self.turtles[playerno].Accelerate()

    def Decelerate(self, playerno=0):
        self.turtles[playerno].Decelerate()

    def TurnLeft(self, playerno=0):
        self.turtles[playerno].TurnLeft()

    def TurnRight(self, playerno=0):
        self.turtles[playerno].TurnRight()

    def Pause(self, event=None):
	if self.running:
	    self.running = 0
	    self.stop()
	else:
	    self.running = 1
	    self.run()	

    def press(self, event):
        dragstart = Vector(event.x, event.y)
        for turtle in self.turtles:
            if (dragstart - turtle.position).length() < 10:
                self.dragging = turtle
                self.dragstart = dragstart
                self.start = turtle.position
                return

    def motion(self, event):
        drag = Vector(event.x, event.y)
        if self.dragging:
            self.dragging.position = self.start + drag - self.dragstart
            self.update(self.dragging)

    def release(self, event):
        self.dragging = None

    def update(self, turtle):
        """Update the drawing of a turtle according to the turtle object."""
        item = self.items[turtle]
        vertices = [(v.x, v.y) for v in turtle.getshape()]
        self.canvas.coords(item, sum(vertices, ()))
        self.canvas.itemconfigure(item, **turtle.style)

    def add(self, turtle):
        """Add a new turtle to this arena."""
        self.turtles.append(turtle)
        self.items[turtle] = self.canvas.create_polygon(0, 0)
        self.update(turtle)

    def step(self, stop=1):
        """Advance all the turtles one step."""
        nextstates = {}
        for turtle in self.turtles:
            nextstates[turtle] = turtle.getnextstate()
        for turtle in self.turtles:
            turtle.setstate(nextstates[turtle])
            self.update(turtle)
        if stop:
            self.running = 0

    def run(self):
        """Start the turtles running."""
        self.running = 1
        self.loop()

    def loop(self):
        """Repeatedly advance all the turtles one step."""
        self.step(0)
        if self.running:
            self.tk.createtimerhandler(self.period, self.loop)

    def stop(self):
        """Stop the running turtles."""
        self.running = 0
