import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class atm_server {
    String username;
    String password;
    int balance;
    int req_id;

    atm_server(String username, String password, int balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public void setBalance(int newBalance) {
        this.balance = newBalance;
    }

    public int getBalance() {
        return this.balance;
    }

    public int getReq_id() {
        return this.req_id;
    }

    public void setReq_id() {
        this.req_id = getReq_id() + 1;
    }

    public String checkBalance() {
        return "Your current balance is: " + getBalance() + " taka";
    }

    public void credit(int value) {
        setBalance(getBalance() + value);
    }

    public boolean debit(int value) {
        if (getBalance() >= value) {
            setBalance(getBalance() - value);
            return true;
        } else
            return false;
    }

    public static void main(String args[]) throws IOException {
        int userNo = -1;

        atm_server[] users;

        users = new atm_server[3];

        users[1] = new atm_server("saima", "tonni", 3000);
        users[0] = new atm_server("tasfia", "tabassum", 6000);

        delay();

        System.out.println("Server started");

        delay();

        System.out.println("Waiting for Clients to connect");

        ServerSocket serverSocket = new ServerSocket(5000);
        Socket socket = serverSocket.accept();
        delay();
        System.out.println("Client Accepted");
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

        try {
            Object message1 = ois.readObject();
            Object msg2 = ois.readObject();

            String Name = (String) message1;
            String Password = (String) msg2;

            for (int i = 0; i < 3; i++) {
                if (Name.equals(users[i].username) && Password.equals(users[i].password)) {
                    oos.writeObject(true);
                    userNo = i;
                    break;
                } else
                    oos.writeObject(false);
            }
            while (true) {
                Object cMsg3 = ois.readObject();
                String command = (String) cMsg3;

                if (userNo >= 0) {
                    if (command.equals("Credit")) {

                        sendPackets();

                        oos.writeObject("Enter amount to be credited:\n");

                        Object cMsg4 = ois.readObject();
                        int value = (int) cMsg4;

                        users[userNo].credit(value);

                        sendPackets();

                        oos.writeObject("Your account has been credited by " + value + " tk\n"
                                + users[userNo].checkBalance());

                    } else if (command.equals("Debit")) {

                        sendPackets();

                        oos.writeObject("Enter amount to be debited:\n");

                        Object cMsg4 = ois.readObject();
                        int value = (int) cMsg4;

                        sendPackets();

                        if (users[userNo].debit(value) == true)
                            oos.writeObject("Your account has been debited by " + value + " tk\n"
                                    + users[userNo].checkBalance());
                        else
                            oos.writeObject("Balance is not sufficient\n" + users[userNo].checkBalance());
                    } else if (command.equals("Quit")) {

                        sendPackets();

                        oos.writeObject("Log Out Successful...\n");

                        delay();

                        System.out.println("System shutting down...\n");

                        break;
                    } else if (command.equals("Balance")) {

                        sendPackets();

                        oos.writeObject(users[userNo].checkBalance());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean error() {

        int num = (int) Math.floor(Math.random() * (100));

        if (num < 50) {
            delay();
            System.out.println("\nData sent successfully to the Client...\n");
            return true;
        } else {
            delay();
            System.out.println("\nData not sent to the Client\nTrying again to sent...\n");
            return false;
        }
    }

    static void sendPackets() {
        while (true) {
            if (error() == true)
                break;
        }
    }

    static void delay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}