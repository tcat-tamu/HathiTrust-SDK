package edu.tamu.tcat.hathitrust.data;

import java.awt.image.BufferedImage;

public interface Page
{

   String getHOcr();
   
   String getOcr();
   
   BufferedImage getImage();
}
