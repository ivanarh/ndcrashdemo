cmake_minimum_required(VERSION 3.4.1)

#Sibling call optimization may affect unwinding: it may omit stack frame creation for a call.
#The same problem may occur with inlining optimizations.
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -fno-optimize-sibling-calls -fno-inline")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -fno-optimize-sibling-calls -fno-inline")

add_library(ndcrashdemo SHARED ${CMAKE_SOURCE_DIR}/src/main/cpp/ndcrashdemo.c)
find_library(log-lib log)
target_link_libraries(ndcrashdemo ${log-lib})
