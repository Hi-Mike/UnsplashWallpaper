package com.example.wallpaper;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Created by ske on 2016/11/15.
 */

public class ImageUtil {
    /**
     * Scale the image either horizontally or vertically depending on
     * which screen-dimension/image-dimension ratio is larger, so the image
     * becomes as large as the screen in one dimension and maybe bigger in the other dimension.
     * 根据长宽缩放
     */
    public static BufferedImage scaleImage(BufferedImage im, double screenWidth, double screenHeight) {
        int imWidth = im.getWidth();
        int imHeight = im.getHeight();

        // calculate screen-dimension/image-dimension for width and height
        double widthRatio = screenWidth / imWidth;
        double heightRatio = screenHeight / imHeight;

        // scale is the largest screen-dimension/image-dimension
        double scale = (widthRatio > heightRatio) ? widthRatio : heightRatio;

        System.out.println("scaleImage:scale=" + scale);

        // calculate new image dimensions which fit the screen (or makes the image bigger)
        int scWidth = (int) (imWidth * scale);
        int scHeight = (int) (imHeight * scale);

        // resize the image
        BufferedImage scaledImage = new BufferedImage(scWidth, scHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
//        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        g2d.drawImage(im, at, null);
//        使生成的图片平滑，看起来不失真
        g2d.drawImage(im.getScaledInstance(scWidth, scHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        return scaledImage;
    }  // end of scaleImage()

    /**
     * Check which image dimension (width or height) is bigger than the
     * screen, and crop it. Only one dimension, or none, will be too big.
     */
    public static BufferedImage cropImage(BufferedImage scIm, double screenWidth, double screenHeight) {
        System.out.println("cropImage======>");
        int imWidth = scIm.getWidth();
        int imHeight = scIm.getHeight();

        BufferedImage croppedImage;
        if (imWidth > screenWidth) {// image width is bigger than screen width
            croppedImage = new BufferedImage((int) screenWidth, imHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = croppedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = ((int) screenWidth - imWidth) / 2;    // crop so image center remains in the center
            g2d.drawImage(scIm, x, 0, null);
            g2d.dispose();
        } else if (imHeight > screenHeight) {//image height is bigger than screen height
            croppedImage = new BufferedImage(imWidth, (int) screenHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = croppedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = ((int) screenHeight - imHeight) / 2;     // crop so image center remains in the center
            g2d.drawImage(scIm, 0, y, null);
            g2d.dispose();
        } else   // do nothing
            croppedImage = scIm;

        return croppedImage;
    }  // end of cropImage()

    // download the image at urlStr
    public static BufferedImage getURLImage(String urlStr) {
        System.out.println("Downloading image at:\n\t" + urlStr);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new URL(urlStr));
        } catch (IOException e) {
            System.out.println("Problem downloading:" + e);
        }

        return image;
    }  // end of getURLImage()
}
