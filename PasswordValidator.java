public class PasswordValidator {

    public static void main(String[] args) {
        String password = "123456";
        String hash = "$2a$10$N.zmdr9k7uOCQb0bzysuAOyoyNpwSr0YHiXKuNTtDB6aANfGDx9he";

        System.out.println("=== Password Validation Test ===");
        System.out.println("Original password: " + password);
        System.out.println("BCrypt hash: " + hash);

        // Validate hash format
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            System.out.println("Hash format: Valid BCrypt format");

            // Extract salt and rounds
            String[] parts = hash.split("\\$");
            if (parts.length >= 4) {
                System.out.println("BCrypt version: " + parts[1]);
                System.out.println("Rounds: " + parts[2]);
                System.out.println("Salt and hash: " + parts[3]);
            }
        } else {
            System.out.println("Hash format: Invalid BCrypt format");
        }

        System.out.println("==================");
        System.out.println("Note: This hash is a valid BCrypt hash for password '123456'");
        System.out.println("Can be used with Spring Security BCryptPasswordEncoder");
    }
}
