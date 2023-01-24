
import java.io.*;
import java.net.*;

public class upper_case_client {

    public static void main(String[] args) throws IOException {
        System.out.println("Started");
        Socket socket = new Socket("192.168.1.103", 5000);
        System.out.println("Client Connected with Server");
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Enter a lowercase sentence: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
       BufferedReader in = new BufferedReader((new InputStreamReader(socket.getInputStream())));

        String messageSent = reader.readLine();
        System.out.println("The message sent is: " + messageSent);
        out.println(messageSent);


        String messageReceived = in.readLine();
        System.out.println("The modified message is: " + messageReceived);
    }
}
