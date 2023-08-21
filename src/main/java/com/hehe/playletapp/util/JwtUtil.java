package com.hehe.playletapp.util;

import com.hehe.playletapp.vo.TokenVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    // 签名密钥
    private static final String SECRET = "joywetch";
    // 过期时间
//    private static final Date expirationTime = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);

    public static final String JWT_ADMIN_SECRET = "joywetch";

    /**
     * 创建token
     *
     * @param id
     * @param subject
     * @param ttlMillis
     * @param baseKey
     * @param issuer
     * @return
     * @throws Exception
     */
    public static String createJWT(String id, String subject, Long ttlMillis, String baseKey, String issuer) throws Exception {
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(baseKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer).signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    /**
     * 反解jwtToken
     *
     * @param jwt
     * @param baseKey
     * @return
     * @throws Exception
     */
    public Claims parseJWT(String jwt, String baseKey) throws Exception {
        Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(baseKey)).parseClaimsJws(jwt).getBody();
        return claims;
    }

    /**
     * 生成token
     *
     * @param name
     * @return
     */
    public static String generateToken(String name, Long id, Long deptId, Long partId, Integer permissionLevel,Integer permissionGroup, Long ttlMillis) {
        // 通过秘钥签名JWT
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        long nowMillis = System.currentTimeMillis();
        Date expirationTime = new Date(nowMillis + ttlMillis);
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("deptId", deptId);
        map.put("partId", partId);
        map.put("permissionLevel", permissionLevel);
        map.put("permissionGroup", permissionGroup);
        map.put("date", new Date());
        String jwt = Jwts.builder().setClaims(map).setExpiration(expirationTime)// 过期时间
                .signWith(SignatureAlgorithm.HS512, signingKey).compact();

        return "Bearer " + jwt; //jwt前面一般都会加Bearer
    }

    /**
     * 验证token
     *
     * @param token
     */
    public static String validateToken(String token, String secret) {
        // parse the token.
        try {
            Map<String, Object> body = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secret)).parseClaimsJws(token.replace("Bearer ", "")).getBody();
            return body.get("name").toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token
     *
     * @param token
     */
    public static String validateTokenInfo(String token, String secret, String param) {
        // parse the token.
        try {
            Map<String, Object> body = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secret)).parseClaimsJws(token.replace("Bearer ", "")).getBody();
            return body.get(param).toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static TokenVO getToken() {
        TokenVO tokenVO = new TokenVO();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        String authorization = request.getHeader("Authorization");
        String partId1 = JwtUtil.validateTokenInfo(authorization, JWT_ADMIN_SECRET, "partId");

        Long partId = getDefault(partId1);
        String name = getDefault(JwtUtil.validateTokenInfo(authorization, JWT_ADMIN_SECRET, "name"),null);
        Long deptId = getDefault(JwtUtil.validateTokenInfo(authorization, JWT_ADMIN_SECRET, "deptId"));
        Long id = getDefault(JwtUtil.validateTokenInfo(authorization, JWT_ADMIN_SECRET, "id"));
        Integer permissionLevel = getDefaultInt(JwtUtil.validateTokenInfo(authorization, JWT_ADMIN_SECRET, "permissionLevel"));
        Integer permissionGroup = getDefaultInt(JwtUtil.validateTokenInfo(authorization, JWT_ADMIN_SECRET, "permissionGroup"));
        tokenVO.setPartId(partId);
        tokenVO.setName(name);
        tokenVO.setPermissionLevel(permissionLevel);
        tokenVO.setPermissionGroup(permissionGroup);
        tokenVO.setId(id);
        tokenVO.setDeptId(deptId);
        return tokenVO;
    }
    public static String getDefault(String str, String defaults) {
        if (StringUtils.isEmpty(str) || "null".equals(str)) {
            return defaults;
        } else {
            return str;
        }
    }
    public static Long getDefault(String str) {
        if (StringUtils.isEmpty(str) || "null".equals(str)) {
            return null;
        } else {
            return Long.valueOf(str);
        }
    }
    public static Integer getDefaultInt(String str) {
        if (StringUtils.isEmpty(str) || "null".equals(str)) {
            return null;
        } else {
            return Integer.valueOf(str);
        }
    }
}
