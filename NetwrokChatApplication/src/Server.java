import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static final int port = 8081;

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        // All writes are performed while synchronized on 'os'.
        private final PrintWriter os;

        // Socket reads do not need to be synchronized as each clients gets its
        // own thread.
        private final BufferedReader is;

        private final Map<String, ClientHandler> clients;

        private String clientId;

        public ClientHandler(Socket socket, Map<String, ClientHandler> clients)
                throws IOException {
            this.socket = socket;
            this.clients = clients;
            this.os = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            this.is = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
        }

        @Override
        public void run() {
            try {
                // First line the client sends us is the client ID.
                clientId = is.readLine();
                clients.put(clientId, this);

                for (String line = is.readLine(); line != null; line = is.readLine()) {
                    int separatorIndex = line.indexOf(':');
                    if (separatorIndex <= 0) {
                        continue;
                    }
                    String toClient = line.substring(0, separatorIndex);
                    String message = line.substring(separatorIndex + 1);
                    ClientHandler client = clients.get(toClient);
                    if (client != null) {
                        client.sendMessage(clientId, message);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client " + clientId + " terminated.");
                clients.remove(clientId);
                try {
                    socket.close();
                } catch (IOException ioe) {
                    // TODO Auto-generated catch block
                    ioe.printStackTrace();
                }
            }
        }

        public void sendMessage(String from, String message) {
            try {
                synchronized (os) {
                    os.println(from + ":" + message);
                    os.flush();
                }
            } catch (Exception e) {
                // We shutdown this client on any exception.
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ioe) {
                    // TODO Auto-generated catch block
                    ioe.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        final Map<String, ClientHandler> clients = new ConcurrentHashMap<String, ClientHandler>();

        ServerSocket ss = new ServerSocket(port);
        for (Socket socket = ss.accept(); socket != null; socket = ss.accept()) {
            Runnable handler = new ClientHandler(socket, clients);
            new Thread(handler).start();
        }
    }
}