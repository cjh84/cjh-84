'''Listen to SCOP streams for commands and render some turtles.'''

from feedbackGUI import *
from Tkinter import *
import scop

sock1 = scop.scop_open("www.srcf.ucam.org", "feedbackp1")
sock2 = scop.scop_open("www.srcf.ucam.org", "feedbackp2")

scop.scop_listen(sock1, "p1status")
scop.scop_listen(sock2, "p2status")

#"ok" "dropout"

tk = Tk()

defaultwidth = 600
defaultheight = 800

gui = feedbackGUI(tk, sock1, sock2, defaultwidth, defaultheight)
gui.pack

"""
p1 = Canvas(tk, width=defaultwidth, height=defaultheight, background="red")
p1.pack(expand=YES, fill=BOTH, side=LEFT)
#p1.create_text(defaultwidth, defaultheight, text="Player 1")

p2 = Canvas(tk, width=defaultwidth, height=defaultheight, background="green")
p2.pack(expand=YES, fill=BOTH, side=LEFT)
#p2.create_text(defaultwidth, defaultheight, text="Player 2")

#w.itemconfig(i, fill="blue") # change color
#w.create_rectangle(0, 0, w.width/2, w.width, fill="red")
"""
mainloop()
