#!/usr/bin/python

import os
import sys
from subprocess import *
import scop

def usage():
    print("Usage: python eval.py <option>")
    print("option: all")
    print("option: neural <parameter> <init> <max> <stepsize> <type: i f>")
    print("option: markov <parameter> <init> <max> <stepsize> <type: i f>")
    print("option: slwindow <parameter> <init> <max> <stepsize> <type: i f>")
    sys.exit()

modes = ["training", "recognition", "no-negs-recog"]
criteria = ["accuracy", "error", "performance"]
classifiers = ["heuristic", "neural", "markov", "hybrid"]

def evaluate(resultsfile, options=""):
    pathname = "/home/cheryl/project/testing/Results/" + resultsfile
    
    #Append results to pathname    
    command = "java Evaluation /home/cheryl/project/testing/Cheryl/ verbose=false "
    command += options + ">>" + pathname
    
    e = os.system(command)
    """
    if (e != 0):
        os.remove(pathname)
    else:
        print options
    """

def arange(start, stop=None, step=None):
    if stop is None:
        stop = float(start)
        start = 0.0
    if step is None:
        step = 1.0
    cur = float(start)
    while cur < stop:
        yield cur
        cur += step
    
try:
    do = sys.argv[1]
except:
    usage()

if do == "all":
    for mode in modes:
        for criterion in criteria:
                for classifier in classifiers:
                    filename =  criterion[0:5] + "_" + mode[0:5]
                    pathname = "all/" + filename + ".dat"
                    options = "criterion=" + criterion + " mode=" + mode + " classifier=" + classifier
                    evaluate(pathname, options)
                    
elif do == "neural":
    try:
        n_option = sys.argv[2]
        n_start = sys.argv[3]
        n_parameter = sys.argv[4]
        n_stepsize = sys.argv[5]
        n_type =  sys.argv[6]
    except:
        usage()

    filename = n_option
    
    for i in arange(float(n_start), float(n_parameter), float(n_stepsize)):
        if n_type == "i":
            i = int(i)
        pathname = "neural/" + filename + ".dat"
        #neural networks generate error and performance data at the same time
        options = "mode=training classifier=neural " + n_option + "=" + str(i) + " criterion=error"
        evaluate(pathname, options)  
        #get accuracy of training
        options = "mode=recognition classifier=neural " + n_option + "=" + str(i) + " criterion=accuracy"
        evaluate(pathname, options)  

elif do == "markov":
    try:
        m_option = sys.argv[2]
        m_start = sys.argv[3]
        m_parameter = sys.argv[4]
        m_stepsize = sys.argv[5]
        m_type =  sys.argv[6]
    except:
        usage()

    filename = m_option
    
    for i in arange(float(m_start), float(m_parameter), float(m_stepsize)):
        if m_type == "i":
            i = int(i)
        pathname = "markov/" + filename + ".dat"
        options = "mode=training classifier=markov " + m_option + "=" + str(i) + " criterion=performance"
        evaluate(pathname, options)
        options = "mode=recognition classifier=markov criterion=accuracy"
        evaluate(pathname, options)
    
else:
    usage()
