from Tkinter import *
from Arena import Arena
from WalkingTurtle import WalkingTurtle
from Vector import *

tk = Tk()
arena = Arena(tk)
arena.pack()
arena.add(WalkingTurtle(Vector(200,300), 0, 1, fill='turquoise'))
arena.add(WalkingTurtle(Vector(600,300), 0, 1, fill='purple'))
tk.mainloop()
