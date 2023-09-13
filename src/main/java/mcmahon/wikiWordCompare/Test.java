package mcmahon.wikiWordCompare;


public class Test {
    public static void main(String[] args) {
        ParsePage thePage = new ParsePage("https://en.wikipedia.org/wiki/Hash_table");

        System.out.println("Page Title:" + thePage.title);

        //System.out.println(paragraphs.text());

    }
}
