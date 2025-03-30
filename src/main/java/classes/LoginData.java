package classes;

import java.util.List;

public class LoginData {
	public List<User> users;
	
    public static class User {
    	public String status;
        public String username;
        public String password;
        public String message;
    }
}
