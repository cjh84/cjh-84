#import ann.bpnn
import os
import bpnn
import vicon
import params

class Trainer:

	def __init__(self):
		self.vicon = vicon.DataParserR()
		self.neural_network = bpnn.NN(18, 18, 2)

	def load(self):
	
		#trainingdata = "/home/cheryl/project/testing/TrainingData"
		#for gestures in os.listdir(trainingdata):
		#	for gesturefile in os.listdir(trainingdata + gestures):
			
		
		trainingdata = []	
	
		trainingdata.extend(self.preprocess("/home/cheryl/project/testing/TrainingData/Accelerate", [0,1]))
		trainingdata.extend(self.preprocess("/home/cheryl/project/testing/TrainingData/Decelerate", [1,0]))
		
		return trainingdata		

	def preprocess(self, gesturedir, label):
		#Load a set of gestures and label

		gestures = [(os.path.join(gesturedir, g)) for g in os.listdir(gesturedir) if os.path.isfile(os.path.join(gesturedir, g))]
		
		for gesture in gestures:
			gesturedata = [[frame,label] for frame in self.vicon.getData(gesture)]

		return gesturedata


	def train(self):

		pat = self.load()

		
		"""
		import pprint
		p = pprint.PrettyPrinter()
		p.pprint(pat[0])
		"""
	
		self.neural_network.train(pat)


	def test(self):
	
		testdata = []
		testdata = self.preprocess("/home/cheryl/project/testing/TestingData", None)
		#print testdata[0]
		self.neural_network.test(testdata)
		
	
if __name__ == '__main__':
	trainer = Trainer()
	trainer.train()
	#trainer.test()
