package ma.project.translator.jakarta.hello;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
	private static final Map<String, User> users = new ConcurrentHashMap<>();

    public static boolean registerUser(User user) {
        if (users.containsKey(user.getUsername())) {
            return false; // user already exists
        }
        users.put(user.getUsername(), user);
        return true;
    }

    public static User getUser(String username) {
        return users.get(username);
    }

    public static boolean validateLogin(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public static Map<String, User> getAllUsers() {
        return users;
    }
    
    public User findByUsername(String username) 
    { 
    	return users.get(username); 
    }
    public boolean validatePassword(User user, String password) { 
    	return user != null && user.getPassword().equals(password); 
    }
}
