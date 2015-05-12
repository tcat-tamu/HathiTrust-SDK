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
import java.nio.file.Path;
import java.nio.file.Paths;
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

   /**
    * All special characters (within the range of 0x21 to 0x7e) that must be hex-encoded when cleaning an identifier.
    * A single string is convenient because {@link String#indexOf(int)} may be used to detect presence rather than
    * explicit iteration.
    * 
    * @see Pairtree#toCleanEncodedId(String)
    * @see #toRawDecodedId(String)
    */
   // (coerce to str, then append)           0x22, 0x2a, 0x2b, 0x2c, 0x3c, 0x3d, 0x3e, 0x3f, 0x5c,  0x5e, 0x7c
   private static final String charStr = "" + '"' + '*' + '+' + ',' + '<' + '=' + '>' + '?' + '\\' + '^' + '|';

   public static final int DEFAULT_LENGTH = 2;

//   private char separator = File.separatorChar;
//
//   private int shortyLength = DEFAULT_LENGTH;
//
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
    * Converts an object identifier into a ppath represented by a normalized, relative {@link Path}.
    * <p>
    * Path segments are as described in the Pairtree spec
    * with each path segment being the default length of {@value #DEFAULT_LENGTH}. Returns
    * the same result as calling <tt>toPPath(objId, {@link DEFAULT_LENGTH})</tt>.
    * 
    * @param objId The object identifier to be converted to a pairtree path.
    * 
    * @return A relative {@link Path} for the corresponding object identifier.
    * @see #toPPath(String, int)
    * @see #DEFAULT_LENGTH
    */
   public static Path toPPath(String objId)
   {
      return toPPath(objId, DEFAULT_LENGTH);
   }

   /**
    * Converts an object identifier into a ppath represented by a normalized, relative {@link Path}.
    * <p>
    * Path segments are as described in the Pairtree spec
    * with each path segment being the specified length. By convention, the
    * path segment length is 2, but this may be adjusted as needed by the needs of the
    * application
    * 
    * @param objId The object identifier to be converted to a pairtree path. Must not be {@code null}.
    * @param length The length of path segments. Must be greater than 0.
    * @return A relative {@link Path} for the corresponding object identifier
    * @see #toPPath(String)
    */
   public static Path toPPath(String objId, int length)
   {
      if (objId == null || objId.trim().isEmpty())
         throw new IllegalArgumentException("Supplied id must not be null.");
      if (length <= 0)
         throw new IllegalArgumentException("Supplied path segment length [" + length + "] must be greater than 0.");

      String cleanId = toCleanEncodedId(objId);

      // Start at current directory and normalize before returning
      Path p = Paths.get(".");
      int start = 0;
      int sz = cleanId.length();
      while (start < sz)
      {
         int endSegment = Math.min(start + length, sz);
         String part = cleanId.substring(start, endSegment);
         p = p.resolve(part);
         start = endSegment;
      }
      
      // Don't forget to normalize to remove the "."
      p = p.normalize();

      return p;
   }

//   /**
//    * Convenience method to convert some string based identifier to a filesystem path with a
//    * supplied prefix (root path) and encapsulating directory to store objects related to the
//    * supplied id.
//    *
//    * @param basePath
//    * @param id
//    * @param encapsulatingDirName
//    * @return
//    */
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

//   /**
//    *
//    * @param ppath
//    * @return
//    * @throws InvalidPpathException
//    */
//   public String toId(Path ppath) throws InvalidPpathException
//   {
//      Path p = ppath.normalize();
//      String id = p.toString();
//      Path encapsulatingDir = this.extractEncapsulatingDirFromPpath(p);
//      if (encapsulatingDir != null)
//      {
//         id = encapsulatingDir.getName(encapsulatingDir.getNameCount()-1).toString();
//      }
//      id = id.replace(Character.toString(this.separator), "");
//      id = uncleanId(id);
//      return id;
//   }

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

   /**
    * Compute and return the ppath "base" portion of the provided {@link Path}. The ppath portion
    * is that sub-path (starting with the same initial segment) which follows the ppath spec,
    * making the returned {@link Path} a ppath with a proper terminal which does not include any
    * object encapsulation path segments.
    * <p>
    * If the provided {@link Path} appears to not follow the ppath spec, an empty {@link Path} is returned.
    * <p>
    * Uses {@link #DEFAULT_LENGTH}
    * 
    * @param fullPath
    * @return A ppath representing the base of the given path.
    */
   public static Path getPpathBase(Path fullPath)
   {
      return getPpathBase(fullPath, DEFAULT_LENGTH);
   }
   
   /**
    * Compute and return the ppath "base" portion of the provided {@link Path}. The ppath portion
    * is that sub-path (starting with the same initial segment) which follows the ppath spec,
    * making the returned {@link Path} a ppath with a proper terminal which does not include any
    * object encapsulation path segments.
    * <p>
    * A ppath is typically not an absolute path, so any known path prefix to the ppath directory
    * structure should be {@link Path#relativize(Path) relativized} prior to being used as an argument.
    * <p>
    * An exception is thrown if the provided {@link Path} appears to not follow the ppath spec by including
    * zero initial path segments of proper length.
    * 
    * @param fullPath
    * @param length The expected length of ppath segments.
    * @return A ppath representing the base of the given path.
    */
   public static Path getPpathBase(Path fullPath, int length)
   {
      Objects.requireNonNull(fullPath);
      fullPath = fullPath.normalize();
      
      int segments = fullPath.getNameCount();
      Path base = null;
      for (int i=0; i < segments; ++i)
      {
         Path seg = fullPath.getName(i);
         String s = seg.toString();
         
         // A ppath terminal case; starts with "pairtree"
         if (s.startsWith("pairtree"))
            break;
         // A ppath terminal case; length is larger than shorty
         if (s.length() > length)
            break;
         
         // A ppath terminal case; length is smaller than shorty, but retain the morty
         if (s.length() < length)
         {
            base = fullPath.subpath(0, i+1);
            break;
         }
         
         base = fullPath.subpath(0, i+1);
      }
      
      if (base == null)
         throw new IllegalArgumentException("Path ["+fullPath+"] contains no shorties");

      return base;
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

   /**
    * Convert a "raw" object identifier into an encoded, "cleaned", UTF-8 identifier.
    * <p>
    * This is done by first replacing characters that are known to cause problems in
    * filesystem identifiers with hex-encoded values (prefixed by {@link #HEX_INDICATOR})
    * and second by replacing certain commonly-occurring characters one-to-one.
    * <p>
    * Cleaning does not require knowlege of segment length.
    * 
    * @param rawId A raw object identifier.
    * @return The cleaned identifier as UTF-8 characters. May be equivalent to the argument if no cleaning was required.
    */
   public static String toCleanEncodedId(String rawId)
   {
      Objects.requireNonNull(rawId, "Supplied id may not be null");

      // First, convert from provided encoding to UTF-8 to ensure mapping into proper character space
      byte[] bytes;
      try
      {
         bytes = rawId.getBytes("utf-8");
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Error getting UTF-8 for raw object identifier [" + rawId + "]", e);
      }

      // First pass
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

   /**
    * Convert a "cleaned" object identifier into a decoded, "uncleaned" identifier.
    * <p>
    * This is done by first reversing the second pass of the cleaning process, namely the
    * one-to-one replacement of common characters. Then, hex-encoded values (prefixed
    * by {@link #HEX_INDICATOR}) are decoded into the actual character value.
    * <p>
    * Cleaning does not require knowlege of segment length.
    * 
    * @param cleanId A clean object identifier.
    * @return The raw identifier. May be equivalent to the argument if no encoding characters were found.
    */
   public static String toRawDecodedId(String cleanId)
   {
      String id = cleanId.replace('=', '/').replace('+', ':').replace(',', '.');
      int len = id.length();
      
      StringBuffer idBuf = new StringBuffer();
      for (int c = 0; c < len; c++)
      {
         char ch = id.charAt(c);
         if (ch != HEX_INDICATOR)
         {
            idBuf.append(ch);
            continue;
         }
         
         // not enough characters to decode hex
         if (len - c < 2)
            throw new IllegalArgumentException("Identifier to decode ["+cleanId+"] contains invalid hex sequence at "+c);
         
         String hex = id.substring(c + 1, c + 3);
         // don't care how many unicode chars this expands to. Might be UTF-16, so expand fully
         int charCode = Integer.parseInt(hex, 16);
         idBuf.appendCodePoint(charCode);
         c += 2;
      }

      return idBuf.toString();
   }
}
