package messager.mailsender.send.dns;

import java.io.*;

public class Address
    extends ResourceRecord
{
    private int[] ipAddress = new int[4];

    protected void decode(DNSInputStream dnsIn)
        throws IOException {
        for (int i = 0; i < 4; i++) {
            ipAddress[i] = dnsIn.readByte();
        }
    }
}