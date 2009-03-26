#A virtual Vicon system to read in recorded training data
import csv

class GestureReader:

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
        
def flatten(data):
    '''Flattens a list of lists of strings into a single list of floats.'''
    flatdata = []
    for frame in data:
        flatdata.append(map(float, sum(frame, [])))
    return flatdata
        
def merge(*args):
    '''Identical to zip() but returns a list of lists rather than a list of tuples.'''
    ret = []
    i = 0
    try:
        while 1:
            item = []
            for s in args:
                item.append(s[i])
            ret.append(item)
            i = i + 1
    except IndexError:
        return ret  



if __name__ == '__main__':

    import sys
    if len(sys.argv) != 2:
        usage()
    
    filename = sys.argv[1]

    gestureRdr = GestureReader()   
    data = gestureRdr.getData(filename)

    print data    
