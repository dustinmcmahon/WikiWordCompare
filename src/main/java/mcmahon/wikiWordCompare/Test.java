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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import mcmahon.wikiWordCompare.WikiGraph.MapNode;

public class Test {
    public static void main(String[] args){
        /* 
        File testFile = new File("testFile");

        ParsePage test1 = new ParsePage("https://en.wikipedia.org/wiki/Blueberry_River_(Minnesota)");
        ParsePage test2 = new ParsePage("https://en.wikipedia.org/wiki/Edith_Lindeman");

        long size1 = test1.insertPage(testFile, 0);
        long size2 = test2.insertPage(testFile, size1);

        ParsePage page1 = ParsePage.getPage(testFile, 0);
        ParsePage page2 = ParsePage.getPage(testFile, size1);
        
        System.out.println("First Test");
        System.out.println(test1.title);
        System.out.println(page1.title);
        System.out.println(size1);
        
        System.out.println("Second Test");
        System.out.println(test2.title);
        System.out.println(page2.title);
        System.out.println(size2);
        */

        WikiGraph graph = WikiGraph.readFromFile(new File(App.MAP_FILE), App.PAGE_COUNT);

        ArrayList<WikiGraph.MapNode> res = graph.shortestPath(2681399, 5211042);

        for(WikiGraph.MapNode n: res){
            System.out.println(n.location);
        }

        //testBB();
        /* 
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
        */
        //testGetUrl();
        //testTree();
    }

    private static void testBB(){
        File testFile = new File("testFile");
        if(!testFile.exists()) {
            try{
                testFile.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        try{
            Charset cs = Charset.forName("UTF-8");
            CharsetEncoder enc = cs.newEncoder();
            CharsetDecoder dec = cs.newDecoder();

            RandomAccessFile raFile = new RandomAccessFile(testFile, "rw");
            FileChannel fileCh = raFile.getChannel();
            
            String testString = "Test String!@#$%!";

            int len = testString.length();
            ByteBuffer bb = ByteBuffer.allocate(len);
            ByteBuffer bb2 = ByteBuffer.allocate(len);
            
            bb = enc.encode(CharBuffer.wrap(testString));
            System.out.println(fileCh.write(bb));
            bb.position(0);
            System.out.println(fileCh.write(bb));
            fileCh.position(0);

            System.out.println(fileCh.read(bb2));
            bb2.position(0);

            String testString2 = dec.decode(bb2).toString();

            System.out.println(testString);
            System.out.println(testString2);

            fileCh.close();
            raFile.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    private static ParsePage testGetBB(File input){
        return ParsePage.getPage(input, 0);
    }

    private static ParsePage testStoreBB(File output) /*throws IOException*/{
        ParsePage test = new ParsePage("https://en.wikipedia.org/wiki/Blueberry_River_(Minnesota)");
        
        System.out.println(test.insertPage(output));

        return test;
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

