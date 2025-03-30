package classes;

public class LoginData {
	public Credentials valid;
	public Credentials invalid;
	
    public static class Credentials {
        public String username;
        public String password;
        public String message;
    }
}
