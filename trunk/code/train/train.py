#import ann.bpnn
import os
import vicon

class Trainer:

	def load(self):
	
		Vicon = vicon.DataParserR()
		#trainingdata = "/home/cheryl/project/testing/TrainingData"
		#for gestures in os.listdir(trainingdata):
		#	for gesturefile in os.listdir(trainingdata + gestures):
			
		#Read in examples of accelerate gestures

		acceldir = "/home/cheryl/project/testing/TrainingData/Accelerate"
		accelerates = [(os.path.join(acceldir, a)) for a in os.listdir(acceldir) if os.path.isfile(os.path.join(acceldir, a))]

		for accelerate in accelerates:
			acceldata = [(a,0) for a in Vicon.getData(accelerate)]
		
		#Read in examples of decelerate gestures
		deceldir = "/home/cheryl/project/testing/TrainingData/Decelerate"
		decelerates = [(os.path.join(deceldir, a)) for a in os.listdir(deceldir) if os.path.isfile(os.path.join(deceldir, a))]

		for decelerate in decelerates:
			deceldata = Vicon.getData(decelerate)		

		
		print acceldata[0]
		print acceldata[1]
	



	
if __name__ == '__main__':
	trainer = Trainer()
	trainer.load()
