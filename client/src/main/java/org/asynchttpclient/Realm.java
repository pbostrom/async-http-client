/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.asynchttpclient;

import static java.nio.charset.StandardCharsets.*;
import static org.asynchttpclient.util.MiscUtils.isNonEmpty;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import org.asynchttpclient.uri.Uri;
import org.asynchttpclient.util.AuthenticatorUtils;
import org.asynchttpclient.util.StringUtils;

/**
 * This class is required when authentication is needed. The class support
 * DIGEST and BASIC.
 */
public class Realm {

    public static RealmBuilder newRealm(Realm prototype) {
        return new RealmBuilder()//
                .realmName(prototype.getRealmName())//
                .algorithm(prototype.getAlgorithm())//
                .methodName(prototype.getMethodName())//
                .nc(prototype.getNc())//
                .nonce(prototype.getNonce())//
                .password(prototype.getPassword())//
                .principal(prototype.getPrincipal())//
                .charset(prototype.getCharset())//
                .opaque(prototype.getOpaque())//
                .qop(prototype.getQop())//
                .scheme(prototype.getScheme())//
                .uri(prototype.getUri())//
                .usePreemptiveAuth(prototype.isUsePreemptiveAuth())//
                .ntlmDomain(prototype.getNtlmDomain())//
                .ntlmHost(prototype.getNtlmHost())//
                .useAbsoluteURI(prototype.isUseAbsoluteURI())//
                .omitQuery(prototype.isOmitQuery());
    }

    public static RealmBuilder newRealm(AuthScheme scheme, String principal, String password) {
        return new RealmBuilder()//
        .scheme(scheme)//
        .principal(principal)//
        .password(password);
    }
    
    public static RealmBuilder newBasicAuth(String principal, String password) {
        return newRealm(AuthScheme.BASIC, principal, password);
    }
    
    public static RealmBuilder newDigestAuth(String principal, String password) {
        return newRealm(AuthScheme.DIGEST, principal, password);
    }
    
    public static RealmBuilder newNtlmAuth(String principal, String password) {
        return newRealm(AuthScheme.NTLM, principal, password);
    }

    private static final String DEFAULT_NC = "00000001";
    private static final String EMPTY_ENTITY_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    private final String principal;
    private final String password;
    private final AuthScheme scheme;
    private final String realmName;
    private final String nonce;
    private final String algorithm;
    private final String response;
    private final String opaque;
    private final String qop;
    private final String nc;
    private final String cnonce;
    private final Uri uri;
    private final String methodName;
    private final boolean usePreemptiveAuth;
    private final Charset charset;
    private final String ntlmHost;
    private final String ntlmDomain;
    private final boolean useAbsoluteURI;
    private final boolean omitQuery;

    public enum AuthScheme {

        BASIC, DIGEST, NTLM, SPNEGO, KERBEROS;
    }

    private Realm(AuthScheme scheme, String principal, String password, String realmName, String nonce, String algorithm, String response, String qop, String nc, String cnonce,
            Uri uri, String method, boolean usePreemptiveAuth, String ntlmDomain, Charset charset, String host, String opaque, boolean useAbsoluteURI, boolean omitQuery) {

        if (scheme == null)
            throw new NullPointerException("scheme");
        if (principal == null)
            throw new NullPointerException("principal");
        if (password == null)
            throw new NullPointerException("password");
        
        this.principal = principal;
        this.password = password;
        this.scheme = scheme;
        this.realmName = realmName;
        this.nonce = nonce;
        this.algorithm = algorithm;
        this.response = response;
        this.opaque = opaque;
        this.qop = qop;
        this.nc = nc;
        this.cnonce = cnonce;
        this.uri = uri;
        this.methodName = method;
        this.usePreemptiveAuth = usePreemptiveAuth;
        this.ntlmDomain = ntlmDomain;
        this.ntlmHost = host;
        this.charset = charset;
        this.useAbsoluteURI = useAbsoluteURI;
        this.omitQuery = omitQuery;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getPassword() {
        return password;
    }

    public AuthScheme getScheme() {

        return scheme;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getNonce() {
        return nonce;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getResponse() {
        return response;
    }

    public String getOpaque() {
        return opaque;
    }

    public String getQop() {
        return qop;
    }

    public String getNc() {
        return nc;
    }

    public String getCnonce() {
        return cnonce;
    }

    public Uri getUri() {
        return uri;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * Return true is preemptive authentication is enabled
     * 
     * @return true is preemptive authentication is enabled
     */
    public boolean isUsePreemptiveAuth() {
        return usePreemptiveAuth;
    }

    /**
     * Return the NTLM domain to use. This value should map the JDK
     * 
     * @return the NTLM domain
     */
    public String getNtlmDomain() {
        return ntlmDomain;
    }

    /**
     * Return the NTLM host.
     * 
     * @return the NTLM host
     */
    public String getNtlmHost() {
        return ntlmHost;
    }

    public boolean isUseAbsoluteURI() {
        return useAbsoluteURI;
    }

    public boolean isOmitQuery() {
        return omitQuery;
    }

    @Override
    public String toString() {
        return "Realm{" + "principal='" + principal + '\'' + ", scheme=" + scheme + ", realmName='" + realmName + '\'' + ", nonce='" + nonce + '\'' + ", algorithm='" + algorithm
                + '\'' + ", response='" + response + '\'' + ", qop='" + qop + '\'' + ", nc='" + nc + '\'' + ", cnonce='" + cnonce + '\'' + ", uri='" + uri + '\''
                + ", methodName='" + methodName + '\'' + ", useAbsoluteURI='" + useAbsoluteURI + '\'' + ", omitQuery='" + omitQuery + '\'' + '}';
    }

    /**
     * A builder for {@link Realm}
     */
    public static class RealmBuilder {

        private static final ThreadLocal<MessageDigest> DIGEST_TL = new ThreadLocal<MessageDigest>() {
            @Override
            protected MessageDigest initialValue() {
                try {
                    return MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        private String principal;
        private String password;
        private AuthScheme scheme;
        private String realmName;
        private String nonce;
        private String algorithm;
        private String response;
        private String opaque;
        private String qop;
        private String nc = DEFAULT_NC;
        private String cnonce;
        private Uri uri;
        private String methodName = "GET";
        private boolean usePreemptive;
        private String ntlmDomain = System.getProperty("http.auth.ntlm.domain");
        private Charset charset = UTF_8;
        private String ntlmHost = "localhost";
        private boolean useAbsoluteURI = false;
        private boolean omitQuery;

        public RealmBuilder ntlmDomain(String ntlmDomain) {
            this.ntlmDomain = ntlmDomain;
            return this;
        }

        public RealmBuilder ntlmHost(String host) {
            this.ntlmHost = host;
            return this;
        }

        public RealmBuilder principal(String principal) {
            this.principal = principal;
            return this;
        }

        public RealmBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RealmBuilder scheme(AuthScheme scheme) {
            this.scheme = scheme;
            return this;
        }

        public RealmBuilder realmName(String realmName) {
            this.realmName = realmName;
            return this;
        }

        public RealmBuilder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public RealmBuilder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public RealmBuilder response(String response) {
            this.response = response;
            return this;
        }

        public RealmBuilder opaque(String opaque) {
            this.opaque = opaque;
            return this;
        }

        public RealmBuilder qop(String qop) {
            if (isNonEmpty(qop)) {
                this.qop = qop;
            }
            return this;
        }

        public RealmBuilder nc(String nc) {
            this.nc = nc;
            return this;
        }

        public RealmBuilder uri(Uri uri) {
            this.uri = uri;
            return this;
        }

        public RealmBuilder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public RealmBuilder usePreemptiveAuth(boolean usePreemptiveAuth) {
            this.usePreemptive = usePreemptiveAuth;
            return this;
        }

        public RealmBuilder useAbsoluteURI(boolean useAbsoluteURI) {
            this.useAbsoluteURI = useAbsoluteURI;
            return this;
        }

        public RealmBuilder omitQuery(boolean omitQuery) {
            this.omitQuery = omitQuery;
            return this;
        }

        public RealmBuilder charset(Charset charset) {
            this.charset = charset;
            return this;
        }
        
        private String parseRawQop(String rawQop) {
            String[] rawServerSupportedQops = rawQop.split(",");
            String[] serverSupportedQops = new String[rawServerSupportedQops.length];
            for (int i = 0; i < rawServerSupportedQops.length; i++) {
                serverSupportedQops[i] = rawServerSupportedQops[i].trim();
            }

            // prefer auth over auth-int
            for (String rawServerSupportedQop : serverSupportedQops) {
                if (rawServerSupportedQop.equals("auth"))
                    return rawServerSupportedQop;
            }

            for (String rawServerSupportedQop : serverSupportedQops) {
                if (rawServerSupportedQop.equals("auth-int"))
                    return rawServerSupportedQop;
            }

            return null;
        }

        public RealmBuilder parseWWWAuthenticateHeader(String headerLine) {
            realmName(match(headerLine, "realm"))//
                    .nonce(match(headerLine, "nonce"))//
                    .opaque(match(headerLine, "opaque"))//
                    .scheme(isNonEmpty(nonce) ? AuthScheme.DIGEST : AuthScheme.BASIC);
            String algorithm = match(headerLine, "algorithm");
            if (isNonEmpty(algorithm)) {
                algorithm(algorithm);
            }

            // FIXME qop is different with proxy?
            String rawQop = match(headerLine, "qop");
            if (rawQop != null) {
                qop(parseRawQop(rawQop));
            }
            
            return this;
        }

        public RealmBuilder parseProxyAuthenticateHeader(String headerLine) {
            realmName(match(headerLine, "realm"))//
                    .nonce(match(headerLine, "nonce"))//
                    .opaque(match(headerLine, "opaque"))//
                    .scheme(isNonEmpty(nonce) ? AuthScheme.DIGEST : AuthScheme.BASIC);
            String algorithm = match(headerLine, "algorithm");
            if (isNonEmpty(algorithm)) {
                algorithm(algorithm);
            }
            // FIXME qop is different with proxy?
            qop(match(headerLine, "qop"));
            
            return this;
        }

        private void newCnonce(MessageDigest md) {
            byte[] b = new byte[8];
            ThreadLocalRandom.current().nextBytes(b);
            b = md.digest(b);
            cnonce = toHexString(b);
        }

        /**
         * TODO: A Pattern/Matcher may be better.
         */
        private String match(String headerLine, String token) {
            if (headerLine == null) {
                return null;
            }

            int match = headerLine.indexOf(token);
            if (match <= 0)
                return null;

            // = to skip
            match += token.length() + 1;
            int trailingComa = headerLine.indexOf(",", match);
            String value = headerLine.substring(match, trailingComa > 0 ? trailingComa : headerLine.length());
            value = value.length() > 0 && value.charAt(value.length() - 1) == '"' ? value.substring(0, value.length() - 1) : value;
            return value.charAt(0) == '"' ? value.substring(1) : value;
        }

        private byte[] md5FromRecycledStringBuilder(StringBuilder sb, MessageDigest md) {
            md.update(StringUtils.charSequence2ByteBuffer(sb, ISO_8859_1));
            sb.setLength(0);
            return md.digest();
        }

        private byte[] secretDigest(StringBuilder sb, MessageDigest md) {

            sb.append(principal).append(':').append(realmName).append(':').append(password);
            byte[] ha1 = md5FromRecycledStringBuilder(sb, md);

            if (algorithm == null || algorithm.equals("MD5")) {
                return ha1;
            } else if ("MD5-sess".equals(algorithm)) {
                appendBase16(sb, ha1);
                sb.append(':').append(nonce).append(':').append(cnonce);
                return md5FromRecycledStringBuilder(sb, md);
            }

            throw new UnsupportedOperationException("Digest algorithm not supported: " + algorithm);
        }

        private byte[] dataDigest(StringBuilder sb, String digestUri, MessageDigest md) {

            sb.append(methodName).append(':').append(digestUri);
            if ("auth-int".equals(qop)) {
                sb.append(':').append(EMPTY_ENTITY_MD5);

            } else if (qop != null && !qop.equals("auth")) {
                throw new UnsupportedOperationException("Digest qop not supported: " + qop);
            }

            return md5FromRecycledStringBuilder(sb, md);
        }

        private void appendDataBase(StringBuilder sb) {
            sb.append(':').append(nonce).append(':');
            if ("auth".equals(qop) || "auth-int".equals(qop)) {
                sb.append(nc).append(':').append(cnonce).append(':').append(qop).append(':');
            }
        }

        private void newResponse(MessageDigest md) {
            // BEWARE: compute first as it used the cached StringBuilder
            String digestUri = AuthenticatorUtils.computeRealmURI(uri, useAbsoluteURI, omitQuery);

            StringBuilder sb = StringUtils.stringBuilder();

            // WARNING: DON'T MOVE, BUFFER IS RECYCLED!!!!
            byte[] secretDigest = secretDigest(sb, md);
            byte[] dataDigest = dataDigest(sb, digestUri, md);

            appendBase16(sb, secretDigest);
            appendDataBase(sb);
            appendBase16(sb, dataDigest);

            byte[] responseDigest = md5FromRecycledStringBuilder(sb, md);
            response = toHexString(responseDigest);
        }

        private static String toHexString(byte[] data) {
            StringBuilder buffer = StringUtils.stringBuilder();
            for (int i = 0; i < data.length; i++) {
                buffer.append(Integer.toHexString((data[i] & 0xf0) >>> 4));
                buffer.append(Integer.toHexString(data[i] & 0x0f));
            }
            return buffer.toString();
        }

        private static void appendBase16(StringBuilder buf, byte[] bytes) {
            int base = 16;
            for (byte b : bytes) {
                int bi = 0xff & b;
                int c = '0' + (bi / base) % base;
                if (c > '9')
                    c = 'a' + (c - '0' - 10);
                buf.append((char) c);
                c = '0' + bi % base;
                if (c > '9')
                    c = 'a' + (c - '0' - 10);
                buf.append((char) c);
            }
        }

        /**
         * Build a {@link Realm}
         * 
         * @return a {@link Realm}
         */
        public Realm build() {

            // Avoid generating
            if (isNonEmpty(nonce)) {
                MessageDigest md = DIGEST_TL.get();
                newCnonce(md);
                newResponse(md);
            }

            return new Realm(scheme, principal, password, realmName, nonce, algorithm, response, qop, nc, cnonce, uri, methodName, usePreemptive, ntlmDomain, charset, ntlmHost,
                    opaque, useAbsoluteURI, omitQuery);
        }
    }
}
