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
package edu.tamu.tcat.hathitrust.htrc.features.simple;

/**
 * Codes for parts of speech as used by the Penn Treebank Project.
 *
 * @see https://www.cis.upenn.edu/~treebank/
 * @since 1.1
 */
public enum PartOfSpeechCode
{
   CC("Coordinating conjunction"),
   CD("Cardinal number"),
   DT("Determiner"),
   EX("Existential 'there'"),
   FW("Foreign word"),
   IN("Preposition or subordinating conjunction"),
   JJ("Adjective"),
   JJR("Adjective, comparative"),
   JJS("Adjective, superlative"),
   LS("List item marker"),
   MD("Modal"),
   NN("Noun, singular or mass"),
   NNS("Noun, plural"),
   NNP("Proper noun, singular"),
   NNPS("Proper noun, plural"),
   PDT("Predeterminer"),
   POS("Possessive ending"),
   PRP("Personal pronoun"),
   PRP$("Possessive pronoun"),
   RB("Adverb"),
   RBR("Adverb, comparative"),
   RBS("Adverb, superlative"),
   RP("Particle"),
   SYM("Symbol"),
   TO("'to'"),
   UH("Interjection"),
   VB("Verb, base form"),
   VBD("Verb, past tense"),
   VBG("Verb, gerund or present participle"),
   VBN("Verb, past participle"),
   VBP("Verb, non-3rd person singular present"),
   VBZ("Verb, 3rd person singular present"),
   WDT("Wh-determiner"),
   WP("Wh-pronoun"),
   WP$("Possessive wh-pronoun"),
   WRB("Wh-adverb");

   private final String title;

   private PartOfSpeechCode(String title)
   {
      this.title = title;
   }

   public String getTitle()
   {
      return title;
   }

   /**
    * Unchecked form of {@link #valueOf(String)} which will return {@code null} if the provided
    * key is not a valid {@link PartOfSpeechCode}. This is useful because some codes used in
    * the HTRC Extracted Features do not match to codes.
    *
    * @param code
    * @return The matching instance to the provided code, or {@code null} if no match is found.
    */
   public static PartOfSpeechCode get(String code)
   {
      try {
         return PartOfSpeechCode.valueOf(code);
      } catch (Exception e) {
         return null;
      }
   }
}
