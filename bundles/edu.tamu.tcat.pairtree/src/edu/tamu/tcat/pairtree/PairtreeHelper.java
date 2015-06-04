/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.pairtree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PairtreeHelper
{
   private static final Pattern PATH_PATTERN = Pattern.compile("(?<libid>[^/]+)/pairtree_root/(?<ppath>.+)/(?<cleanid>[^/]+)\\.[^.]+$");

   /**
    * Parses an HTRC pairtree file path into a {@link PairtreeDocumentPath} that can be used
    * to extract metadata about the document
    *
    * @param filePath The pairtree file path
    * @return A {@link PairtreeDocumentPath}
    * @throws InvalidPairtreePathException If the given filePath is not a valid pairtree path
    */
   public static PairtreeDocumentPath parse(String filePath) throws InvalidPairtreePathException
   {
      Matcher pairtreeFilePartsMatcher = PATH_PATTERN.matcher(filePath);
      if (!pairtreeFilePartsMatcher.find())
         throw new InvalidPairtreePathException("File path ["+filePath + "] is not a valid HTRC pairtree file path");

      String libraryId = pairtreeFilePartsMatcher.group("libid");
      String ppath = pairtreeFilePartsMatcher.group("ppath");
      String cleanId = pairtreeFilePartsMatcher.group("cleanid");
      String uncleanId = Pairtree.toRawDecodedId(cleanId);

      return new PairtreeDocumentPath(filePath, libraryId, uncleanId, cleanId, ppath);
   }

   /**
    * Parses an HTRC pairtree file into a {@link PairtreeDocumentPath} that can be used
    * to extract metadata about the document
    *
    * @param file The pairtree file
    * @return The {@link PairtreeDocumentPath}
    * @throws IOException Thrown if the canonical path of the given file cannot be resolved
    * @throws InvalidPairtreePathException Thrown if the given filePath is not a valid pairtree path
    */
   public static PairtreeDocumentPath parse(File file) throws IOException, InvalidPairtreePathException
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
      return Pairtree.toCleanEncodedId(uncleanId);
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
      return Pairtree.toRawDecodedId(cleanId);
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
      String uncleanId = Pairtree.toRawDecodedId(cleanId);
      Path ppath = Pairtree.toPPath(uncleanId);

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
      String cleanId = Pairtree.toCleanEncodedId(uncleanId);
      Path ppath = Pairtree.toPPath(uncleanId);

      return String.format("%s/pairtree_root/%s/%s/", libId, ppath, cleanId);
   }

   /**
    * Represents an HTRC pairtree document's abstract file path. Segments are stored
    * representing the library identifier, pairtree-path, and other data.
    */
   public static class PairtreeDocumentPath
   {
      private final String _documentPath;
      private final String _libraryId;
      private final String _cleanId;
      private final String _uncleanId;
      private final String _ppath;

      private PairtreeDocumentPath(String documentPath, String source, String uncleanId, String cleanId, String ppath)
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
         if (other == null || !(other instanceof PairtreeDocumentPath))
            return false;

         PairtreeDocumentPath document = (PairtreeDocumentPath)other;
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
      {
         showUsage();
         System.exit(-1);
         return;
      }

      boolean showHeader = true;
      String filePath = null;

      if (args.length == 2 && args[1].equals(NO_HEADER_ARG))
      {
         showHeader = false;
         filePath = args[0];
      }
      else if (args.length == 2 && args[0].equals(NO_HEADER_ARG))
      {
         showHeader = false;
         filePath = args[1];
      }
      else if (args.length == 1)
      {
         if (args[0].equals(NO_HEADER_ARG))
         {
            showUsage();
            System.exit(-1);
            return;
         }

         filePath = args[0];
      }

      if (filePath == null)
      {
         showUsage();
         System.exit(-1);
         return;
      }

      try
      {
         File file = new File(filePath);
         PairtreeDocumentPath document = parse(file);
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

   private static void showUsage()
   {
      System.out.println(String.format("Usage: %s <pairtree_file> [--no-header]", PairtreeHelper.class.getSimpleName()));
   }
}
