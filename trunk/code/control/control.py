from Tkinter import *
from ControlGUI import Controls
import scop, os

def usage():
    print("Usage: python control.py [2]")
    sys.exit()

scopserver = os.getenv("SCOPCTRLSERVER", "www.srcf.ucam.org")

player = 1

argv = sys.argv[1:]

for arg in argv:
    if arg == '2':
        player = 2
    else:
        usage()

sock = scop.scop_open(scopserver, "controlp" + str(player))
scop.scop_set_source_hint(sock, "p" + str(player) + "ctrl")

tk = Tk()
controls = Controls(tk, sock, player)
controls.pack()
tk.mainloop()
