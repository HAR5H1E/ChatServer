# ChatServer
 
A multi-user chat application in Java with user accounts, contact lists, and direct messaging, built on raw TCP sockets and a terminal UI ([Lanterna](https://github.com/mabe02/lanterna)) client. Passwords are hashed with BCrypt ([jBCrypt](https://github.com/jeremyh/jBCrypt)) and user/contact data is persisted in SQLite.
 
## Features
 
- **Accounts** — register or log in with a username/password (BCrypt-hashed, never stored in plaintext)
- **Unique per-user UUID**, issued at registration, used to authorize contact requests
- **Contacts list** — add contacts by username + their UUID; only mutual contacts can DM each other
- **Direct messaging** — `@username message` sends privately to an online contact
- **Concurrent clients** — one virtual thread per connection (`Executors.newVirtualThreadPerTaskExecutor()`), tracked in a `ConcurrentHashMap<String, ClientHandlerThread>`
- **Session enforcement** — a username can't log in twice at once
- **Idle timeout** — connections are dropped after 15 minutes of inactivity (`setSoTimeout(900000)`)
- **Terminal UI client** — scrollable chat feed + input box via Lanterna
- **Server control messages** the client reacts to specially:
  - `r--ShutDown--r` — client closes its connection
  - `r--Clear--r` — client clears its chat feed
 
## Requirements
 
- **Java 21+** (uses virtual threads via `Executors.newVirtualThreadPerTaskExecutor()`)
- [Lanterna](https://github.com/mabe02/lanterna) — client terminal UI
- [jBCrypt](https://github.com/jeremyh/jBCrypt) (`org.mindrot.jbcrypt`) — password hashing
- [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) — SQLite driver (`jdbc:sqlite:Users.db`)

## Usage
 
On connecting, the server prompts **LOGIN OR REGISTER**:
 
- `register` -> choose a username and password. The server returns a **UUID** - share this with people you want as contacts (it's required to add you).
- `login` -> username + password. 5 incorrect attempts closes the connection.
Once in the main session:
 
| Command | Description |
|---|---|
| `@username message` | Send a direct message to a contact (must be online) |
| `/add` | Add a contact — prompts for their username and UUID |
| `/contacts` | List your current contacts |
| `/details` | Show your username and UUID |
| `/clear` | Clear the chat screen |
| `/delete` | Delete your account (prompts for password confirmation) |
| `help` | List available commands |
| `quit` | Disconnect and close the client |
 
## How It Works
 
- **Server**: `mainServer` opens a `ServerSocket` and, for each incoming connection, submits a `ClientHandlerThread` to a virtual-thread executor. Each handler owns its own `ObjectInputStream`/`ObjectOutputStream` pair and communicates with the client using plain `String` objects.
- **Auth & contacts**: `ClientHandlerThread` drives the login/register flow and the main command loop; `DBManager` handles all SQLite reads/writes (user lookup, password hash comparisons via `BCrypt.checkpw`, contact table inserts/queries).
- **Messaging**: DMs are routed by looking up the recipient's `ClientHandlerThread` in the shared `ConcurrentHashMap` (`mainServer.serverClient`) and calling `sendMessage` directly — there's no server-wide broadcast, only mutual-contact DMs.
- **Client**: `ClientServer` connects to `localhost:3000`, and a background listener thread pushes incoming messages into the Lanterna UI thread via `invokeLater`, keeping the terminal responsive. Typing `quit` closes the socket and streams.

## Known Limitations / Stuff to improve
 
- Data (usernames, messages, UUIDs) is sent as raw `String` objects over `ObjectOutputStream` with **no transport encryption** — not safe over an untrusted network as-is.
- The client's server host is hardcoded to `localhost`; update it to connect across machines.
- No message history/persistence — only account and contact data survive restarts, not chat logs.
- Contact requests require knowing the other user's UUID in advance (no discovery/search).


