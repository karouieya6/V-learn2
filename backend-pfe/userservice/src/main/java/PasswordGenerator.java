import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("Instructor Password: " + encoder.encode("instructorpassword"));
        System.out.println("Student Password: " + encoder.encode("studentpassword"));
        System.out.println("NewUser Password: " + encoder.encode("testpassword"));
    }
}
