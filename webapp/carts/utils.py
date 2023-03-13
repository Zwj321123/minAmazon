import socket
import threading
from amazon.models import Package

HOST = "localhost"

def send(packageID_lst):
    for package_id in packageID_lst:
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # use port 8888 to communicate with daemon
        #amazonbackend
        client.connect((HOST, 8888))
        # NOTE: append a \n at the end to become a line
        msg = str(package_id) + '\n'
        client.send(msg.encode('utf-8'))
        # expected response: ack:<package_id>
        data = client.recv(1024)
        data = data.decode()
        res = data.split(":")
        if res[0] != "ack" or res[1] != str(package_id):
            print('recv:', data)
            return False
        print('recv:', data)
    return True

def updateStatus(client):
    data = client.recv(1024)
    data = data.decode()
    res = data.split(":")
    if res[1] == "packed" or \
            res[1] == "loading" or \
            res[1] == "loaded" or \
            res[1] == "delivering" or \
            res[1] == "delivered":
        print('recv:', data)
        try:
            packageID = int(res[0])
            package = Package.objects.filter(id=packageID).first()
            package.status = res[1]
            package.save()
            return True
        except ValueError:
            print("Invalid package id")
            return False
        except:
            print("Cannot update status!")
            return False
    print('recv:', data)
    return False

class thread(threading.Thread):
    def __init__(self, thread_name, thread_ID):
        threading.Thread.__init__(self)
        self.thread_name = thread_name
        self.thread_ID = thread_ID

    def run(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # use port 8888 to communicate with daemon
        #amazonbackend
        client.connect((HOST, 8888))
        while 1:
            updateStatus(client)


if __name__ == '__main__':
    thread1 = thread("listenToBackEnd", 1)
    thread1.start()