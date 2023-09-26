package mcmahon.wikiWordCompare;


public class Test {
    public static void main(String[] args) {
        ParsePage thePage = new ParsePage("https://en.wikipedia.org/wiki/Tf%E2%80%93idf");

        System.out.println("Page Title:" + thePage.title);
        thePage.wordMap.printAll();
        //System.out.println(paragraphs.text());

    }
}
