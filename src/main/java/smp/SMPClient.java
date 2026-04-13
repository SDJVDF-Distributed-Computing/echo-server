package smp;

import smp.network.MyStreamSocket;
import smp.protocol.Command;
import smp.protocol.ResponseCode;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;

public class SMPClient {
    private static final String TRUSTSTORE_FILE = getEnv("SMP_KEYSTORE_PATH",     "certs/smp_keystore.jks");
    private static final char[] TRUSTSTORE_PASS = getEnv("SMP_KEYSTORE_PASSWORD", "changeit").toCharArray();

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isEmpty()) ? val : fallback;
    }

    private MyStreamSocket socket;
    private boolean loggedIn = false;

    private void connect(String host, int port)
            throws IOException, KeyStoreException, NoSuchAlgorithmException,
                   CertificateException, KeyManagementException {
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(TRUSTSTORE_FILE), TRUSTSTORE_PASS);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, tmf.getTrustManagers(), null);

        SSLSocket sslSocket = (SSLSocket) sc.getSocketFactory().createSocket(host, port);
        sslSocket.startHandshake();
        socket = new MyStreamSocket(sslSocket);
        System.out.println("[Client] Connected to " + host + ":" + port + " (TLS)");
    }

    private void disconnect() {
        if (socket != null) {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public boolean logon(String username, String password) throws IOException {
        socket.sendMessage(Command.HELO.name() + " " + username);
        String heloResp = socket.receiveMessage();
        System.out.println("[Server] " + heloResp);
        if (!ResponseCode.CHALLENGE.matches(heloResp)) {
            System.out.println("[Client] Login failed: unexpected HELO response");
            return false;
        }

        socket.sendMessage(Command.AUTH.name() + " " + username + " " + password);
        String authResp = socket.receiveMessage();
        System.out.println("[Server] " + authResp);
        loggedIn = ResponseCode.OK.matches(authResp);
        return loggedIn;
    }

    public void upload(String message) throws IOException {
        if (!loggedIn) { System.out.println("[Client] Not logged in."); return; }
        socket.sendMessage(Command.UPLD.name() + " " + message);
        System.out.println("[Server] " + socket.receiveMessage());
    }

    public void download() throws IOException {
        if (!loggedIn) { System.out.println("[Client] Not logged in."); return; }
        socket.sendMessage(Command.DNLD.name());
        System.out.println("[Client] -- Messages --");
        while (true) {
            String resp = socket.receiveMessage();
            if (resp == null) break;
            if (ResponseCode.END_MESSAGES.matches(resp)) {
                System.out.println("[Server] " + resp);
                break;
            }
            if (ResponseCode.MESSAGE.matches(resp)) {
                System.out.println(resp.substring(resp.indexOf(' ') + 1));
            } else {
                System.out.println("[Server] " + resp);
                break;
            }
        }
        System.out.println("[Client] -- End --");
    }

    public void logoff() throws IOException {
        if (socket == null) return;
        socket.sendMessage(Command.QUIT.name());
        System.out.println("[Server] " + socket.receiveMessage());
        loggedIn = false;
        disconnect();
        System.out.println("[Client] Disconnected.");
    }

    private void menu() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== SMP Client ===");
        System.out.println("Commands: login, upload, download, logoff, quit");

        while (true) {
            System.out.print("\n> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] tokens = line.split(" ", 2);
            String cmd = tokens[0].toLowerCase();

            switch (cmd) {
                case "login": {
                    System.out.print("Username: ");
                    String user = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String pass = scanner.nextLine().trim();
                    logon(user, pass);
                    break;
                }
                case "upload": {
                    if (tokens.length < 2 || tokens[1].trim().isEmpty()) {
                        System.out.print("Message: ");
                        upload(scanner.nextLine().trim());
                    } else {
                        upload(tokens[1]);
                    }
                    break;
                }
                case "download": download(); break;
                case "logoff":   logoff();   return;
                case "quit":
                    if (loggedIn) logoff();
                    return;
                default:
                    System.out.println("[Client] Unknown command. Use: login, upload, download, logoff, quit");
            }
        }
    }

    public static void main(String[] args) {
        String host = args.length >= 1 ? args[0] : "localhost";
        int    port = Integer.parseInt(getEnv("SMP_PORT", "8443"));
        if (args.length >= 2) {
            try { port = Integer.parseInt(args[1]); }
            catch (NumberFormatException e) { System.err.println("Invalid port, using " + port); }
        }

        SMPClient client = new SMPClient();
        try {
            client.connect(host, port);
            client.menu();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException
                 | CertificateException | KeyManagementException e) {
            System.err.println("[Client] Error: " + e.getMessage());
            System.err.println("[Client] Make sure '" + TRUSTSTORE_FILE + "' exists.");
        } finally {
            client.disconnect();
        }
    }
}
