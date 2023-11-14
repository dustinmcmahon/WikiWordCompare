package mcmahon.wikiWordCompare;

import java.io.IOException;
//import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParsePage implements java.io.Serializable {
    String url;
    String title;
    ObjFreqHashMap wordMap = new ObjFreqHashMap();

    public ParsePage (String url) {
        this.url = url;
        try{
            Document website = Jsoup.connect(url).get();
            title = website.title();
            parseWebToMap(website);
            wordMap.generateTF();
            
            //wordMap.printAll();
        } catch (IOException e){
            e.printStackTrace();
            url += ": URL BROKEN";
            title = "Broken Page";
        }
    }

    private void writeObject(ObjectOutputStream s) {
        try {
            s.writeObject(url);
            s.writeObject(title);
            s.writeObject(wordMap);
        } catch(IOException e){
            System.out.println("Could not write ParsePage");
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream s) {
        //s.defaultReadObject();
        try{
            url = (String)s.readObject();
            title = (String)s.readObject();
            wordMap = (ObjFreqHashMap)s.readObject();
        } catch(IOException e){
            System.out.println("Could not read ParsePage");
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            System.out.println("Could not find class");
            e.printStackTrace();
        }
    }

    private void parseWebToMap(Document website){
        Elements paragraphs = getParagraphs(website);
        paragraphs = cleanUnparsable(paragraphs);
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

    private Elements getParagraphs(Document website){
        return website.select(".mw-parser-output p");
    }

    /**
     * This function is to remove some of the portions that will not parse well
     * currently actions:
     * * replace '<math*</math>' with 'MathFunction'
     * * removing '<sup*</sup>'
     */
    private Elements cleanUnparsable(Elements paragraphs){
        for(Element e: paragraphs.select("math")){
            e.html(" MathFunction ");
        }
        for(Element e: paragraphs.select("sup")){
            e.remove();
        }
        return paragraphs;
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
}
