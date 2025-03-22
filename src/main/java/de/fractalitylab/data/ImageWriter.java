package de.fractalitylab.data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageWriter {

    public static void writeImage(String className, String fileName, BufferedImage image, boolean isTrainImage){
        try {
            File targetFolder;
            if(isTrainImage){
                targetFolder = new File("dataset"+File.separator+"train"+File.separator+className);
                if(!targetFolder.exists()){
                    targetFolder.mkdirs();
                }
            } else {
                targetFolder = new File("dataset"+File.separator+"test"+File.separator+className);
                if(!targetFolder.exists()){
                    targetFolder.mkdirs();
                }
            }
            ImageIO.write(image, "png", new File(targetFolder + File.separator + fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
