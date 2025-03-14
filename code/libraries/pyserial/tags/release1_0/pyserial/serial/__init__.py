#!/usr/bin/env python
#portable serial port access with python
#this is a wrapper module for different platform implementations
#
# (C)2001 Chris Liechti <cliechti@gmx.net>
# this is distributed under a free software license, see license.txt

import sys, os, string
VERSION = string.split("$Revision: 1.1.1.1 $")[1]     #extract CVS version

#chose an implementation, depending on os
if os.name == 'nt': #sys.platform == 'win32':
    from serialwin32 import *
elif os.name == 'posix':
    from serialposix import *
elif os.name == 'java':
    from serialjava import *
else:
    raise "Sorry no implementation for your platform available."

#no "mac" implementation. someone want's to write it? i have no access to a mac.
