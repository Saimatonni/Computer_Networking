import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class cong_server {
    private static final int HEADER_SIZE = 12;

    private static byte[] createHeader(int seqNum, int ackNum, int ack, int sf, int rwnd) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.putInt(seqNum);
        buffer.putInt(ackNum);
        buffer.put((byte) ack);
        buffer.put((byte) sf);
        buffer.putShort((short) rwnd);
        return buffer.array();
    }

    private static int[] parseHeader(byte[] segment) {
        ByteBuffer buffer = ByteBuffer.wrap(segment);
        int seqNum = buffer.getInt();
        int ackNum = buffer.getInt();
        int ack = buffer.get();
        int sf = buffer.get();
        int rwnd = buffer.getShort();
        return new int[]{seqNum, ackNum, ack, sf, rwnd};
    }

    public static void main(String[] args) {
        System.out.println("Server started");

        System.out.println("Waiting for a client ...");
        try (ServerSocket serverSocket = new ServerSocket(5001);
             
             Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client accepted");

            clientSocket.setReceiveBufferSize(HEADER_SIZE + 512);
            int expectedSeqNum = 0;
            int ackNum = 0;
            ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

            while (true) {
                byte[] header = new byte[HEADER_SIZE];
                if (clientSocket.getInputStream().read(header) == -1) {
                    break;
                }
                int[] headerFields = parseHeader(header);
                int seqNum = headerFields[0];
                int ackNumm = headerFields[1];
                int ack = headerFields[2];
                int sf = headerFields[3];
                int rwnd = headerFields[4];
                System.out.println("Sequence number: " + seqNum + ", ACK: " + ack);

                byte[] data = new byte[rwnd];
                int bytesRead = clientSocket.getInputStream().read(data);
                if (bytesRead == -1) {
                    break;
                }

                String str = new String(data, 0, bytesRead);
                System.out.println(str);

                seqNum = ackNum;

                if (seqNum == expectedSeqNum) {
                    receivedData.write(data);
                    ackNum += bytesRead;
                    expectedSeqNum += bytesRead;

                    byte[] ackHeader = createHeader(seqNum, ackNum, 1, 0, HEADER_SIZE + 512);
                    clientSocket.getOutputStream().write(ackHeader);
                } else {
                    byte[] ackHeader = createHeader(seqNum, ackNum, 1, 0, HEADER_SIZE + 512);
                    clientSocket.getOutputStream().write(ackHeader);
                }
            }

            String receivedDataStr = receivedData.toString("UTF-8");
            System.out.println("Received data:\n" + receivedDataStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


