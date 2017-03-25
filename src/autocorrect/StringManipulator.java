package autocorrect;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class StringManipulator {

    //First call to recursive permutations algorithm
    public void permutation(String str, GroupedDictionary dict, ArrayList<String> permutations) {
        permutation("", str, dict, permutations);
    }
    //Obtains all the permutations of the given word and only adds those that are valid words to the plausible list
    private void permutation(String prefix, String str, GroupedDictionary dict, ArrayList<String> permutations) {
        int n = str.length();
        if (n == 0){
            if(dict.isInDictionary(prefix))
                permutations.add(prefix);
        }
        else {
            for (int i = 0; i < n; i++)
                permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n), dict, permutations);
        }
    }


    // Adapted from:
    //https://github.com/crwohlfeil/damerau-levenshtein/blob/master/src/main/java/com/codeweasel/DamerauLevenshtein.java
    // I am using this version in order to count a letter swap as just one edit rather than two
    public int editableDif(String source, String target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
        int sourceLength = source.length();
        int targetLength = target.length();
        if (sourceLength == 0) return targetLength;
        if (targetLength == 0) return sourceLength;
        int[][] dist = new int[sourceLength + 1][targetLength + 1];
        for (int i = 0; i < sourceLength + 1; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j < targetLength + 1; j++) {
            dist[0][j] = j;
        }
        for (int i = 1; i < sourceLength + 1; i++) {
            for (int j = 1; j < targetLength + 1; j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
                if (i > 1 &&
                        j > 1 &&
                        source.charAt(i - 1) == target.charAt(j - 2) &&
                        source.charAt(i - 2) == target.charAt(j - 1)) {
                    dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + cost);
                }
            }
        }
        return dist[sourceLength][targetLength];
    }


    //Checks to see if the candidate word contains only the same letters as the given word
    public int editableDifNoDup(String a, String b) {
        //Convert each strings into an array
        char[] original = a.toCharArray();
        char[] suggested = b.toCharArray();
        //Create a set to account for duplicates
        Set<Character> originalSet = new LinkedHashSet<>();
        for(char each : original) originalSet.add(each);
        Set<Character> suggestedSet = new LinkedHashSet<>();
        for(char each : suggested) suggestedSet.add(each);
        //Reconstruct string object for editable difference comparison
        StringBuilder originalNoDup = new StringBuilder();
        for (Character character : originalSet) {originalNoDup.append(character);}
        StringBuilder suggestedNoDup = new StringBuilder();
        for (Character character : suggestedSet) {suggestedNoDup.append(character);}
        //Return editable difference between string without duplicates
        return editableDif(originalNoDup.toString(), suggestedNoDup.toString());
    }


    //Remove repeated letters in order to determine the likelihood of a misspelled word due to
    //accidentally forgetting or not knowing whether a word contains double letters
    public int editableDifNoRepeat(String a, String b){
        //Convert each strings into an array
        char[] original = a.toCharArray();
        char[] suggested = b.toCharArray();
        //Variables to store the newly built string
        String origNoRep = removeRepeatedCharacters(a);
        String suggNoRep = removeRepeatedCharacters(b);
        return editableDif(origNoRep, suggNoRep);
    }


    //Remove characters repeated in a row
    private String removeRepeatedCharacters(String word){
        //Convert each strings into an array
        char[] original = word.toCharArray();
        //Variables to store the newly built string
        StringBuilder origNoRep = new StringBuilder();
        //Remove Duplicates
        for(int i = 0; i < word.length(); i++){
            if(i < word.length()-1 && original[i] != original[i+1]) origNoRep.append(original[i]);
            else if(i == word.length()-1) origNoRep.append(original[i]);
        }
        //Return the string without repeated characters
        return origNoRep.toString();
    }


    //Correct capitalization of string
    public String checkCapitalization(String each, String correctedWord){
        //Check if original word was capitalized
        if(correctedWord != null && !each.equals(each.toLowerCase()) && !each.equals(each.toUpperCase())){
            correctedWord = correctedWord.substring(0, 1).toUpperCase() + correctedWord.substring(1);
        } else if(each.equals(each.toUpperCase())){
            correctedWord = correctedWord.toUpperCase();
        }
        return correctedWord;
    }


    //Check for valid form of plural word
    public String pluralityCheck(String word, GroupedDictionary dict){
        //If word ends in s
        if(word.charAt(word.length()-1) == 's'){
            //Try to convert word to a plural form ending in 'es'
            word = word.substring(0, word.length()-1) + "es";
            //Check if this form is a valid word
            if(dict.isInDictionary(word))
                return word;
        }
        return null;
    }
}