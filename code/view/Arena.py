from Tkinter import *
from math import sin, cos, pi
from Vector import *
import scop
import select


class Arena(Frame):
    """This class provides the user interface for an arena of turtles."""

    def __init__(self, parent, sock1, sock2, width=800, height=800, **options):
        Frame.__init__(self, parent, **options)
        self.width, self.height = width, height
        self.canvas = Canvas(self, width=width, height=height)
        self.canvas.pack()
        parent.title("Viewing p1ctrl and p2ctrl")
        self.parent = parent
        self.sock1 = sock1
        self.sock2 = sock2

        self.turtles = []
        self.items = {}
        self.running = 0
        self.period = 20 # milliseconds

        self.canvas.bind('<ButtonPress>', self.press)
        self.canvas.bind('<Motion>', self.motion)
        self.canvas.bind('<ButtonRelease>', self.release)
    
        parent.after(40, self.checkmsg)
        self.dragging = None


    def checkmsg(self):
        while True:
            read_fds = [self.sock1, self.sock2]
            r, w, e = select.select(read_fds, [], [], 0)
            if not r:   break
            for fd in r:
                msg, rpc_flag = scop.scop_get_message(fd)
                
                player = -1
                if fd == self.sock1:    player = 1
                elif fd == self.sock2:  player = 2
                
                #print "Received <" + msg + "> from player " + str(player)
                
                if player > 0:
                    if msg == "a":    self.Accelerate(player-1)
                    elif msg == "d":    self.Decelerate(player-1)
                    elif msg == "l":    self.TurnLeft(player-1)
                    elif msg == "r":    self.TurnRight(player-1)
                    elif msg == "s":    self.Pause(player-1)
        
        self.parent.after(40, self.checkmsg)
        

    def Accelerate(self, turtleno=0):
        self.turtles[turtleno].Accelerate()

    def Decelerate(self, turtleno=0):
        self.turtles[turtleno].Decelerate()

    def TurnLeft(self, turtleno=0):
        self.turtles[turtleno].TurnLeft()

    def TurnRight(self, turtleno=0):
        self.turtles[turtleno].TurnRight()

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
