import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        InputStream inputStream;
        try {
            // Read host and port from config properties file
            Properties properties = new Properties();
            String propertyFileName = "config.properties";
            inputStream = ChatClient.class.getClassLoader().getResourceAsStream(propertyFileName);
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + propertyFileName + "' not found in the classpath");
            }
            String host = properties.getProperty("host");
            int port = Integer.parseInt(properties.getProperty("port"));

            Socket clientSocket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));   // read data from socket
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());     // write data into socket
            Scanner scanner = new Scanner(System.in); // Input data from user

            Thread senderThread = new Thread(new Runnable() {
                String message;

                @Override
                public void run() {
                    while (true) {
                        message = scanner.nextLine();
                        out.println(message);
                        out.flush();
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
                        while (message != null && !message.equals("/q")) {
                            System.out.println("Server says: " + message);
                            message = in.readLine();
                        }
                        out.close();
                        clientSocket.close();
                        System.out.println("Connection is closed");
                    } catch (IOException e) {
                        e.printStackTrace();
                        //System.out.println("Unexpected error occured, disconnected");
                    }
                }
            });
            receiverThread.start();
        } catch (UnknownHostException e) {
            System.out.println("Server is not reachable, please make sure the server is up and running!");
        } catch (ConnectException e) {
            System.out.println("Server is not reachable, please make sure the server is up and running!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}