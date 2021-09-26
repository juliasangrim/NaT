cmake_minimum_required(VERSION 3.16)
project(lab1)

set(CMAKE_CXX_STANDARD 14)

add_executable(lab1 main.cpp message.h message.cpp storage.cpp storage.h ipv4.cpp ipv4.h ipv6.cpp ipv6.h)
