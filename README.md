# chat-room
Server API:

1. Authentication

    Request: {"type": "auth", "username": "abc", "password": "123"}

    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

2. Send Message

    Request: {"type": "message", "from": "myname", "to": "targetname", "msg":"messages..."}

    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

3. Broadcast

    Request: {"type": "broadcast", "from": "myname", "msg":"messages..."}

    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

4. Online list

    Request: {"type": "online"}

    Response: {"user1":"user1", "user2":"user2", ...}

5. Block

    Request: {"type": "block", "from": "myname", "target": "baduser"}

    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

6. Unblock

    Request: {"type": "unblock", "from": "myname", "target": "baduser"}

    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

7. Logout

    Request: {"type": "logout", "from": "myname"}

    Response: {"result": "ok"} if success, {"result": "fail"} if fail.

8. Get Address

    Request: {"type": "address", "from": "myname", "target": "targetuser"}

    Response: {"result" : "ok", "ip": "192.168.1.1", "port":"123"} if success, {"result" : "fail", "ip": "null", "port":"null"} if failed

9. Life Keeper

    Request: {"type": "alive", "from": "username"}

    Response: {"result": "ok"}

Client p2p API:

1. Send Message

    Request: {"type": "message", "from": "myname", "to": "targetname", "msg":"messages..."}

    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

2. Being notified offline

    Request: {"type": "offline", "from": "offline user"}

    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.



3. Kill the user

    Request: {"type": "kill", "reason": "you have logged in at another place."}

    Response: {"result":"ok"} if success, {"result": "some fail reason"} if fail.

4. Need IP

    Request: {"type": "ip", "from":"user"}

    Response: {"result":"ok"} if success, {"result": "rej"} if fail.
