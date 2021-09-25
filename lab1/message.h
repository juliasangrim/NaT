#ifndef LAB1_MESSAGE_H
#define LAB1_MESSAGE_H

#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <iostream>
#include <cstring>
#include <cstring>
#include <sys/poll.h>
#include <unistd.h>
#include <ctime>
#include <map>

#define PORT 8080
#define MAX_LEN 256
const int timeout =  5;

struct Message{
    std::string id;
    time_t curr_time{};
    std::string ip_from;

    static int send_message(int socket_fd, std::string spam_message, sockaddr_in address);
    static int receive_message(int socket_fd, Message *message);
};

#endif //LAB1_MESSAGE_H
