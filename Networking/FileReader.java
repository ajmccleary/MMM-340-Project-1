package Networking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FileReader {
    ArrayList<Path> outList;

    public FileReader(String filePath) {
        try(Stream<Path> paths = Files.walk(Paths.get("Home"))) {
            paths
                .filter(Files::isRegularFile)
                .forEach(System.out::println);
        } catch(IOException e) {
            System.out.println("DEBUG: Fuck Off");
        }
    }
}
