
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class multi_client
{
	public static void main(String[] args) throws IOException
	{
		try
		{
			Scanner scn = new Scanner(System.in);
			InetAddress ip = InetAddress.getByName("192.168.1.101");
			Socket s = new Socket(ip, 5056);
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			while (true)
			{
				System.out.println(dis.readUTF());
				String tosend = scn.nextLine();
				dos.writeUTF(tosend);
				if(tosend.equals("Exit"))
				{
					System.out.println("Closing this connection : " + s);
					s.close();
					System.out.println("Connection closed");
					break;
				}
				String received = dis.readUTF();
				System.out.println(received);
			}
			scn.close();
			dis.close();
			dos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}