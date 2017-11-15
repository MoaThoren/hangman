package server.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class for requesting a random word from a (at start-up) loaded text file.
 */
class WordHandler {
    private String[] words;

    /**
     * Constructor starting the class by loading the dictionary.
     */
    public WordHandler() throws IOException {
        createList();
    }

    private void createList() throws IOException {
        FileReader fileReader = new FileReader(new java.io.File("words.txt"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        ArrayList<String> tempList = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            tempList.add(line);
        }
        words = tempList.toArray(new String[0]);
        fileReader.close();
    }

    /**
     * Ask for a random word in the dictionary.
     *
     * @return A <code>String</code> with a random word.
     */
    public String randomWord() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, words.length);
        return words[randomNum];
    }
}
