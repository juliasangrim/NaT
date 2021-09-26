#ifndef LAB1_IPV6_H
#define LAB1_IPV6_H

#include <sys/socket.h>
#include <cstdio>
#include <iostream>
#include <netinet/in.h>
#include <cstring>
#include <arpa/inet.h>



#define PORT 8080
#define ERROR -1
#define IPv6 2

int create_multicast_ipv6(const char* ip);
int create_server_ipv6(const char* ip);



#endif //LAB1_IPV6_H
