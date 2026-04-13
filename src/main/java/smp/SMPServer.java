package smp;

import smp.network.MyStreamSocket;
import smp.network.SMPServerThread;
import smp.store.MessageStore;
import smp.store.UserStore;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

public class SMPServer {

    private static final int    PORT          = Integer.parseInt(getEnv("SMP_PORT",          "8443"));
    private static final String KEYSTORE_FILE = getEnv("SMP_KEYSTORE_PATH",     "certs/smp_keystore.jks");
    private static final char[] KEYSTORE_PASS = getEnv("SMP_KEYSTORE_PASSWORD", "changeit").toCharArray();

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isEmpty()) ? val : fallback;
    }

    public static void main(String[] args) {
        UserStore    userStore = new UserStore();
        MessageStore msgStore  = new MessageStore();

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(KEYSTORE_FILE), KEYSTORE_PASS);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, KEYSTORE_PASS);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();

            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT);

            System.out.println("[Server] SMP server listening on port " + PORT + " (TLS)");
            System.out.println("[Server] Test accounts: alice/pass123, bob/pass456, carol/pass789");

            while (true) {
                Socket rawSocket = serverSocket.accept();
                MyStreamSocket dataSocket = new MyStreamSocket(rawSocket);

                SMPServerThread session = new SMPServerThread(dataSocket, userStore, msgStore);
                Thread t = new Thread(session);
                t.setDaemon(true);
                t.start();

                System.out.println("[Server] Accepted connection from "
                        + rawSocket.getInetAddress().getHostAddress());
            }

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException
                 | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
            System.err.println("[Server] Make sure '" + KEYSTORE_FILE + "' exists.");
            System.err.println("[Server] Generate it with: ./scripts/generate_keystore.sh");
        }
    }
}
