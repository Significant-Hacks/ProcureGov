package com.procuregov.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Tracks which users are currently online by monitoring HttpSession creation/destruction.
 * Used by the notification system to determine whether email reminders are needed
 * for evaluators who have not completed their evaluations, or officers who have not
 * awarded a tender after evaluation is complete.
 */
public class OnlineUserTracker implements HttpSessionListener {

    private static final Set<Integer> onlineUserIds = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null && userId > 0) {
            onlineUserIds.add(userId);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            onlineUserIds.remove(userId);
        }
    }

    /**
     * Checks if a user is currently online (has an active session).
     * @param userId the user ID to check
     * @return true if the user has an active session
     */
    public static boolean isOnline(int userId) {
        return onlineUserIds.contains(userId);
    }

    /**
     * Registers a user as online when they log in.
     * Called from SessionUtil.createSession() since the listener
     * fires before the userId attribute is set.
     * @param userId the logged-in user's ID
     */
    public static void userLoggedIn(int userId) {
        if (userId > 0) {
            onlineUserIds.add(userId);
        }
    }

    /**
     * Removes a user from the online set on logout.
     * @param userId the logging-out user's ID
     */
    public static void userLoggedOut(int userId) {
        onlineUserIds.remove(userId);
    }
}
