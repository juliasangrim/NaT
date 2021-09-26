#include "message.h"
#include "ipv4.h"
#include "ipv6.h"

int Message::send_message(int socket_fd, std::string spam_message, struct sockaddr* address, const size_t len) {
    size_t send = sendto(socket_fd, &spam_message[0], strlen(&spam_message[0]), 0,
                         address, len);
    if (send < 0) {
        perror("failed to send");
        return -1;
    }
    return 0;
}


int Message::receive_message(int socket_fd, Message* message, int type_addr) {
    char buffer[100];
    sockaddr_in addr_from{};
    sockaddr_in6 addr_from6{};
    ssize_t read = 0;
    if (type_addr == IPv4) {
        int len = sizeof(addr_from);
        read = recvfrom(socket_fd, buffer, MAX_LEN, 0, reinterpret_cast<sockaddr *>(&addr_from),
                                (socklen_t *) &len);
    }
    if (type_addr == IPv6) {
        int len = sizeof(addr_from6);
        read = recvfrom(socket_fd, buffer, MAX_LEN, 0, reinterpret_cast<sockaddr *>(&addr_from6),
                        (socklen_t *) &len);
    }
    if (read < 0) {
        perror("failed receiving message");
        return -1;
    }
    buffer[read] = '\0';
    const std::string pid = buffer;

    if (type_addr == IPv4) {
        std::string ip(inet_ntoa(addr_from.sin_addr));
        Message new_message {
                pid,
                time(nullptr),
                ip
        };
        *message = new_message;
    }
    if (type_addr == IPv6) {
        char ip[INET6_ADDRSTRLEN];
        inet_ntop(AF_INET6, &(addr_from6.sin6_addr), ip, INET6_ADDRSTRLEN);
        Message new_message {
                pid,
                time(nullptr),
                &ip[0]
        };
        *message = new_message;
    }

    return 0;
}


