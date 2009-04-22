from Tkinter import *
import scop
import select


class feedbackGUI(Frame):
    """This class provides the user interface for feedback."""

    def __init__(self, parent, sock1, sock2, width=800, height=800, **options):
        Frame.__init__(self, parent, **options)
        self.width, self.height = width, height

        canvasp1 = Canvas(parent, width=width, height=height, background="red")
        canvasp1.pack(expand=YES, fill=BOTH, side=LEFT)

        canvasp2 = Canvas(parent, width=width, height=height, background="green")
        canvasp2.pack(expand=YES, fill=BOTH, side=LEFT)

        parent.title("Feedback")
        self.parent = parent
        self.sock1 = sock1
        self.sock2 = sock2

        self.canvases = [canvasp1, canvasp2]   
        parent.after(40, self.checkmsg)

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
                
                print "Received <" + msg + "> from player " + str(player)
                
                if player > 0:
                    if msg == "ok":    self.ok(player-1)
                    elif msg == "dropout":   self.dropout(player-1)
        
        self.parent.after(40, self.checkmsg)
        
    def ok(self, playerno):
        self.canvases[playerno].config(bg="green")
        #print "Player " + str(playerno) + " received ok"
        
    def dropout(self, playerno):
        self.canvases[playerno].config(bg="red")
        #print "Player " + str(playerno) + " received dropout"
