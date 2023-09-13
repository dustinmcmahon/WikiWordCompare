package mcmahon.wikiWordCompare;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {
    public static void main(String[] args) {
        ParsePage thePage = new ParsePage("https://en.wikipedia.org/wiki/Hash_table");

        System.out.println(thePage.website.title());

        Elements paragraphs = thePage.website.select(".mw-parser-output p");

        for (Element e : paragraphs) {
            Elements mathElements = e.select("math");

            // this can be done better, maybe a replace would be better and then insert a special word like 'mathFormula'
            for(Element mE : mathElements){
                mE.remove();
            }

            // this is extra reference material
            Elements supElements = e.select("sup");
            for(Element sE : supElements){
                sE.remove();
            }
            
            System.out.println(e.text());
            System.out.println("");
        }

        //System.out.println(paragraphs.text());

    }
}
