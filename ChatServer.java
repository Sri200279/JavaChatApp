
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, PrintWriter> clientMap = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException {
        System.out.println("Private Chat Server started on port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private String username;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for and store the username
                out.println("SUBMITNAME");


                username = in.readLine();
                synchronized (clientMap) {
                    if (username == null || clientMap.containsKey(username)) {
                        out.println("NAMEEXISTS");
                        socket.close();
                        return;

                    }
                    clientMap.put(username, out);
                }

                out.println("NAMEACCEPTED");
                broadcast("[" + username + "] has joined the chat.");

                String message;

                while ((message = in.readLine()) != null) {
                    if (message.startsWith("@")) {
                        int spaceIdx = message.indexOf(' ');
                        if (spaceIdx > 1) {
                            String targetUser = message.substring(1, spaceIdx);
                            String privateMsg = message.substring(spaceIdx + 1);
                            PrintWriter targetOut = clientMap.get(targetUser);
                            if (targetOut != null) {
                                targetOut.println("[Private from " + username + "] " + privateMsg);
                                out.println("[Private to " + targetUser + "] " + privateMsg);
                            } else {
                                out.println("User @" + targetUser + " not found.");
                            }
                        }
                    } else {
                        broadcast("[" + username + "]: " + message);
                    }

                }

            } catch (IOException e) {
                System.out.println(username + " disconnected.");
            } finally {
                if (username != null) {
                    clientMap.remove(username);
                    broadcast("[" + username + "] has left the chat.");        }
                try {
                    socket.close();
                } catch (IOException e) { }
            }
        }

        private void broadcast(String message) {
            synchronized (clientMap) {
                for (PrintWriter writer : clientMap.values()) {
                    writer.println(message);
                }
            }
        }
    }
}
