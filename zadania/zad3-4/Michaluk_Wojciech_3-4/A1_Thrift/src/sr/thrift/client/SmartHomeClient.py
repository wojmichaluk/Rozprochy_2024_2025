import sys

sys.path.append('../../../../gen-py')

from random import randint
from ThriftGen import *
from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TCompactProtocol, TMultiplexedProtocol

if __name__ == "__main__":
    # servers info
    host = "127.0.0.2"
    port1 = 9080
    port2 = 9070

    server = input("Which server you want to choose? Type '1' for blinds and bulbulators, '2' for detectors, '3' for both: ")
    while server not in ['1', '2', '3']:
        print("Unknown option. Please try again")
        server = input("Which server you want to choose? ")

    if server != '2':
        transport1 = TTransport.TBufferedTransport(TSocket.TSocket(host, port1))
        protocol1 = TCompactProtocol.TCompactProtocol(transport1)

        # services
        blinds1 = Blinds.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol1, "bl1"))
        blinds2 = Blinds.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol1, "bl2"))
        bulbulator1 = Bulbulator.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol1, "bu1"))
        bulbulator2 = Bulbulator.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol1, "bu2"))

        # connecting to server
        transport1.open()

    if server != '1':
        transport2 = TTransport.TBufferedTransport(TSocket.TSocket(host, port2))
        protocol2 = TCompactProtocol.TCompactProtocol(transport2)

        # services
        detector1 = Detector.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol2, "dt1"))
        detector2 = Detector.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol2, "dt2"))
        detector3 = Detector.Client(TMultiplexedProtocol.TMultiplexedProtocol(protocol2, "dt3"))

        # connecting to server
        transport2.open()

    while True:
        line = input("==> ")
        flag = False
        try:
            if line == "x":
                break
            if server != '2':
                if line == "pull-up1":
                    percent = randint(15, 30)
                    blindsState = blinds1.pullUp(percent)
                    print(f"Blinds1: after pullUp({percent}) roll percent is {blindsState}")
                elif line == "pull-down1":
                    percent = randint(15, 30)
                    blindsState = blinds1.pullDown(percent)
                    print(f"Blinds1: after pullDown({percent}) roll percent is {blindsState}")
                elif line == "pull-up2":
                    percent = randint(25, 40)
                    blindsState = blinds2.pullUp(percent)
                    print(f"Blinds2: after pullUp({percent}) roll percent is {blindsState}")
                elif line == "pull-down2":
                    percent = randint(25, 40)
                    blindsState = blinds2.pullDown(percent)
                    print(f"Blinds2: after pullDown({percent}) roll percent is {blindsState}")
                elif line == "turn-on1":
                    bulbulator1.turnOn()
                    print("Bulbulator1: turned on!")
                elif line == "turn-off1":
                    bulbulator1.turnOff()
                    print("Bulbulator1: turned off!")
                elif line == "turn-on2":
                    bulbulator2.turnOn()
                    print("Bulbulator2: turned on!")
                elif line == "turn-off2":
                    bulbulator2.turnOff()
                    print("Bulbulator2: turned off!")
                else:
                    if server == '3': flag = True
                    else: print("???")
            if server != '1':
                if line == "get-params1":
                    params = detector1.getParams()
                    print("Detector1: parameters:")
                    for param in params:
                        print(f"{param}: {params[param]}")
                elif line == "get-params2":
                    params = detector2.getParams()
                    print("Detector2: parameters:")
                    for param in params:
                        print(f"{param}: {params[param]}")
                elif line == "get-params3":
                    params = detector3.getParams()
                    print("Detector3: parameters:")
                    for param in params:
                        print(f"{param}: {params[param]}")
                elif line == "get-param1":
                    param = input("Please give parameter name: ")
                    val = detector1.getParamValue(param)
                    print(f"Detector1: value of this parameter is {val}")
                elif line == "get-param2":
                    param = input("Please give parameter name: ")
                    val = detector2.getParamValue(param)
                    print(f"Detector2: value of this parameter is {val}")
                elif line == "get-param3":
                    param = input("Please give parameter name: ")
                    val = detector3.getParamValue(param)
                    print(f"Detector3: value of this parameter is {val}")
                elif line == "param-safe1":
                    param = input("Please give parameter name: ")
                    bounds = [0, 10] if param == "smoke" else [2, 100] if param == "humidity" else [0, 0]
                    detector1.checkParamSafety(param, bounds)
                    print(f"Detector1: value of this parameter is within safe bounds: {bounds}")
                elif line == "param-safe2":
                    param = input("Please give parameter name: ")
                    bounds = [0, 3] if param == "CO" else [10, 40] if param == "CO2" else [0, 0]
                    detector2.checkParamSafety(param, bounds)
                    print(f"Detector2: value of this parameter is within safe bounds: {bounds}")
                elif line == "param-safe3":
                    param = input("Please give parameter name: ")
                    bounds = [-15, 35] if param == "temperature" else [960, 1040] if param == "pressure" else [0, 0]
                    detector3.checkParamSafety(param, bounds)
                    print(f"Detector3: value of this parameter is within safe bounds: {bounds}")
                elif line == "param-unsafe1":
                    param = input("Please give parameter name: ")
                    bounds = [0, 3] if param == "smoke" else [30, 100] if param == "humidity" else [0, 0]
                    detector1.checkParamSafety(param, bounds)
                    print(f"Detector1: value of this parameter is within safe bounds: {bounds}")
                elif line == "param-unsafe2":
                    param = input("Please give parameter name: ")
                    bounds = [0, 1] if param == "CO" else [10, 25] if param == "CO2" else [0, 0]
                    detector2.checkParamSafety(param, bounds)
                    print(f"Detector2: value of this parameter is within safe bounds: {bounds}")
                elif line == "param-unsafe3":
                    param = input("Please give parameter name: ")
                    bounds = [-15, 15] if param == "temperature" else [960, 1000] if param == "pressure" else [0, 0]
                    detector3.checkParamSafety(param, bounds)
                    print(f"Detector3: value of this parameter is within safe bounds: {bounds}")
                elif server == '2' or flag: print("???")

        except ttypes.InvalidParamName as ipn:
            print(f"Invalid parameter name: {ipn.paramName}")

        except ttypes.DangerousParamValue as dpv:
            print(f"Dangerous parameter '{dpv.paramName}' value: {dpv.dangerousValue} - outside safe bounds: {dpv.bounds}")

    # closing connection
    if server != '2':
        transport1.close()
    if server != '1':
        transport2.close()
