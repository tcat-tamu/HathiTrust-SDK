package edu.tamu.tcat.hathitrust;

import java.awt.image.BufferedImage;

public interface Page
{

   String getHOcr();
   
   String getOcr();
   
   BufferedImage getImage();
}
