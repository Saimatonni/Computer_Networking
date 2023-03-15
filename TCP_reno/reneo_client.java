import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.*;
import java.util.*;

public class reneo_client {
    public static byte[] BytetoHeader(int numofseq, int numofack, int ack, int sf, int rwnd) {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(numofseq);
        buffer.putInt(numofack);
        buffer.put((byte) ack);
        buffer.put((byte) sf);
        buffer.putShort((short) rwnd);
        return buffer.array();
    }

    public static int[] fromHeader(byte[] segment) {
        ByteBuffer buffer = ByteBuffer.wrap(segment);
        int seqNum = buffer.getInt();
        int ackNum = buffer.getInt();
        int ack = buffer.get();
        int sf = buffer.get();
        int rwnd = buffer.getShort();
        return new int[] { seqNum, ackNum, ack, sf, rwnd };
    }

    public static boolean duplicate_ACKs() {
        Random rand = new Random();
        int randomNumber = rand.nextInt(100);
        // System.out.println(randomNumber);
        if (randomNumber < 30)
            return true;
        return false;
    }

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 5000);
        System.out.println("Client Connected");
        int recvBufferSize = 2;
        int windowSize = 4 * recvBufferSize;
        clientSocket.setReceiveBufferSize(recvBufferSize);

        DecimalFormat df = new DecimalFormat("#0.000");

        Stack<Integer> window = new Stack<>();

        window.push(8);
        window.push(4);
        window.push(2);
        window.push(1);

        int seqNum = 0;
        int expectedAckNum = 0;

        String data = "TCP Reno is a congestion control algorithm used in the Transmission Control Protocol (TCP) to manage network congestion.";
        int dataLen = data.length();

        long timeout = 2; // in seconds
        long startTime = System.currentTimeMillis();
        long StartTime = System.nanoTime();

        while (expectedAckNum < dataLen) {

            if (!window.empty())
                windowSize = window.pop();

            else if (window.empty()) {
                if (!duplicate_ACKs())
                    windowSize++;
                else {
                    windowSize = windowSize / 2 + 3;
                    System.out.println("Duplicate Acks recieved");
                }
            }

            int sendSize = Math.min(windowSize, dataLen - expectedAckNum);

            System.out.println("\nSeqence Number: " + seqNum + "\nWindow Size: " + sendSize + "\n");

            byte[] header = BytetoHeader(seqNum, expectedAckNum, 1, 0, sendSize);
            byte[] message = data.substring(seqNum, seqNum + sendSize).getBytes();
            byte[] segment = new byte[header.length + message.length];
            System.arraycopy(header, 0, segment, 0, header.length);
            System.arraycopy(message, 0, segment, header.length, message.length);

            clientSocket.getOutputStream().write(segment);

            byte[] ackHeader = new byte[12];
            clientSocket.getInputStream().read(ackHeader);

            int[] result = fromHeader(ackHeader);

            int ackNum = result[1];

            seqNum += sendSize;
            expectedAckNum = ackNum;

            if ((System.currentTimeMillis() - startTime) > timeout * 1000) {
                seqNum = expectedAckNum;
                startTime = System.currentTimeMillis();
            }
        }
        long endtime = System.nanoTime();

        long duration = (endtime - StartTime);
        double delay = (double) duration / 1_000_000.0;

        System.out.println(
                "\nTotal delay: " + df.format(delay) + " ms\n\n");

        clientSocket.close();
    }
}
