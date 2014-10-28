package edu.tamu.tcat.hathitrust.basic.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.base.Joiner;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class OAuthRequest
{

   // TODO find a better way to obtain one of these
   static SecureRandom prng = new SecureRandom();
//   = SecureRandom.getInstance("SHA1PRNG");

   // key and secret, token (for 1.0a and more recent)

   private OAuthCredentials cred = null;
   private Multiset<Parameter> params = TreeMultiset.create();
   private URI baseUri;
   private String method;

   public OAuthRequest(OAuthCredentials cred)
   {
      this.cred = cred;
      // TODO Auto-generated constructor stub
   }

   public OAuthRequest setCredentials(OAuthCredentials cred)
   {

      throw new UnsupportedOperationException();
   }

   public OAuthRequest setMethod(String m)
   {
      this.method = m.toUpperCase();
      return this;
   }

   public OAuthRequest setUri(URI uri)
   {
      // TODO strip off any supplied query string and add to parameter set.
      this.baseUri = uri;

      return this;
   }

   public OAuthRequest addParameter(Parameter param)
   {
      this.params.add(param);

      return this;
   }

   public void setBody()
   {

      throw new UnsupportedOperationException();
   }

   public void setHeader(String header, String value)
   {
      throw new UnsupportedOperationException();
   }




   private Multiset<Parameter> getOAuthParams() throws OAuthException
   {
      Multiset<Parameter> oauthParams = TreeMultiset.create();

      oauthParams.add(SimpleParameter.create("oauth_consumer_key", cred.getKey()));
      long timestamp = System.currentTimeMillis() / 1000;
      oauthParams.add(SimpleParameter.create("oauth_timestamp", "1414521913")); //Long.toString(timestamp)));

      oauthParams.add(SimpleParameter.create("oauth_nonce", generateNonce(10)));
      oauthParams.add(SimpleParameter.create("oauth_version", "1.0"));
      oauthParams.add(SimpleParameter.create("oauth_signature_method", "HMAC-SHA1"));

      // TODO add in all other parameters including header and body (form-encoded) parameters.
      // HACK for now just add query params
      // FIXME open to attack if someone supplies signature or other oauth params manually. Need to prevent this.

      Multiset<Parameter> allParams = TreeMultiset.create(oauthParams);
      allParams.addAll(params);
      String signatureBase = createSignatureBaseString(allParams);

      try
      {
         String signature = computeSignature(signatureBase);
         oauthParams.add(SimpleParameter.create("oauth_signature", signature));
      }
      catch (SignatureException e)
      {
         throw new OAuthException("Failed to generate signature for request [" + signatureBase + "]", e);
      }

      return oauthParams;
   }


   // TODO need a better name
   Joiner joiner = Joiner.on("&").skipNulls();
   Escaper escaper = UrlEscapers.urlFormParameterEscaper();
   private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

   public static byte[] computeHmacSha1(String data, String key) throws SignatureException
   {
      try {

         // get an hmac_sha1 key from the raw key bytes
         SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

         // get an hmac_sha1 Mac instance and initialize with the signing key
         Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
         mac.init(signingKey);
         return mac.doFinal(data.getBytes("UTF-8"));

      } catch (Exception e) {
         throw new SignatureException("Failed to generate HMAC", e);
      }
   }

   private String computeSignature(String signatureBase) throws SignatureException
   {
      String secret = cred.getSecret();
      String token = cred.getToken();
      String key = joiner.join(escaper.escape(secret), escaper.escape(token));

      byte[] rawHmac = computeHmacSha1(signatureBase, key);
      return Base64.encodeBase64String(rawHmac);
   }

   private String createSignatureBaseString(Multiset<Parameter> oauthParams)
   {
      // TODO possibly remove 'oauth_signature' and 'realm'
      String url = escaper.escape(normalizeUrl());
      String normParams = escaper.escape(normalizeParams(oauthParams));

      return joiner.join(method, url, normParams);
   }


   private String normalizeParams(Multiset<Parameter> oauthParams)
   {
      TreeMultiset<?> pairs = oauthParams.stream()
            .map(Parameter::format)
            .collect(Collectors.toCollection(TreeMultiset::create));

      return joiner.join(pairs);
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
            ? String.format("%s://%s%s", lc(baseUri.getScheme()), lc(baseUri.getHost()), baseUri.getPath())
            : String.format("%s://%s:%d%s", lc(baseUri.getScheme()), lc(baseUri.getHost()), Integer.valueOf(port), baseUri.getPath());
   }

   private String lc(String value)
   {
      if (value == null)
         return "";

      return value.toLowerCase();
   }


   private String generateNonce(int len)
   {
      byte[] bytes = new byte[len];
      prng.nextBytes(bytes);

//      return Hex.encodeHexString(bytes);
      return "a5b0a1806dc39919c697";
   }


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

   private void getAuthenticatedQuery(String method, URI Ruri, String token)
   {
   }

   public static void main(String[] args) throws ClientProtocolException, OAuthException, URISyntaxException, IOException
   {
      URI base = URI.create("http://babel.hathitrust.org/cgi/htdc/dapiserver");
      OAuthRequest req = new OAuthRequest(new OAuthCredentials()
      {
         @Override
         public String getToken() { return ""; }
         @Override
         public String getSecret() { return "8e4abd6a59c453b3e96509c30451"; }
         @Override
         public String getKey() { return "7a121fe33c"; }
      });

      req.setUri(base);
      req.addParameter(SimpleParameter.create("hello", "world"));

      HttpResponse execute = req.execute();
   }
}
