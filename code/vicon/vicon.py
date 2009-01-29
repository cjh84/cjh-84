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
				if line == ['Belt']:
					while True:
						belt.append(line[1:-1])
						line = reader.next()
						if not line: break
				if line == ['LeftHand']:
					while True:
						lefthand.append(line[1:-1])
						line = reader.next()
						if not line: break
				if line == ['RightHand']:
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
	
def numberize(ls):
	fls = []
	for l in ls:
		fls.append(map(float_and_round, l))
	return fls

def float_and_round(str_numb):
	#Multiply by 1000 so that everything doesn't round to same values!
	return int(float(str_numb)*1000)

	
def flatten(data):
	#Use mm ints instead of floats for position? Exact value not important with ANN?
	flatdata = []
	for frame in data:
		flatdata.append(sum(numberize(frame), []))
	return flatdata
	
	'''
	if not isinstance(line,(tuple,list)): return [line]
	if len(line)==0: return []
	return self.flatten(line[0])+self.flatten(line[1:])
	'''
	
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

if __name__ == '__main__':
	vicon = DataParserR()		
	data = vicon.getData("/home/cheryl/project/testing/TrainingData/Accelerate/001a.csv")
	print data

