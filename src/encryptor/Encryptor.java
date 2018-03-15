package encryptor;

import org.apache.commons.lang.ArrayUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;

public class Encryptor {
    private static class EncryptMethod {
        protected int ivLen, kLen;
        protected String jName;

        public EncryptMethod(String jName, int kLen, int ivLen) {
            this.jName = jName;
            this.kLen = kLen;
            this.ivLen = ivLen;
        }
    }

    static HashMap<String, EncryptMethod> CIPHERS = new HashMap<>();

    static {
        CIPHERS.put(
                "aes-256-cfb",
                new EncryptMethod("AES/CFB/NoPadding", 32, 16)
        );
        CIPHERS.put(
                "aes-128-cfb",
                new EncryptMethod("AES/CFB/NoPadding", 16, 16)
        );
    }

    private String password;
    private Cipher cipher;
    private EncryptMethod m;

    private SecretKey sKey;

    public Encryptor(String method, String password) {
        m = CIPHERS.get(method);
        this.password = password;
        try {
            setSecretKey(password, m.kLen, m.ivLen);
            cipher = Cipher.getInstance(m.jName);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public byte[] encrypt(byte[] data) {
        Random rnd = new Random(System.nanoTime());
        byte[] iv = new byte[m.ivLen];
        rnd.nextBytes(iv);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, sKey, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(data);
            return ArrayUtils.addAll(iv, encrypted);
        } catch (Exception e) {
            e.printStackTrace();
//            System.exit(-1);
        }
        return null;
    }

    public byte[] decrypt(byte[] data) {
        byte[] iv = ArrayUtils.subarray(data, 0, m.ivLen);
        data = ArrayUtils.subarray(data, m.ivLen, data.length);
        try {
            cipher.init(Cipher.DECRYPT_MODE, sKey, new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            System.out.println(data.length);
            e.printStackTrace();
//            System.exit(-1);
        }
        return null;
    }

    private void setSecretKey(String password, int kLen, int ivLen) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bKey = md5.digest(password.getBytes());
            byte[] bsKey;
            while(bKey.length<kLen+ivLen){
                bsKey=ArrayUtils.addAll(bKey,password.getBytes());
                bKey=ArrayUtils.addAll(bKey,md5.digest(bsKey));
            }
            sKey = new SecretKeySpec(ArrayUtils.subarray(bKey,0,kLen),m.jName.split("/")[0]);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] argv) throws UnsupportedEncodingException {
        Encryptor e = new Encryptor("aes-256-cfb", "wangbowenmax");
        byte[] encrypted = new byte[]{
                (byte) 241, 26, (byte) 173, (byte) 183, (byte) 152, 26, (byte) 199, 111, (byte) 141, (byte) 238, (byte) 202, 110, (byte) 223, (byte) 129, 13, 80, (byte) 162, (byte) 180, 13, (byte) 217, 36, 45, 94, (byte) 153, (byte) 246, 65, 57, (byte) 142
        };
        byte[] decrypted = e.decrypt(encrypted);
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put(decrypted);
        bb.flip();
        System.out.println(StandardCharsets.UTF_8.decode(bb));
    }

}
