package cz.cvut.fel.ear.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
// Service to handle JWT operations like creation and validation - main logic for JWT
public class JwtService {

    //application.properties values
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //function to extract any claim from the token - in our case username
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //generates token without any extra claims
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    //generates token with extra claims - can be used to add roles or other info
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    //validates the token - checks username and expiration
    public boolean isTokenValid(String token, UserDetails userDetails) {
        //extracts username from the token
        final String username = extractUsername(token);
        //checks if the username matches and if the token is not expired
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    //checks if the token is expired
    private boolean isTokenExpired(String token) {
        //compares expiration date with current date
        return extractExpiration(token).before(new Date());
    }

    //extracts expiration date from the token - as claim
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //gets the signing key from the secret key
    //signing key is used to sign and verify the token - the same key must be used for both operations
    //decodes the base64 encoded secret key - we must verify, that the key was used for HS256 algorithm and is of sufficient length
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //extracts all claims from the token
    //we need to compare token from user and verify it with our signing key else - exception will be thrown
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                //verifies the token with the signing key - must be the same as used for signing
                .verifyWith(getSigningKey())
                .build()
                //parses the token - signed claims means the token is signed with the key in step above
                //if the key wasnt signed with the same key, an exception will be thrown
                .parseSignedClaims(token)
                //get the claims from the parsed token
                .getPayload();
    }

}