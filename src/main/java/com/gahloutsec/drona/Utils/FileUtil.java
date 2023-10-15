

package com.gahloutsec.drona.Utils;

import com.gahloutsec.drona.licensedetector.LicenseDetector;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 *
 * @author Pratham Gahlout
 */
public class FileUtil {
    
    public static File searchFile(File file, String searchRegex) {
        
        if (file.isDirectory()) {
            File[] arr = file.listFiles();
            for (File f : arr) {
                File found = searchFile(f, searchRegex);
                if (found != null)
                    return found;
            }
        } else {
//            if (file.getName().equals(search)) {
//                return file;
//            }
            Pattern searchPattern = Pattern.compile(searchRegex);
            if(searchPattern.matcher(file.getName()).matches()) {
                return file;
            }
        }
        return null;
    }
    
    
    public static Path getFilePathFromURL(String path, String cloneLocation){
        if(validateRepository(path)){
                try {
                    // is git URL?
                    Path clonePath = FileSystems.getDefault().getPath(cloneLocation + path.split("//")[1]);
                    if(clonePath.toFile().exists()) {
                        deleteDirectory(clonePath.toFile());
                    }
                    // No option to set -depth?? Cloning full repo xD.. 
                    System.out.println("Cloning "+path);
                    Git git = Git.cloneRepository()
                            .setURI(path)
                            .setDirectory(clonePath.toFile())
                            .call();

                    return clonePath;
                }catch (GitAPIException ex) {
                    Logger.getLogger(LicenseDetector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                // May be this is not a git repository
                // Should I try to GET the file pointed by the URL
                System.out.println("Trying to download the file...");
                try {
                    URL url = new URL(path);
                    File file = FileSystems.getDefault().getPath(cloneLocation + UUID.randomUUID().toString() +".zip").toFile();
                    if(file.exists()) {
                        deleteDirectory(file);
                    }
                    FileUtils.copyURLToFile(url, file);
                    String uuid = UUID.randomUUID().toString();
                    extractFolder(file.toPath().toString(), cloneLocation + uuid);
                    if(file.exists()) {
                        deleteDirectory(file);
                    }
                    
                    return Paths.get(cloneLocation + uuid);
                }catch(MalformedURLException ex){
                    System.out.println("MalformedURLException for URL: "+path);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        return null;
    }
    
     public static boolean validateRepository(String repositoryURL) {
        boolean result = false;
        File file = new File("/tmp");
        Repository db;
        try {
            db = FileRepositoryBuilder.create(file);
        } catch (IOException ex) {
            return false;
        }
        Git git = Git.wrap(db);
        final LsRemoteCommand lsCmd = git.lsRemote();
        lsCmd.setRemote(repositoryURL);

        try {
            if (null != lsCmd.call()){
                result = true;
            }
        } catch (GitAPIException ex) {
            
        }
        if(file.exists()) {
            deleteDirectory(file);
        }
        return result;
    }
    
     
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    
    
    
    public static void extractFolder(String zipFile,String extractFolder) 
    {
        try
        {
            int BUFFER = 2048;
            File file = new File(zipFile);

            ZipFile zip = new ZipFile(file);
            String newPath = extractFolder;

            new File(newPath).mkdir();
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(newPath, currentEntry);
                //destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory())
                {
                    BufferedInputStream is = new BufferedInputStream(zip
                    .getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                    BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }


            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }

    }
    
    public static void cleanup(){
        String loc = "/.drona/";
        File file = FileSystems.getDefault().getPath(loc).toFile();
        deleteDirectory(file);
    }

}
