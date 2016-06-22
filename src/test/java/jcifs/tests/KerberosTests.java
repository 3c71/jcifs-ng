/*
 * © 2016 AgNO3 Gmbh & Co. KG
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jcifs.tests;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.KeyTab;

import org.junit.Test;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.Kerb5Authenticator;
import jcifs.smb.SmbFile;
import sun.security.jgss.krb5.Krb5Util;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbAsReqBuilder;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
public class KerberosTests {

    @Test
    public void testKRB () throws IOException, Asn1Exception, KrbException {
        CIFSContext ctx = SingletonContext.getInstance();
        Subject s = getInitiatorSubject(TestConfig.getTestUser(), TestConfig.getTestUserPassword(), TestConfig.getTestUserDomain());
        ctx = ctx.withCredentials(new Kerb5Authenticator(ctx, s));
        SmbFile f = new SmbFile("smb://" + TestConfig.getTestServer() + "/test/", ctx);

        for ( SmbFile entry : f.listFiles() ) {
            System.out.println(entry);

            if ( entry.getContentLength() < 4096 ) {
                long size = 0;
                try ( InputStream is = entry.getInputStream() ) {
                    byte[] buffer = new byte[4096];
                    int read = 0;
                    while ( ( read = is.read(buffer) ) >= 0 ) {
                        size += read;
                    }
                }
                assertEquals(entry.getContentLength(), size);
            }
        }
    }


    /**
     * @param keytab
     * @param principal
     * @param ticketCacheName
     * @param renewTGT
     * @return a subjct with the kerberos initiate credentials (TGT)
     * @throws KerberosException
     */
    public static Subject getInitiatorSubject ( KeyTab keytab, final KerberosPrincipal principal ) throws Asn1Exception, KrbException, IOException {
        KerberosTicket ticket = getKerberosTicket(keytab, principal);
        Set<Object> privCreds = new HashSet<>();
        privCreds.add(ticket);
        return new Subject(false, new HashSet<>(Arrays.asList((Principal) principal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param keytab
     * @param principal
     * @param ticketCacheName
     * @param renewTGT
     * @return
     * @throws KerberosException
     */

    private static KerberosTicket getKerberosTicket ( KeyTab keytab, final KerberosPrincipal principal )
            throws Asn1Exception, KrbException, IOException {
        PrincipalName principalName = new PrincipalName(principal.toString(), PrincipalName.KRB_NT_PRINCIPAL);
        EncryptionKey[] keys = Krb5Util.keysFromJavaxKeyTab(keytab, principalName);

        if ( keys == null || keys.length == 0 ) {
            throw new KrbException("Could not find any keys in keytab for " + principalName); //$NON-NLS-1$
        }

        KrbAsReqBuilder builder = new KrbAsReqBuilder(principalName, keytab);
        Credentials creds = builder.action().getCreds();
        builder.destroy();

        return Krb5Util.credsToTicket(creds);
    }


    /**
     * @param principal
     * @param password
     * @param ticketCacheName
     * @param renewTGT
     * @return a subjct with the kerberos initiate credentials (TGT)
     * @throws KerberosException
     */
    public static Subject getInitiatorSubject ( KerberosPrincipal principal, String password ) throws Asn1Exception, KrbException, IOException {
        KerberosTicket ticket = getKerberosTicket(principal, password);
        Set<Object> privCreds = new HashSet<>();
        privCreds.add(ticket);
        return new Subject(false, new HashSet<>(Arrays.asList((Principal) principal)), Collections.EMPTY_SET, privCreds);
    }


    /**
     * @param principal
     * @param password
     * @param ticketCacheName
     * @param renewTGT
     * @return
     * @throws KerberosException
     */
    private static KerberosTicket getKerberosTicket ( KerberosPrincipal principal, String password ) throws Asn1Exception, KrbException, IOException {
        PrincipalName principalName;

        principalName = new PrincipalName(principal.getName(), PrincipalName.KRB_NT_PRINCIPAL, principal.getRealm());

        KrbAsReqBuilder builder = new KrbAsReqBuilder(principalName, password != null ? password.toCharArray() : new char[0]);
        Credentials creds = builder.action().getCreds();
        builder.destroy();

        KerberosTicket ticket = Krb5Util.credsToTicket(creds);
        return ticket;
    }


    /**
     * @param userName
     * @param password
     * @param realm
     * @param ticketCacheName
     * @param renewTGT
     * @return a subjct with the kerberos initiate credentials (TGT)
     * @throws KerberosException
     */
    public static Subject getInitiatorSubject ( String userName, String password, String realm ) throws Asn1Exception, KrbException, IOException {
        String fullPrincipal = String.format("%s@%s", userName, realm); //$NON-NLS-1$
        KerberosPrincipal principal = new KerberosPrincipal(fullPrincipal, KerberosPrincipal.KRB_NT_PRINCIPAL);
        return getInitiatorSubject(principal, password);
    }
}
