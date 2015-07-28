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

import edu.tamu.tcat.hathitrust.HathiTrustClientException;

/**
 * A provider API for "simple" {@link ExtractedFeatures}. This is intended to be implemented
 * by a client application and provided via service or some other means to allow access to
 * extracted feature data.
 * @since 1.1
 */
public interface ExtractedFeaturesProvider
{
   /**
    * Get an {@link ExtractedFeatures} for the given HTRC Volume Identifier.
    *
    * @param htrcVolumeId
    * @return An {@link ExtractedFeatures} to access data for the volume. Does not
    *         return {@code null}
    * @throws HathiTrustClientException If no volume feature metadata can be found.
    */
   ExtractedFeatures getExtractedFeatures(String htrcVolumeId) throws HathiTrustClientException;
}
