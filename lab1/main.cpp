
#include "message.h"
#include "storage.h"

int create_multicast(char* ip) {
    int socket_fd = 0;
    if ((socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {

        perror("failed to create sock");
        return  -1;

    } else {
        std::cout << "Create sock." << std::endl;
    }
    //set options
    const int optval = 1;
    if (setsockopt(socket_fd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval)) < 0) {

        perror("failed to set sock option - reuseaddr");
        return -1;

    }

    //bind sock for receiving data
    struct sockaddr_in recv_addr{};
    memset(&recv_addr, 0, sizeof(recv_addr));
    recv_addr.sin_family = AF_INET;
    recv_addr.sin_port = htons(PORT);
    recv_addr.sin_addr.s_addr = htonl(INADDR_ANY);

    if (bind(socket_fd, (const struct sockaddr* ) &recv_addr, sizeof (recv_addr)) < 0) {

        perror("failed to bind sock");
        return -1;

    }

    //add to multicast group
    struct ip_mreq mreq{};
    mreq.imr_multiaddr.s_addr = inet_addr(ip);
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);

    if (setsockopt(socket_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)) < 0) {

        perror("failed to set option : add to multicast group");
        return -1;

    }
    return socket_fd;
}

int main(int argc, char** argv) {
    if (argc != 2) {

        std::cout << "Write the address, please." << std::endl;
        return -1;

    }
    //create sockets
    int server_sock, sock = 0;
    if ((server_sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {

        perror("failed to create sock");
        return -1;

    } else {
        std::cout << "Create server socket." << std::endl;
    }

    if ((sock = create_multicast(argv[1])) < 0) {
        return -1;
    }

    struct sockaddr_in dest_addr{};
    dest_addr.sin_family = AF_INET;
    dest_addr.sin_port = htons(PORT);
    dest_addr.sin_addr.s_addr = inet_addr(argv[1]);

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
            return -1;

        }
        if (res_poll == 0) {
            Message::send_message(server_sock, my_id, dest_addr);
        } else {

            Message new_message;
            Message::receive_message(sock, &new_message);

            if (my_id == new_message.id) {
                continue;
            }

            if (storage_messages.exist(new_message.id)) {

                storage_messages.update(new_message.id);
                if (storage_messages.erase_iftimeout() > 0) {
                    storage_messages.print_content();
                }

            } else {

                storage_messages.fill(new_message);
                storage_messages.print_content();

            }
        }
    }

}