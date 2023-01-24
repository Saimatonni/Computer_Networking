
import java.io.*;
import java.net.*;

public class upper_case_server{

    public static void main (String[] args) throws IOException {

        System.out.println("Server started");
        System.out.println("Waiting for Clients...");
        ServerSocket serverSocket = new ServerSocket (5000);
        Socket clientSocket = serverSocket.accept ();

        BufferedReader in = new BufferedReader (new InputStreamReader (clientSocket.getInputStream ()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String message, modifiedMessage;
        message = in.readLine ();
        System.out.print("The received message from client: " + message);
        modifiedMessage = message.toUpperCase();
        out.println(modifiedMessage);
        System.out.println ("\nModified message which is sent to client: " + modifiedMessage);
    }

}
