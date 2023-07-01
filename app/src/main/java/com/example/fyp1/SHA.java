package com.example.fyp1;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {
    public static byte[ ] getSHA( String input ) throws NoSuchAlgorithmException
    {
        // Static getInstance method is called with hashing SHA
        // Make sure you pass "SHA-256" without any spaces
        // and just a '-' in between properly
        MessageDigest md = MessageDigest.getInstance( "SHA-256" ) ;

        // To caculate message digest of an input
        // digest( ) method is called
        // which returns an array of bytes
        return md.digest( input.getBytes( StandardCharsets.UTF_8 ) ) ;
    }
    public static String toHexString( byte[ ] hash )
    {
        // For converting byte array into signum representation
        BigInteger number = new BigInteger( 1, hash ) ;
        // For converting message digest into hex value
        StringBuilder hexString = new StringBuilder( number.toString( 16 ) ) ;

        // Pad with leading zeros
        while ( hexString.length( ) < 32 )
        {
            hexString.insert( 0,  " 0 " ) ;
        }
        return hexString.toString( ) ;
    }
}
