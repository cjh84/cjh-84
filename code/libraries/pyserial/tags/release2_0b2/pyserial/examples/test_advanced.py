#!/usr/bin/env python
#needs at least python 2.2.3

#Python Serial Port Extension for Win32, Linux, BSD, Jython
#see __init__.py
#
#(C) 2001-2003 Chris Liechti <cliechti@gmx.net>
# this is distributed under a free software license, see license.txt

"""Some tests for the serial module.
Part of pyserial (http://pyserial.sf.net)  (C)2002 cliechti@gmx.net

Intended to be run on different platforms, to ensure portability of
the code.

These tests open a serial port and change all the settings on the fly.
If the port is realy correctly configured cannot be determined - that
would require external hardware or a nullmodem cable and an other
serial port library... Thus it mainly tests that all features are
correctly implemented and that the interface does what it should.
"""

import unittest
import serial

#on which port should the tests be performed:
PORT=0

class Test_ChangeAttributes(unittest.TestCase):
    """Test with timeouts"""
    
    def setUp(self):
        self.s = serial.Serial()        #create an closed serial port
    
    def tearDown(self):
        self.s.close()

    def test_PortSetting(self):
        self.s.port = PORT
        #portstr has to be set
        self.failUnlessEqual(self.s.portstr, serial.device(PORT))
        #test internals
        self.failUnlessEqual(self.s._port, PORT)
        #test on the fly change
        self.s.open()
        self.failUnless(self.s.isOpen())
        self.s.port = 0
        self.failUnless(self.s.isOpen())
        self.failUnlessEqual(self.s.port, 0)
        self.failUnlessEqual(self.s.portstr, serial.device(0))
        try:
            self.s.port = 1
        except serial.SerialException: #port not available on system
            pass        #cant test on this machine...
        else:
            self.failUnless(self.s.isOpen())
            self.failUnlessEqual(self.s.port, 1)
            self.failUnlessEqual(self.s.portstr, serial.device(1))

    def test_BaudrateSetting(self):
        for baudrate in (300, 9600, 19200, 115200):
            self.s.baudrate = baudrate
            #test get method
            self.failUnlessEqual(self.s.baudrate, baudrate)
            #test internals
            self.failUnlessEqual(self.s._baudrate, baudrate)
        #test illegal values
        for illegal_value in (-300, -1, 0, 301, 9000, 12345, 'a', None):
            self.failUnlessRaises(ValueError, self.s.setBaudrate, illegal_value)

    def test_BytesizeSetting(self):
        for bytesize in (5,6,7,8):
            self.s.bytesize = bytesize
            #test get method
            self.failUnlessEqual(self.s.bytesize, bytesize)
            #test internals
            self.failUnlessEqual(self.s._bytesize, bytesize)
        #test illegal values
        for illegal_value in (0, 1, 3, 4, 9, 10, 'a', None):
            self.failUnlessRaises(ValueError, self.s.setByteSize, illegal_value)

    def test_ParitySetting(self):
        for parity in (serial.PARITY_NONE, serial.PARITY_EVEN, serial.PARITY_ODD):
            self.s.parity = parity
            #test get method
            self.failUnlessEqual(self.s.parity, parity)
            #test internals
            self.failUnlessEqual(self.s._parity, parity)
        #test illegal values
        for illegal_value in (0, 57, 'a', None):
            self.failUnlessRaises(ValueError, self.s.setParity, illegal_value)

    def test_StopbitsSetting(self):
        for stopbits in (1, 2):
            self.s.stopbits = stopbits
            #test get method
            self.failUnlessEqual(self.s.stopbits, stopbits)
            #test internals
            self.failUnlessEqual(self.s._stopbits, stopbits)
        #test illegal values
        for illegal_value in (0, 3, 1.5, 57, 'a', None):
            self.failUnlessRaises(ValueError, self.s.setStopbits, illegal_value)

    def test_TimeoutSetting(self):
        for timeout in (None, 0, 1, 3.14159, 10, 1000, 3600):
            self.s.timeout = timeout
            #test get method
            self.failUnlessEqual(self.s.timeout, timeout)
            #test internals
            self.failUnlessEqual(self.s._timeout, timeout)
        #test illegal values
        for illegal_value in (-1, 'a'):
            self.failUnlessRaises(ValueError, self.s.setTimeout, illegal_value)

    def test_XonXoffSetting(self):
        for xonxoff in (True, False):
            self.s.xonxoff = xonxoff
            #test get method
            self.failUnlessEqual(self.s.xonxoff, xonxoff)
            #test internals
            self.failUnlessEqual(self.s._xonxoff, xonxoff)
        #no illegal values here, normal rules for the boolean value of an
        #object are used thus all objects have a truth value.

    def test_RtsCtsSetting(self):
        for rtscts in (True, False):
            self.s.rtscts = rtscts
            #test get method
            self.failUnlessEqual(self.s.rtscts, rtscts)
            #test internals
            self.failUnlessEqual(self.s._rtscts, rtscts)
        #no illegal values here, normal rules for the boolean value of an
        #object are used thus all objects have a truth value.

    def test_UnconfiguredPort(self):
        #an unconfigured port cannot be opened
        self.failUnlessRaises(serial.SerialException, self.s.open)
        
    def test_PortOpenClose(self):
        self.s.port = PORT
        for i in range(3):
            #open the port and check flag
            self.failUnless(not self.s.isOpen())
            self.s.open()
            self.failUnless(self.s.isOpen())
            self.s.close()
            self.failUnless(not self.s.isOpen())


if __name__ == '__main__':
    import sys
    print __doc__
    print "Testing port", PORT
    sys.argv.append('-v')
    # When this module is executed from the command-line, it runs all its tests
    unittest.main()
