"""Ported from ExampleClient.cpp"""

import math

EPSILON = 1e-10

def to_euler(rx,ry,rz):

    #Convert AxisAngles rx,ry,rz to Quaternions qw,qx,qy,qz
    #ie. scale by the magnitude

    len = math.sqrt(rx*rx + ry*ry + rz*rz)
    qw = math.cos(len/2.0)
    tmp = math.sin(len/2.0)

    if len < 1e-10:
        qx = rx
        qy = ry
        qz = rz
    else:
        qx = rx * tmp/len
        qy = ry * tmp/len
        qz = rz * tmp/len

    # Convert AxisAngles to rotation matrix

    #Initialize an empty 3*3 list of lists ~ 3*3 matrix
    global_rotation = []
    for i in range(3):
        global_rotation.append([0]*3)

    if len < 1e-15:
        global_rotation[0][0] = global_rotation[1][1] = global_rotation[2][2] = 1.0
        global_rotation[0][1] = global_rotation[0][2] = global_rotation[1][0] = 0.0
        global_rotation[1][2] = global_rotation[2][0] = global_rotation[2][1] = 0.0
    else:
        x = rx/len
        y = ry/len
        z = rz/len
    
        c = math.cos(len)
        s = math.sin(len)
        t = 1 - c

        global_rotation[0][0] = c + t*x*x
        global_rotation[0][1] = t*x*y - s*z
        global_rotation[0][2] = t*x*z + s*y
        global_rotation[1][0] = t*x*y + s*z
        global_rotation[1][1] = c + t*y*y
        global_rotation[1][2] = t*y*z - s*x
        global_rotation[2][0] = t*x*z - s*y
        global_rotation[2][1] = t*y*z + s*x
        global_rotation[2][2] = c + t*z*z

    #Convert Rotation matrix to Euler angles

    ey = math.asin(-global_rotation[2][0])

    if math.fabs(math.cos(y)) > EPSILON:
        ex = math.atan2(global_rotation[2][1], global_rotation[2][2])
        ez = math.atan2(global_rotation[1][0], global_rotation[0][0])
    else:
        ex = math.atan2(global_rotation[2][1], global_rotation[2][2])
        ez = 0
 
    return ex,ey,ez

if __name__ == "__main__":
    import sys

    if len(sys.argv) != 4:
        print "Usage: python euler.py <x> <y> <z>"

    else:

        x = int(sys.argv[1])
        y = int(sys.argv[2])
        z = int(sys.argv[3])
        print to_euler(x,y,z)
