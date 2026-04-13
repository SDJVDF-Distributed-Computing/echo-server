package smp.network;

import smp.model.Message;
import smp.model.User;
import smp.protocol.Command;
import smp.protocol.ResponseCode;
import smp.protocol.SMPProtocol;
import smp.store.MessageStore;
import smp.store.UserStore;

import java.io.IOException;
import java.util.List;

public class SMPServerThread implements Runnable {
    private final MyStreamSocket socket;
    private final UserStore userStore;
    private final MessageStore msgStore;

    private String currentUser = null;

    public SMPServerThread(MyStreamSocket socket, UserStore userStore, MessageStore msgStore) {
        this.socket    = socket;
        this.userStore = userStore;
        this.msgStore  = msgStore;
    }

    @Override
    public void run() {
        boolean done = false;
        try {
            while (!done) {
                String raw = socket.receiveMessage();
                if (raw == null) break;

                String[] parts = SMPProtocol.parseRequest(raw);
                Command command;
                try {
                    command = Command.valueOf(parts[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    send(ResponseCode.BAD_REQUEST, "Unknown command: " + parts[0]);
                    continue;
                }

                switch (command) {
                    case HELO: handleHelo(parts); break;
                    case AUTH: handleAuth(parts); break;
                    case UPLD: handleUpld(parts); break;
                    case DNLD: handleDnld();      break;
                    case QUIT: handleQuit(); done = true; break;
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] I/O error for "
                    + (currentUser != null ? currentUser : "unauthenticated client") + ": " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("[Server] Session closed for "
                    + (currentUser != null ? currentUser : "unauthenticated client"));
        }
    }

    private void handleHelo(String[] parts) throws IOException {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            send(ResponseCode.BAD_REQUEST, "Usage: HELO <username>");
            return;
        }
        currentUser = parts[1].trim();
        send(ResponseCode.CHALLENGE, "Send AUTH " + currentUser + " <password>");
    }

    private void handleAuth(String[] parts) throws IOException {
        if (parts.length < 2) {
            send(ResponseCode.BAD_REQUEST, "Usage: AUTH <username> <password>");
            return;
        }

        String[] authParts = parts[1].split(" ", 2);
        if (authParts.length < 2) {
            send(ResponseCode.BAD_REQUEST, "Usage: AUTH <username> <password>");
            return;
        }

        String username = authParts[0].trim();
        String password = authParts[1].trim();

        if (!username.equals(currentUser)) {
            send(ResponseCode.UNAUTHORIZED, "Username mismatch");
            currentUser = null;
            return;
        }

        if (userStore.verify(username, password)) {
            send(ResponseCode.OK, "Welcome " + currentUser);
            System.out.println("[Server] " + currentUser + " authenticated");
        } else {
            currentUser = null;
            send(ResponseCode.UNAUTHORIZED, "Invalid credentials");
        }
    }

    private void handleUpld(String[] parts) throws IOException {
        if (currentUser == null) {
            send(ResponseCode.FORBIDDEN, "Not authenticated");
            return;
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            send(ResponseCode.BAD_REQUEST, "Usage: UPLD <message>");
            return;
        }
        User user = userStore.getUser(currentUser);
        msgStore.addMessage(user.getId(), parts[1].trim());
        send(ResponseCode.OK, "Message stored");
    }

    private void handleDnld() throws IOException {
        if (currentUser == null) {
            send(ResponseCode.FORBIDDEN, "Not authenticated");
            return;
        }
        List<Message> snapshot = msgStore.getAllMessages();
        for (Message msg : snapshot) {
            send(ResponseCode.MESSAGE,
                    msg.getUserId() + " " + msg.getMessage() + " " + msg.getTimestamp());
        }
        send(ResponseCode.END_MESSAGES, "END (" + snapshot.size() + " messages)");
    }

    private void handleQuit() throws IOException {
        send(ResponseCode.OK, "BYE");
    }

    private void send(ResponseCode code, String payload) throws IOException {
        socket.sendMessage(SMPProtocol.makeResponse(code, payload));
    }
}
