package mcmahon.wikiWordCompare;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test {
    public static void main(String[] args){
        testGetUrl();
        //testTree();
    }

    private static void testGetUrl(){
        String IMPORT_FILE = "wikiPages1.txt";
        String RANDOM_WIKI_URL = "https://en.wikipedia.org/wiki/Special:Random";

        Document newDoc;
        File importFile = new File(IMPORT_FILE);
        try{
            FileWriter output = new FileWriter(importFile);
            for(int i = 0; i < 100; i++){
                newDoc = Jsoup.connect(RANDOM_WIKI_URL).get();
                output.write(newDoc.location() + "\n");
            }
            output.close();
        } catch (IOException e){
            System.out.println("Error with website");
            e.printStackTrace();
        }

    }

    private static void testTree(){
        ObjBTree tree = new ObjBTree(5);

        System.out.println("Number of Keys: " + tree.root.keys.length);
        System.out.println("Number of Children: " + tree.root.children.length);

        for(int i = 700; i > 0; i--){
            System.out.println("insert# " + i);
            /* for debugging purposes
            if(i == 62){
                //break here
                System.out.println(i);
            }
            */
            tree.insert(i);                  
        }
        
        System.out.println("Final Print");
        tree.root.breadthPrint();
    }
}

