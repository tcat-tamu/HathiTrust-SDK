package edu.tamu.tcat.hathitrust.basic.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.base.Joiner;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

/**
 * Implements the tasks associated with building and signing requests to the HathiTrust
 * API.
 *
 *  <p>
 * Sends a signed OAuth 1.0 HTTP request using query string parameters.
 *
 * Header and Body parameters are not currently supported.
 */
public class HathiTrustAPICommandBuilder
{
   private static final String SIGNATURE_METHOD = "HMAC-SHA1";
   private static final int NONCE_BYTES = 10;
   private static final String HMAC_SHA1_SPEC = "HmacSHA1";

   private static final String OAUTH_VERSION = "1.0";

   private static final String PARAM_OAUTH_CONSUMER_KEY = "oauth_consumer_key";
   private static final String PARAM_OAUTH_NONCE = "oauth_nonce";
   private static final String PARAM_OAUTH_SIGNATURE = "oauth_signature";
   private static final String PARAM_OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
   private static final String PARAM_OAUTH_TIMESTAMP = "oauth_timestamp";
   private static final String PARAM_OAUTH_VERSION = "oauth_version";
   private static final String PARAM_REALM = "realm";

   private static final Set<String> OAUTH_PARAMETERS = new HashSet<>(Arrays.asList(
      PARAM_OAUTH_CONSUMER_KEY,
      PARAM_OAUTH_NONCE,
      PARAM_OAUTH_SIGNATURE,
      PARAM_OAUTH_SIGNATURE_METHOD,
      PARAM_OAUTH_TIMESTAMP,
      PARAM_OAUTH_VERSION
   ));

   Joiner queryParamJoiner = Joiner.on("&").skipNulls();
   Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

   // TODO find a better way to obtain one of these
   static SecureRandom prng = new SecureRandom();

   // key and secret, token (for 1.0a and more recent)
   private Multiset<Parameter> params = TreeMultiset.create();
   private URI baseUri;
   private String method;
   private String accessKey;

   private byte[] signatureKey;


   public HathiTrustAPICommandBuilder setCredentials(String accessKey, String sharedSecret)
   {
      String secret = queryParamEscaper.escape(sharedSecret);
      String token = queryParamEscaper.escape("");
      String key = queryParamJoiner.join(secret, token);

      this.accessKey = accessKey;
      this.signatureKey = key.getBytes();

      return this;
   }

   public HathiTrustAPICommandBuilder setUri(URI uri)
   {
      this.baseUri = uri;

      return this;
   }

   public HathiTrustAPICommandBuilder setMethod(String m)
   {
      this.method = m.toUpperCase();
      return this;
   }


   public HathiTrustAPICommandBuilder addParameter(Parameter param)
   {
      if (!isValidParameter(param)) {
         throw new IllegalArgumentException("Tried to set reserved request parameter [" + param + "]");
      }

      this.params.add(param);

      return this;
   }

   /**
    * Sets the body of the HTTP request. Currently unsupported.
    *
    * @return A reference to this object to support query chaining.
    */
   public HathiTrustAPICommandBuilder setBody()
   {
      throw new UnsupportedOperationException();
   }

   public HathiTrustAPICommandBuilder setHeader(String header, String value)
   {
      throw new UnsupportedOperationException();
   }

   private Multiset<Parameter> constructOAuthParams() throws OAuthException
   {
      Multiset<Parameter> oauthParams = TreeMultiset.create();

      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_CONSUMER_KEY, accessKey));
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_NONCE, generateNonce(NONCE_BYTES)));
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_SIGNATURE_METHOD, SIGNATURE_METHOD));

      long timestamp = System.currentTimeMillis() / 1000;
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_TIMESTAMP, Long.toString(timestamp)));
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_VERSION, OAUTH_VERSION));

      Multiset<Parameter> allParams = TreeMultiset.create(oauthParams);
      allParams.addAll(params);
      String signatureBase = createSignatureBaseString(allParams);

      try
      {
         String signature = computeSignature(signatureBase, signatureKey);
         oauthParams.add(SimpleParameter.create(PARAM_OAUTH_SIGNATURE, signature));
      }
      catch (SignatureException e)
      {
         throw new OAuthException("Failed to generate signature for request [" + signatureBase + "]", e);
      }

      return oauthParams;
   }


   private static String computeSignature(String signatureBase, byte[] key) throws SignatureException
   {
      byte[] data;
      try
      {
         data = signatureBase.getBytes("UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new IllegalStateException("Unable to generate HMAC", e);
      }

      try
      {
         // get an hmac_sha1 key from the raw key bytes
         SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_SPEC);

         // get an hmac_sha1 Mac instance and initialize with the signing key
         Mac mac = Mac.getInstance(HMAC_SHA1_SPEC);
         mac.init(signingKey);
         byte[] rawHmac = mac.doFinal(data);
         return Base64.encodeBase64String(rawHmac);


      }
      catch (Exception e)
      {
         throw new SignatureException("Failed to generate HMAC", e);
      }
   }

   private String createSignatureBaseString(Multiset<Parameter> oAuthParams)
   {
      Multiset<Parameter> filteredOAuthParams = TreeMultiset.create();
      oAuthParams.forEach(p -> {
         if (!p.getKey().equals(PARAM_REALM)) {
            filteredOAuthParams.add(p);
         }
      });

      String url = queryParamEscaper.escape(normalizeUrl());
      String normParams = queryParamEscaper.escape(normalizeParams(filteredOAuthParams));

      return queryParamJoiner.join(method, url, normParams);
   }


   private String normalizeParams(Multiset<Parameter> oauthParams)
   {
      TreeMultiset<?> pairs = oauthParams.stream()
            .map(Parameter::format)
            .collect(Collectors.toCollection(TreeMultiset::create));

      return queryParamJoiner.join(pairs);
   }

   private String normalizeUrl()
   {
      Objects.requireNonNull(baseUri, "No base URI supplied.");

      String scheme = baseUri.getScheme();
      if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")))
         throw new IllegalStateException("Invalid request URI scheme [" + baseUri + "]");

      int port = baseUri.getPort();
      return (port <= 0 || (port == 80 || port == 443))
            ? String.format("%s://%s%s", lowercase(baseUri.getScheme()), lowercase(baseUri.getHost()), baseUri.getPath())
            : String.format("%s://%s:%d%s", lowercase(baseUri.getScheme()), lowercase(baseUri.getHost()), Integer.valueOf(port), baseUri.getPath());
   }

   private String lowercase(String value)
   {
      if (value == null)
         return "";

      return value.toLowerCase();
   }


   private String generateNonce(int len)
   {
      byte[] bytes = new byte[len];
      prng.nextBytes(bytes);

      return Hex.encodeHexString(bytes);
   }

   public Callable<HttpResponse> build() throws OAuthException
   {
      final URI uri = constructUri();
      return () -> {
         HttpGet get = new HttpGet(uri);
         HttpClient client = new DefaultHttpClient();    // TODO this is legacy HttpClient

         try
         {
            return client.execute(get);
         }
         catch (IOException ex)
         {
            throw new OAuthException("Failed to execute request [" + uri + "]", ex);
         }
      };
   }

   private URI constructUri() throws OAuthException
   {
      Multiset<Parameter> allParams = TreeMultiset.create(params);
      allParams.addAll(constructOAuthParams());
      String query = normalizeParams(allParams);

      URI uri;
      try
      {
         uri = new URI(normalizeUrl() + "?" + query);
      }
      catch (URISyntaxException ex)
      {
         throw new IllegalStateException("Failed to create URI.", ex);
      }
      return uri;
   }

   private boolean isValidParameter(Parameter param)
   {
      // This is open to attack if someone supplies signature or other
      // oauth params manually. This prevents such an attack
      return ! OAUTH_PARAMETERS.contains(param.getKey());
   }

}
