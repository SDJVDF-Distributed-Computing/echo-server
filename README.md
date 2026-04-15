# SMP Server

A multithreaded TCP message server using a custom text protocol (SMP — Short Message Protocol) over TLS. Built with Java 21 and Maven.

## What it does

Clients connect over TLS, authenticate, then upload and download messages stored on the server. Multiple clients are handled concurrently via threads.

**Test accounts:** `alice/pass123`, `bob/pass456`, `carol/pass789`

## Running the server

**Prerequisites:** Docker, a keystore in `./certs/`, and a `.env` file.

```bash
cp .env.example .env        # set SMP_KEYSTORE_PASSWORD
./generate_keystore.sh      # one-time cert generation
```

Pull and run the pre-built image published to GitHub Container Registry on every push to `main`:

```bash
docker run --rm \
  --env-file .env \
  -p 8443:8443 \
  -v ./certs:/app/certs:ro \
  ghcr.io/sdjvdf-distributed-computing/echo-server:main
```

## Running the server locally (without Docker)

**Prerequisites:** Java 21, Maven, `keytool` (bundled with the JDK).

```bash
# 1. Generate the keystore (one-time)
./generate_keystore.sh

# 2. Build
mvn package -q

# 3. Run
export SMP_KEYSTORE_PASSWORD=changeit   # match what generate_keystore.sh used
java -jar target/smp-server.jar
```

`SMP_PORT`, `SMP_KEYSTORE_PATH`, and `SMP_KEYSTORE_ALIAS` all have defaults (see [Environment variables](#environment-variables)), so only the password needs to be exported if you used the default.

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

| Variable                | Default                  | Description                |
|-------------------------|--------------------------|----------------------------|
| `SMP_PORT`              | `8443`                   | Port the server listens on |
| `SMP_KEYSTORE_PATH`     | `certs/smp_keystore.jks` | Path to the JKS keystore   |
| `SMP_KEYSTORE_PASSWORD` | `changeit`               | Keystore password          |
| `SMP_KEYSTORE_ALIAS`    | `smpserver`              | Keystore alias             |

## Protocol

SMP is a line-delimited text protocol over TLS/TCP.

**Quick reference:**

| Command                        | Description           |
|--------------------------------|-----------------------|
| `HELO <username>`              | Begin login           |
| `AUTH <username> <password>`   | Complete login        |
| `UPLD <message>`               | Store a message       |
| `DNLD`                         | Retrieve all messages |
| `QUIT`                         | Close the session     |

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