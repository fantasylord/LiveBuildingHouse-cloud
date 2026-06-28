package com.livehouse.util;

import java.util.Map;

/**
 * Helper for lightweight H5 JWT parsing.
 */
public class H5AuthUtil {

    private H5AuthUtil() {
    }

    public static Long getUserId(String authorization, JwtUtil jwtUtil) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        Map<String, Object> claims = jwtUtil.parseToken(authorization.substring(7));
        if (claims == null || claims.get("userId") == null) {
            return null;
        }
        return Long.valueOf(claims.get("userId").toString());
    }
}
