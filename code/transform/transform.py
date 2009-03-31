'''Transforms world coordinates to body coordinates.'''

import math

EPSILON = 1.0e-5
DEBUG = False
STDOUT = False

class Frame():
    def __init__(self):
        self.body = SixDOF()
        self.leftarm = SixDOF()
        self.rightarm = SixDOF()

    def dump(self):
        print '%7.2f%7.2f%7.2f' % (self.body.ax, self.body.ay, self.body.az),
        print '%7.0f%7.0f%7.0f' % (self.leftarm.tx, self.leftarm.ty, \
            self.leftarm.tz),
        print '%7.0f%7.0f%7.0f' % (self.rightarm.tx, self.rightarm.ty, \
            self.rightarm.tz),
        print

    def headings(self):
        print ' BodyAx BodyAy BodyAz  LArmTx LArmTy LArmTz  RArmTx RArmTy \
            RArmTz'

class SixDOF():
    pass

def structure(data):
    '''Converts a list of lists into a list of Frames.'''
    frames = []
    for datum in data:
        frame = Frame()

        frame.body.ax = datum[0]
        frame.body.ay = datum[1]
        frame.body.az = datum[2]
        frame.body.tx = datum[3]
        frame.body.ty = datum[4]
        frame.body.tz = datum[5]

        frame.leftarm.ax = datum[6]
        frame.leftarm.ay = datum[7]
        frame.leftarm.az = datum[8]
        frame.leftarm.tx = datum[9]
        frame.leftarm.ty = datum[10]
        frame.leftarm.tz = datum[11]

        frame.rightarm.ax = datum[12]
        frame.rightarm.ay = datum[13]
        frame.rightarm.az = datum[14]
        frame.rightarm.tx = datum[15]
        frame.rightarm.ty = datum[16]
        frame.rightarm.tz = datum[17]

        frames.append(frame)

    return frames

def process(frames):
    if STDOUT:
        Frame().headings()
    for frame in frames:

        if DEBUG:
            print ' BodyTx BodyTy BodyTz'
            print '%7.0f%7.0f%7.0f' % (frame.body.tx, frame.body.ty,
frame.body.tz)

        if DEBUG: frame.dump()
        #Translate arms relative to belt
        translate(frame.leftarm, frame.body)
        translate(frame.rightarm, frame.body)

        if DEBUG: frame.dump()
        #Rotate arms into belt coordinate system
        rotate(frame.leftarm, frame.body)
        rotate(frame.rightarm, frame.body)

        if STDOUT:
            frame.dump()
        if DEBUG: break

def translate(obj,axes):
    obj.tx -= axes.tx
    obj.ty -= axes.ty
    obj.tz -= axes.tz

def rotate(obj,axes):
    obj.tx, obj.ty, obj.tz = rotate_point(axes.ax, axes.ay, axes.az, \
        obj.tx, obj.ty, obj.tz)
    obj.ax, obj.ay, obj.az = rotate_point(axes.ax, axes.ay, axes.az, \
        obj.ax, obj.ay, obj.az)

def rotate_point(ax,ay,az,x0,y0,z0):
    ''' ax,ay,az is angle-axis rotation
        x0,y0,z0 is point to be rotated
        Inefficient if rotating multiple points by same angle-axis vector '''

    magnitude = math.sqrt(ax*ax + ay*ay + az*az)

    if magnitude < EPSILON:
        return x0,y0,z0

    ax /= magnitude
    ay /= magnitude
    az /= magnitude

    if DEBUG:
        print 'ax=%f, ay=%f, az=%f, magnitude=%f' % (ax,ay,az,magnitude)
    s = math.sin(magnitude)
    c = math.cos(magnitude)
    t = 1 - c

    ''' Graphics Gems (Glassner, Academic Press, 1990) '''
    #http://www.gamedev.net/reference/articles/article1199.asp
    x1 = (t*ax*ax + c)*x0 + (t*ax*ay + s*az)*y0 + (t*ax*az - s*ay)*z0
    y1 = (t*ax*ay - s*az)*x0 + (t*ay*ay + c)*y0 + (t*ay*az + s*ax)*z0
    z1 = (t*ax*az + s*ay)*x0 + (t*ay*az - s*ax)*y0 + (t*az*az + c)*z0

    return x1,y1,z1

def headingtest(frames):
    for frame in frames:
        fx,fy,fz = rotate_point(frame.body.ax, frame.body.ay, frame.body.az, \
            0.0, 1.0, 0.0)
        print '%7.2f%7.2f%7.2f' % (fx,fy,fz)

def rotatetest():
    frame = Frame()

    frame.body.ax = 1.0 * math.pi
    frame.body.ay = 0.0
    frame.body.az = 0.0
    frame.body.tx = 1000.0
    frame.body.ty = 2000.0
    frame.body.tz = 1500.0

    frame.leftarm.ax = 1.0
    frame.leftarm.ay = 0.0
    frame.leftarm.az = 0.0
    frame.leftarm.tx = 1030.0
    frame.leftarm.ty = 1980.0
    frame.leftarm.tz = 1550.0

    frame.rightarm.ax = frame.leftarm.ax
    frame.rightarm.ay = frame.leftarm.ay
    frame.rightarm.az = frame.leftarm.az
    frame.rightarm.tx = frame.leftarm.tx
    frame.rightarm.ty = frame.leftarm.ty
    frame.rightarm.tz = frame.leftarm.tz

    frame.dump()
    translate(frame.leftarm, frame.body)
    translate(frame.rightarm, frame.body)

    frame.dump()
    rotate(frame.leftarm, frame.body)
    rotate(frame.rightarm, frame.body)
    frame.dump()

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
    import features

    if len(sys.argv) != 2:
        usage()

    filename = sys.argv[1]

    gestureRdr = GestureReader.GestureReader()
    data = gestureRdr.getData(filename)
    frames = structure(data)
    # rotatetest()
    #headingtest(frames)
    #sys.exit(0)
    feat = features.Features(frames)
    feat.dump()
    process(frames)
    feat = features.Features(frames)
    feat.dump()
