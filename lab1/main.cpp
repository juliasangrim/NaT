
#include <netdb.h>
#define IPv4 1
#define IPv6 2
#include "message.h"
#include "storage.h"
#include "ipv4.h"
#include "ipv6.h"



bool is_ipv4(const char* str) {
    struct sockaddr_in sa{};
    return inet_pton(AF_INET, str, &(sa.sin_addr)) != 0;
}

bool is_ipv6(const char* str) {
    struct sockaddr_in6 sa{};
    return inet_pton(AF_INET6, str, &(sa.sin6_addr)) != 0;
}


int main(int argc, char** argv) {
    if (argc != 2) {

        std::cerr << "Write the address, please.(use 224.0.0.0-239.255.255.255 or ff00::/8)" << std::endl;
        exit(EXIT_FAILURE);

    }

    int type_addr = 0;
    if (is_ipv4(argv[1])) {
        type_addr = IPv4;
    } else if (is_ipv6(argv[1])) {
        type_addr = IPv6;
    } else {
        std::cerr << "Use ipv4 or ipv6 address." << std::endl;
        exit(EXIT_FAILURE);
    }

    //create sockets
    int server_sock, sock = 0;
    if (type_addr == IPv4) {
        if ((server_sock = create_server_ipv4(argv[1])) < 0) {
            exit(EXIT_FAILURE);
        }
        if ((sock = create_multicast_ipv4(argv[1])) < 0) {
            exit(EXIT_FAILURE);
        }
    }
    if (type_addr == IPv6) {
        if ((server_sock = create_server_ipv6(argv[1])) < 0) {
            exit(EXIT_FAILURE);
        }
        if ((sock = create_multicast_ipv6(argv[1])) < 0) {
            exit(EXIT_FAILURE);
        }

    }

    struct sockaddr_in dest_addr{};
    struct sockaddr_in6 dest_addr6{};
    if (type_addr == IPv4) {
        dest_addr.sin_port = htons(PORT);
        dest_addr.sin_family = AF_INET;
        dest_addr.sin_addr.s_addr = inet_addr(argv[1]);
    }
    if (type_addr == IPv6) {
        dest_addr6.sin6_port = htons(PORT);
        dest_addr6.sin6_family = AF_INET6;
        inet_pton(AF_INET6, argv[1], &dest_addr6.sin6_addr);
    }

    //sending and receiving message

    std::string my_id = std::to_string(getpid());
    std::cout << "My id: " << my_id << std::endl;
    struct pollfd fd [1];
    fd->fd = sock;
    fd->events = POLLIN;
    Storage storage_messages{};

    while(true) {
        int res_poll = poll(fd, 1, 1000);
        if (res_poll < 0) {

            perror("failed to call poll");
            exit(EXIT_FAILURE);

        }
        if (res_poll == 0) {
            if (type_addr == IPv4) {
                Message::send_message(server_sock, my_id,   reinterpret_cast<sockaddr *>(&dest_addr), sizeof (dest_addr));
            }
            if (type_addr == IPv6) {
                Message::send_message(server_sock, my_id, reinterpret_cast<sockaddr *>(&dest_addr6), sizeof(dest_addr6));
            }
        } else {

            Message new_message;
            Message::receive_message(sock, &new_message, type_addr);
            if (storage_messages.erase_iftimeout() > 0) {
                storage_messages.print_content();
            }
            if (my_id == new_message.id) {
                continue;
            }

            if (storage_messages.exist(new_message.id)) {

                storage_messages.update(new_message.id);

            } else {

                storage_messages.fill(new_message);
                storage_messages.print_content();

            }
        }
    }

}