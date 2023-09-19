package messager.mailsender.send.dns;

import java.io.*;

public abstract class ResourceRecord
{
    public int preference;
    public String mx;
    public int[] ipAddress = new int[4];

    public void initMailExchanger(DNSInputStream dnsIn)
        throws IOException {
        preference = dnsIn.readShort();
        mx = dnsIn.readDomainName();
    }

    public void initAddress(DNSInputStream dnsIn)
        throws IOException {
        for (int i = 0; i < 4; i++) {
            ipAddress[i] = dnsIn.readByte();
        }
    }

    protected abstract void decode(DNSInputStream dnsIn)
        throws IOException;
}
