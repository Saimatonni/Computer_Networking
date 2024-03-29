
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class tahoe_server {
    private static byte[] CreateHeader(int seqNum, int ackNum, int ack, int sf, int rwnd) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(ByteBuffer.allocate(4).putInt(seqNum).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(ackNum).array());
            outputStream.write(ByteBuffer.allocate(1).put((byte) ack).array());
            outputStream.write(ByteBuffer.allocate(1).put((byte) sf).array());
            outputStream.write(ByteBuffer.allocate(2).putShort((short) rwnd).array());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    private static int[] fromHeader(byte[] segment) {
        int[] header = new int[5];
        ByteBuffer buffer = ByteBuffer.wrap(segment);
        header[0] = buffer.getInt();
        header[1] = buffer.getInt();
        header[2] = (int) buffer.get();
        header[3] = (int) buffer.get();
        header[4] = buffer.getShort();
        return header;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server started");

			System.out.println("Waiting for a client ...");
			
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client accepted");
            int recvBufferSize = 12;
            clientSocket.setReceiveBufferSize(recvBufferSize);
            int expectedSeqNum = 0;
            int ackNum = 0;
            ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

            while (true) {
                byte[] header = new byte[12];
                clientSocket.getInputStream().read(header);
                int[] headerFields = fromHeader(header);
                int seqNum = headerFields[0];
                int ackNumm = headerFields[1];
                int ack = headerFields[2];
                int sf = headerFields[3];
                int rwnd = headerFields[4];

                byte[] data = new byte[rwnd];
                int bytesRead = clientSocket.getInputStream().read(data);

                if (bytesRead == 0) {
                    break;
                }

                String str = new String(data, 0, bytesRead);

                System.out.println("\nData sent: " + str);

                seqNum = ackNum;

                if (seqNum == expectedSeqNum) {
                    receivedData.write(data);
                    ackNum += bytesRead;
                    expectedSeqNum += bytesRead;

                    byte[] toSendAck = CreateHeader(seqNum, ackNum, 1, 0, 12);
                    clientSocket.getOutputStream().write(toSendAck);
                } else {
                    byte[] toSendAck = CreateHeader(seqNum, ackNum, 1, 0, 12);
                    clientSocket.getOutputStream().write(toSendAck);
                }
            }

            String receivedDataStr = receivedData.toString("UTF-8");
            System.out.println(receivedDataStr);

            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
