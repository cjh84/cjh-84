'''GUI for listening to status messages'''

from feedbackGUI import *
from Tkinter import *
import scop

sock1 = scop.scop_open("www.srcf.ucam.org", "feedbackp1")
sock2 = scop.scop_open("www.srcf.ucam.org", "feedbackp2")

scop.scop_listen(sock1, "p1status")
scop.scop_listen(sock2, "p2status")

tk = Tk()

defaultwidth = 600
defaultheight = 800

gui = feedbackGUI(tk, sock1, sock2, defaultwidth, defaultheight)
gui.pack

mainloop()
