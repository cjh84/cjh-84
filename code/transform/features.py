class Features():
    def __init__(self, frames):
        self.leftarm = Ranges()
        self.rightarm = Ranges()
        for frame in frames:
            self.leftarm.update(frame.leftarm)
            self.rightarm.update(frame.rightarm)

    def dump(self):
        print 'Left Arm:'
        self.leftarm.dump()
        print 'Right Arm:'
        self.rightarm.dump()

class Ranges():
    def __init__(self):
        self.minx = self.miny = self.minz = 9999.0
        self.maxx = self.maxy = self.maxz = -9999.0

    def update(self, bodypart):
        self.minx = min(self.minx, bodypart.tx)
        self.maxx = max(self.maxx, bodypart.tx)
        self.miny = min(self.miny, bodypart.ty)
        self.maxy = max(self.maxy, bodypart.ty)
        self.minz = min(self.minz, bodypart.tz)
        self.maxz = max(self.maxz, bodypart.tz)

    def dump(self):
        dx = self.maxx - self.minx
        dy = self.maxy - self.miny
        dz = self.maxz - self.minz
        print 'dx = %f, dy = %f, dz = %f' % (dx,dy,dz)
