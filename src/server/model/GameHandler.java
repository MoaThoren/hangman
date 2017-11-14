package server.model;

public class GameHandler{
    private LeaderboardAccess leaderboardAccess = new LeaderboardAccess();
    private WordHandler wh = new WordHandler();
    private String word = wh.randomWord();
    private String[] wordArray = new String[1];
    private String name = "";
    private int bodyParts = 0;
    private int score = 0;

    public GameHandler(){
        newWord();
    }

    public String newGame(String name) {
        this.name = name;
        leaderboardAccess.addScore(name);
        return "The word is " + word.length() + " characters long, meaning you have " + word.length() + " tries. (" + word + ")";
    }

    public String guessWord(String s){
        String ret = "";


        int errors = 0;
        for(int i = 0; i < s.length(); i++) {
            boolean correct = false;
            for(int j = 0; j < wordArray.length; j++){
                if(s.charAt(i) == word.charAt(j)){
                    wordArray[j] = s.substring(i, i+1);
                    correct = true;
                }
            }
            if(!correct){
                errors++;
            }
        }
        if(errors > 0){         //INCORRECT LETTER(S)
          bodyParts = bodyParts - errors;
          ret = ret + ("Oh no! You lost " + errors + " body part(s)! \nYou now have " + bodyParts + " body parts left.");
        }

        if(wordDone()){         //WORD CORRECTLY GUESSED
            score++;
            ret = ret + ("Good job! The word was " + arrToString() + "! \nCurrent score: " + score + ".");
            ret = ret + newWord();
        }
        else{                   //SHOWS STATUS
            ret = ret + (arrToString());
        }

        if(bodyParts <= 0){     //IF YOU'RE DEAD
            ret = ("Ouch! You haven't got any body parts left!");
            ret = ret + newWord();
        }

        return ret;
    }

    private Boolean wordDone(){
        for(int i = 0; i < wordArray.length ; i++) {
            if(wordArray[i].equals("-") || bodyParts <= 0) {
                return false;
            }
        }
        return true;
    }

    private String arrToString(){
        StringBuilder s = new StringBuilder();
        for (String word : wordArray) {
            s.append(word);
        }
        return s.toString();
    }

    private String newWord(){
        word = wh.randomWord().toLowerCase();
        wordArray = new String[word.length()];
        for(int i = 0; i < wordArray.length ; i++) {
            wordArray[i] = "-";
        }
        bodyParts = word.length();
        return ("\nThe new word is " + word.length() + " characters long, meaning you have " + word.length() + " tries. (" + word + ")\n");
    }
}
