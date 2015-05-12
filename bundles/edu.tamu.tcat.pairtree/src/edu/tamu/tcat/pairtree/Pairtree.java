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
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Provides an implementation of the Pairtree specification v 0.1. This class contains
 * static methods for general use or may be instantiated to work with a filesystem directory
 * structure as a Pairtree.
 * <p>
 * The pairtree algorithm maps an object identifier (UTF-8 encoded string) into a filesystem
 * directory path which is an object directory containing the files that comprise the object.
 * The mapping is defined both forward and reverse, from pathname to identifier.
 * The identifier is first encoded to avoid characters unfriendly to filesystem
 * identifiers and then segments the identifier. The segments (known as "shorty" seequences)
 * are each mapped into a directory name in a filesystem path and are typically pairs (hence the
 * name "pair tree"). Successive segments map to successive path components until there are
 * no characters left, with the last component's length being between one and the segment
 * length (such a component of shorter length than a "shorty" is known as a "morty").
 * <p>
 * The inverse mapping, from a ppath to its identifier string, recognizes the segments until
 * one of three terminal cases is met. The ppath includes one or more "shorties" ending in
 * a "shorty" or a "morty". If a "morty" is encountered, it terminates the ppath. The ppath also
 * terminates at an empty "shorty" or "morty" and is considered an empty ppath which contains no
 * object in the pairtree. Finally, a ppath terminates at any directory name beginning with
 * "pairtree". Such ppaths are reserved and do not alter the count of objects considered to
 * be present in the pairtree.
 * 
 * @see https://confluence.ucop.edu/download/attachments/14254128/PairtreeSpec.pdf
 * @see https://confluence.ucop.edu/display/Curation/PairTree
 */
public class Pairtree
{
   private static final Logger debug = Logger.getLogger(Pairtree.class.getName());

   public static final char HEX_INDICATOR = '^';

   public static final int DEFAULT_LENGTH = 2;

   private char separator = File.separatorChar;

   private int shortyLength = DEFAULT_LENGTH;
   
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
    * (i.e. a "pair path")
    * with each path segment being the default length of {@value #DEFAULT_LENGTH}. Returns
    * the same result as calling <tt>toPPath(id, {@link DEFAULT_LENGTH})</tt>.
    * 
    * @param id The id to be converted to a pairtree path.
    * 
    * @return A relative {@link Path} for the corresponding id.
    * @see #toPPath(String, int)
    */
   public static Path toPPath(String id)
   {
      return toPPath(id, DEFAULT_LENGTH);
   }

   /**
    * Converts an identifier into a relative {@link Path} as described in the Pairtree spec
    * (i.e. a "pair path").
    * 
    * @param id The id to be converted to a pairtree path. Must not be {@code null}.
    * @param length The length of path segments. Must be greater than 0. By convention, the
    *      path segment length is 2, but this may be adjusted as needed by the needs of the
    *      application
    * @return A relative {@link Path} for the corresponding id.
    * @see #toPPath(String)
    */
   public static Path toPPath(String id, int length)
   {
      if (id == null || id.trim().isEmpty())
         throw new IllegalArgumentException("Supplied id must not be null.");
      if (length <= 0)
         throw new IllegalArgumentException("Supplied path segment length [" + length + "] must be greater than 0.");

      String cleanId = cleanId(id);

      // Start at current directory and normalize before returning
      Path p = Paths.get(".");
      int start = 0;
      int sz = cleanId.length();
      while (start < sz)
      {
         int end = Math.min(start + length, sz);
         String part = cleanId.substring(start, end);
         p = p.resolve(part);
         start = end;
      }
      
      p = p.normalize();

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
   public String toId(Path ppath) throws InvalidPpathException
   {
      Path p = ppath.normalize();
      String id = p.toString();
      Path encapsulatingDir = this.extractEncapsulatingDirFromPpath(p);
      if (encapsulatingDir != null)
      {
         id = encapsulatingDir.getName(encapsulatingDir.getNameCount()-1).toString();
      }
      id = id.replace(Character.toString(this.separator), "");
      id = uncleanId(id);
      return id;
   }

   // old ver of what is directly above
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

//   public String extractEncapsulatingDirFromPpath(String basepath, String ppath) throws InvalidPpathException
//   {
//      String newPath = this.removeBasepath(basepath, ppath);
//      return this.extractEncapsulatingDirFromPpath(newPath);
//   }

   // re-evaluate necessity - refactor this use
   public Path extractEncapsulatingDirFromPpath(Path ppath) throws InvalidPpathException
   {
      Objects.requireNonNull(ppath);
      ppath = ppath.normalize();

      //Walk the ppath looking for first non-shorty
      //String[] ppathParts = ppath.split("\\" + this.separator);

      //If there is only 1 part
      if (ppath.getNameCount() == 1)
      {
         //If part <= shorty length then no encapsulating dir
         if (ppath.getName(0).toString().length() <= this.shortyLength)
            return null;
         
         //Else no ppath
         throw new InvalidPpathException(MessageFormat.format("Ppath ({0}) contains no shorties", ppath));
      }

      //All parts up to next to last and last should have shorty length
      for (int i = 0; i < ppath.getNameCount() - 2; i++)
         if (ppath.getName(i).toString().length() != this.shortyLength)
            throw new InvalidPpathException(MessageFormat.format("Ppath ({0}) has parts of incorrect length", ppath));
      
      String nextToLastPart = ppath.getName(ppath.getNameCount()-2).toString();
      String lastPart = ppath.getName(ppath.getNameCount() - 1).toString();
      //Next to last should have shorty length or less
      if (nextToLastPart.length() > this.shortyLength)
         throw new InvalidPpathException(MessageFormat.format("Ppath ({0}) has parts of incorrect length", ppath));
      
      //If next to last has shorty length
      if (nextToLastPart.length() == this.shortyLength)
      {
         //If last has length > shorty length then encapsulating dir
         if (lastPart.length() > this.shortyLength)
         {
            return ppath.subpath(0, ppath.getNameCount() -1);
         }
         //Else no encapsulating dir
         return null;
      }
      //Else last is encapsulating dir
      return ppath.subpath(0, ppath.getNameCount() -1);
   }

   // not necessary when using Path
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

   public String removeBasepath(String basePath, String path)
   {
      Objects.requireNonNull(basePath);
      Objects.requireNonNull(path);

      String newPath = path;
      if (path.startsWith(basePath))
      {
         newPath = newPath.substring(basePath.length());
         if (newPath.startsWith(Character.toString(this.separator)))
            newPath = newPath.substring(1);
      }
      return newPath;
   }

   public static String cleanId(String id)
   {
      Objects.requireNonNull(id, "Supplied id may not be null");

      //First pass
      byte[] bytes;
      try
      {
         bytes = id.getBytes("utf-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new IllegalStateException("Error getting UTF-8 for path [" + id + "]", e);
      }

      StringBuffer idBuf = new StringBuffer();
      for (int c = 0; c < bytes.length; c++)
      {
         byte b = bytes[c];
         // if out of range of printable characters OR is cleanable special character
         if ((b < 0x21 || b > 0x7e) || charStr.indexOf(b) >= 0)
         {
            idBuf.append(HEX_INDICATOR).append(Integer.toHexString(b));
            continue;
         }
         
         // if not picked up as special hex code, then it is printable as-is. It is impossible that a byte's
         // amount of precision (only regarding the positive portion, which is 0x00-0x7F) pushes the
         // character itself past the basic multilingual plane (\u0000-\u0FFF) requring dealing at all
         // with character point codes.
         idBuf.append((char)b);
      }

      String result = idBuf.toString().replace('/', '=').replace(':', '+').replace('.', ',');
      return result;
   }

   //private static HashSet<Integer> chars = new HashSet<>(Arrays.asList(0x22, 0x2a, 0x2b, 0x2c, 0x3c, 0x3d, 0x3e, 0x3f, 0x5c, 0x5e, 0x7c));
   // coerce to a string, then add other (printable) characters as values instead of codes. Retain codes above for code comparison
   private static String charStr = "" + '"' + '*' + '+' + ',' + '<' + '>' + '\\' + '^' + '|';

   /**
    * @param id
    * @return
    */
   public static String uncleanId(String id)
   {
      id = id.toString().replace('/', '=').replace(':', '+').replace('.', ',');
      StringBuffer idBuf = new StringBuffer();
      for (int c = 0; c < id.length(); c++)
      {
         char ch = id.charAt(c);
         if (ch == HEX_INDICATOR)
         {
            //Get the next 2 chars
            String hex = id.substring(c + 1, c + 3);
            char[] chars = Character.toChars(Integer.parseInt(hex, 16));
            assert chars.length == 1; // TODO throw
            idBuf.append(chars[0]);
            c = c + 2;
         }
         else
         {
            idBuf.append(ch);
         }
      }

      return idBuf.toString();
   }

   public static class InvalidPpathException extends Exception
   {
      private static final long serialVersionUID = 1L;

      public InvalidPpathException(String msg)
      {
         super(msg);
      }
   }
}
