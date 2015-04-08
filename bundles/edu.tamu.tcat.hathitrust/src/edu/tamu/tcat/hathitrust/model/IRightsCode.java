package edu.tamu.tcat.hathitrust.model;

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
 *  within HathiTrust, please consult the {@link http://www.hathitrust.org/rights_database}.
 */
public interface IRightsCode
{

   public enum RightsType {
      Copyright, Access
   }

   /**
    * @return A unique, numeric key for identifying this code.
    */
   int getId();

   /**
    * @return A URI for uniquely identifying this rights code in RDF or similar contexts.
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
