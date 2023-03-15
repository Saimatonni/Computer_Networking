import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.*;
import java.util.*;

public class client1 {
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

    public static boolean duplicate_Acks() {
        Random random = new Random();
        int randomNumber = random.nextInt(100);
        System.out.println(randomNumber);
        if (randomNumber < 10)
            return true;
        return false;
    }

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 1234);
        System.out.println("Client Connected");
        int recvBufferSize = 2;
        int windowSize = 4 * recvBufferSize;
        clientSocket.setReceiveBufferSize(recvBufferSize);

        DecimalFormat df = new DecimalFormat("#0.000");

        Stack<Integer> window = new Stack<>();

        int cwnd = 1;
        int ssthrs = 8;
        int flag = 0;

        int seqNum = 0;
        int expectedAckNum = 0;

        String data = "TCP provides reliable, ordered, and error-checked delivery of a stream of bytes between applications running on hosts communicating via an IP network.";
        int dataLen = data.length();

        long timeout = 2; // in seconds
        long startTime = System.currentTimeMillis();

        while (expectedAckNum < dataLen) {

            if (cwnd <= ssthrs && flag == 0) {
                windowSize = cwnd;
                if (cwnd * 2 <= ssthrs)
                    cwnd *= 2;
                else {
                    flag = 1;
                    cwnd = ssthrs;
                }
            } else {
                if (!duplicate_Acks()) {
                    cwnd++;
                    windowSize = cwnd;
                } else {
                    ssthrs = cwnd / 2 - 1;
                    cwnd = 1;
                    flag = 0;
                    windowSize = cwnd;
                    if (cwnd * 2 <= ssthrs)
                        cwnd *= 2;
                    System.out.println("Duplicate Acks received...");
                }
            }
            long RTT_starttime = System.nanoTime();

            int sendSize = Math.min(windowSize, dataLen - expectedAckNum);

            byte[] header = BytetoHeader(seqNum, expectedAckNum, 1, 0, sendSize);
            byte[] message = data.substring(seqNum, seqNum + sendSize).getBytes();
            byte[] segment = new byte[header.length + message.length];
            System.arraycopy(header, 0, segment, 0, header.length);
            System.arraycopy(message, 0, segment, header.length, message.length);

            clientSocket.getOutputStream().write(segment);

            byte[] ackHeader = new byte[12];
            clientSocket.getInputStream().read(ackHeader);

            double EstimatedRTT = 0.2;
            double alpha = 0.125;
            double DevRTT = 0.2;
            double beta = 0.125;

            long RTT_endtime = System.nanoTime();

            long duration = (RTT_endtime - RTT_starttime);
            double SampleRTT = (double) duration / 1_000_000.0;

            EstimatedRTT = (1 - alpha) * EstimatedRTT + alpha * SampleRTT;

            DevRTT = (1 - beta) * DevRTT + beta * (SampleRTT - EstimatedRTT);

            double RTO = EstimatedRTT + 4 * DevRTT;

            System.out.println("round-trip time: " + df.format(SampleRTT) + " ms\n" + "Estimated round-trip time: " + df.format(EstimatedRTT) + " ms\n" +
                    "Dev round-trip time: " + df.format(DevRTT) + " ms\n" + "retransmission timeout: " + df.format(RTO) + " ms\n");

            int[] result = fromHeader(ackHeader);

            int ackNum = result[1];

            seqNum += sendSize;
            expectedAckNum = ackNum;

            if ((System.currentTimeMillis() - startTime) > timeout * 1000) {
                seqNum = expectedAckNum;
                startTime = System.currentTimeMillis();
            }
        }

        clientSocket.close();
    }
}
