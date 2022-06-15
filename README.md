### README

* This is a course project of Computer network of BUAA
* This is a C/S structured communication software, which could:
  * have N clients and 1 server
  * N clients could send messages to each other / server
  * message will be also shown on server
* A toy application of socket programming in JAVA

#### Environment:

* JDK 16
* IDEA
* java.nio java.net

#### RUN:

* git clone & build project
* Edit run/debug configuration :
  have 2 client apps(**named** "1" and "2") and 1 server app
* RUN server and clients

#### Functions:

* Server:
  * all messages will be shown
  * type "client": get all clients' names
* Clients:
  * type "to/NAME": assign peer as "NAME" (default: Server)
  * type "ANYTHING": send messages
  * type "Exit": close connection
* With notification of success and failure! 

#### NOTE:

* Only test on windows
* Actually a API(Socket) test program

