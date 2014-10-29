package edu.tamu.tcat.hathitrust.basic.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Sends a signed OAuth 1.0 HTTP request using query string parameters.
 *
 * Header and Body parameters are not allowed.
 *
 * @author matt.barry
 */
public class OAuthQueryRequest implements OAuthRequest
{
   private static final int NONCE_BYTES = 10;

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
   private OAuthCredentials cred = null;
   private Multiset<Parameter> params = TreeMultiset.create();
   private URI baseUri;
   private String method;

   private OAuthSignatureProvider signatureProvider = new HmacSha1SignatureProvider();

   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#setCredentials(edu.tamu.tcat.hathitrust.basic.oauth.OAuthCredentials)
    */
   @Override
   public OAuthRequest setCredentials(OAuthCredentials cred)
   {
      this.cred = cred;

      String secret = queryParamEscaper.escape(cred.getSecret());
      String token = queryParamEscaper.escape(cred.getToken());
      String key = queryParamJoiner.join(secret, token);
      this.signatureProvider.setKey(key.getBytes());

      return this;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#setMethod(java.lang.String)
    */
   @Override
   public OAuthRequest setMethod(String m)
   {
      this.method = m.toUpperCase();
      return this;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#setUri(java.net.URI)
    */
   @Override
   public OAuthRequest setUri(URI uri)
   {
      // TODO strip off any supplied query string and add to parameter set.
      this.baseUri = uri;

      return this;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#addParameter(edu.tamu.tcat.hathitrust.basic.oauth.Parameter)
    */
   @Override
   public OAuthRequest addParameter(Parameter param)
   {
      if (!isValidParameter(param)) {
         throw new IllegalArgumentException("Tried to set reserved request parameter [" + param + "]");
      }

      this.params.add(param);

      return this;
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#setBody()
    */
   @Override
   public OAuthRequest setBody()
   {
      throw new UnsupportedOperationException();
   }

   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#setHeader(java.lang.String, java.lang.String)
    */
   @Override
   public OAuthRequest setHeader(String header, String value)
   {
      throw new UnsupportedOperationException();
   }


   private Multiset<Parameter> getOAuthParams() throws OAuthException
   {
      Multiset<Parameter> oauthParams = TreeMultiset.create();

      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_CONSUMER_KEY, cred.getKey()));
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_NONCE, generateNonce(NONCE_BYTES)));
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_SIGNATURE_METHOD, signatureProvider.getName()));

      long timestamp = System.currentTimeMillis() / 1000;
      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_TIMESTAMP, Long.toString(timestamp)));

      oauthParams.add(SimpleParameter.create(PARAM_OAUTH_VERSION, OAUTH_VERSION));

      // FIXME open to attack if someone supplies signature or other oauth params manually. Need to prevent this.

      Multiset<Parameter> allParams = TreeMultiset.create(oauthParams);
      allParams.addAll(params);
      String signatureBase = createSignatureBaseString(allParams);

      try
      {
         String signature = computeSignature(signatureBase);
         oauthParams.add(SimpleParameter.create(PARAM_OAUTH_SIGNATURE, signature));
      }
      catch (SignatureException e)
      {
         throw new OAuthException("Failed to generate signature for request [" + signatureBase + "]", e);
      }

      return oauthParams;
   }

   private String computeSignature(String signatureBase) throws SignatureException
   {
      byte[] rawHmac;
      try {
         rawHmac = signatureProvider.getSignature(signatureBase.getBytes("UTF-8"));
         return Base64.encodeBase64String(rawHmac);
      }
      catch (UnsupportedEncodingException e) {
         throw new SignatureException("Unable to generate HMAC", e);
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
      String scheme = baseUri.getScheme();
      if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")))
      {
         throw new IllegalStateException("Request URI scheme [" + baseUri + "]");
      }

      int port = baseUri.getPort();
      return (port <= 0 || (port == 80 || port == 443))
            ? String.format("%s://%s%s", lowercase(baseUri.getScheme()), lowercase(baseUri.getHost()), baseUri.getPath())
            : String.format("%s://%s:%d%s", lowercase(baseUri.getScheme()), lowercase(baseUri.getHost()), Integer.valueOf(port), baseUri.getPath());
   }

   private String lowercase(String value)
   {
      if (value == null)
      {
         return "";
      }

      return value.toLowerCase();
   }


   private String generateNonce(int len)
   {
      byte[] bytes = new byte[len];
      prng.nextBytes(bytes);

      return Hex.encodeHexString(bytes);
   }


   /* (non-Javadoc)
    * @see edu.tamu.tcat.hathitrust.basic.oauth.OAuthRequest#execute()
    */
   @Override
   public HttpResponse execute() throws OAuthException
   {
      Multiset<Parameter> allParams = TreeMultiset.create(params);
      allParams.addAll(getOAuthParams());

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

      HttpGet get = new HttpGet(uri);

      HttpClient client = new DefaultHttpClient();
      try
      {
         return client.execute(get);
      }
      catch (IOException ex)
      {
         throw new OAuthException("Failed to execute request [" + uri + "]", ex);
      }
   }

   private boolean isValidParameter(Parameter param)
   {
      return ! OAUTH_PARAMETERS.contains(param.getKey());
   }
}