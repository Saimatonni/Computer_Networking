import socket
import threading
from dnslib.dns import *


def __init__(self, ip, port, dns_records_file):
        self.ip = ip
        self.port = port
        self.buffer_size = 1024
        self.thread_no = 0
        self.dns_records = {}
        self.load_dns_records(dns_records_file)

ip_address     = "192.168.1.104"
port   = 1234
BUFFER  = 1024
THREAD_NO = 0

DNS_RECORDS = {}

def type_get(qtype,value):
    if qtype == 'A':
        return QTYPE.A,A(value)
    if qtype == 'AAAA':
        return QTYPE.AAAA,AAAA(value)
    if qtype == 'MX':
        return QTYPE.MX,MX(value)
    if qtype == 'CNAME':
        return QTYPE.CNAME,CNAME(value)
    if qtype == 'NS':
        return QTYPE.NS,NS(value)


def response_creating(domain_name,r_record):
    qtype,value = type_get(r_record[1],r_record[0])
    return DNSRecord(
        DNSHeader(qr=1,aa=1,ra=0),
        q=DNSQuestion(domain_name),
        a=RR(domain_name,qtype,rdata=value,ttl=int(r_record[2]))
    ).pack()


def split_query(message):
    domain_name = str(message.get_q()).strip(';').split()[0]
    record_list = []
    try:
        record_list = DNS_RECORDS[domain_name]
    except Exception as e:
        return "SYS_EXIT"

    return domain_name,record_list


def server_start(UDPServerSocket):  
    while(True):
        message,address = UDPServerSocket.recvfrom(BUFFER)
        message = DNSRecord.parse(message)

        domain_name,record_list = split_query(message)

        if record_list == "SYS_EXIT":
            UDPServerSocket.sendto("wrong query".encode(),address)
            
    
        resource_record = record_list[random.randrange(max(1,len(record_list) - 1))]
        response = response_creating(domain_name,resource_record)
        UDPServerSocket.sendto(response,address)


if __name__ == "__main__":
    file_in = open("dns_records.txt","r")
    lines = file_in.readlines()
    for record in lines:
        temp = record.split()
        DNS_RECORDS[temp[0]] = []

    for record in lines:
        temp = record.split()
        DNS_RECORDS[temp[0]].append((temp[1],temp[2],temp[3]))


    print("Server is Ready")
    UDPServerSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
    UDPServerSocket.bind((ip_address, port))
    while True:
        server_thread = threading.Thread(target=server_start(UDPServerSocket,))
        server_thread.start()