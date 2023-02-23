import socket
from dnslib.dns import *

ip_port   = ("192.168.1.104", 1234)
BUFFER          = 1024

def create_q(name):
    query = DNSRecord.question(name)
    return query.pack()

def split_response(message):
    message = DNSRecord.parse(message)
    r_record = str(message.get_a()).split()
    response,TYPE = r_record[4],r_record[3]
    return response,TYPE

def dns_res(UDPClientSocket, dns_query):
    byte_encode = create_q(dns_query)
    UDPClientSocket.sendto(byte_encode, ip_port)
    message,address = UDPClientSocket.recvfrom(BUFFER)

    response,res_type = split_response(message)

    if res_type == "A" or res_type == "AAAA":
        return response
    else:
        print(response, " <--")
        return dns_res(UDPClientSocket, response)

if __name__ == "__main__":
    UDPClientSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
    while True:
        dns_query = input("Enter Domain name to get IP Address: ")
        ip_address = dns_res(UDPClientSocket, dns_query)
        print("IP address:", ip_address)