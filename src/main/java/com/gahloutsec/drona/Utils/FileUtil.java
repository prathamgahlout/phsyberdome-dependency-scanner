

package com.gahloutsec.drona.Utils;

import com.gahloutsec.drona.licensedetector.LicenseDetector;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
            sortListOfFilesToKeepDirectoriesAtEnd(arr);
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
    
    private static void sortListOfFilesToKeepDirectoriesAtEnd(File[] arr){
        Arrays.sort(arr, new Comparator<File>(){
            @Override
            public int compare(File o1, File o2) {
                if(o1.isDirectory() && o2.isDirectory()){
                    return 0;
                }else if(o1.isDirectory()){
                    return 1;
                }else if(o2.isDirectory()){
                    return -1;
                }else{
                    return 0;
                }
            }
            
        });
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
                    
                    File file = FileSystems.getDefault().getPath(cloneLocation + FilenameUtils.getName(url.getPath())).toFile();
                    if(file.exists()) {
                        deleteDirectory(file);
                    }
                    FileUtils.copyURLToFile(url, file);
                    String uuid = UUID.randomUUID().toString();
                    if(FilenameUtils.getExtension(url.getPath()).equals("zip")){
                        extractZipFolder(file.toPath().toString(), cloneLocation + uuid);
                    }else if(FilenameUtils.getExtension(url.getPath()).equals("tgz")){
                        extractTarball(file.toPath().toString(), cloneLocation + uuid);
                    }
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
    
    
    
    public static void extractZipFolder(String zipFile,String extractFolder) 
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
    // https://stackoverflow.com/questions/11431143/how-to-untar-a-tar-file-using-apache-commons
    
    public static void extractTarball(String tarfile,String toPath) throws FileNotFoundException{
        
        File tarFile = FileSystems.getDefault().getPath(tarfile).toFile();
        File dest = FileSystems.getDefault().getPath(toPath).toFile();
        
        dest.mkdir();
        try{
            TarArchiveInputStream tarIn = null;

            tarIn = new TarArchiveInputStream(
                        new GzipCompressorInputStream(
                            new BufferedInputStream(
                                new FileInputStream(
                                    tarFile
                                )
                            )
                        )
                    );

            TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
            // tarIn is a TarArchiveInputStream
            while (tarEntry != null) {// create a file with the same name as the tarEntry
                File destPath = new File(dest, tarEntry.getName());
                if (tarEntry.isDirectory()) {
                    destPath.mkdirs();
                } else {
                    if (!destPath.getParentFile().exists()) {                     
                        destPath.getParentFile().mkdirs(); 
                    }                
                    destPath.createNewFile();
                    //byte [] btoRead = new byte[(int)tarEntry.getSize()];
                    byte [] btoRead = new byte[1024];
                    //FileInputStream fin 
                    //  = new FileInputStream(destPath.getCanonicalPath());
                    BufferedOutputStream bout = 
                        new BufferedOutputStream(new FileOutputStream(destPath));
                    int len = 0;

                    while((len = tarIn.read(btoRead)) != -1)
                    {
                        bout.write(btoRead,0,len);
                    }

                    bout.close();
                    btoRead = null;

                }
                tarEntry = tarIn.getNextTarEntry();
            }
            tarIn.close();
        }catch(IOException e){
            System.out.println("Failed to untar file " + tarfile);
        }

    }
    
    public static void cleanup(){
        String loc = "/.drona/";
        File file = FileSystems.getDefault().getPath(loc).toFile();
        deleteDirectory(file);
    }

}
