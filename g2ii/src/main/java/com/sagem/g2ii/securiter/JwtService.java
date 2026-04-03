package com.sagem.g2ii.securiter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // On récupère la clé secrète depuis le fichier application.yml
    @Value("${app.jwt.secret}")
    private String secretKey;

    // On récupère le temps d'expiration depuis le fichier application.yml
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // 1. Extraire l'email (username) à partir du Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Générer un Token simple basé sur les infos de l'utilisateur
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // 3. Générer un Token avec des infos supplémentaires (ex: le rôle, le matricule...)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // L'email de l'utilisateur
                .setIssuedAt(new Date(System.currentTimeMillis())) // Date de création
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Date d'expiration
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Signature avec la clé secrète
                .compact();
    }

    // 4. Vérifier si un Token est valide (S'il appartient bien à l'utilisateur et s'il n'est pas expiré)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 5. Vérifier si un Token est expiré
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extraire la date d'expiration
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Méthode générique pour extraire une information spécifique du Token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Décrypter le Token pour lire toutes ses informations (Claims)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Préparer la clé de sécurité pour signer ou lire le Token
    private Key getSignInKey() {
        // Comme votre clé dans YAML est du texte brut, on la convertit en bytes
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}