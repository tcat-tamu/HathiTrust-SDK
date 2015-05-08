package edu.tamu.tcat.pairtree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PairtreeHelper
{
   //    protected static final Pairtree pairtree = new Pairtree();
   protected static final Pattern PairtreeFilePartsRegex = Pattern.compile("(?<libid>[^/]+)/pairtree_root/(?<ppath>.+)/(?<cleanid>[^/]+)\\.[^.]+$");

   /**
    * Parses an HTRC pairtree file path into a {@link PairtreeDocument} that can be used
    * to extract metadata about the document
    *
    * @param filePath The pairtree file path
    * @return The {@link PairtreeDocument}
    * @throws InvalidPairtreePathException Thrown if the given filePath is not a valid pairtree path
    */
   public static PairtreeDocument parse(String filePath) throws InvalidPairtreePathException
   {
      Matcher pairtreeFilePartsMatcher = PairtreeFilePartsRegex.matcher(filePath);
      if (!pairtreeFilePartsMatcher.find())
         throw new InvalidPairtreePathException(String.format("%s is not a valid HTRC pairtree file path", filePath));

      String libraryId = pairtreeFilePartsMatcher.group("libid");
      String ppath = pairtreeFilePartsMatcher.group("ppath");
      String cleanId = pairtreeFilePartsMatcher.group("cleanid");
      String uncleanId = Pairtree.uncleanId(cleanId);

      return new PairtreeDocument(filePath, libraryId, uncleanId, cleanId, ppath);
   }

   /**
    * Parses an HTRC pairtree file into a {@link PairtreeDocument} that can be used
    * to extract metadata about the document
    *
    * @param file The pairtree file
    * @return The {@link PairtreeDocument}
    * @throws IOException Thrown if the canonical path of the given file cannot be resolved
    * @throws InvalidPairtreePathException Thrown if the given filePath is not a valid pairtree path
    */
   public static PairtreeDocument parse(File file) throws IOException, InvalidPairtreePathException
   {
      return parse(file.getCanonicalPath());
   }

   /**
    * Converts an unclean id to a clean id<br>
    * Note: clean ids can be used as valid file or path names, while unclean ids cannot be used for such purposes
    *
    * @param uncleanId The unclean id
    * @return The clean id
    */
   public static String cleanId(String uncleanId)
   {
      return Pairtree.cleanId(uncleanId);
   }

   /**
    * Converts a clean id to an unclean id <br>
    * Note: clean ids can be used as valid file or path names, while unclean ids cannot be used for such purposes
    *
    * @param cleanId The clean id
    * @return The unclean id
    */
   public static String uncleanId(String cleanId)
   {
      return Pairtree.uncleanId(cleanId);
   }

   /**
    * Constructs the pairtree path associated with the given HTRC clean id
    *
    * @param htrcCleanId The HTRC clean id
    * @return The pairtree path
    * @throws InvalidHtrcIdException Thrown if the given id is not a valid HTRC clean id
    */
   public static String getPathFromCleanId(String htrcCleanId) throws InvalidHtrcIdException
   {
      int index = htrcCleanId.indexOf('.');
      if (index == -1)
         throw new InvalidHtrcIdException(String.format("%s is not a valid HTRC clean id", htrcCleanId));

      String libId = htrcCleanId.substring(0, index);
      String cleanId = htrcCleanId.substring(index + 1);
      String uncleanId = Pairtree.uncleanId(cleanId);
      Path ppath = Pairtree.mapToPPath(uncleanId);

      return String.format("%s/pairtree_root/%s/%s/", libId, ppath, cleanId);
   }

   /**
    * Constructs the pairtree path associated with the given HTRC unclean id
    *
    * @param htrcUncleanId The HTRC unclean id
    * @return The pairtree path
    * @throws InvalidHtrcIdException Thrown if the given id is not a valid HTRC unclean id
    */
   public static String getPathFromUncleanId(String htrcUncleanId) throws InvalidHtrcIdException
   {
      int index = htrcUncleanId.indexOf('.');
      if (index == -1)
         throw new InvalidHtrcIdException(String.format("%s is not a valid HTRC unclean id", htrcUncleanId));

      String libId = htrcUncleanId.substring(0, index);
      String uncleanId = htrcUncleanId.substring(index + 1);
      String cleanId = Pairtree.cleanId(uncleanId);
      Path ppath = Pairtree.mapToPPath(uncleanId);

      return String.format("%s/pairtree_root/%s/%s/", libId, ppath, cleanId);
   }

   /**
    * A class representing an HTRC pairtree document
    */
   public static class PairtreeDocument
   {
      private final String _documentPath;
      private final String _libraryId;
      private final String _cleanId;
      private final String _uncleanId;
      private final String _ppath;

      private PairtreeDocument(String documentPath, String source, String uncleanId, String cleanId, String ppath)
      {
         _documentPath = documentPath;
         _libraryId = source;
         _uncleanId = uncleanId;
         _cleanId = cleanId;
         _ppath = ppath;
      }

      /**
       * Returns the document path for this pairtree document
       *
       * @return The document path
       */
      public String getDocumentPath()
      {
         return _documentPath;
      }

      /**
       * Returns the library identifier for the source library that provided this document
       *
       * @return The source library id
       */
      public String getLibraryId()
      {
         return _libraryId;
      }

      /**
       * Returns the HTRC clean id for this document <br>
       * Note: clean ids can be used as valid file or path names, while unclean ids cannot be used for such purposes
       *
       * @return The HTRC clean id
       */
      public String getCleanId()
      {
         return String.format("%s.%s", _libraryId, _cleanId);
      }

      /**
       * Returns a clean id without the library identifier prefix
       *
       * @return A clean id without the library identifier prefix
       */
      public String getCleanIdWithoutLibId()
      {
         return _cleanId;
      }

      /**
       * Returns the HTRC unclean id for this document <br>
       * Note: clean ids can be used as valid file or path names, while unclean ids cannot be used for such purposes
       *
       * @return The HTRC unclean id
       */
      public String getUncleanId()
      {
         return String.format("%s.%s", _libraryId, _uncleanId);
      }

      /**
       * Returns an unclean id without the library identifier prefix
       *
       * @return An unclean id without the library identifier prefix
       */
      public String getUncleanIdWithoutLibId()
      {
         return _uncleanId;
      }

      /**
       * Returns the non-HTRC pairtree parent path of this document
       *
       * @return The non-HTRC pairtree parent path
       */
      public String getPpath()
      {
         return _ppath;
      }

      @Override
      public boolean equals(Object other)
      {
         if (this == other)
            return true;
         if (other == null || !(other instanceof PairtreeDocument))
            return false;

         PairtreeDocument document = (PairtreeDocument)other;
         return _cleanId.equals(document._cleanId);
      }

      @Override
      public int hashCode()
      {
         return _cleanId.hashCode();
      }

      @Override
      public String toString()
      {
         return String.format("%s(uncleanId: %s, cleanId: %s, doc: %s)",
                              getClass().getSimpleName(), _uncleanId, _cleanId, _documentPath);
      }
   }

   public static void main(String[] args) throws InvalidPairtreePathException
   {
      final String NO_HEADER_ARG = "--no-header";

      if (args.length < 1 || args.length > 2)
         showUsageAndExit();

      boolean showHeader = true;
      String filePath = null;

      if (args.length == 2 && args[1].equals(NO_HEADER_ARG))
      {
         showHeader = false;
         filePath = args[0];
      }

      else

      if (args.length == 2 && args[0].equals(NO_HEADER_ARG))
      {
         showHeader = false;
         filePath = args[1];
      }

      else

      if (args.length == 1)
      {
         if (args[0].equals(NO_HEADER_ARG))
            showUsageAndExit();

         filePath = args[0];
      }

      if (filePath == null)
         showUsageAndExit();

      try
      {
         File file = new File(filePath);
         PairtreeDocument document = parse(file);
         String uncleanId = document.getUncleanId();
         String cleanId = document.getCleanId();
         String libraryId = document.getLibraryId();
         String name = file.getName();

         if (showHeader)
         {
            String fmtUncleanId = String.format("%%-%ds", uncleanId.length());
            String fmtCleanId = String.format("%%-%ds", cleanId.length());
            String fmtLibId = String.format("%%-%ds", libraryId.length());
            String fmtName = String.format("%%-%ds", name.length());
            String fmt = String.format("%s %s %s %s", fmtUncleanId, fmtCleanId, fmtLibId, fmtName);
            System.out.println(String.format(fmt, "uncleanId", "cleanId", "lib", "file"));
         }

         System.out.println(String.format("%s %s %s %s", uncleanId, cleanId, libraryId, name));
      }
      catch (IOException e)
      {
         System.err.println(e.getMessage());
         System.exit(1);
      }
   }

   private static void showUsageAndExit()
   {
      System.out.println(String.format("Usage: %s <pairtree_file> [--no-header]", PairtreeHelper.class.getSimpleName()));
      System.exit(-1);
   }
}
