#!/usr/bin/env python
''' this program changes the Krogh model into the new one '''

def _get_list_val(prob):
    if ":" in prob:
        tmpp=" ".join(prob.split(':'))
        vtmp=tmpp.split()
        list=" ".join([vtmp[x] for x in range(0,len(vtmp),2)])
        val=" ".join([vtmp[x] for x in range(1,len(vtmp),2)])
    elif prob == 'NULL':
        list='None'
        val='None'
    else:
        list=prob
        val='uniform'
    return(list,val)
#######################

import sys
#class krogh_state:
#   def __init__(self,name,tr,type,end,letter,label):

dict={}
names=[]
megalist=[]

if len(sys.argv)<2:
   print "usage :",sys.argv[0]," model_krogh"
   sys.exit(-1)

filename=sys.argv[1]
file=open(filename).readlines()
for line in file:
   stripped=line.strip()
   if stripped and stripped[0] != '#': # not a comment
       vec=line.split()
       megalist=megalist+vec    

#first level parsing
pos=0
while ('{' in megalist):
    name=megalist[0]
    names.append(name)
    b=megalist.index('{')+1
    e=(megalist.index('}'))
    # second level
    vec=(" ".join(megalist[b:e])).split(';')[:-1]
    d={}
    for item in vec:
       vtmp=item.split()
       if(vtmp[0] =='letter'): # mettilo = only
           vtmp[0]='only'
       d[vtmp[0]]=" ".join(vtmp[1:])
    dict[name]=d
#    dict[name]=megalist[b:e]
    pos=e+1
    megalist=megalist[pos:]

###########################
if names[0] == 'header':
    print "EMISSION_ALPHABET ",
    for i in dict['header']['alphabet']:
        print " ",i,
    print
    names=names[1:]
print "TRANSITION_ALPHABET ",
for name in names: print name,
print
for name in names:
    print "########## STATE ",name,"###############################################"
    print "NAME ",name
# transitions
    if 'trans' in dict[name].keys():
       trans,val=_get_list_val(dict[name]['trans'])
    else:
       trans=val='None'
    print "LINK ",trans
    print "TRANS ",val
# emissions
    if 'only' in dict[name].keys():
        em,val=_get_list_val(dict[name]['only'])
    else:
        tied_name=dict[name]['tied_letter']
        em,val=_get_list_val(dict[tied_name]['only'])
        val="tied "+tied_name
    if em == 'None':
        print "EM_LIST ",em
        print "EMISSION ",val
    else:
        print "EM_LIST ",
        for letter in dict['header']['alphabet']:
            print letter,
        print
        if val[0:5] == "tied ":
            print "EMISSION ",val
        else: 
            print "EMISSION ",
            letter_pos=em.split()
            letter_val=val.split()
            for letter in dict['header']['alphabet']:
                print letter_val[letter_pos.index(letter)],
            print
# endstate
    if 'end' not in dict[name].keys() or dict[name]['end']!='0':
        print "ENDSTATE 1"
    else:
        print "ENDSTATE 0"
   
# label 
    if 'label' in dict[name].keys():
        print "LABEL ",dict[name]['label']
    else:
        print "LABEL None"
#    for k in dict[name].keys():
#        print " ",k,dict[name][k]
#######################

