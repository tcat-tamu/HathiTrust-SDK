package edu.tamu.tcat.hathitrust.client;

import java.util.Set;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.hathitrust.Record.RecordIdentifier;

/**
 *  Interfaces with the HathiTrust Bibliographic API.
 *
 *  <p>For more detail about the HathiTrust Bibliographic API, see {@link http://www.hathitrust.org/bib_api}.
 *
 *  <p>
 *  This API returns bibliographic, rights, and volume information when given a single or
 *  multiple standard identifiers (ISBN, LCCN, OCLC, etc.). It is intended for use to
 *  retrieve information about small numbers of items at a time. Bulk retrieval should
 *  be done using OAI or the HathiTrust tab-delimited inventory files, as described at
 *  {@link http://www.hathitrust.org/data}. Note that use of the data may be subject to
 *  third-party agreements, such as OCLC's Record Use policy. Permission must be sought
 *  for bulk retrieval of OCLC records by non-OCLC members.
 *
 */
public interface BibliographicAPIClient
{
   Record lookup(RecordIdentifier id) throws HathiTrustClientException;

   Record lookup(Set<RecordIdentifier> id) throws HathiTrustClientException;

   /**
    * @return Indicates whether the service has been properly configured and can connect to
    *    the remote HathiTrust API.
    */
   boolean canConnect();
}
