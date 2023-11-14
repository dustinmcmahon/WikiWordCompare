package mcmahon.wikiWordCompare;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test {
    public static void main(String[] args){
        try{
            File temp = File.createTempFile("node", "obj", new File("data"));
            System.out.println(temp.getAbsolutePath());
            ObjFreqHashMap origMap = testStore(temp);
            ObjFreqHashMap newMap = testRead(temp);
            System.out.println(origMap.cosSimilarity(newMap));
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        //testGetUrl();
        //testTree();
    }

    private static ObjFreqHashMap testStore(File output) throws IOException{
        ParsePage test = new ParsePage("https://en.wikipedia.org/wiki/Blueberry_River_(Minnesota)");
        FileOutputStream out = new FileOutputStream(output);
        ObjectOutputStream outStream = new ObjectOutputStream(out);
        outStream.writeObject(test.wordMap);
        outStream.close();
        return test.wordMap;
    }

    private static ObjFreqHashMap testRead(File input) throws IOException, ClassNotFoundException{
        FileInputStream in = new FileInputStream(input);
        ObjectInputStream inStream = new ObjectInputStream(in);
        ObjFreqHashMap result = (ObjFreqHashMap)inStream.readObject();
        inStream.close();
        return result;
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

