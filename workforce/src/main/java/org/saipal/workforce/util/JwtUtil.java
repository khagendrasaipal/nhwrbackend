package org.saipal.workforce.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.saipal.fmisutil.util.DB;
import org.saipal.workforce.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtUtil {
	private static Logger log = LoggerFactory.getLogger(JwtUtil.class);
	
	@Autowired
	DB db;
	
	@Autowired
	UserService us;

	private String SECRET_KEY = "SaIpAlSuTrAtEcHn0lOgY";

	public String extractInfo(String token) {
		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
	}
	public String extractId(String token) {
		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getId();
	}
	
	

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();

	}

	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public String generateToken(String userId) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userId);
		// return createToken(claims, userId + "::" + remoteAdr + "::" +
		// browserDetails);
	}

	private String createToken(Map<String, Object> claims, String subject) {
		// log.info("generating token:" + subject);
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
				.setId(db.newIdInt())
				.signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();

	}

	public Object createToken(String subject) {
		String token = Jwts.builder().setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
		.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
		.setId(db.newIdInt())
		.signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
		return token;
	}
}
