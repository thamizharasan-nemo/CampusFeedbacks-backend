package com.feedbacks.FeedbackSystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {} // prevents instantiation

    public static int getCurrentUserId() {
        return getPrincipal().getUserId();
    }

    public static String getCurrentUsername() {
        return getPrincipal().getUsername();
    }

    public static Integer getInstitutionId() {
        return getPrincipal().getInstitutionId();
    }

    // Get the current user from Principal
    private static CustomUserDetails getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("Invalid principal type! \nJWT token might not present!\nSpring principal requires userId, instituteId, name and password");
        }

        return userDetails;
    }
}
