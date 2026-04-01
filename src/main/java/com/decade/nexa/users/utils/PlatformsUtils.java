package com.decade.nexa.users.utils;

import jakarta.servlet.http.HttpServletRequest;

public class PlatformsUtils {
    public static boolean isBrowserNavigation(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
