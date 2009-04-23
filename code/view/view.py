'''Listen to SCOP streams for commands and render some turtles.'''

from Tkinter import *
from Arena import Arena
from WalkingTurtle import WalkingTurtle
from Vector import *
import scop

scopserver = os.getenv("SCOPCTRLSERVER", "www.srcf.ucam.org")

sock1 = scop.scop_open(scopserver, "viewp1")
sock2 = scop.scop_open(scopserver, "viewp2")

scop.scop_listen(sock1, "p1ctrl")
scop.scop_listen(sock2, "p2ctrl")

tk = Tk()
arena = Arena(tk, sock1, sock2)
arena.pack()
arena.add(WalkingTurtle(Vector(200,300), 0, 1, fill='turquoise'))
arena.add(WalkingTurtle(Vector(600,300), 0, 1, fill='purple'))
tk.mainloop()
