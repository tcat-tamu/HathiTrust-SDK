package edu.tamu.tcat.hathitrust.model;

import java.net.URI;

@Deprecated
public enum RightsCodeEnum implements RightsCode
{
   // NOTE: See http://www.hathitrust.org/access_use
   //       http://www.hathitrust.org/rights_database#Attributes
//   http://www.hathitrust.org/rights_database#RightsAssignment
   // These two definitions seem to be in conflict.
   // This looks a lot like something that should be an extension point

   // need to figure out rights determination reason code
   // http://www.hathitrust.org/rights_database#Reasons

   PublicDomain(1, "pd", RightsType.Copyright, "public domain"),
   InCopyright(2, "ic", RightsType.Copyright, "in-copyright"),
   OutOfPrint(3, "op", RightsType.Copyright, "out-of-print (implies in-copyright)"),
   Orphan(4, "orph", RightsType.Copyright, "copyright-orphaned (implies in-copyright)"),
   Undetermined(5, "und", RightsType.Copyright, "undetermined copyright status"),
   UMAll(6, "umall", RightsType.Access, "available to UM affiliates and walk-in patrons (all campuses)"),
   InCopyrightWorld(7, "ic-world", RightsType.Access, "in-copyright and permitted as world viewable by the copyright holder"),
   Nobody(8, "nobody", RightsType.Access, "available to nobody; blocked for all users"),
   PublicDomainUS(9, "pdus", RightsType.Copyright, "public domain only when viewed in the US"),
   CC_BY_3(10, "cc-by-3.0", RightsType.Copyright, "Creative Commons Attribution license, 3.0 Unported"),
   CC_BY_ND_3(11, "cc-by-nd-3.0", RightsType.Copyright, "Creative Commons Attribution-NoDerivatives license, 3.0 Unported"),
   CC_BY_NC_ND_3(12, "cc-by-nc-nd-3.0", RightsType.Copyright, "Creative Commons Attribution-NonCommercial-NoDerivatives license, 3.0 Unported"),
   CC_BY_NC_3(13, "cc-by-nc-3.0", RightsType.Copyright, "Creative Commons Attribution-NonCommercial license, 3.0 Unported"),
   CC_BY_NC_SA_3(14, "cc-by-nc-sa-3.0", RightsType.Copyright, "Creative Commons Attribution-NonCommercial-ShareAlike license, 3.0 Unported"),
   CC_BY_SA_3(15, "cc-by-sa-3.0", RightsType.Copyright, "Creative Commons Attribution-ShareAlike license, 3.0 Unported"),
   OrphanCandidate(16, "orphcand", RightsType.Copyright, "orphan candidate - in 90-day holding period (implies in-copyright)"),
   CC0(17, "cc-zero", RightsType.Copyright, "Creative Commons Zero license (implies pd)"),
   UndeterminedWorld(18, "und-world", RightsType.Access, "undetermined copyright status and permitted as world viewable by the depositor"),
   InCopyrightUS(19, "icus", RightsType.Copyright, "in copyright in the US"),
   CC_BY_4(20, "cc-by-4.0", RightsType.Copyright, "Creative Commons Attribution 4.0 International license"),
   CC_BY_ND_4(21, "cc-by-nd-4.0", RightsType.Copyright, "Creative Commons Attribution-NoDerivatives 4.0 International license"),
   CC_BY_NC_ND_4(22, "cc-by-nc-nd-4.0", RightsType.Copyright, "Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International license"),
   CC_BY_NC_4(23, "cc-by-nc-4.0", RightsType.Copyright, "Creative Commons Attribution-NonCommercial 4.0 International license"),
   CC_BY_NC_SA_4(24, "cc-by-nc-sa-4.0", RightsType.Copyright, "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International license"),
   CC_BY_SA_4(25, "cc-by-sa-4.0", RightsType.Copyright, "Creative Commons Attribution-ShareAlike 4.0 International license");
//   http://schemas.hathitrust.org/htd/2009#pd
//   http://schemas.hathitrust.org/htd/2009#pd-google
//      http://schemas.hathitrust.org/htd/2009#pd-us
//      http://schemas.hathitrust.org/htd/2009#pd-us-google
//      http://schemas.hathitrust.org/htd/2009#oa
//      http://schemas.hathitrust.org/htd/2009#oa-google
//      http://schemas.hathitrust.org/htd/2009#section108
//      http://schemas.hathitrust.org/htd/2009#ic
//      http://schemas.hathitrust.org/htd/2009#cc-by
//      http://schemas.hathitrust.org/htd/2009#cc-by-nd
//      http://schemas.hathitrust.org/htd/2009#cc-by-nc-nd
//      http://schemas.hathitrust.org/htd/2009#cc-by-nc
//      http://schemas.hathitrust.org/htd/2009#cc-by-nc-sa
//      http://schemas.hathitrust.org/htd/2009#cc-by-sa
//      http://schemas.hathitrust.org/htd/2009#cc-zero
//      http://schemas.hathitrust.org/htd/2009#und-world

   public final int id;
   public final String key;
   public final RightsType type;
   public final String description;

   private RightsCodeEnum(int id, String key, RightsType type, String description)
   {
      this.id = id;
      this.key = key;
      this.type = type;
      this.description = description;

   }

   @Override
   public int getId()
   {
      return id;
   }

   @Override
   public URI getUri()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public RightsType getType()
   {
      return type;
   }

   @Override
   public String getTitle()
   {
      return description;
   }

   @Override
   public String getDescription()
   {
      return description;
   }
}
