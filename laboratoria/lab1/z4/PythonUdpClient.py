import socket;

serverIP = "127.0.0.1"
serverPort = 9008
clientPort = 9009

msg = "Ping Python Udp!"

print('PYTHON UDP CLIENT')

client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.bind(('', clientPort))
client.sendto(bytes(msg, 'cp1250'), (serverIP, serverPort))

buff, _ = client.recvfrom(128)
print("python udp client received msg: " + str(buff, 'cp1250'))
