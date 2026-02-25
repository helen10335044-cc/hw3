package vo;

public class LoginUser {
    public enum Type { CUSTOMER, EMPLOYEE }
    private final Type type;
    private final int id;
    private final String username;
    private final String role; // for employee: ADMIN / STAFF ; for customer: CUSTOMER

    public LoginUser(Type type, int id, String username, String role) {
        this.type = type;
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Type getType() { return type; }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    public boolean isAdmin() {
        return type == Type.EMPLOYEE && "ADMIN".equalsIgnoreCase(role);
    }
}
