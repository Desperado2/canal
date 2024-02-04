package com.alibaba.otter.canal.k2s.starrocks.sink;


import java.io.StringWriter;

/**
 * 分隔符解析
 * @author mujingjing
 * @date 2024/2/4
 **/
public class StarRocksDelimiterParser {

    private static final String HEX_STRING = "0123456789ABCDEF";

    /**
     * 解析
     * @param sp 原始分隔符
     * @param dSp 目标分隔符
     * @return 目标分隔符
     * @throws RuntimeException 运行时异常
     */
    public static String parse(String sp, String dSp) throws RuntimeException {
        if (sp == null || sp.length() == 0) {
            return dSp;
        }
        if (!sp.toUpperCase().startsWith("\\X")) {
            return sp;
        }
        String hexStr = sp.substring(2);
        // check hex str
        if (hexStr.isEmpty()) {
            throw new RuntimeException("Failed to parse delimiter: `Hex str is empty`");
        }
        if (hexStr.length() % 2 != 0) {
            throw new RuntimeException("Failed to parse delimiter: `Hex str length error`");
        }
        for (char hexChar : hexStr.toUpperCase().toCharArray()) {
            if (HEX_STRING.indexOf(hexChar) == -1) {
                throw new RuntimeException("Failed to parse delimiter: `Hex str format error`");
            }
        }
        // transform to separator
        StringWriter writer = new StringWriter();
        for (byte b : hexStrToBytes(hexStr)) {
            writer.append((char) b);
        }
        return writer.toString();
    }

    private static byte[] hexStrToBytes(String hexStr) {
        String upperHexStr = hexStr.toUpperCase();
        int length = upperHexStr.length() / 2;
        char[] hexChars = upperHexStr.toCharArray();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) HEX_STRING.indexOf(c);
    }

}
