cmake_minimum_required(VERSION 3.22.1)
project("pty_helper")

add_library(pty-helper SHARED pty_helper.c)
target_link_libraries(pty-helper log)
