#import ann.bpnn
import os
import bpnn
import vicon
import format

class Trainer:

	def load(self):
	
		Vicon = vicon.DataParserR()
		#trainingdata = "/home/cheryl/project/testing/TrainingData"
		#for gestures in os.listdir(trainingdata):
		#	for gesturefile in os.listdir(trainingdata + gestures):
			
		#Read in examples of accelerate gestures

		acceldir = "/home/cheryl/project/testing/TrainingData/Accelerate"
		accelerates = [(os.path.join(acceldir, a)) for a in os.listdir(acceldir) if os.path.isfile(os.path.join(acceldir, a))]
		
		#Preprocess
		#Label accelerates with 0
		for accelerate in accelerates:
			acceldata = [[a,[0]] for a in Vicon.getData(accelerate)]
		
		import pprint
		p = pprint.PrettyPrinter()
		#p.pprint(acceldata[0])
		
		#Read in examples of decelerate gestures
		deceldir = "/home/cheryl/project/testing/TrainingData/Decelerate"
		decelerates = [(os.path.join(deceldir, a)) for a in os.listdir(deceldir) if os.path.isfile(os.path.join(deceldir, a))]

		#Label decelerates with 1
		for decelerate in decelerates:
			deceldata = [[d,[1]] for d in Vicon.getData(decelerate)]
			
		trainingdata = []
		trainingdata.extend(acceldata)
		trainingdata.extend(deceldata)
		
		return trainingdata

	def preprocess(self, gesturedir, label):
		gestures = [(os.path.join(gesturedir, g)) for g in os.listdir(gesturedir) if os.path.isfile(os.path.join(gesturedir, g))]
		for gesture in gestures:
			
			gesturedata = [[frame,[label]] for frame in Vicon.getData(gesture)]


	def train(self):

		pat = self.load()

		n = bpnn.NN(18, 18, 1)
	
		n.train(pat)

		n.test(pat)

	
if __name__ == '__main__':
	trainer = Trainer()
	trainer.train()
