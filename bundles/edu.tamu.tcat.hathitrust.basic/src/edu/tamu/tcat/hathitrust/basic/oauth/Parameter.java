package edu.tamu.tcat.hathitrust.basic.oauth;

public interface Parameter extends Comparable<Parameter>
{
   String getKey();

   String getValue();

   String format();
}
