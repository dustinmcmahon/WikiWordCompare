package mcmahon.wikiWordCompare;

import java.io.IOException;
//import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParsePage {
    String url;
    String title;
    Document website;
    ObjFreqHashMap wordMap = new ObjFreqHashMap();

    public ParsePage (String url) {
        this.url = url;
        try{
            website = Jsoup.connect(url).get();
            title = website.title();
            parseWebToMap();
        } catch (IOException e){
            e.printStackTrace();
            website = null;
            url += ": URL BROKEN";
            title = "Broken Page";
        }
    }

    private void parseWebToMap(){
        cleanUnparsable();
        Elements paragraphs = getParagraphs();

        String tempString;
        String[] pWords;
        for (Element e : paragraphs){
            tempString = e.text();
            if(tempString.length() != 0){
                //remove some punctuation
                tempString = removeExtraChars(tempString);

                pWords = tempString.split(" ");
                for (String word : pWords){
                    if(!extraWord(word)){
                        wordMap.add(word.toLowerCase());
                    }
                }
            }
        }
        wordMap.printAll();
    }

    private Elements getParagraphs(){
        return website.select(".mw-parser-output p");
    }

    /**
     * This function is to remove some of the portions that will not parse well
     * currently actions:
     * * replace '<math*</math>' with 'MathFunction'
     * * removing '<sup*</sup>'
     */
    private void cleanUnparsable(){
        for(Element e: website.select("math")){
            e.html("MathFunction");
        }
        for(Element e: website.select("sup")){
            e.remove();
        }
    }

    /**
     * removes punctuation from the target string
     * @param theString
     * @return
     */
    private String removeExtraChars(String theString){
        String tempString = theString;

        tempString = tempString.replace(".", "");
        tempString = tempString.replace(",", "");
        tempString = tempString.replace(":", "");
        tempString = tempString.replace(";", "");
        tempString = tempString.replace("!", "");
        tempString = tempString.replace("?", "");
        tempString = tempString.replace(")", "");
        tempString = tempString.replace("(", "");
        tempString = tempString.replace("'s", "");

        return tempString;
    }

    /**
     * returns true if the word is considered 'extra'
     * @param word
     * @return
     */
    private boolean extraWord(String word){
        boolean result = false;

        if(word.equalsIgnoreCase("a")) return true;
        if(word.equalsIgnoreCase("an")) return true;
        if(word.equalsIgnoreCase("the")) return true;
        if(word.equalsIgnoreCase("to")) return true;
        if(word.equalsIgnoreCase("is")) return true;
        if(word.equalsIgnoreCase("like")) return true;
        if(word.equalsIgnoreCase("this")) return true;
        if(word.equalsIgnoreCase("of")) return true;
        if(word.equalsIgnoreCase("and")) return true;

        return result;
    }
}
