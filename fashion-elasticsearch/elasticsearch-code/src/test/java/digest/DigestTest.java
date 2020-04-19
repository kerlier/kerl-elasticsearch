package digest;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class DigestTest {

    @Test
    public void testDigest(){
        //08368e5bbd597a0864294cfdde400482
        //08368e5bbd597a0864294cfdde400482
        String md5 = getMd5("aa-bb-cc-dd");
        System.out.println(md5);
    }

    public  String getMd5(String string) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] bs = digest.digest(string.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
