#include "ipv4.h"


int create_server_ipv4() {
    int socket_fd;
    if ((socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {

        perror("failed to create server socket");
        return ERROR;

    } else {
        std::cout << "Create server socket." << std::endl;
    }
    return socket_fd;
}

int create_multicast_ipv4(char* ip) {
    int socket_fd;
    if ((socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {

        perror("failed to create sock");
        return ERROR;

    } else {
        std::cout << "Create sock." << std::endl;
    }
    //set options
    const int optval = 1;
    if (setsockopt(socket_fd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval)) < 0) {

        perror("failed to set sock option - reuseaddr");
        return ERROR;

    }

    //bind sock for receiving data
    struct sockaddr_in recv_addr{};
    memset(&recv_addr, 0, sizeof(recv_addr));
    recv_addr.sin_family = AF_INET;
    recv_addr.sin_port = htons(PORT);
    recv_addr.sin_addr.s_addr = inet_addr(ip);

    if (bind(socket_fd, (const struct sockaddr* ) &recv_addr, sizeof (recv_addr)) < 0) {

        perror("failed to bind sock");
        return ERROR;

    }

    //add to multicast group
    struct ip_mreq mreq{};
    inet_aton(ip, &(mreq.imr_multiaddr));
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);

    if (setsockopt(socket_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)) < 0) {

        perror("failed to set option : add to multicast group");
        return ERROR;

    }
    return socket_fd;
}
