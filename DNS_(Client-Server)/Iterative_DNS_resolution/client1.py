import socket
from dnslib.dns import *


ip_port = ("192.168.1.104", 1234)
BUFFER_SIZE = 1024


def create_q(name):
    return DNSRecord.question(name).pack()


def split_response(message):
    message = DNSRecord.parse(message)
    r_record = str(message.get_a()).split()
    response, TYPE = r_record[4], r_record[3]
    return response, TYPE


def send_request():
    with socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM) as client_socket:
        while True:
            dns_query = input("Enter Domain name to get IP Address: ")
            byte_encode = create_q(dns_query)
            flag = False
            while not flag:
                client_socket.sendto(byte_encode, ip_port)
                message, address = client_socket.recvfrom(BUFFER_SIZE)
                response, res_type = split_response(message)

                if res_type in {"A", "AAAA"}:
                    print("IP Adress:", response)
                    flag = True
                else:
                    print(response, "<-")
                    byte_encode = create_q(response)


"""
 UDPClientSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
 send_request(UDPClientSocket)
"""

if __name__ == "__main__":
    send_request()