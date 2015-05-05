package edu.tamu.tcat.pairtree;

/* This has been subtantially revised from the Library of Congress implementation,
 * originally retrieved from https://github.com/LibraryOfCongress/pairtree/blob/master/src/main/java/gov/loc/repository/pairtree/Pairtree.java
 * on 2015-03-25.
 * 
 * Original version is a work of the United States Government and is not subject to copyright
 * protection in the United States.
 * 
 * Revisions are Copyright Texas A&M Engineering Experiment Station, 2015
 * Released under the terms of Apache 2.0
 */
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

/**
 * Provides an implementation of the Pairtree specification v 0.1.
 * 
 * <p>
 * The pairtree algorithm maps an arbitrary UTF-8 encoded identifier string into a filesystem
 * directory path based on successive pairs of characters, and also defines the reverse
 * mapping (from pathname to identifier).
 * 
 * <p>
 * The mapping from identifier string to path has two parts. First, the string is cleaned
 * by converting characters that would be illegal or especially problemmatic in Unix or
 * Windows filesystems. The cleaned string is then split into pairs of characters, each of
 * which becomes a directory name in a filesystem path: successive pairs map to successive
 * path components until there are no characters left, with the last component being either a
 * 1- or 2-character directory name. The resulting path is known as a pairpath, or ppath
 * 
 * @see https://confluence.ucop.edu/download/attachments/14254128/PairtreeSpec.pdf
 * @see https://confluence.ucop.edu/display/Curation/PairTree
 * 
 *
 */
public class Pairtree
{
   public static final String HEX_INDICATOR = "^";

   private static final int DEFAULT_LENGTH = 2;
   
   private char separator = File.separatorChar;
   
   private int shortyLength = 2;
   
//   public int getShortyLength() {
//      return this.shortyLength;
//   }
//
//   public void setShortyLength(int length) {
//      this.shortyLength = length;
//   }
//
//   public Character getSeparator() {
//      return separator;
//   }
//
//   public void setSeparator(Character separator) {
//      this.separator = separator;
//   }
//

   /**
    * Converts an identifier into a relative {@link Path} as described in the Pairtree spec
    * with each path segment being two characters long. Returns the same result as calling
    * {@code mapToPPath(id, 2)}.
    * 
    * @param id The id to be converted to a pairtree path.
    * 
    * @return A relative {@link Path} for the corresponding id.
    * @see #mapToPPath(String)
    */
   public static Path mapToPPath(String id)
   {
      return mapToPPath(id, DEFAULT_LENGTH);
   }
   
   /**
    * Converts an identifier into a relative {@link Path} as described in the Pairtree spec.
    * 
    * @param id The id to be converted to a pairtree path. Must not be {@code null}.
    * @param length The length of path segments. Must be greater than 0. By convention, the
    *      path segment length is 2, but this may be adjusted as needed by the needs of the
    *      application
    * @return A relative {@link Path} for the corresponding id.
    * @see #mapToPPath(String)
    */
   public static Path mapToPPath(String id, int length)
   {
      if (id == null || id.trim().isEmpty())
         throw new IllegalArgumentException("Supplied id must not be null.");
      if (length <= 0)
         throw new IllegalArgumentException("Supplied path segment length [" + length + "] must be greater than 0.");
      
      String cleanId = cleanId(id);
      
      Path p = null;
      int start = 0;
      int sz = cleanId.length();
      while (start < sz)
      {
         int end = Math.min(start + length, sz);
         String part = cleanId.substring(start, end);
         p = (p == null) ? Paths.get(part) : p.resolve(part);
         
         start = end;
      }
      
      return p;
   }
   
   /**
    * Convenience method to convert some string based identifier to a filesystem path with a
    * supplied prefix (root path) and encapsulating directory to store objects related to the
    * supplied id.
    * 
    * @param basePath
    * @param id
    * @param encapsulatingDirName
    * @return
    */
//   public static String mapToPPath(String basePath, String id, String encapsulatingDirName) {
//      // TODO evaluate if needed. Seems like boilerplate that should be supplied by client
//      //      (NOTE, base path should be a path not a string)
//      return concat(basePath, mapToPPath(id), encapsulatingDirName).toString();
//   }
//
//   public String mapToId(String basepath, String ppath) throws InvalidPpathException {
//      String newPath = this.removeBasepath(basepath, ppath);
//      return this.mapToId(newPath);
//   }
   
   /**
    * 
    * @param ppath
    * @return
    * @throws InvalidPpathException
    */
   public String mapToId(String ppath) throws InvalidPpathException
   {
      String id = ppath;
      if (id.endsWith(Character.toString(this.separator))) {
         id = id.substring(0, id.length()-1);
      }
      String encapsulatingDir = this.extractEncapsulatingDirFromPpath(ppath);
      if (encapsulatingDir != null) {
         id = id.substring(0, id.length() - encapsulatingDir.length());
      }
      id = id.replace(Character.toString(this.separator), "");
      id = uncleanId(id);
      return id;
   }
   
//   public String mapToId(Path ppath) throws InvalidPpathException
//   {
//      String id = ppath;
//      if (id.endsWith(Character.toString(this.separator))) {
//         id = id.substring(0, id.length()-1);
//      }
//      String encapsulatingDir = this.extractEncapsulatingDirFromPpath(ppath);
//      if (encapsulatingDir != null) {
//         id = id.substring(0, id.length() - encapsulatingDir.length());
//      }
//      id = id.replace(Character.toString(this.separator), "");
//
//      id = this.uncleanId(id);
//      return id;
//   }

   public String extractEncapsulatingDirFromPpath(String basepath, String ppath) throws InvalidPpathException {
      String newPath = this.removeBasepath(basepath, ppath);
      return this.extractEncapsulatingDirFromPpath(newPath);
   }
   
   public String extractEncapsulatingDirFromPpath(String ppath) throws InvalidPpathException {
      assert ppath != null;
      
      //Walk the ppath looking for first non-shorty
      String[] ppathParts = ppath.split("\\" + this.separator);
      
      //If there is only 1 part
      if (ppathParts.length == 1) {
         //If part <= shorty length then no encapsulating dir
         if (ppathParts[0].length() <= this.shortyLength) {
            return null;
         }
         //Else no ppath
         else {
            throw new InvalidPpathException(MessageFormat.format("Ppath ({0}) contains no shorties", ppath));
         }
      }

      //All parts up to next to last and last should have shorty length
      for(int i=0; i < ppathParts.length-2; i++) {
         if (ppathParts[i].length() != this.shortyLength) throw new InvalidPpathException(MessageFormat.format("Ppath ({0}) has parts of incorrect length", ppath));
      }
      String nextToLastPart = ppathParts[ppathParts.length-2];
      String lastPart = ppathParts[ppathParts.length-1];
      //Next to last should have shorty length or less
      if (nextToLastPart.length() > this.shortyLength) {
         throw new InvalidPpathException(MessageFormat.format("Ppath ({0}) has parts of incorrect length", ppath));
      }
      //If next to last has shorty length
      if (nextToLastPart.length() == this.shortyLength) {
         //If last has length > shorty length then encapsulating dir
         if (lastPart.length() > this.shortyLength) {
            return lastPart;
         }
         //Else no encapsulating dir
         else {
            return null;
         }
      }
      //Else last is encapsulating dir
      return lastPart;
               
   }
   
//   private static Path concat(String... paths) {
//      if (paths == null || paths.length == 0)
//         throw new IllegalArgumentException("Invalid path sequence. Must supply a non-empty array of path segments.");
//
//      Path path = Paths.get(paths[0]);
//      for (int i = 1; i < paths.length; i++)
//      {
//         path = path.resolve(paths[i]);
//      }
//
//      return path;
//   }
   
   public String removeBasepath(String basePath, String path) {
      Objects.requireNonNull(basePath);
      Objects.requireNonNull(path);

      String newPath = path;
      if (path.startsWith(basePath)) {
         newPath = newPath.substring(basePath.length());
         if (newPath.startsWith(Character.toString(this.separator))) newPath = newPath.substring(1);
      }
      return newPath;
   }
   
   public static String cleanId(String id) {
      Objects.requireNonNull(id, "Supplied id may not be null");

      //First pass
      byte[] bytes;
      try {
         bytes = id.getBytes("utf-8");
      } catch (UnsupportedEncodingException e)  {
         throw new IllegalStateException("Error getting UTF-8 for path [" + id + "]", e);
      }
      
      StringBuffer idBuf = new StringBuffer();
      for (int c = 0; c < bytes.length; c++) {
         byte b = bytes[c];
         int i = (int)b & 0xff;
         if (i < 0x21 || i > 0x7e || chars.contains(Integer.valueOf(i)))
         {
            idBuf.append(HEX_INDICATOR).append(Integer.toHexString(i));
         }
         else
         {
            char[] chars = Character.toChars(i);
            if (chars.length != 1)
               throw new IllegalArgumentException("Could not clean supplied id. Found characters in UTF supplementary code point range.");
            
            idBuf.append(chars[0]);
         }
      }
      
      
      String result = idBuf.toString().replace('/', '=').replace(':', '+').replace('.', ',');
      return result;
   }

   private static HashSet<Integer> chars = new HashSet<>(Arrays.asList(0x22, 0x2a, 0x2b, 0x2c, 0x3c, 0x3d, 0x3e, 0x3f, 0x5c, 0x5e, 0x7c));
   
   
   /**
    * @param id
    * @return
    */
   public static String uncleanId(String id) {
      id = id.toString().replace('/', '=').replace(':', '+').replace('.', ',');
      StringBuffer idBuf = new StringBuffer();
      for(int c=0; c < id.length(); c++) {
         char ch = id.charAt(c);
         if (ch == '^') {
            //Get the next 2 chars
            String hex = id.substring(c+1, c+3);
            char[] chars = Character.toChars(Integer.parseInt(hex, 16));
            assert chars.length == 1; // TODO throw
            idBuf.append(chars[0]);
            c=c+2;
         } else {
            idBuf.append(ch);
         }
      }
      
      return idBuf.toString();
   }
   
   public static class InvalidPpathException extends Exception
   {
      private static final long serialVersionUID = 1L;

      public InvalidPpathException(String msg) {
         super(msg);
      }
   }
}
