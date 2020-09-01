import java.io.*;
import java.util.*;

/*
 *   Vadim (Wacka) : Thu Aug 27 23:12:36 VLAT 2020
 *   Encode given number to ASN.1 tag (Identifier Octets) format
 *   and further Decode the ByteArray obtained as a result of Encoding
 *   The long data type is a 64-bit signed two's complement integer.
 *   It has a minimum value of -9,223,372,036,854,775,808
 *   and a maximum value of 9,223,372,036,854,775,807 (inclusive).
 *   Bit 8 of leading octet is always 1
 *   Bit 7 of leading octet is always 0 (we simulate context - specific tag)
 *   Bit 6 is set to zero (primitive tag)
 */

public class encodeDecodeAsnTag {

    public static void main(String[] args)  throws IOException {
        System.out.println(new Date());

        ArrayList<Byte> asnTagOctets = encodeAsnTag(Long.parseLong((new BufferedReader(new InputStreamReader(System.in))).readLine()));
        asnTagOctets.forEach(encodeDecodeAsnTag::printSingleOctet);
        System.out.println();
        System.out.println(decodeAsnTag(asnTagOctets));

    }

    public static ArrayList<Byte> encodeAsnTag(Long value) {
        ArrayList<Byte> asnTagOctets00 = new ArrayList<>();

        if (value < 0b00011111 ) {  // low tag values
            //asnTagOctets00.add(((Long)(value|0b10000000)).byteValue());  // primitive case (bit 6 equals 0)
            asnTagOctets00.add(((Long)(value|0b10100000)).byteValue());  // constructive case (bit 6 equals 1)
            return asnTagOctets00;
        }

        //tag values more or equal than 31

        // fill leading octet
        //asnTagOctets00.add(((Integer)0b10011111).byteValue());  // primitive case of tag (bit 6 equals 0 in leading octet)
        asnTagOctets00.add(((Integer)0b10111111).byteValue());  // constructive case of tag (bit 6 equals 1 in leading octet)

        // get maxExponent value
        final int base128 = 128;  //set base degree constant
        int maxExponent = 0;
        while (value >= Math.pow(base128,maxExponent + 1))
            maxExponent++;

        // set subsequent octets
        int factor;
        for (int k=maxExponent; k>0; k--) {
                factor = (int) Math.floor(value / Math.pow(base128,k));
                asnTagOctets00.add(((Integer)(factor|0b10000000)).byteValue());
                value -= (long)Math.floor(factor * Math.pow(base128,k));
            }
        asnTagOctets00.add(value.byteValue());  // set last subsequent octet

        return asnTagOctets00;
    }

    public static Long decodeAsnTag (ArrayList<Byte> byteArray) {

        // single octet case
        if ( ((byteArray.get(0).longValue()) & (0b00011111)) != 31 )
            return (byteArray.get(0).longValue() & (0b00011111));

        // two octets case : leading octet and one subsequent octet
        if ( byteArray.get(1).longValue() > 0 )
            return byteArray.get(1).longValue();  // single octet case

        // several octets case : leading octet together with several subsequent octets

        long decodedValue=0L;
        final int base128 = 128;  //set base degree constant
        int nSubseq = byteArray.size() - 1;  // the number of subsequent octets

        for (int i=1; i < nSubseq; i++)
            decodedValue += (byteArray.get(i).longValue() & (0b01111111)) * (long)Math.pow(base128,nSubseq - i);

        decodedValue += byteArray.get(nSubseq).longValue();

        return decodedValue;
    }

    public static void printSingleOctet(Byte b) {
        System.out.print(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16)));
        System.out.print(Character.toUpperCase(Character.forDigit((b) & 0xF, 16)));
        System.out.print(" ");
    }

}
