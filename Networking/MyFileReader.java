package Networking;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyFileReader implements Serializable {
    static ArrayList<Path> outList;
    static ArrayList<File> outFiles;
        // paths;
    
        public static ArrayList<File> FileReader() {
            outList = new ArrayList<>();
            outFiles = new ArrayList<>();
            try(Stream<Path> paths = Files.walk(Paths.get("Home"))) {
                outList=paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toCollection(ArrayList::new));
            } catch(IOException e) {
                System.out.println("DEBUG: Fuck Off I'm going to kill a man");
            }
            for(int i = 0; i<outList.size(); i++) {
                outFiles.add(i, outList.get(i).toFile());
            }
            
            return outFiles; 
        }
        public List<Path> getFilePaths() {
            return outList;
        }
        public ArrayList<File> getFiles() {
            return this.outFiles;
        }
        public static void main(String[] args) {
            System.out.println(FileReader().toString());
        }
}
