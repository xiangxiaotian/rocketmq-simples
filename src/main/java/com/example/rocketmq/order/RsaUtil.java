package com.example.rocketmq.order;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class RsaUtil {
	/** 
     * 定义加密方式 
     */  
    private final static String KEY_RSA = "RSA";  
    /** 
     * 定义签名算法 
     */  
    private final static String KEY_RSA_SIGNATURE = "MD5withRSA";  
    /** 
     * 定义公钥算法 
     */  
    private final static String KEY_RSA_PUBLICKEY = "RSAPublicKey";  
    /** 
     * 定义私钥算法 
     */  
    private final static String KEY_RSA_PRIVATEKEY = "RSAPrivateKey";  
    
    private RsaUtil() {}
    /** 
     * 创建密钥 
     * @return 
     */  
    public static Map<String, Object> generateKey() {  
        Map<String, Object> map = null;  
        try {  
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_RSA);  
            generator.initialize(1024);  
            KeyPair keyPair = generator.generateKeyPair();  
            // 公钥  
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  
            // 私钥  
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();  
            // 将密钥封装为map  
            map = new HashMap(2);
            map.put(KEY_RSA_PUBLICKEY, publicKey);  
            map.put(KEY_RSA_PRIVATEKEY, privateKey);  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
        return map;  
    }


    /** 
     * 用私钥对信息生成数字签名 
     * @param data 加密数据 
     * @param privateKey 私钥 
     * @return 
     */  
    public static String sign(String privateKey, String data) {  
        String str = ""; 
        
        try {  
            // 解密由base64编码的私钥  
            byte[] bytes = decryptBase64(privateKey);  
            // 构造PKCS8EncodedKeySpec对象  
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(bytes);  
            // 指定的加密算法  
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);  
            // 取私钥对象  
            PrivateKey key = factory.generatePrivate(pkcs);  
            // 用私钥对信息生成数字签名  
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);  
            signature.initSign(key);  
            signature.update(data.getBytes());  
            str = encryptBase64(signature.sign());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return str;  
    }  
  
    /** 
     * 校验数字签名 
     * @param data 加密数据 
     * @param publicKey 公钥 
     * @param sign 数字签名 
     * @return 校验成功返回true，失败返回false 
     */  
    public static boolean verify(String publicKey, String data, String sign) {  
        boolean flag = false;  
        try {  
            // 解密由base64编码的公钥  
            byte[] bytes = decryptBase64(publicKey);  
            // 构造X509EncodedKeySpec对象  
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);  
            // 指定的加密算法  
            KeyFactory factory = KeyFactory.getInstance(KEY_RSA);  
            // 取公钥对象  
            PublicKey key = factory.generatePublic(keySpec);  
            // 用公钥验证数字签名  
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);  
            signature.initVerify(key);  
            signature.update(data.getBytes());  
            flag = signature.verify(decryptBase64(sign));  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return flag;  
    }  
  
    /** 
     * 公钥加密 
     * @param key 公钥 
     * @param data 待加密数据 
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     */  
    public static String encryptByPublicKey(String key, String data) throws Exception {  
    	//base64编码的公钥
    	byte[] decoded = Base64.getDecoder().decode(key);
    	RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    	//RSA加密
    	Cipher cipher = Cipher.getInstance("RSA");
    	cipher.init(Cipher.ENCRYPT_MODE, pubKey);
    	
    	byte[] inputArray = data.getBytes("UTF-8");
        int inputLength = inputArray.length;
    	int MAX_ENCRYPT_BLOCK = 117;
         // 标识
  		int offSet = 0;
  		byte[] resultBytes = {};
  		byte[] cache = {};
  		while (inputLength - offSet > 0) {
  			if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
  				cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
  				offSet += MAX_ENCRYPT_BLOCK;
  			} else {
  				cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
  				offSet = inputLength;
  			}
  			resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
  			System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
  		}
         String outStr = Base64.getEncoder().encodeToString(resultBytes);
    	return outStr; 
    }  
    
    /** 
     * 私钥解密 
     * @param data 加密数据 
     * @param key 私钥 
     * @return 
     * @throws Exception 
     */  
    public static String decryptByPrivateKey(String key, String data) throws Exception {  
    	//64位解码加密后的字符串
    	byte[] inputByte = Base64.getDecoder().decode(data.getBytes("UTF-8"));
		//base64编码的私钥
		byte[] decoded = Base64.getDecoder().decode(key);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));  
		//RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, priKey);
		
		int inputLength = inputByte.length;
        int MAX_ENCRYPT_BLOCK = 128;
        // 标识
 		int offSet = 0;
 		byte[] resultBytes = {};
 		byte[] cache = {};
 		while (inputLength - offSet > 0) {
 			if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
 				cache = cipher.doFinal(inputByte, offSet, MAX_ENCRYPT_BLOCK);
 				offSet += MAX_ENCRYPT_BLOCK;
 			} else {
 				cache = cipher.doFinal(inputByte, offSet, inputLength - offSet);
 				offSet = inputLength;
 			}
 			resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
 			System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
 		}
        String outStr = new String(resultBytes);
		return outStr;
    }  
    
    /** 
     * 私钥加密 
     * @param data 待加密数据 
     * @param key 私钥 
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     */  
    public static String encryptByPrivateKey(String key, String data) throws Exception {  
    	//base64编码的公钥
    	byte[] decoded = Base64.getDecoder().decode(key);
    	RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    	//RSA加密
    	Cipher cipher = Cipher.getInstance("RSA");
    	cipher.init(Cipher.ENCRYPT_MODE, priKey);
    	String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
    	return outStr;  
    }  
    
    /** 
     * 公钥钥解密 
     * @param key  公钥 
     * @param data 加密数据 
     * @return 
     * @throws UnsupportedEncodingException 
     */  
    public static String decryptByPublicKey(String key,String data) throws Exception {  
    	//64位解码加密后的字符串
    	byte[] inputByte = Base64.getDecoder().decode(data.getBytes("UTF-8"));
		//base64编码的私钥
		byte[] decoded = Base64.getDecoder().decode(key);
		RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));  
		//RSA解密
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		String outStr = new String(cipher.doFinal(inputByte));
		return outStr; 
    }  
  
    /** 
     * 获取公钥 
     * @param map 
     * @return 
     */  
    public static String getPublicKey(Map<String, Object> map) {  
        String str = "";  
        try {  
            Key key = (Key) map.get(KEY_RSA_PUBLICKEY);  
            str = encryptBase64(key.getEncoded());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return str;  
    }  
  
    /** 
     * 获取私钥 
     * @param map 
     * @return 
     */  
    public static String getPrivateKey(Map<String, Object> map) {  
        String str = "";  
        try {  
            Key key = (Key) map.get(KEY_RSA_PRIVATEKEY);  
            str = encryptBase64(key.getEncoded());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return str;  
    }  
  
    /** 
     * BASE64 解密 
     * @param key 需要解密的字符串 
     * @return 字节数组 
     * @throws Exception 
     */  
    public static byte[] decryptBase64(String key) throws Exception {  
        return Base64.getDecoder().decode(key);
    }  
  
    /** 
     * BASE64 加密 
     * @param key 需要加密的字节数组 
     * @return 字符串 
     * @throws Exception 
     */  
    public static String encryptBase64(byte[] key) throws Exception {  
        return Base64.getEncoder().encodeToString(key);
    }  
    /**
     * 测试
     * @Title: main   
     * @Description: TODO   
     * @param: @param args      
     * @return: void      
     * @throws
     */
    public static void main(String[] args) {
    	//获取秘要对
    	Map<String, Object> map = RsaUtil.generateKey();
		String publicKey = RsaUtil.getPublicKey(map);
		String privateKey = RsaUtil.getPrivateKey(map);
		System.out.println("公钥："+publicKey);
		System.out.println("私钥："+privateKey);
		/*String data = "{aac002:51302319920502614X,aac003:郑长艳}";
		//私钥加密
		try {
			//加密文件
			String encrypt = RsaUtil.encryptByPrivateKey(privateKey, data);
			//进行签名
			System.out.println("私钥加密串"+encrypt);
			//获取签名串
			String sign = RsaUtil.sign(privateKey, encrypt);
			System.out.println("签名串："+sign);
			//校验数字签名
			boolean falge = RsaUtil.verify(publicKey, encrypt, sign);
			System.out.println("校验数字签名"+falge);
			String pubStr = RsaUtil.decryptByPublicKey(publicKey,encrypt);
			System.out.println(""+new String(pubStr));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
    }
}
