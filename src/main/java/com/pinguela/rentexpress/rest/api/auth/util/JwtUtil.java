package com.pinguela.rentexpress.rest.api.auth.util;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public final class JwtUtil {

    public static final String SECRET_KEY = "RentExpressSecretKey2024-RentExpressSecretKey2024";
    public static final long EXPIRATION_TIME = 3600000L;

    private JwtUtil() {
    }

    public static String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder().claims(claims).subject(subject).issuedAt(now).expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes())).compact();
    }

    public static Claims parseClaims(String token) throws JwtException {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes())).build()
                .parseSignedClaims(token).getPayload();
    }

    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return false;
            }
            return expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    public static String validateToken(String token) {
        return parseClaims(token).getSubject();
    }

    public static Set<String> extractRoles(Claims claims) {
        Set<String> roles = new HashSet<String>();
        Object profileClaim = claims.get("profile");
        if (profileClaim != null) {
            roles.add(profileClaim.toString());
        }
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof String) {
            roles.add((String) rolesClaim);
        }
        if (rolesClaim instanceof Collection) {
            Collection<?> collection = (Collection<?>) rolesClaim;
            Iterator<?> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element != null) {
                    roles.add(element.toString());
                }
            }
        }
        return roles;
    }
}
