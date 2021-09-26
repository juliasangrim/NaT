#include "ipv6.h"
#include <net/if.h>

int create_server_ipv6(const char* ip) {
    int socket_fd;
    if ((socket_fd = socket(AF_INET6, SOCK_DGRAM, 0)) < 0) {

        perror("failed to create server socket");
        return ERROR;

    } else {
        std::cout << "Create server socket." << std::endl;
    }

    unsigned int ifindex = if_nametoindex("enp3s0");
    std::cout << "ifindex is " << ifindex << std::endl;

    if(setsockopt(socket_fd, IPPROTO_IPV6, IPV6_MULTICAST_IF, &ifindex, sizeof ifindex)) {
        perror("Setting local interface error");
        return 1;
    }

    return socket_fd;
}

int create_multicast_ipv6(const char* ip) {
    int socket_fd;
    if ((socket_fd = socket(AF_INET6, SOCK_DGRAM, IPPROTO_UDP)) < 0) {

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

    if (setsockopt(socket_fd, SOL_SOCKET, SO_REUSEPORT, &optval, sizeof(optval)) < 0) {
        perror("setsockopt resuseport failed");
        exit(EXIT_FAILURE);
    }

    int hops = 255;
    if (setsockopt(socket_fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS, &hops, sizeof(hops))) {
        perror("setsockopt");
        return 1;
    }

    //bind sock for receiving data
    struct sockaddr_in6 recv_addr{};
    char buf[INET6_ADDRSTRLEN * 2];
    recv_addr.sin6_family = AF_INET6;
    recv_addr.sin6_port = htons(PORT);
    recv_addr.sin6_addr = in6addr_any;
    std::cout << buf << std::endl;
    if (bind(socket_fd, (const struct sockaddr *)(&recv_addr), sizeof(recv_addr)) < 0) {

        perror("failed to bind sock");
        return ERROR;

    }

    //add to multicast group
    struct ipv6_mreq mreq{};
    inet_pton(AF_INET6, ip, &(mreq.ipv6mr_multiaddr));
    mreq.ipv6mr_interface = 0;

    if (setsockopt(socket_fd, IPPROTO_IPV6,  IPV6_ADD_MEMBERSHIP, &mreq, sizeof(mreq)) < 0) {

        perror("failed to set option : add to multicast group");
        return ERROR;

    }
    return socket_fd;
}