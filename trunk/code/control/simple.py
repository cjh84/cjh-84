from Tkinter import *
from Arena import Arena
import scop

sock = scop.scop_open("www.srcf.ucam.org", "control")
if len(sys.argv) > 1 and sys.argv[1] == "2":
	player = 2
	scop.scop_set_source_hint(sock, "p2ctrl")
else:
	player = 1
	scop.scop_set_source_hint(sock, "p1ctrl")

tk = Tk()
arena = Arena(tk, sock, player)
arena.pack()
#arena.add(WalkingTurtle(Vector(200,300), 0, 1, fill='turquoise'))
#arena.add(WalkingTurtle(Vector(600,300), 0, 1, fill='purple'))
tk.mainloop()
