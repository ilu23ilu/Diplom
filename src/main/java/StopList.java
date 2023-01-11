import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopList {

    private Set<String> stopList = new HashSet<>();

    public StopList() {
        try (BufferedReader reader = new BufferedReader(new FileReader("stop-ru.txt"))) {
            String stopWord;
            while ((stopWord = reader.readLine()) != null) {
                stopList.add(stopWord.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getStopList() {
        return stopList;
    }
}