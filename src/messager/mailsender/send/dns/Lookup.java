package messager.mailsender.send.dns;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Lookup
{
	
	private static final Logger LOGGER = LogManager.getLogger(Lookup.class.getName());
	
    Vector result = new Vector(); // Exchanger or Address
    private String nameServer;
    public static String errorMessage;
    public static boolean isUnknownHost;

    public Lookup(String nameServer, String hostName, int type) {
        init(nameServer, hostName, type);
    }

    public void init(String nameServer, String hostName, int type) {
        Packet pack = new Packet(hostName, type, 1);
        isUnknownHost = false;
        try {
            boolean received = false;
            int count = 0;
            DatagramSocket sock = new DatagramSocket();
            sock.setSoTimeout(5000);
            try {
                while (!received) {
                    try {
                        sendQuery(pack, sock, InetAddress.getByName(nameServer));
                        getResponse(pack, sock);
                        received = true;
                    }
                    catch (InterruptedIOException ex) {
                    	LOGGER.error(ex);
                        if (count++ >= 3) {
                            throw new UnknownHostException(hostName + " : NOT Exist Domain");

                        }
                    }
                }
            }
            finally {
                sock.close();
            }
        }
        catch (UnknownHostException e) {
        	LOGGER.error(e);
            //LogWriter.writeException("Lookup", "init()", "Lookup Total - UnknownHostException",e);
        }
        catch (IOException e) {
        	LOGGER.error(e);
            errorMessage = e.toString();
            //LogWriter.writeException("Lookup", "init()", "Lookup Total - IOException", e);
        }

        result = pack.getAnswers();
    }

    public void sendQuery(Packet pack, DatagramSocket sock, InetAddress nameServer)
        throws IOException {
        byte[] data = pack.extractQuery();
        DatagramPacket packet = new DatagramPacket(data, data.length, nameServer, 53);
        sock.send(packet);
    }

    public void getResponse(Packet pack, DatagramSocket sock)
        throws IOException {
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        sock.receive(packet);
        pack.receiveResponse(packet.getData(), packet.getLength());
    }

    public Vector getResult() {
        return result;
    }
}
