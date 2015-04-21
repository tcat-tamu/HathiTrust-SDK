package edu.tamu.tcat.hathitrust.rights;

import java.net.URI;

/**
 *  Provides information about the intellectual property and access rights of a particular
 *  volume (Item) within HahtiTrust. Rights management within HathiTrust is a critical access
 *  of managing a large digital library that includes both in copyright and out of copyright
 *  works. This task is further complicated by requirements of various digitizers (e.g.,
 *  Google, Internet Archives, various contributing institutions) and inconsistent international
 *  copyright law.
 *
 *  <p>This interface provides a simple mechanism for representing and describing the rights
 *  of individual resources provided by HathiTrust an is intended for use within programmatic
 *  applications (e.g., to filter results that users cannot see or access) and for display
 *  purposes to aid users in understanding how they can access various resources.
 *
 *  <p>For a more detailed description of the rights access and control policy implemented
 *  within HathiTrust, please consult the documentation at
 *  {@link http://www.hathitrust.org/rights_database} and {@link http://www.hathitrust.org/access_use}.
 */
public interface RightsCode
{

   // NOTE may need to add types/support for ReasonCode, ContributingInstitution and RightsPrecedence
   //      may generalize out to support non-HT material, notions of provenance, etc.

   public enum RightsType {
      /**
       * Codes that characterize the copyright status of the volume. Examples of this type of
       * attribute are "public domain," "public domain when viewed in the U.S." and
       * "in-copyright"; each attribute is only present when appropriate. Intended to insulate
       * codes from frequent change and provide ensure accuracy in legal terms.
       */
      Copyright,

      /**
       * Codes that directly specify access control rules as distinct from characterizing
       * the volume in terms of copyright status.
       */
      Access
   }

   /**
    * @return A unique, numeric key for identifying this code.
    */
   int getId();

   /**
    * @return A URI for uniquely identifying this rights code in RDF or similar contexts. May
    *       be {@code null} if no URI is defined.
    */
   URI getUri();

   /**
    * @return A short alphabetic key for identifying this rights code.
    */
   String getKey();

   /**
    * HathiTrust identifies two types of rights status. The {@link RightsType#Copyright}
    * codes indicate the copyright status of the referenced work. The
    * {@link RightsType#Access} indicates which users may access this work depending on
    * a combination of their location and institutional affiliation.
    *
    * @return The type of this rights code.
    */
   RightsType getType();

   /**
    * @return A title for display.
    */
   String getTitle();

   /**
    * @return A detailed description of this rights code to aid users in understanding the
    *       legal and technical ramifications.
    */
   String getDescription();

}
