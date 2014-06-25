package com.google.cloud.backend.spi;

import com.google.appengine.api.images.Image;

/**
 * Interface for a image transformer.
 */
public interface ImageTransformer {

  /**
   * Applies some transformations to the old image and returns the transformed image.
   *
   * @param oldImage
   * @return
   */
  public Image transform(Image oldImage);
}
