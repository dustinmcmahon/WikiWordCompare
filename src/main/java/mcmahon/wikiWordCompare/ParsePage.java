package mcmahon.wikiWordCompare;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ParsePage {
    String url;
    Document website;
    ObjFreqHashMap wordMap = new ObjFreqHashMap();

    public ParsePage (String url) {
        this.url = url;
        try{
            website = Jsoup.connect(url).get();
        } catch (IOException e){
            e.printStackTrace();
            website = null;
            url += ": URL BROKEN";
        }
    }
}
