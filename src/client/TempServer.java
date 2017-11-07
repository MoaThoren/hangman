package client;

public class TempServer {
    private String[] wordArray = new String[1];
    private int bodyParts = 0;
    private int score = 0;
    WordHandler wh = new WordHandler();
    private String word = wh.randomWord();


    public TempServer(){
        System.out.println("Temp server started.");
        newWord();
        System.out.println("The word is " + word.length() + " characters long, meaning you have " + word.length() + " tries. (" + word + ")\n");
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
        if(errors > 0){
          bodyParts = bodyParts - errors;
          ret = ret + ("Oh no! You lost " + errors + " body part(s)! You now have " + bodyParts + " body parts left. \n");
        }

        if(wordDone()){
            score++;
            ret = ret + ("Good job! The word was " + arrToString() + "! \nCurrent score: " + score + ". \n");
            ret = ret + newWord();
        }
        else{
            ret = ret + (arrToString());
        }

        if(bodyParts <= 0){
            ret = ret + ("Ouch! You haven't got any body parts left!  \n");
            ret = ret + newWord();
        }

        return ret;
    }

    private Boolean wordDone(){
        for(int i = 0; i < wordArray.length ; i++) {
            if(wordArray[i] == "-"){
                return false;
            }
            if(bodyParts <= 0){
                return false;
            }
        }
        return true;
    }

    private String arrToString(){
        String s = "";
        for(int i = 0; i < wordArray.length; i++) {
            s = s + wordArray[i];
        }
        return s;
    }

    private String newWord(){
        word = wh.randomWord().toLowerCase();
        wordArray = new String[word.length()];
        for(int i = 0; i < wordArray.length ; i++) {
            wordArray[i] = "-";
        }
        bodyParts = word.length();
        return ("The word is " + word.length() + " characters long, meaning you have " + word.length() + " tries. (" + word + ")\n");
    }
}
