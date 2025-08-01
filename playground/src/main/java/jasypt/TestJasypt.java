package jasypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class TestJasypt {

    public static void main(String[] args) {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setPassword("Demo_pwd!2025");
        standardPBEStringEncryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        standardPBEStringEncryptor.setIvGenerator(new RandomIvGenerator());

        String result = standardPBEStringEncryptor.encrypt("springCloud-Pwd!");
        System.out.println(result);
        System.out.println(standardPBEStringEncryptor.decrypt(result));
    }
}
