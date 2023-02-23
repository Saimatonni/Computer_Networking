import socket
import time
from dnslib.dns import *

ip_port = ("192.168.1.104", 1234)
BUFFER = 1024

DNS_CACHE = {}

def create_q(name):
    query = DNSRecord.question(name)
    return query.pack()

def split_response(message):
    message = DNSRecord.parse(message)
    r_record = str(message.get_a()).split()
    response, TYPE, ttl = r_record[4], r_record[3], int(r_record[1])
    return response, TYPE, ttl

def dns_res(UDPClientSocket, dns_query):
    byte_encode = create_q(dns_query)

    # send to server
    UDPClientSocket.sendto(byte_encode, ip_port)

    # receive from server
    message, address = UDPClientSocket.recvfrom(BUFFER)

    response, res_type, ttl = split_response(message)

    init_time = int(time.perf_counter())
    if res_type == "A" or res_type == "AAAA":
        expiration_time = init_time + ttl
        DNS_CACHE[dns_query] = (response, res_type, expiration_time)
        return response
    else:
        print(response, " <--")
        return dns_res(UDPClientSocket, response)

if __name__ == "__main__":
    UDPClientSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
    while True:
        cur_time = int(time.perf_counter())
        expired_records = []
        for key in DNS_CACHE:
            if DNS_CACHE[key][2] < cur_time:
                expired_records.append(key)
        for key in expired_records:
            del DNS_CACHE[key]

        dns_query = input("Enter Domain name to get IP Address(Enter 'view_cache' to see the cached data): ")

        if dns_query == 'view_cache':
            print("\ncached data:->  \n")
            for key in DNS_CACHE:
                print(key, " -> ", DNS_CACHE[key])
            print("___________________\n")
            continue

        cached_record = DNS_CACHE.get(dns_query, None)
        if cached_record is not None:
            response, res_type, expiration_time = cached_record
            if expiration_time < int(time.perf_counter()):
                del DNS_CACHE[dns_query]
                cached_record = None

        if cached_record is None:
            response = dns_res(UDPClientSocket, dns_query)
            res_type = "A" if '.' in response else "AAAA"

        print("IP address:", response)
