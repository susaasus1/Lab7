package Data;

import Reader.ConsoleReader;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class User implements Serializable {
    private static final long serialVersionUID = -7238636389902242255L;
    private String login=null;
    private String password=null;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User() {
    }

    private static String encrypt(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(message.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static User fillUser(Scanner scanner) {
        User user = new User();
        user.login = (String) ConsoleReader.conditionalRead(scanner, "Введите логин: ", false,
                String::toString, (m) -> !m.equals(""), (m) -> m.length() > 3);
        user.password = (String) ConsoleReader.conditionalRead(scanner, "Введите пароль: ", false,
                String::toString);

        user.password = encrypt(user.password);
        return user;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return login;
    }
}
