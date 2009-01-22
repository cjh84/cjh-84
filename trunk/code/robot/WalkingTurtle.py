from Turtle import Turtle
from Vector import *
from Color import *

class WalkingTurtle(Turtle):       #### Inherit behavior from Turtle
    """This turtle walks in a straight line forever."""

    def __init__(self, position, heading, speed, fill=blue, **style):
        Turtle.__init__(self, position, heading, fill=fill, **style)
        self.speed = speed

    def Drive(self, velocity, radius):
	# +ve speed = forwards, -ve speed = backwards
	# +ve radius = turn left, -ve radius = turn right

	self.speed = velocity
	self.heading = radius

    #Change these turn left and  turn right for the actual robot!

    def TurnLeft(self):
	self.heading -= 10

    def TurnRight(self):
	self.heading += 10

    def Accelerate(self):
	self.speed += 1

    def Decelerate(self):
	self.speed -= 1

    def getnextstate(self):
        """Advance straight ahead."""
        return self.position + unit(self.heading)*self.speed, self.heading
