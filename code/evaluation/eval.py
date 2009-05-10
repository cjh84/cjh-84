#!/usr/bin/python

import os
import sys
from subprocess import *

modes = ["training", "recognition", "recog-no-negs"]
criteria = ["accuracy", "error", "performance"]
classifiers = ["heuristic", "neural", "markov", "hybrid"]

for mode in modes:
	for criterion in criteria:
			for classifier in classifiers:
				filename = mode[-1] + criterion[-1] + classifier[-1]
				pathname = "/home/cheryl/project/testing/Results/" + filename + ".dat"
				args = ["Evaluation", "/home/cheryl/project/testing/Cheryl/", "verbose=true"]
				args.append("mode=" + mode)
				args.append("criterion=" + criterion)				
				args.append("classifier=" + classifier)
				
				allargs = " ".join(args)
				
				cmd = "java >" + pathname + " Evaluation /home/cheryl/project/testing/Cheryl/ verbose=true mode=" + mode + " criterion=" + criterion + " classifier=" + classifier
				
				e = os.system(cmd)
				
				if (e != 0):
					os.remove(pathname)
