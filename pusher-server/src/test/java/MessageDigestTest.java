import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class MessageDigestTest {
    public static void main(String[] args) throws Exception{
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        String content = "123456";
        byte[] digestBytes1 = digest.digest(content.getBytes());
        System.out.println(DatatypeConverter.printHexBinary(digestBytes1));

        digest.reset();
        char[] chars = content.toCharArray();
        for (char a: chars) {
            digest.update((byte)a);
        }
        byte[] digestBytes2 = digest.digest();
        System.out.println(DatatypeConverter.printHexBinary(digestBytes2));
    }
}
