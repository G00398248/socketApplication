import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class ChatServer {
    public static void main(String[] args) throws IOException {

        // Read port from config properties file
        Properties properties = new Properties();
        String propertyFileName = "config.properties";
        InputStream inputStream = ChatClient.class.getClassLoader().getResourceAsStream(propertyFileName);
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("Property file '" + propertyFileName + "' not found in the classpath.");
        }
        int port = Integer.parseInt(properties.getProperty("port"));

        // Set up client and server sockets
        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket;

        // Accept incoming client connection on the server
        clientSocket = serverSocket.accept();
        System.out.println("Client is connected");

        // Set up input and output streams for message passing between client and server sockets
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

        // To take input message
        Scanner scanner = new Scanner(System.in);


        // Sender and Receiver threads to facilitate message passing
        Thread senderThread = new Thread(new Runnable() {
            String message;
            @Override
            public void run() {
                while (true) {
                    message = scanner.nextLine();
                    out.println(message);
                    out.flush(); // flushes the outputstream i.e. makes it empty once message is sent
                }
            }
        });
        senderThread.start();

        Thread receiverThread = new Thread(new Runnable() {
            String message;
            @Override
            public void run() {
                try {
                    message = in.readLine();
                    // Null safety and terminate the program/communication once /q is received
                    while (message != null && !message.equals("/q")) {
                        System.out.println("Client says: " + message);
                        message = in.readLine();
                    }
                    out.close();
                    clientSocket.close();
                    serverSocket.close();

                    System.out.println("Connection is closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiverThread.start();

    }
}