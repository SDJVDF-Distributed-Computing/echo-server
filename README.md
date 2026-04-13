# SMP Server

A multithreaded TCP message server using a custom text protocol (SMP — Secure Message Protocol) over TLS. Built with Java 21 and Maven.

## What it does

Clients connect over TLS, authenticate, then upload and download messages stored on the server. Multiple clients are handled concurrently via threads.

**Test accounts:** `alice/pass123`, `bob/pass456`, `carol/pass789`

## Running the server

**Prerequisites:** Docker and Docker Compose.

```bash
./generate_keystore.sh
cp .env.example .env
docker compose up --build
```

On first start, the server generates a self-signed TLS certificate and writes two files to `./certs/` on the host:

| File | Used by       |
|------|---------------|
| `certs/smp_keystore.jks` | Java client   |
| `certs/smpserver.cer` | Other clients |

## Running the Java client

The client runs locally against the server. Build first if needed:

```bash
mvn package -q
```

Then connect:

```bash
java -cp target/smp-server.jar smp.SMPClient [host] [port]
# defaults: localhost 8443
```

Interactive commands: `login`, `upload <message>`, `download`, `logoff`, `quit`

## Environment variables

| Variable | Default           | Description                  |
|----------|-------------------|------------------------------|
| `SMP_PORT` | `8443`| Port the server listens on   |
| `SMP_KEYSTORE_PATH` | `certs/smp_keystore.jks` | Path to the JKS keystore     |
| `SMP_KEYSTORE_PASSWORD` | — | Keystore password (required) |
| `SMP_KEYSTORE_ALIAS` | smpserver | Keystore alias (required)    |

## Protocol

SMP is a line-delimited text protocol over TLS/TCP. See [docs/PROTOCOL.md](docs/PROTOCOL.md) for the full spec including commands, response codes, and session state machine.

**Quick reference:**

| Command | Description |
|---------|-------------|
| `HELO <username>` | Begin login |
| `AUTH <username> <password>` | Complete login |
| `UPLD <message>` | Store a message |
| `DNLD` | Retrieve all messages |
| `QUIT` | Close the session |

## Project structure

```
src/main/java/smp/
├── SMPServer.java          # Entry point, accepts connections
├── SMPClient.java          # Interactive CLI client
├── network/
│   ├── MyStreamSocket.java     # Line-oriented socket wrapper
│   └── SMPServerThread.java    # Per-client session handler
├── protocol/
│   ├── Command.java            # Client command verbs
│   ├── ResponseCode.java       # Server response codes
│   └── SMPProtocol.java        # Protocol logic
├── store/
│   ├── UserStore.java          # In-memory user/auth store
│   └── MessageStore.java       # In-memory message store
└── model/
    ├── User.java
    └── Message.java
```