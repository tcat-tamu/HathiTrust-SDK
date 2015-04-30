package edu.tamu.tcat.hathitrust.htrc.features.simple;

import java.util.Map;
import java.util.Set;

/**
 * The main abstraction for dealing with HTRC Extracted Feature data for a single volume.
 * This is a part of
 * the "simple" API, which provides direct access to the various fields and directly represents
 * a single version-set of the HTRC Extracted Feature schema.
 * <p>
 * Since the HTRC Extracted Feature data has independent schema versions for metadata,
 * basic features, and advanced features, this API represents all segments of the data over
 * some range of those versions.
 * <p>
 * This API applies to: Metadata schema version 1.2; Basic Features schema version 2.0; Advanced
 * Features schema version 2.0.
 */
public interface ExtractedFeatures extends AutoCloseable
{
   String getVolumeId();
   
   /**
    * Identifies the Metadata <tt>schemaVersion</tt> for which this "simple" HTRC Extracted Feature API applies.
    */
   static String schemaVersionMetadata = "1.2";
   /**
    * Identifies the Basic Features <tt>schemaVersion</tt> for which this "simple" HTRC Extracted Feature API applies.
    */
   static String schemaVersionBasic = "2.0";
   /**
    * Identifies the Advanced Features <tt>schemaVersion</tt> for which this "simple" HTRC Extracted Feature API applies.
    */
   static String schemaVersionAdvanced = "2.0";
   
   /**
    * Get the Metadata for the volume represented by this {@link ExtractedFeatures}. The Metadata
    * is the same across both Basic and Advanced extracted features.
    * 
    * @return The volume metadata for this {@link ExtractedFeatures}.
    */
   Metadata getMetadata();
   
   interface Metadata
   {
      /**
       * Get the main volume info as {@link ExtractedFeatures} from which this page was retrieved.
       */
      ExtractedFeatures getVolume();
      
      String title();
      //TODO: add others
   }
   
   //TODO: Page count better be the same between basic and advanced. If it is not, then need two separate
   //      pageCount accessors and the provided features data vehicle will be inconsistent in the data it pulls.
   //      But then that's the fault of whoever composed the files on disk that they don't match.
   int pageCount();
   //int pageCountBasic();
   //int pageCountAdvanced();
   
   ExtractedPageFeatures getPage(int page);
   
   interface ExtractedPageFeatures
   {
      /**
       * Get the main volume info as {@link ExtractedFeatures} from which this page was retrieved.
       */
      ExtractedFeatures getVolume();
      
      String seq();
      
      //TODO: can this be assumed to be the same for both basic and advanced?
      String dateCreated();
      //String dateCreatedBasic();
      //String dateCreatedAdvanced();
      
      int tokenCount();
      int lineCount();
      
      //TODO: add language API
      //Map<String, Number> languages();
      
      //TODO: add header API
      //ExtractedPagePartOfSpeechData getHeaderData();
      ExtractedPagePartOfSpeechData getBodyData();
      //TODO: add footer API
      //ExtractedPagePartOfSpeechData getFooterData();
   }
   
   /**
    * A data set representing part-of-speech features extracted from a page. Since a page may have multiple
    * data sets depending on whether features were extracted from the header, body, or footer, this type
    * is used to represent the uniform data structure.
    */
   interface ExtractedPagePartOfSpeechData
   {
      /**
       * Get the page features of the main volume features from which this POS data was retrieved.
       */
      ExtractedPageFeatures getPage();
      
      //TODO: is this a good API? - allows determination of what data set this is
      boolean isBody();
      boolean isHeader();
      boolean isFooter();
      
      /**
       * Get the set of unique tokens (typically words) appearing in this
       * {@link ExtractedPagePartOfSpeechData} used in POS feature extraction.
       */
      Set<String> tokens();
      
      /**
       * Get the part-of-speech codes and counts per code for the given token. Since each token may appear
       * as multiple parts of speech in a given {@link ExtractedPagePartOfSpeechData}, the
       * returned map contains all occurrences.
       * 
       * @see #tokens()
       * @see {@link PartOfSpeechCode#get(String)} to translate the raw code into a known value if possible.
       * @param token
       * @return A map from part-of-speech-code to count, or an empty map if the token has no POS data.
       *         Does not return {@code null}.
       */
      Map<String, Integer> getPosCount(String token);
      
      /**
       * Get the count of uses of the given token on the page. This count ignores part of speech and
       * effectively sums the occurrences of any part of speech for the same token.
       * 
       * @param token
       * @return The number of times the token appeared in the data. May be zero.
       */
      int getCount(String token);
   }
}

