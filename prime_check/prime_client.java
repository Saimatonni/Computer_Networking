import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class prime_client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Started");
        Socket socket = new Socket("192.168.1.103", 5000);
        System.out.println("Client Connected with Server");

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a number to check prime or not:");
        int number = scanner.nextInt();

        objectOutputStream.writeObject(number);

        try {
            Object fromServer = objectInputStream.readObject();
            System.out.println("From server : " + (String) fromServer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}