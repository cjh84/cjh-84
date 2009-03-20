#A virtual Vicon system to read in recorded training data
import csv

class DataParserR:

    def getData(self, filename):
    
        reader = csv.reader(open(filename))
        
        belt = []
        lefthand = []
        righthand = []
        
        # Read in data and zip together the belt, lefthand and righthand
        # A-X, A-Y, A-Z are the global rotational values and they are AxisAngles. T-X ect are the global positions of an object in millimeters.
        # [1:-1] to discard frame number and empty end of line
        # One tuple of ([Belt values], [LeftHand values], [RightHand values]) per frame
        
        try:
            for line in reader:
                if line == ['Belt'] or line == ['Hat'] or line == ['BeltP1'] or line == ['BeltP2']:
                    while True:
                        belt.append(line[1:-1])
                        line = reader.next()
                        if not line: break
                if line == ['LeftHand'] or line == ['LeftArm'] or line == ['LeftArmP1']  or line == ['LeftArmP2']:
                    while True:
                        lefthand.append(line[1:-1])
                        line = reader.next()
                        if not line: break
                if line == ['RightHand'] or line == ['RightArm'] or line == ['RightArmP1'] or line == ['RightArmP2']:
                    while True:
                        righthand.append(line[1:-1])
                        line = reader.next()
                        if not line: break
                        
        except StopIteration:
            pass
        
        frames = merge(belt, lefthand, righthand)[2:]
        return flatten(frames)
        
        # Discard headers
        #return self.flatten(zip(belt, lefthand, righthand)[2:])        
        
def flatten(data):
    #Use mm ints instead of floats for position? Exact value not important with ANN?
    flatdata = []
    for frame in data:
        #flatdata.append(sum(numberize(frame), []))
        flatdata.append(map(float, sum(frame, [])))
    return flatdata
        
def merge(*args):
    #Identical to zip() but returns a list of lists
    ret = []
    i = 0
    try:
        while 1:
            item = []
            for s in args:
                item.append(s[i])
            ret.append(item)
            #ret.append(tuple(item))
            i = i + 1
    except IndexError:
        return ret  


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
            
    
    
    

"""
def transform(frames):
    data = discard(frames)
    return translate(data)

def discard(frames):
    newframes = []
    for frame in frames:
        newframe = frame[0:6] + frame[9:12] + frame[15:18]
        newframes.append(newframe)
    return newframes
"""

def dump(frames):
    for frame in frames:
        for datapoint in frame:
            print '%9.2f' %datapoint,
        print 

def usage():
    print "Usage: transform.py <filename>"

if __name__ == '__main__':

    import sys
    if len(sys.argv) != 2:
        usage()
    
    filename = sys.argv[1]

    vicon = DataParserR()   
    data = vicon.getData(filename)

    data = process(data)
    
