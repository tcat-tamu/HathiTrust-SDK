package edu.tamu.tcat.hathitrust.model;

import edu.tamu.tcat.hathitrust.model.Record.IdType;
import edu.tamu.tcat.hathitrust.model.Record.RecordIdentifier;

/**
 * Basic implementation of a record identifier.
 */
public final class BasicRecordIdentifier implements RecordIdentifier
{
   private final IdType idType;
   private final String id;

   public BasicRecordIdentifier(IdType idType, String id) {
      this.idType = idType;
      this.id = id;
   }

   @Override
   public IdType getScheme()
   {
      return idType;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String toString()
   {
      return idType + ":" + id;
   }
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof BasicRecordIdentifier))
         return false;

      BasicRecordIdentifier other = (BasicRecordIdentifier)obj;
      return other.id.equalsIgnoreCase(id) && other.idType.equals(idType);
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + idType.hashCode();
      result = 31 * result + id.toLowerCase().hashCode();

      return result;
   }
}