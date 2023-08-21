package com.hehe.playletapp.util;

import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESUtil {

    private static final String CHAR_ENCODING = "UTF-8";
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * 加密
     *
     * @param data 需要加密的内容
     * @param key  加密密码
     * @return
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        if (key.length != 16) {
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);// 创建密码器
            IvParameterSpec iv = new IvParameterSpec(key);//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, seckey, iv);// 初始化
            byte[] result = cipher.doFinal(data);
            return result; // 加密
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("encrypt fail!", e);
        }
    }

    /**
     * 解密
     *
     * @param data 待解密内容
     * @param key  解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
//        CheckUtils.notEmpty(data, "data");
//        CheckUtils.notEmpty(key, "key");
        if (key.length != 16) {
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            // 创建密码器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            //使用CBC模式，需要一个向量iv，可增加加密算法的强度
            IvParameterSpec iv = new IvParameterSpec(key);
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, seckey, iv);
            byte[] result = cipher.doFinal(data);
            // 解密
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("decrypt fail!", e);
        }
    }

    public static String encryptToBase64(String data, String key) {
        try {
            byte[] valueByte = encrypt(data.getBytes(CHAR_ENCODING), key.getBytes(CHAR_ENCODING));
            return new String(Base64Utils.encode(valueByte));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encrypt fail!", e);
        }

    }

    public static String decryptFromBase64(String data, String key) {
        try {
            byte[] originalData = Base64Utils.decode(data.getBytes());
            byte[] valueByte = decrypt(originalData, key.getBytes(CHAR_ENCODING));
            return new String(valueByte, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }

    public static String encryptWithKeyBase64(String data, String key) {
        try {
            byte[] valueByte = encrypt(data.getBytes(CHAR_ENCODING), Base64Utils.encode(key.getBytes()));
            return new String(Base64Utils.encode(valueByte));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encrypt fail!", e);
        }
    }

    public static String decryptWithKeyBase64(String data, String key) {
        try {
            byte[] originalData = Base64Utils.encode(data.getBytes());
            byte[] valueByte = decrypt(originalData, Base64Utils.encode(key.getBytes()));
            return new String(valueByte, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }

    public static byte[] genarateRandomKey() {
        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance(AES_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(" genarateRandomKey fail!", e);
        }
        SecureRandom random = new SecureRandom();
        keygen.init(random);
        Key key = keygen.generateKey();
        return key.getEncoded();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
//        String content = "flkdsjfkljsflkjdklfjsklfjlsdjfkldjlllllllllllkkkkkkkkkkkkkkkkkkkkkkkkkkk";
//        String key = "mklonjibhuvgytyu";
//        byte[] encrypt = encrypt(content.getBytes("utf8"), key.getBytes("utf8"));
//        System.out.println("加密后：" + Base64Utils.encodeToString(encrypt));
//        byte[] decrypt = decrypt(encrypt, key.getBytes("utf8"));
//        System.out.println("解密后：" + new String(decrypt, "utf8"));
//        String data = "qMpTn9UZKg1eP/L2CcE5aMN+2ob40uLfyOgK7AeqfRH41z4n5iU7OoMZyoJOIkl7/NQ9e0SLF2p3njEhC62Ew65SHVIGfs6l+TvdZV7gjNFSdsdoOTZc4uexz6G8p5FXZoEFpJsX0TLFaKtJX5s5pPu8BBVjpbKGuYg+t8wSEQnUHTEoujZPR9YGaGkbwUDMY2CKbUEviQVEKA0Solh2Vwta/etsdFUGatIl5NPP8wLbGU1uunTp0Xb6REAc2s/O3CxzTgqElOn8pGuvq7Tpb2uehAGVWt39Fsh+Y5otoCzCHTO0ypUZqzlDrnjhlq3x38sJ6BUREGRa4bB2XNlxLeWqXIL1FNGKQ98DhU+Arp0wKgAm3rT1LPrAN/7CrrVCQTE374uox4s1KVCo9JCmJ8+P3OvPXFYC6a/iLQhdh2z/570NqOjGsOgsjerbbDGznb9q9RWkmkIhOeJDEBDq+6OTsNJOUQEEpp+RWxcQXW3p8mKXMCSvp4/4IWRx75eHRHbrh0geewc1e6GyqQDg4fPC5nMiy8W/ZFz0vk5cQ1wwKDQIjrLTpywUL4ef5YH4J4VhGwMtepA5kLpxxebmMmu06hk2lor6GxWXgBn2irOYD74qziHtosr9Tk3jqQHORY4x5kdR0XVUE6I1QW8UU2BerOjGSexL6n6stClWtTqaOutuRLus5n2Ast0gsBVBo0gl0mrXXZewFpfSuHJuaSkZi9wM39H7f/W+449shXUVydce5BFyjGriCl0TKIBXOJYgvA4r895RrWL1XvIasywd/5c3I8qYjpdmMZeyo1diSVh//XZZ5e6ygjORSfLt1eXO/Y/W4+/sswVYk1byvqhapw+Temr7KFwAtry27SEMHOWol69WRHNsVrEt07hltugeQI8+OiivvhTeegJG9LE6CKK84KVQRjyRvCjZqNMvCt8eVPGhmmp78ouS+hsem+OqzKUaM0Nr1hrwxzDlJ6MdC8VF7Y2Wps9oFv1pEqEBW9B9RMDC0/wEEWlXGiqcxvAlGc28xXRsBwnvu3GviIC5H5wzgmTQeLDMhjNIH8dfDZmw/3lXgrbxysrXjJBg4Jd4GCxq4znfZ2vXdHONJ75p9OYm9LOHBeHpM+ZwutYlDbh6TxyRmXlZMM42UmzqceTT42Hh2eXbzzGsKZOMO+fPV8iNbxHbaL9orZ4s/wOYkYYtClZvCTHoP8UY/FkFleJCRLQ369225IDCb97VYZZ+MSMmv9RneQq6Wuk7QJ0FKWg/j2PQo+0fP3cLEl4lbimcHNQi1+yke34hKk9pROUJ4yntDDVwojjZjZUeaEOMYDvZgxohh1h0ut/G/XOIu0vFUSBtK8FzTy2Reds2gLW0TSbUNwpva4GWtW64LbvVEcWgnYQxAKQjBQq625tG8ZnBI4KZEOgKJGSCxy0dXVe+pAKA1fY0GBpXmvaUnDkCWkmmAbJcrD18SgxY91g0";
//        String key = "HHVECBGLYJSNhhmb";
//        byte[] encrypt = encrypt(content.getBytes("utf8"), key.getBytes("utf8"));
//        System.out.println("加密后内容" + Base64Utils.encodeToString(encrypt));
//        String ss = data.replaceAll(" ", "+");
//        String s = decryptFromBase64(data, key);
//        System.out.println(s);
//        String s = decryptWithKeyBase64(ss, key);
//        System.out.println("解密后内容 = " + s);
//        String ss = "1127916601@qq.com&&&" + System.currentTimeMillis();
//        System.out.println(ss);
//        String s = Base64Utils.encodeToString(("1127916601@qq.com&&&" + System.currentTimeMillis()).getBytes());
//        System.out.println(s);
        String salt = decryptFromBase64("HYvQ0mBicNqKaC4N3GaZxyLpXbAeJiEYGtPt8lxi2uqYJarcT4H+fkCDqYsfMnbyr4ijpLfyKw5+LZF0HnqUKw==", "O2Q6XDSGBSLY6P8E");
        System.out.println(salt);
    }

}
