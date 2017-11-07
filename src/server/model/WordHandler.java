package server.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WordHandler {
    private String[] words;

    public WordHandler() {
        createList();
    }

    private void createList() {
        try {
            FileReader fileReader = new FileReader(new java.io.File("words.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            ArrayList<String> tempList = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                tempList.add(line);
            }
            words = tempList.toArray(new String[0]);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String randomWord() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, words.length);
        return words[randomNum];
    }
}
