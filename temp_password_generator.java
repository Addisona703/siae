import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("Original password: " + password);
        System.out.println("BCrypt encoded: " + encoded);
        System.out.println("Verification: " + encoder.matches(password, encoded));
    }
}
