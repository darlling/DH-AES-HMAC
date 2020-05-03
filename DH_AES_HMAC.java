import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * DH�㷨Э�̹�����Կ
 * �ԳƼ����㷨AES�������ݼӽ���
 * HMAC�㷨������Ϣ�����ԺͿɿ��Լ���
 * @author 20175415-������
 * */
public class DH_AES_HMAC {
    // �ǶԳ���Կ�㷨
    public static final String KEY_ALGORITHM="DH"; 
    // �ԳƼ����㷨
    public static final String SECRET_ALGORITHM="AES";
    // ��Կ����
    private static final int KEY_SIZE=512;
    // ��Կ
    private static final String PUBLIC_KEY="DHPublicKey";
    // ˽Կ
    private static final String PRIVATE_KEY="DHPrivateKey";
    // HMAC�㷨
    private static final String ALGORITHM_MAC = "HmacMD5";
    
    /**
     * ��ʼ���׷���Կ
     * @return Map �׷���Կ��Map
     * */
    public static Map<String,Object> initKey() throws Exception{
        //ʵ������Կ������
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //��ʼ����Կ������
        keyPairGenerator.initialize(KEY_SIZE);
        //������Կ��
        KeyPair keyPair=keyPairGenerator.generateKeyPair();
        //�׷���Կ
        DHPublicKey publicKey=(DHPublicKey) keyPair.getPublic();
        //�׷�˽Կ
        DHPrivateKey privateKey=(DHPrivateKey) keyPair.getPrivate();
        //����Կ�洢��map��
        Map<String,Object> keyMap=new HashMap<String,Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
        
    }
    
    /**
     * ��ʼ���ҷ���Կ
     * @param key �׷���Կ
     * @return Map �ҷ���Կ��Map
     * */
    public static Map<String,Object> initKey(byte[] key) throws Exception{
        //�����׷��Ĺ�Կ
        //ת����Կ�Ĳ���
        X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);
        //ʵ������Կ����
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //������Կ
        PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);
        //�ɼ׷��Ĺ�Կ�����ҷ���Կ
        DHParameterSpec dhParamSpec=((DHPublicKey)pubKey).getParams();
        //ʵ������Կ������
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(keyFactory.getAlgorithm());
        //��ʼ����Կ������
        keyPairGenerator.initialize(dhParamSpec);
        //������Կ��
        KeyPair keyPair=keyPairGenerator.genKeyPair();
        //�ҷ���Կ
        DHPublicKey publicKey=(DHPublicKey)keyPair.getPublic();
        //�ҷ�˽Կ
        DHPrivateKey privateKey=(DHPrivateKey)keyPair.getPrivate();
        //����Կ�洢��Map��
        Map<String,Object> keyMap=new HashMap<String,Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }
    
    /**
     * ����
     * @param data ����
     * @param key ��Կ
     * @return byte[] ����
     * */
    public static byte[] AESEncrypt(byte[] data,byte[] key) throws Exception{
        //���ɱ�����Կ
        SecretKey secretKey=new SecretKeySpec(key,SECRET_ALGORITHM);
        //���ݼ���
        Cipher cipher=Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    
    /**
     * ����
     * @param data ����
     * @param key ��Կ
     * @return byte[] ����
     * */
    public static byte[] AESDecrypt(byte[] data,byte[] key) throws Exception{
        //���ɱ�����Կ
        SecretKey secretKey=new SecretKeySpec(key,SECRET_ALGORITHM);
        //���ݽ���
        Cipher cipher=Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    
    /**
     * ������Կ
     * @param publicKey ��Կ
     * @param privateKey ˽Կ
     * @return byte[] ������Կ
     * */
    public static byte[] getSecretKey(byte[] publicKey,byte[] privateKey) throws Exception{
        //ʵ������Կ����
        KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
        //��ʼ����Կ
        //��Կ����ת��
        X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(publicKey);
        //������Կ
        PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);
        //��ʼ��˽Կ
        //��Կ����ת��
        PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(privateKey);
        //����˽Կ
        PrivateKey priKey=keyFactory.generatePrivate(pkcs8KeySpec);
        //ʵ����
        KeyAgreement keyAgree=KeyAgreement.getInstance(keyFactory.getAlgorithm());
        //��ʼ��
        keyAgree.init(priKey);
        keyAgree.doPhase(pubKey, true);
        //���ɱ�����Կ
        SecretKey secretKey=keyAgree.generateSecret(SECRET_ALGORITHM);
        return secretKey.getEncoded();
    }
    
    /**
     * ȡ��˽Կ
     * @param keyMap ��Կmap
     * @return byte[] ˽Կ
     * */
    public static byte[] getPrivateKey(Map<String,Object> keyMap){
        Key key=(Key)keyMap.get(PRIVATE_KEY);
        return key.getEncoded();
    }
    
    /**
     * ȡ�ù�Կ
     * @param keyMap ��Կmap
     * @return byte[] ��Կ
     * */
    public static byte[] getPublicKey(Map<String,Object> keyMap) throws Exception{
        Key key=(Key) keyMap.get(PUBLIC_KEY);
        return key.getEncoded();
    }
    
    /** 
     * @param key ��Կ�ַ��� 
     * @param algorithm �����㷨 
     * @return ������Կ���� 
     */  
    private static Key toKey(byte[] key,String algorithm) {  
        SecretKey secretKey = new SecretKeySpec(key, algorithm);
        return secretKey;  
    }
    
    /** 
     * ʹ��HMAC���� 
     * @param data Ҫ���ܵ����� 
     * @param key ��Կ 
     * @return ���ؼ��ܺ����Ϣ 
     */  
    public static byte[] HMACEncode(byte[] data, byte[] key) {  
        Key k = toKey(key,ALGORITHM_MAC);
        try {  
            Mac mac = Mac.getInstance(k.getAlgorithm());  
            mac.init(k);  
            return mac.doFinal(data);  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (InvalidKeyException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }
}