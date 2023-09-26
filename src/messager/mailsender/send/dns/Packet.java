package messager.mailsender.send.dns;

import java.io.*;
import java.util.*;
import messager.mailsender.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Packet
{
	private static final Logger LOGGER = LogManager.getLogger(Packet.class.getName());
	
    private int queryID;
    private static int globalID;
    private String queryHost;
    private int queryType, queryClass;

    public Packet(String host, int type, int classes) {
        StringTokenizer labels = new StringTokenizer(host, ".");
        while (labels.hasMoreTokens()) {
            if (labels.nextToken().length() > 63) {
                throw new IllegalArgumentException("Invalid hostname : " + host);
            }
        }
        queryHost = host;
        queryType = type; //값 15 writed by 오범석
        queryClass = classes; //값 1 writed by 오범석

        synchronized (getClass()) {
            queryID = (++globalID) % 65536;
        }
    }

    public byte[] extractQuery() {
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteArrayOut);
        try {
            dataOut.writeShort(queryID);
            dataOut.writeShort( (0 << 15) | (0 << 11) | (1 << 8));
            dataOut.writeShort(1);
            dataOut.writeShort(0);
            dataOut.writeShort(0);
            dataOut.writeShort(0);
            StringTokenizer labels = new StringTokenizer(queryHost, ".");
            while (labels.hasMoreTokens()) {
                String alabel = labels.nextToken();
                dataOut.writeByte(alabel.length());
                dataOut.writeBytes(alabel);
            }
            dataOut.writeByte(0);

            dataOut.writeShort(queryType);
            dataOut.writeShort(queryClass);
        }
        catch (IOException e) {
        	LOGGER.error(e);
            LogWriter.writeException("Packet", "extractQuery()", "OutputStream", e);
        }
        return byteArrayOut.toByteArray();
    }

    private boolean authoritative, truncated, recursive;
    private Vector answers = new Vector();
    private ArrayList authorities = new ArrayList();
    private ArrayList additional = new ArrayList();

    public void receiveResponse(byte[] data, int length)
        throws IOException {
        DNSInputStream dnsIn = new DNSInputStream(data, 0, length);
        int id = dnsIn.readShort();
        if (id != queryID) {
            throw new IOException("ID does not match request");
        }

        int flags = dnsIn.readShort();
        decodeFlags(flags);
        int numQueries = dnsIn.readShort();
        int numAnswers = dnsIn.readShort();
        int numAuthorities = dnsIn.readShort();
        int numAdditional = dnsIn.readShort();

        while (numQueries-- > 0) {
            String queryName = dnsIn.readDomainName();
            int queryType = dnsIn.readShort();
            int queryClass = dnsIn.readShort();
        }

        try {
            while (numAnswers-- > 0) {
                answers.add(dnsIn.readRR());
            }

            while (numAuthorities-- > 0) {
                authorities.add(dnsIn.readRR());
            }

            while (numAdditional-- > 0) {
                additional.add(dnsIn.readRR());
            }
        }
        catch (EOFException e) {
        	LOGGER.error(e);
            if (!truncated) {
                LogWriter.writeException("Packet", "receiveResponse()", "EOFException", e);
            }
        }
    }

    protected void decodeFlags(int flags)
        throws IOException {
        boolean isResponse = ( (flags >> 15) & 1) != 0;
        if (!isResponse) {
            throw new IOException("Response flag not set");
        }

        int opcode = (flags >> 11) & 15;
        authoritative = ( (flags >> 10) & 1) != 0;
        truncated = ( (flags >> 9) & 1) != 0;
        recursive = ( (flags >> 7) & 1) != 0;
        boolean recurseRequest = ( (flags >> 8) & 1) != 0;
        int code = (flags >> 0) & 15;

        if (code != 0) {
            throw new IOException("header.flags(RCODE)!=0");
        }
    }

    public Vector getAnswers() {
        return answers;
    }
}