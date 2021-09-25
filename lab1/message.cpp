#include "message.h"

int Message::send_message(int socket_fd, std::string spam_message, sockaddr_in address) {
    size_t send = sendto(socket_fd, &spam_message[0], strlen(&spam_message[0]), 0,
                         reinterpret_cast<const sockaddr *>(&address), sizeof(address));
    if (send < 0) {
        perror("failed to send");
        return -1;
    }
    return 0;
}


int Message::receive_message(int socket_fd, Message* message) {
    char buffer[100];
    sockaddr_in addr_from{};
    int len = sizeof(addr_from);

    ssize_t read = recvfrom(socket_fd, buffer, MAX_LEN, 0, reinterpret_cast<sockaddr *>(&addr_from),
                            (socklen_t *) &len);
    if (read < 0) {
        perror("failed receiving message");
        return -1;
    }
    buffer[read] = '\0';
    const std::string pid = buffer;
    std::string ip(inet_ntoa(addr_from.sin_addr));

    Message new_message {
            pid,
            time(nullptr),
            std::move(ip)
    };
    *message = new_message;
    return 0;
}


