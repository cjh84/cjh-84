'''Transforms world coordinates to body coordinates.'''

def process(frames):
    for frame in frames:
        
        #Discard angles for arms
        frame = frame[0:6] + frame[9:12] + frame[15:18]   
        
        #Translate arms relative to belt
        for i in range(0,3):
            frame[i+6] = frame[i+6] - frame[i+3]     
            frame[i+9] = frame[i+9] - frame[i+3]
        
        #Discard position of belt
        frame = frame[0:3] + frame[6:12]
                
        
        #Rotate arms into belt coordinate system
            
        for datapoint in frame:
            print '%9.2f' %datapoint,
        print 
           
def dump(frames):
    for frame in frames:
        for datapoint in frame:
            print '%9.2f' %datapoint,
        print 

def usage():
    print "Usage: transform.py <filename>"

if __name__ == '__main__':

    import sys
    import GestureReader

    if len(sys.argv) != 2:
        usage()
    
    filename = sys.argv[1]

    gestureRdr = GestureReader.GestureReader()
    data = gestureRdr.getData(filename)
    data = process(data)
