/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jms.config.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author kameshs
 */
@SuppressWarnings({
                   "rawtypes", "unchecked"
})
public class DecryptorTest {

    @Test
    public void testDecryptWithNoEncryptedProperties() {
        Dictionary cfProps = new Hashtable<>();
        cfProps.put("name", "testCF");
        cfProps.put("timeout", 2000);

        Decryptor decryptor = new Decryptor(getEncryptor());
        Dictionary decryptedConfig = decryptor.decrypt(cfProps);

        for (Enumeration e = decryptedConfig.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String expectedValue = String.valueOf(cfProps.get(key));
            String actualValue = String.valueOf(decryptedConfig.get(key));
            assertEquals(expectedValue, actualValue);
        }
    }

    @Test
    public void testDecryptWithEncryptedProperties() {

        final String myPassword = "password";
        final StringEncryptor testStringEnryptor = getEncryptor();
        String encryptedPassword = testStringEnryptor.encrypt(myPassword);

        Dictionary cfProps = new Hashtable<>();
        cfProps.put("name", "testCF");
        cfProps.put("password", "ENC(" + encryptedPassword + ")");
        cfProps.put("timeout", 2000);

        Decryptor decryptor = new Decryptor(testStringEnryptor);
        Dictionary decryptedConfig = decryptor.decrypt(cfProps);

        assertEquals("testCF", decryptedConfig.get("name"));
        assertEquals("password", decryptedConfig.get("password"));
        assertEquals("2000", decryptedConfig.get("timeout"));
    }

    @Test
    public void testDecryptWithEncryptedPropertiesAndAlias() {
        final String myPassword = "password";
        final String alias = "testAlias";
        final StringEncryptor testStringEnryptor = getEncryptor();
        String encryptedPassword = testStringEnryptor.encrypt(myPassword);

        Dictionary cfProps = new Hashtable<>();
        cfProps.put("name", "testCF");
        cfProps.put("password", "ENC(" + encryptedPassword + ", " + alias + ")");
        cfProps.put("timeout", 2000);

        Decryptor decryptor = new Decryptor(testStringEnryptor);
        Dictionary decryptedConfig = decryptor.decrypt(cfProps);

        assertEquals("testCF", decryptedConfig.get("name"));
        assertEquals("password", decryptedConfig.get("password"));
        assertEquals("2000", decryptedConfig.get("timeout"));
    }

    @Test
    public void testDecryptWithEncryptedPropertiesAndUnknownAlias() {
        Dictionary cfProps = new Hashtable<>();
        cfProps.put("name", "testCF");
        cfProps.put("password", "ENC(something,testAlias)");
        cfProps.put("timeout", 2000);
        Assert.assertEquals("testAlias", Decryptor.getAlias(cfProps));
    }
    
    @Test(expected = RuntimeException.class)
    public void testDecryptWithTwoDifferentAliases() {
        Dictionary cfProps = new Hashtable<>();
        cfProps.put("password", "ENC(something,testAlias)");
        cfProps.put("password2", "ENC(something,testAlias2)");
        Decryptor.getAlias(cfProps);
    }

    @Test
    public void testIsEncrypted() {
        assertTrue(Decryptor.isEncrypted("ENC(123456abce)"));
        assertFalse(Decryptor.isEncrypted("123456abce"));
    }
    
    private StandardPBEStringEncryptor getEncryptor() {
        StandardPBEStringEncryptor textEncryptor = new StandardPBEStringEncryptor();
        textEncryptor.setPassword("myPassword");
        return textEncryptor;
    }

}
