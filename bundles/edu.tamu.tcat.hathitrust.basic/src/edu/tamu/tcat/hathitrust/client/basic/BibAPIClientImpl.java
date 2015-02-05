package edu.tamu.tcat.hathitrust.client.basic;

import java.util.Set;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.RecordIdentifier;
import edu.tamu.tcat.hathitrust.client.BibliographicAPIClient;
import edu.tamu.tcat.hathitrust.client.HathiTrustClientException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.oss.json.JsonMapper;

public class BibAPIClientImpl implements BibliographicAPIClient
{

   public BibAPIClientImpl()
   {
      // TODO Auto-generated constructor stub
   }

   public void setConfig(ConfigurationProperties config)
   {
      throw new UnsupportedOperationException();
   }

   public void setJsonMapper(JsonMapper mapper)
   {
      throw new UnsupportedOperationException();
   }

   public void activate()
   {

   }

   public void dispose()
   {

   }

   @Override
   public Record lookup(RecordIdentifier id) throws HathiTrustClientException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Record lookup(Set<RecordIdentifier> id) throws HathiTrustClientException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean canConnect()
   {
      throw new UnsupportedOperationException();
   }
}
