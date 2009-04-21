from Tkinter import *
from Controls import Controls
import scop

def usage():
    print("Usage: python control.py [2]")
    sys.exit()

sock = scop.scop_open("www.srcf.ucam.org", "control")

player = 1
scop.scop_set_source_hint(sock, "p1ctrl")

#if '2' in sys.argv: print "2!"

argv = sys.argv[1:]

for arg in argv:
    if arg == '2':
        player = 2
        scop.scop_set_source_hint(sock, "p2ctrl")
    else:
        usage()

tk = Tk()
controls = Controls(tk, sock, player)
controls.pack()
tk.mainloop()
