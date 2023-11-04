package de.fractalitylab.data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageWriter {

    public static void writeImage(String className, String fileName, BufferedImage image){
        try {
            File targetFolder = new File("dataset"+File.separator+"class"+File.separator+className);
            if(!targetFolder.exists()){
                targetFolder.mkdirs();
            }
            ImageIO.write(image, "png", new File("dataset"+File.separator+"class"+File.separator+className +File.separator+ fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
