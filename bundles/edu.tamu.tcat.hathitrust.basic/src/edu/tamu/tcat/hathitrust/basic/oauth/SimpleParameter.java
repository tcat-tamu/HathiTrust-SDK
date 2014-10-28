package edu.tamu.tcat.hathitrust.basic.oauth;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class SimpleParameter implements Parameter
{
   private static Escaper escaper = UrlEscapers.urlFormParameterEscaper();

   private final String key;
   private final String value;

   private SimpleParameter(String key, String value)
   {
      this.key = key;
      this.value = value;
   }

   public static SimpleParameter create(String key, String value)
   {
      if (key == null || key.trim().isEmpty())
         throw new IllegalArgumentException("A non-empty key value must be supplied.");

      if (value == null)
         value = "";

      return new SimpleParameter(key, value);
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public String getValue()
   {
      return value;
   }

   @Override
   public int compareTo(Parameter p)
   {
      // TODO check for null.
      int keyCmp = key.compareTo(p.getKey());

      return keyCmp != 0 ? keyCmp : value.compareTo(p.getValue());
   }

   @Override
   public String format()
   {
      return String.format("%s=%s", escaper.escape(key), escaper.escape(value));
   }

   @Override
   public String toString()
   {
      return format();
   }
}
