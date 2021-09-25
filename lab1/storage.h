#ifndef LAB1_STORAGE_H
#define LAB1_STORAGE_H


#include <map>
#include "message.h"

class Storage{
private:
    std::map<std::string, Message> storage_messages;
public:

    int fill(const Message& message);
    bool exist(std::string id);
    int update(const std::string& id);
    unsigned long erase_iftimeout();
    void print_content();
};


#endif //LAB1_STORAGE_H
