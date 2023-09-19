package messager.mailsender.send.dns;

import java.io.*;

public class NameServer
    extends ResourceRecord
{
    private String nameServer;

    protected void decode(DNSInputStream dnsIn)
        throws IOException {
        nameServer = dnsIn.readDomainName();
    }
}