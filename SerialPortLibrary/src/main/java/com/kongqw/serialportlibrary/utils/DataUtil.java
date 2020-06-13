package com.kongqw.serialportlibrary.utils;

/**
 * @author giftedcat
 */
public class DataUtil {

    //-------------------------------------------------------
    static public int isOdd(int num) {
        return num & 0x1;
    }

    //-------------------------------------------------------
    static public int HexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    static public byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }

    //-------------------------------------------------------
    static public byte[] HexToByteArr(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
}