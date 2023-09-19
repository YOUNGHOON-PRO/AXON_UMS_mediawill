package messager.mailsender.send.dns;

import java.io.*;

public class MailExchanger
    extends ResourceRecord
{
    private int preference;
    private String mx;

    protected void decode(DNSInputStream dnsIn)
        throws IOException {
        preference = dnsIn.readShort();
        mx = dnsIn.readDomainName();
    }
}