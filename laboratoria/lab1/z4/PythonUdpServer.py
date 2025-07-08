import socket;

clientIP = "127.0.0.1"
serverPort = 9008
clientPortPython = 9009
clientPortJava = 9010

server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server.bind(('', serverPort))
buff = []

print('PYTHON UDP SERVER')

while True:
    buff, _ = server.recvfrom(128)
    buff_str = str(buff, 'cp1250')
    print("python udp server received msg: " + buff_str)

    if "Python" in buff_str:
        server.sendto(bytes("Pong Python", 'cp1250'), (clientIP, clientPortPython))
    elif "Java" in buff_str:
        server.sendto(bytes("Pong Java", 'cp1250'), (clientIP, clientPortJava))
