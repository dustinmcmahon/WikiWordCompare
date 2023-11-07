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
            wordMap.generateTF();
            
            //wordMap.printAll();
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
                    wordMap.add(word.toLowerCase());
                }
            }
        }
        /*
        wordMap.printAll();
        System.out.println("Unique Word Count: " + wordMap.uniqueWords);
        System.out.println("Total Word Count: " + wordMap.totalWords);
         */
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
            e.html(" MathFunction ");
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

        tempString = tempString.replace("  ", " ");
        tempString = tempString.replace("-", "");
        tempString = tempString.replace("*", "");
        tempString = tempString.replace(".", "");
        tempString = tempString.replace(",", "");
        tempString = tempString.replace(":", "");
        tempString = tempString.replace(";", "");
        tempString = tempString.replace("!", "");
        tempString = tempString.replace("?", "");
        tempString = tempString.replace(")", "");
        tempString = tempString.replace("(", "");
        tempString = tempString.replace("'s", "");
        tempString = tempString.replace("\"", "");

        return tempString;
    }

    /**
     * Override from Object to ensure the correct hash is used
     */
    public int hashCode(){
        return website.hashCode();
    }
}
