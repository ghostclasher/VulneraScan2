package com.example.SpringREST.util.AppUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;


public class AppUtil {
    static String  PATH="src\\main\\resources\\static\\uploads\\";
    public static String get_photo_upload_path(String fileName,long album_id,String folder_name)throws IOException{
        String path="src\\main\\resources\\static\\uploads\\"+album_id +"\\" + folder_name;
        Files.createDirectories(Paths.get(path));
        return new File(path).getAbsolutePath() + "\\" + fileName;
    }

    // method for thumbnail creation.
    public static BufferedImage getThumbnail(MultipartFile originalFile,Integer width) throws IOException{
        BufferedImage thumbImg=null;
        BufferedImage img=ImageIO.read(originalFile.getInputStream());
        thumbImg=Scalr.resize(img,Scalr.Method.AUTOMATIC,Scalr.Mode.AUTOMATIC,width,Scalr.OP_ANTIALIAS);
        return thumbImg;
    }


public static Resource getFileAsResource(long album_id, String folder_name, String originalFileName) throws IOException {
    // Decode filename in case it has URL-encoded characters
    String decodedFileName = URLDecoder.decode(originalFileName, StandardCharsets.UTF_8);

    String folderPath = "src" + File.separator + "main" + File.separator + "resources" + File.separator +
                        "static" + File.separator + "uploads" + File.separator + album_id + File.separator + folder_name;

    File folder = new File(folderPath);
    if (!folder.exists()) return null;

    // Try to find the actual file (e.g., UUID prefix + original name)
    File[] matchingFiles = folder.listFiles((dir, name) -> name.endsWith(decodedFileName));
    if (matchingFiles != null && matchingFiles.length > 0) {
        File file = matchingFiles[0];
        System.out.println("Resolved actual file: " + file.getAbsolutePath());
        Path path = Paths.get(file.getAbsolutePath());
        return new UrlResource(path.toUri());
    } else {
        System.out.println("No matching file found in folder: " + folderPath);
        return null;
    }
}


// method to delete file from given path
public static boolean delete_photo_from_path(String filename,String folder_name,long album_id){
    try {
        File f=new File(PATH + album_id + "\\" + folder_name + "\\" +  filename);
        if(f.delete()){
            return true;
        }
        else{
            return false;
        }
    } catch (Exception e) {
         e.printStackTrace();
         return false;
    }

}




}
