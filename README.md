# myChatServer 💬
> A chat server that supports my chatting app named myChat(platform independent)
sumario

A guide written and edited with great appreciation by [@rajdip-b](https://github.com/rajdip-b) and [@GustavoTxFreitas](https://github.com/GustavoTxFreitas)

📌 **Table of contents** 
1. [Environment setup](#environment-setup)
2. [Project Structure](#project-structure)
3. [Message Framework](#message-framework)
   1. [Login Request](#login-request)
   2. [Signup Request](#signup-request)
   3. [Handshake](#handshake-request)
   4. [Client List](#client-list)
   5. [Text Message](#text-message)
4. [Running the server](#running-server)

<h2 id="environment-setup">Environment setup</h2>
## Environment setup {#environment-setup}

1. Maven framework is used.
2. It is strongly recommended that you use an IDE that supports the maven framework (e.g., [Eclipse](https://www.eclipse.org/ide/), [IntelliJ](https://www.jetbrains.com/idea/), etc).
3. Once that's figured out, clone the repository into your IDE directly using the repo link.
4. From the IDE, import mysql-connectorj into the project build path and build the project.
5. That should get you set up. In case you still face some error, contact me or visit [StackOverflow](https://stackoverflow.com/)
6. Configure the .gitignore file to include all the IDE specific setting so that they don't get uploaded to the repo when you push.
7. In Main.java, change the `System.setProperty("javax.net.ssl.keyStore", "myChatKeyStore.jks")` to `System.setProperty("javax.net.ssl.keyStore", "src/main/java/myChatKeyStore.jks")` while running the project on ur IDE and revert back to normal when compiling as a jar.
8. Happy Coding! 🥳🥳🥳

<h2 id="project-structure">Project Structure</h2>

This project is divided in two parts:

**Part 1**: handles the tasks associated with logging a user in or creating a new account (we call this the Credential part).

**Part 2**: handles all the tasks related to the main chat framework.

<h2 id="message-framework">Message Framework</h2>

This project uses `HashMap<String, Object>` type of objects to communicate between the client and server or client and client. The different kinds of messages used are given below.

### Message types

(All the response messages already contain the query type from the request message in it, so I'm not rewriting them.)

<h4 id="login-request"> Login Request:</h4>

> This type of message is used between the client and the credential network. The purpose of this type of message is to let the server know that someone is trying to access the chat network and to log them in. 
 
This message has the following pattern: `[REQUEST -> FROM USER TO SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_QUERY | `QUERY_LOGIN_REQUEST` | The header that tells the server someone is trying to log in. |
| KEY_USERNAME | `USERNAME` | Holds the username of the person trying to access. |
| KEY_PASSWORD | `PASSWORD` | Holds the hashed password that is validated against the password that is stored in the database. |

This message has the following pattern: `[RESPONSE -> TO USER FROM SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_RESPONSE_CODE | `RESPONSE_CODE_FAILURE` or `RESPONSE_CODE_SUCCESS` | The response code tells the client whether their attempt to log in was successful or not. |
| KEY_RESPONSE_MESSAGE | `MESSAGE` | A message from the server regarding the response code in case the client wants to display it to the users. It varies for different kinds of failure. |

Once the login is successful, the user is re-directed to the chat network.

---

<h4 id="signup-request">Signup Request:</h4>

> This type of message is used between the client and the credential network. The purpose of this type of message is to let the server know that someone is trying to register themselves into the server.

This message has the following pattern: `[REQUEST -> FROM CLIENT TO SERVER]`
   
| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_QUERY | `QUERY_SIGNUP_REQUEST` | The header that tells the server someone is trying to sign up to the server. |
| KEY_USERNAME | `USERNAME` | [^1] |
| KEY_PASSWORD | `PASSWORD` | - |
| KEY_FIRST_NAME | `FIRST_NAME` | - |
| KEY_LAST_NAME | `LAST_NAME` | - |
| KEY_EMAIL | `EMAIL` | - |

[^1]: I skip the explanation of the next few lines since they are self-explanatory. If someone feels that they might want to fill it up then you are most welcome!

This message has the following pattern: `[RESPONSE -> TO CLIENT FROM SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_RESPONSE_CODE | `RESPONSE_CODE_FAILURE` or `RESPONSE_CODE_SUCCESS` | The response code tells the client whether their attempt to sign up was successful or not. |
| KEY_RESPONSE_MESSAGE | `MESSAGE` | A message from the server regarding the response code in case the client wants to display it to the users. It varies for different kinds of failure. |

Once the signup is successful, the user is redirected to the login screen

---

<h4 id="handshake-request">Handshake:</h4>

> This type of message is used between the client and the chat network. The purpose of this message is to let the chat server know about the whereabouts of the client. This message gets send to the chat network as the very first message. 
 
This message has the following pattern: `[REQUEST -> FROM CLIENT TO SERVER]`
   
| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_QUERY | `QUERY_HANDSHAKE` | The header that tells the server that someone is trying to send their metadata to the server |
| KEY_USERNAME | `SENDER` | The person who is sending the message |

This message has the following pattern: `[RESPONSE -> TO CLIENT FROM SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_EXISTING_MESSAGES | A list of existing messages | A list of the previous messages is sent back to the user. |

If successful, the  client moves on to the next segment of establishing the communication.

---

<h4 id="client-list"> Client List Requests:</h4>

> This type of message is used between the client and the chat network. The purpose of this message is to get the list of active and inactive users from the server.

##### The client sends this message to the chat network to fetch the list.

This message has the following pattern: `[REQUEST -> FROM CLIENT TO SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_QUERY | `QUERY_CLIENT_LIST` | Tells the server that the client is requesting for the client list. |
| KEY_USERNAME | `USERNAME` | The client who is requesting the client list. |

This message has the following pattern: `[RESPONSE -> TO CLIENT FROM SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_ACTIVE_USERS_LIST | Active Users list | The server sends an arraylist of the number of clients that are currently active. |
| KEY_INACTIVE_USERS_LIST | Inactive Users list | The server sends an arraylist of the number of clients that are currently inactive. |

Once successful, the GUI in the client side is updated accordingly.

##### The chat server sends this message all active clients if any user gets connected/disconnected.

This message has the following pattern: `[RESPONSE -> TO CLIENT FROM SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_ACTIVE_USERS_LIST | Active Users list | The server sends an arraylist of the number of clients that are currently active. |
| KEY_INACTIVE_USERS_LIST | Inactive Users list | The server sends an arraylist of the number of clients that are currently inactive. |

Once successful, the GUI in the client side is updated accordingly.

---

<h4 id="text-message"> Text Message:</h4>

> This type of message is used between clients on the chat network. The purpose of this message is simply to carry the message of one user to another via the chat server. This message both generates and ends in the client side.

This message has the following pattern: `[REQUEST -> FROM CLIENT TO SERVER]`

| KEY | VALUE | DESCRIPTION |
| :--- | :--- | :--- |
| KEY_QUERY | `QUERY_SEND_MESSAGE` | Tells the server that someone wants to send a message. |
| KEY_MESSAGE | `MESSAGE` | The message that the user sends. |

This message has the following pattern: `[RESPONSE -> TO CLIENT FROM SERVER]`

(The initial message gets sent to all the active users except the one who sends it. The server doesn't reply on this kind of query)

Once successful, all the active users on the network receives the message sent.

<h2 id="running-server"> Running the server:</h2>

1. Create a runnable `jar`;
2. Run `java -jar jarfilename.jar`.
