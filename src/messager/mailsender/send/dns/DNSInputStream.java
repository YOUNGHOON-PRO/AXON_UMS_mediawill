package messager.mailsender.send.dns;

import java.io.*;

import messager.mailsender.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DNSInputStream
    extends ByteArrayInputStream
{
	private static final Logger LOGGER = LogManager.getLogger(DNSInputStream.class.getName());
	
    protected DataInputStream dataIn;
    public DNSInputStream(byte[] data, int off, int len) {
        super(data, off, len);
        dataIn = new DataInputStream(this);
    }

    public int readByte()
        throws IOException {
        return dataIn.readUnsignedByte();
    }

    public int readShort()
        throws IOException {
        return dataIn.readUnsignedShort();
    }

    public long readInt()
        throws IOException {
        return dataIn.readInt() & 0xffffffffL;
    }

    public String readString()
        throws IOException {
        int len = readByte();
        if (len == 0) {
            return "";
        }
        else {
            byte[] buffer = new byte[len];
            dataIn.readFully(buffer);
            return new String(buffer, "latin1");
        }
    }

    public ResourceRecord readRR()
        throws IOException {
        String name = readDomainName();
        int type = readShort();
        int clas = readShort();
        long ttl = readInt();
        int len = readShort();
        DNSInputStream rrDNSIn = new DNSInputStream(buf, pos, len);
        pos += len;

        try {
            String record = "";
            if (type == 15) {
                record = "messager.mailsender.send.dns.MailExchanger";
            }
            else if (type == 1) {
                record = "messager.mailsender.send.dns.Address";
            }
            else {
                return null;
            }
            Class theClass = Class.forName(record);
            ResourceRecord rr = (ResourceRecord) theClass.newInstance();
            if (type == 15) {
                rr.initMailExchanger(rrDNSIn);
            }
            else if (type == 1) {
                rr.initAddress(rrDNSIn);
            }
            return rr;
        }
        catch (Exception e) {
        	LOGGER.error(e);
            LogWriter.writeException("DNSInputStream", "readRR()", "Dynamic Class Loading(" + type + ")", e);
            return null;
        }
    }

    public String readDomainName()
        throws IOException {
        if (pos >= count) {
            throw new EOFException("EOF reading domain name");
        }

        if ( (buf[pos] & 0xc0) == 0) {
            String labels = readString();
            if (labels.length() > 0) {
                String tail = readDomainName();
                if (tail.length() > 0) {
                    labels = labels + "." + tail;
                }
            }
            return labels;
        }
        else {
            if ( (buf[pos] & 0xc0) != 0xc0) {
                throw new IOException("Invalid domain name compression offset");
            }
            int offset = readShort() & 0x3fff;
            DNSInputStream dnsIn = new DNSInputStream(buf, offset, buf.length - offset);
            return dnsIn.readDomainName();
        }
    }
}