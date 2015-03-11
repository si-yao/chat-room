Report for PA1
Name: Siyao Li
UNI: sl3766

------------------------------------------------------
1.General Description
------------------------------------------------------
The program meets all requirements listed in programming assignment description, including 2 bonus parts.
And also besides 2 bonus requirements, I added a function to simplify the input command, which will be introduced in later section.
I encapsulate the socket service into utility.SocketService It provides service about request and response, both for client and server.
And there are two package client and server for client side and server side respectively.

-----------------------
1.1 Server side
-----------------------
In server side, server.HubService is the main thread listening the incoming socket request.
And server.HubHandle is the thread to handle the request.
In addition, server.LifeKiller is the thread to keep the clients alive according to their heartbeats.
-----------------------
1.2 Client side
-----------------------
In client side, client.SendService is the main thread listening all keyboard input, and save all information about current user.
And client.SendHandle is the thread to handle keyboard commands.
And client.ReceiveService is the main thread for receive the socket from p2p user or server.
And client.ReceiveHandle is the thread handles received socket request.
At last, client.LifeKepper is the thread for heartbeats.
-----------------------
1.3 Socket and protocol
-----------------------
I design my own protocol. Every message in request/response is actually key value pairs (implemented by hash map).
And I encode the map into serialized string by utility.KVSerialize.encode().
And when the endpoint receives the message, it could use utility.KVSerialize.decode() to get the hash map from the string.
The implementation of the protocol is simple. I used ":" as delimiter for key/value.
And if the message has ":" itself, then use escape symbol "\" to indicate the next ":" is a actual character, not a delimiter.
So, the protocol supports all kinds of string.
The socket is non-persistent. I close the connect right after the communication ends.
-----------------------
1.4 Workflow
-----------------------
Basically, when user A wants to communicate with user B, then it could send message to server, and server send it to user B.
If user A wants to send message to user B by p2p, then it will get the ip/port from server and send message to user B directly.
For other request, user A will send request message to server, and get response from server.
-----------------------
1.5 API
-----------------------
I used the key/value pair as the message to communicate between client/servers.
So I designed a will-formed API for request from client to server, or also from server to client.

Server API:

1) Authentication
    Request: {"type": "auth", "username": "abc", "password": "123"}
    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

2) Send Message
    Request: {"type": "message", "from": "myname", "to": "targetname", "msg":"messages..."}
    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

3) Broadcast
    Request: {"type": "broadcast", "from": "myname", "msg":"messages..."}
    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

4) Online list
    Request: {"type": "online"}
    Response: {"user1":"user1", "user2":"user2", ...}

5) Block
    Request: {"type": "block", "from": "myname", "target": "baduser"}
    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

6) Unblock
    Request: {"type": "unblock", "from": "myname", "target": "baduser"}
    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

7) Logout
    Request: {"type": "logout", "from": "myname"}
    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

8) Get Address
    Request: {"type": "address", "from": "myname", "target": "targetuser"}
    Response: {"result" : "ok", "ip": "192.168.1.1", "port":"123"} if success, {"result" : "fail", "ip": "null", "port":"null"} if failed

9) Life Keeper
    Request: {"type": "alive", "from": "username"}
    Response: {"result": "ok"}


Client P2P API:

1) Send Message
    Request: {"type": "message", "from": "myname", "to": "targetname", "msg":"messages..."}
    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

2) Being notified offline
    Request: {"type": "offline", "from": "offline user"}
    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

3) Kill the user
    Request: {"type": "kill", "reason": "you have logged in at another place."}
    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

4) Need IP
    Request: {"type": "ip", "from":"user"}
    Response: {"result":"ok"} if success, {"result": "rej"} if fail.


------------------------------------------------------
2. Explanation of source code
------------------------------------------------------
The organization of source code is shown as follows.
And for explanation, please refer to the first section (1.General Description), and also please refer to the comments in the code.

src
    - client
        - LifeKepper.java
        - ReceiveHandle.java
        - ReceiveService.java
        - SendHandle.java
        - SendService.java
    - server
        - HubHandle.java
        - HubService.java
        - LiferKiller.java
    - utility
        - KVSerialize.java
        - LogService.java
        - SocketService.java

    - Client.java
    - Server.java

------------------------------------------------------
3. How to run
------------------------------------------------------
Jump to the directory src that contains Client.java and Server.java.

1) First change mode of make.sh by:
chmod 755 ./make.sh

2) Then run the make.sh to compile the code by:
./make.sh

Then stay at the current directory (src which contains Client.java and Server.java).

3) And start server by:
java Server <server port number>

where <...> is the argument.

4) And then start client by:
java Cient <server IP> <server port number> [<client port number>]

where [<client port number>] is an optional argument. If you don't specify it, it will use the same port with server.

IMPORTANT NOTES:
1) Please put the credential.txt at the current directory (src) which contains Client.java and Server.java.
2) Please make sure you have write privilege in the current directory, because it needs to write log into files.
3) If any thing wrong, please contact: sl3766@columbia.edu


------------------------------------------------------
4. Sample command
------------------------------------------------------

-----------------------
4.1. User authentication
-----------------------
The client will ask you to log in at beginning, no commands needed.

-----------------------
4.2. Message exchange/ offline message
-----------------------
message <username> <messages>

for example:
message user2 hello, this is user1.

-----------------------
4.3. Block
-----------------------
block <username>

-----------------------
4.4. Unblock
-----------------------
unblock <username>

-----------------------
4.5. Broadcast
-----------------------
broadcast <message>

-----------------------
4.6. logout
-----------------------
logout

-----------------------
4.7. display other users
-----------------------
online

-----------------------
4.8. get address
-----------------------
getaddress <username>

-----------------------
4.9. p2p message/ offline message when target user is offline
-----------------------
private <username> <message>

for example:
private user2 Hi, this is user1 from p2p connection.

-----------------------
4.10. P2P privacy and consent
-----------------------
When the source user wants to get address of this user (either by getaddress or private),
the user will be notified to approve the request by typing Y or N.

-----------------------
4.11. Simple Chat
-----------------------
If the user types command that does not start with above key words,
then the client will send the typed message to the most recent user.
Before sending, the client will confirm with the user whether send or not.


------------------------------------------------------
5. Additional features
------------------------------------------------------

-----------------------
5.1 P2P privacy and consent
-----------------------
Meet all requirements described in assignment.

***Test case 1***
Get address of target user

Type in user1:
getaddress user2

Then user2 will get a message:
System: the user: user1 is requesting your address. Consent(Y/N)? Y

If user2 enter return, or type Y or y and enter, then user1 will get the address.
If user2 type N or n and enter, then the user1 will get a message that user2 rejected it.




***Test case 2***
Establish P2P connection
If user1 has not has a p2p connection with user2.

Then type in user1:
private user2 hello from user1

Then user2 will get a message:
System: the user: user1 is requesting your address. Consent(Y/N)? Y

If user2 enter return, or type Y or y and enter, then user1 will get the address, and user2 will get the message.
If user2 type N or n and enter, then the user1 will get a message that user2 rejected it, but user2 still can receive the message via server (not p2p).




***Test case 3***
If user2 blocks user1.
Then type in user1:
getaddress user2

Then it will display:
you are blocked1!

-----------------------
5.2 Guaranteed message delivery
-----------------------
Meet all requirements described in assignment.

***Test case 1***
When establish p2p connection from user1 to user2, and user2 reject it.
This is the same scenario with test case 2 in 5.1(P2P privacy and consent)
The message is guaranteed to be delivered even p2p connection is rejected (as long as user1 is not blocked by user2)




***Test case 2***
During p2p chatting, user2 loses connection unexpectedly.
Then user1 type:
private user2 hello, there

User2 will not get the message because he is offline now, although user1 and server don't not know.
Now user1 will try to connect user2, but it fails. Then user1 will send the message to server instead.
And now server also cannot connect user2, then server save the message to the offline message queue.

Besides that, server will notify user1 that user2 is offline and user1 will receive a message:
System: p2p user: user2 is offline now.

Next time when user2 is online, it will receive:
user1: hello, there




***Test case 3***
When user2 loses connection unexpectedly.
Then user1 type:
message user2 hello, there

Now server does not know that user2 is offline now. It will to connect user2, but it fails.
Then server save the message to the offline message queue.
Next time when user2 is online, it will receive:
user1: hello, there




***Test case 4***
During p2p chatting between user1 and user2, server finds that user2 logs out or loses the connection.
Then user1 will receive a message from server:
System: p2p user: user2 is offline now.

Then next time user1 wants to start a p2p chat with user2, the message will be added in offline message queue and send to user2 next time logged in.




***Test case 5***
If user2 is offline in the beginning, then when user1 tries to start a p2p chat, the message will be added in offline message queue and send to user2 next time logged in.
For example,
user1 type:
private user2 hello, there

then he will receive:
user2 is offline now. Msg will be delivered when online.

next time user2 logs in, he will receive:
user1: hello, there

-----------------------
5.2 Simple chat
-----------------------
Type the command like "message username" and "private username" is too tedious!

So I simplify the command.
For the first time that user1 sends message to user2, user1 should type the whole command:
message user2 hello, there

or for p2p connection:
private user2 hello, there

Then next time, the user1 only needs to type the message itself without any prefix!
For exmaple:
Hello, there

Then client will ask user1:
Send msg to user2? (Y/N) Y

And if user1 type enter or type Y or y and enter, then the message will send out.
Otherwise if user type N or n and enter, then the message will be cancelled.

-----------------------
5.3 Well-formed API
-----------------------
I use key/value pairs as protocol. And I create a well-formed api between server and client.
In this way, anyone who know the API could design their own client in any language on any platform.
The detail of API is shown in section 1.5 API.