from Tkinter import *
from Controls import Controls
import scop

sock = scop.scop_open("www.srcf.ucam.org", "control")
if len(sys.argv) > 1 and sys.argv[1] == "2":
	player = 2
	scop.scop_set_source_hint(sock, "p2ctrl")
else:
	player = 1
	scop.scop_set_source_hint(sock, "p1ctrl")

tk = Tk()
controls = Controls(tk, sock, player)
controls.pack()
tk.mainloop()
