#ifndef LAB1_IPV4_H
#define LAB1_IPV4_H


#include <sys/socket.h>
#include <cstdio>
#include <iostream>
#include <netinet/in.h>
#include <cstring>
#include <arpa/inet.h>



#define PORT 8080
#define ERROR -1
#define IPv4 1

int create_multicast_ipv4(char* ip);
int create_server_ipv4(char* ip);

#endif //LAB1_IPV4_H
