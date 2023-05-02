/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javampfx;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.media.Media;

/**
 *
 * @author Kuba
 */
public class Utils {
    
    public static List<Path> convertFileToPathList(List<File> fileList) {
        return fileList.stream().map(File::toPath).collect(Collectors.toList());
    }

    public static Media convertObjectToMedia(Object object) {
        Path path = Paths.get(object.toString());
        File fileFromPath = path.toFile();
        Media media = new Media(fileFromPath.toURI().toString());    
        return media;
    }
    
    
    public static String getMediaFileExtension(Media media) {
        File file = new File(media.getSource());
        int i = file.getName().lastIndexOf('.');
        String extension = "";
        if (i > 0) {
            extension = file.getName().substring(i+1);
        }
        return extension;
    }
}