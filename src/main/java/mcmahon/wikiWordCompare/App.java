package mcmahon.wikiWordCompare;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class App {

    static final String IMPORT_FILE = "data\\wikiPages.txt";
    static final String RANDOM_WIKI_URL = "https://en.wikipedia.org/wiki/Special:Random";
    static final String CLUSTER_FILE = "data\\clusterDataFile";
    static final String PAGE_FILE_PREFIX = "data\\pages";
    static final String PAGE_DATA = "data\\pData";
    static final String PAGE_INDEX = "data\\pIndex";
    static final int PAGE_COUNT = 100;
    static final int CLUSTER_CYCLE_COUNT = 45000;
    static final int CLUSTER_NODE_COUNT = 8;
    
    static GUI gui;
    static SimCluster clusters;
    public static void main(String[] args) {
        // do import pages exist?
        ArrayList<String> siteTitleList = new ArrayList<String>();
        ArrayList<Long> parsedPagesIndexes;
        if(!importPages()){
            // if they dont exist
            // create, parse, process and save the pages
            // the create function returns the indexes of the pages as an arraylist
            parsedPagesIndexes = create();
        } else {
            // if they do exist
            // get the indexes of all saved & parsed pages
            parsedPagesIndexes = readIndexes();
        }
        siteTitleList = getTitleList(parsedPagesIndexes);

        File clusterFile = new File(CLUSTER_FILE);
        clusters = null;

        // cluster stuff
        if(clusterFile.exists()){
            // recreates the best cluster from a prior run
            // this may not be needed
            clusters = getCluster(clusterFile, parsedPagesIndexes);
        } else { 
            // get the clusters
            clusters = getBestCluster(parsedPagesIndexes);
            try{
                FileOutputStream outStream = new FileOutputStream(CLUSTER_FILE);
                FileChannel outChannel = outStream.getChannel();
                ByteBuffer bb = ByteBuffer.allocate(Long.BYTES*CLUSTER_NODE_COUNT);
                for(int i = 0; i < clusters.centerIndexes.length; i++){
                    bb.putLong(clusters.centerIndexes[i]);
                }
                bb.position(0);
                outChannel.write(bb);
                outStream.close();
            } catch(IOException e){
                e.printStackTrace();
                return;
            }
        }

        gui = new GUI();

        gui.initialize(siteTitleList);

        gui.okBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                okButtonPressed();
            }
        });

        gui.clearBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                gui.clear();
            }
        });
    }

    public static void okButtonPressed(){
        String keyword = gui.category.getText();
        String website = gui.website.getSelectedItem().toString();

        if(keyword.equals("") && website.equals("")){
            return;
        }

        // required variables
        ArrayList<Long> indexList = readIndexes(); 
        ArrayList<ParsePage> tempList = new ArrayList<ParsePage>();
        File pageDataFile = new File(App.PAGE_DATA);

        // has a keyword
        if(!keyword.equals("")){
            for(int i = 0; i < indexList.size(); i++){
                ParsePage tempPage = ParsePage.getPage(pageDataFile, indexList.get(i));
                if(tempPage.wordMap.get(keyword) != null){
                    tempList.add(tempPage);
                }                 
            }
            // does not have a website
            // picks the 2 websites with the highest number of that word
            if(website.equals("")){
                while(tempList.size() > 2 ){
                    int lowest = 0;
                    for(int j = 0; j < tempList.size(); j++){
                        
                        if(tempList.get(j).wordMap.get(keyword) != null && tempList.get(lowest).wordMap.get(keyword) != null 
                            && tempList.get(j).wordMap.get(keyword).freq < tempList.get(lowest).wordMap.get(keyword).freq){
                            lowest = j;
                        }
                    }
                    tempList.remove(lowest);
                }
            }
        }
        // has a website
        // picks the 2 with the highest cosine value
        if(!website.equals("")){
            // get the correct page from the master list
            ParsePage givenPage = null;
            long givenIndex = -1;
            for(int i = 0; i < indexList.size(); i++){
                ParsePage temp = ParsePage.getPage(pageDataFile, indexList.get(i));
                if(temp.title.equals(website)){
                    // set the page and then remove it from the list
                    givenPage = temp;
                    givenIndex = indexList.get(i);
                    indexList.remove(i);
                    break;
                }
            }
            // page was found -could be assumed
            if(givenPage != null){

                // create a list of cosine similarity values
                ArrayList<Double> simArrayList = new ArrayList<Double>();
                for(int j = 0; j < indexList.size(); j++){
                    ParsePage temp = ParsePage.getPage(pageDataFile, indexList.get(j));
                    simArrayList.add(j, givenPage.wordMap.cosSimilarity(temp.wordMap));
                }

                // remove all but the highest 2 values
                while(simArrayList.size() > 2){
                    int lowest = 0;
                    for(int k = 1; k < tempList.size(); k++){
                        if(simArrayList.get(k) < simArrayList.get(lowest)){
                            lowest = k;
                        }
                    }
                    simArrayList.remove(lowest);
                    indexList.remove(lowest);
                }

                for(int j = 0; j < indexList.size(); j++){
                    tempList.add(ParsePage.getPage(pageDataFile, indexList.get(j)));
                }

                SimCluster cluster = getCluster(new File(App.CLUSTER_FILE), readIndexes());

                ParsePage clusterHead = cluster.centerNodes[cluster.getCenterIndex(givenPage)];
                long closestPageIndex = cluster.getClosest(givenPage,givenIndex);
                ParsePage closestPage;
                if(closestPageIndex == -1){
                    closestPage = null;
                } else {
                    closestPage = ParsePage.getPage(pageDataFile, closestPageIndex);
                }

                gui.displayClusterResults(clusterHead, closestPage);
            }
        }
        
        gui.displaySimResults(tempList);
    }

    public static ArrayList<Long> create(){
        ArrayList<Long> result = new ArrayList<Long>();
        ArrayList<String> webSites = importWebsites();
        ArrayList<ParsePage> parsedPages = parseWebsites(webSites);
        parsedPages = setTFIDF(parsedPages);

        result = writeParsedPages(parsedPages);

        return result;
    }

    private static ArrayList<Long> writeParsedPages(ArrayList<ParsePage> parsedPages){
        ArrayList<Long> result = new ArrayList<Long>();
        File indexFile = new File(PAGE_INDEX);
        File dataFile = new File(PAGE_DATA);
        long location = 0;

        try{
            FileOutputStream outStream = new FileOutputStream(indexFile);
            FileChannel outChannel = outStream.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(Long.BYTES*PAGE_COUNT);
            for(ParsePage p: parsedPages){
                long size = p.insertPage(dataFile,location);
                bb.putLong(location);
                result.add(location);
                location += size;
            }
            bb.position(0);
            outChannel.write(bb);
            outStream.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }


        return result;
    }

    public static ArrayList<String> importWebsites(){
        ArrayList<String> result = new ArrayList<String>();
        try {
            File importFile = new File(IMPORT_FILE);
            Scanner importReader = new Scanner(importFile);
            while (importReader.hasNextLine()){
                result.add(importReader.nextLine());
            }
            importReader.close();
        } catch (FileNotFoundException e) {
            System.out.println(IMPORT_FILE + " Was Not Found!");
            // create file
            createImportFile();
            // fill file with websites
            fillImportFile();
            result = importWebsites();
        }
        return result;
    }

    public static ArrayList<ParsePage> parseWebsites(ArrayList<String> webSites){
        ArrayList<ParsePage> result = new ArrayList<ParsePage>();
        for(int i = 0;i < webSites.size();i++){
            result.add(new ParsePage(webSites.get(i)));
        }
        return result;
    }

    public static ArrayList<ParsePage> setTFIDF(ArrayList<ParsePage> parsedPages){
        for(int i = 0; i < parsedPages.size(); i++){
            ParsePage tempPage = parsedPages.get(i);
            for(int k = 0; k < tempPage.wordMap.map.length; k++){
                ObjFreqHashMap.Node tempNode = tempPage.wordMap.map[k];
                while(tempNode != null){
                    int wordDocFreq = 0;
                    for(int j = 0; j < parsedPages.size(); j++){
                        if(parsedPages.get(j).wordMap.get(tempNode.key) != null){
                            wordDocFreq++;
                        }
                    }
                    tempNode.idf = Math.log((double)parsedPages.size()/wordDocFreq);
                    tempNode.tfidf = tempNode.tf * tempNode.idf;
                    tempNode = tempNode.next;
                }
            }
        }
        return parsedPages;
    }

    private static void fillImportFile(){
        Document newDoc;
        File importFile = new File(IMPORT_FILE);
        try{
            FileWriter output = new FileWriter(importFile);
            for(int i = 0; i < PAGE_COUNT; i++){
                newDoc = Jsoup.connect(RANDOM_WIKI_URL).get();
                output.write(newDoc.location() + "\n");
            }
            output.close();
        } catch (IOException e){
            System.out.println("Error with website");
            e.printStackTrace();
        }
    }

    private static void createImportFile(){
        File importFile = new File(IMPORT_FILE);
        File dataFile = new File(PAGE_DATA);
        File indexFile = new File(PAGE_INDEX);
        try{
            importFile.createNewFile();
            dataFile.createNewFile();
            indexFile.createNewFile();
        } catch (IOException e){
            System.out.println("Error Creating File");
            e.printStackTrace();
        }
    }

    /**
     * 
     * @return
     */
    private static boolean importPages(){
        File importFile = new File(IMPORT_FILE);
        File dataFile = new File(PAGE_DATA);
        File indexFile = new File(PAGE_INDEX);
        if(!(importFile.exists() && dataFile.exists() && indexFile.exists())) return false;
        return true;
    }

    private static ArrayList<Long> readIndexes(){
        ArrayList<Long> result = new ArrayList<Long>();
        try{
            File indexFile = new File(PAGE_INDEX);
            FileInputStream inStream = new FileInputStream(indexFile);
            FileChannel inChannel = inStream.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);

            while(inChannel.position() < inChannel.size()){
                inChannel.read(bb);
                bb.position(0);
                result.add(bb.getLong());
                bb.position(0);
            }
            inStream.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }
   
    private static SimCluster getBestCluster(ArrayList<Long> pageIndexes){
        ArrayList<Long> goodIndexes = new ArrayList<Long>(pageIndexes);
        SimCluster result = null;
        SimCluster tempCluster;
        for(int i = 0; i < CLUSTER_CYCLE_COUNT; i++){
            tempCluster = new SimCluster(CLUSTER_NODE_COUNT);
            for(int k = 0; k < CLUSTER_NODE_COUNT; k++){
                int rand = ThreadLocalRandom.current().nextInt(goodIndexes.size());
                long index = goodIndexes.get(rand);
                ParsePage thePage = ParsePage.getPage(new File(PAGE_DATA), index);
                if(!tempCluster.isCenter(index)){
                    tempCluster.addCenter(thePage, index);
                } else {
                    k--;
                }
            }
            for(int j = 0; j < pageIndexes.size(); j++){
                tempCluster.insert(pageIndexes.get(j));
            }
            /* Tried this to make the clustering better
            for(int l = 0; l < CLUSTER_NODE_COUNT; l++){
                if(tempCluster.centerNodeTrees[l].root.count == 0){
                    int badIndex = goodIndexes.indexOf(tempCluster.centerIndexes[l]);
                    if(badIndex != -1) {
                        goodIndexes.remove(badIndex);
                        break;
                    }
                }
            }
            */
            if(result == null || tempCluster.isBetter(result)){
                result = tempCluster;
            }
        }
        return result;
    }

    private static SimCluster getCluster(File inFile, ArrayList<Long> indexes){
        SimCluster result = new SimCluster(CLUSTER_NODE_COUNT);
        try{
            FileInputStream inStream = new FileInputStream(CLUSTER_FILE);
            FileChannel inChannel = inStream.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(Long.BYTES*CLUSTER_NODE_COUNT);

            inChannel.read(bb);
            bb.position(0);
            for(int i = 0; i < CLUSTER_NODE_COUNT; i++){
                long l = bb.getLong();
                result.addCenter(ParsePage.getPage(new File(PAGE_DATA), l), l);
            }
            inStream.close();

            for(int i = 0; i < indexes.size(); i++){
                result.insert(indexes.get(i));
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        return result;
    }

    private static ArrayList<String> getTitleList(ArrayList<Long> indexes){
        File inFile = new File(PAGE_DATA);
        ArrayList<String> result = new ArrayList<String>();

        for(int i = 0; i < indexes.size(); i++){
            result.add(i, ParsePage.getTitle(inFile, indexes.get(i)));
        }

        return result;
    }
}
