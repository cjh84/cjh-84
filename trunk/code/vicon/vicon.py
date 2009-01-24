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
	
		# Discard headers
		return self.flatten(zip(belt, lefthand, righthand)[2:])
		
	def flatten(self, data):
		#Converts the mixed tuple and list return type into one list per frame
		#Floaterize
		#Use mm ints instead of floats for position? Exact value not important with ANN?
		for frame in data:
			frame = list(frame)
			for bodypart in frame:
				bodypart = [float(datapoint) for datapoint in bodypart]
			frame = sum(frame, [])

		return data
		
		'''
		if not isinstance(line,(tuple,list)): return [line]
		if len(line)==0: return []
		return self.flatten(line[0])+self.flatten(line[1:])
		'''
	
if __name__ == '__main__':
	vicon = DataParserR()		
	print vicon.getData("/home/cheryl/project/testing/TrainingData/Accelerate/001a.csv")[0]

