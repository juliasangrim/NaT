#include "storage.h"

int Storage::fill(const Message& message) {
    storage_messages[message.id] = message;
    return 0;
}


bool Storage::exist(const std::string& pid) {
    return storage_messages.count(pid) > 0;
}

void Storage::print_content() {
    if (storage_messages.empty()) {
        std::cout << "No alive copies." << std::endl;
    } else {
        std::cout << "---------Alive version of program---------" << std::endl;
        std::cout << "ID PROGRAM" << "               " << "IP" << std::endl;
        for (auto &it: storage_messages)
            std::cout << it.first << "               " << it.second.ip_from << std::endl;
    }
}
unsigned long Storage::erase_iftimeout() {
    unsigned long old_size = storage_messages.size();
    for(auto pos = storage_messages.begin(); pos != storage_messages.end();) {
        if (time(nullptr) - pos->second.curr_time >= timeout) {
            pos = storage_messages.erase(pos);
        } else {
            ++pos;
        }
    }
    return old_size - storage_messages.size();
}

int Storage::update(const std::string& id) {
    auto iter = storage_messages.find(id);
    iter->second.curr_time = time(nullptr);
    return 0;
}

