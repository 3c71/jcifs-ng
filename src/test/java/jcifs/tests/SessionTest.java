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


import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jcifs.smb.SmbFile;


/**
 * 
 * 
 * 
 * Compatability Notes:
 * - Windows (2k8, 2k12) servers do not like extended security + DOS error codes
 * 
 * @author mbechler
 *
 */
@RunWith ( Parameterized.class )
@SuppressWarnings ( "javadoc" )
public class SessionTest extends BaseCIFSTest {

    public SessionTest ( String name, Map<String, String> properties ) {
        super(name, properties);
    }


    @Parameters ( name = "{0}" )
    public static Collection<Object> configs () {
        return getConfigs("noSigning", "forceSigning", "legacyAuth", "noUnicode", "forceUnicode", "noNTStatus");
    }


    @Test
    public void logonUser () throws IOException {
        SmbFile f = getDefaultShareRoot();
        checkConnection(f);
    }


    @Test
    public void logonAnonymous () throws IOException {
        SmbFile f = new SmbFile(getTestShareGuestURL(), withAnonymousCredentials());
        checkConnection(f);
    }


    @Test
    public void logonGuest () throws IOException {
        SmbFile f = new SmbFile(getTestShareGuestURL(), withTestGuestCredentials());
        checkConnection(f);
    }

}
