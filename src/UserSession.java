public class UserSession {

    private static int currentUserId = -1;
    private static String currentUserEmail = null;

    public static void setUser(int userId, String email) {
        currentUserId = userId;
        currentUserEmail = email;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }

    public static void clear() {
        currentUserId = -1;
        currentUserEmail = null;
    }
}

