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
import java.util.ArrayList;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class App {

    static final String IMPORT_FILE = "data\\wikiPages.txt";
    static final String RANDOM_WIKI_URL = "https://en.wikipedia.org/wiki/Special:Random";
    static final String CLUSTER_FILE = "data\\clusterDataFile";
    static final String PAGE_FILE_PREFIX = "data\\pages";
    static final int CLUSTER_CYCLE_COUNT = 10;
    static final int CLUSTER_NODE_COUNT = 5;
    
    static GUI gui;
    static ArrayList<ParsePage> parsedPages;
    static ArrayList<File> parsedPagesFileLocations;
    static SimCluster clusters;
    public static void main(String[] args) {
        // read the pages if they exist already
        if(!importPages()){
            // if they dont exist
            // create, parse, process and save the pages
            ArrayList<String> webSites = importWebsites();
            parsedPages = parseWebsites(webSites);
            setTFIDF();
            parsedPagesFileLocations = writeParsedPages();
        }

        File clusterFile = new File(CLUSTER_FILE);
        clusters = null;

        // cluster stuff
        if(clusterFile.exists()){
            try{
                ObjectInputStream clusterInStream = new ObjectInputStream(new FileInputStream(CLUSTER_FILE));
                clusters = (SimCluster)clusterInStream.readObject();
                clusterInStream.close();
            } catch(IOException e){
                e.printStackTrace();
                System.out.println("Cluster Reading Failed, Delete Cluster File and try again");
                return;
            } catch(ClassNotFoundException e){
                e.printStackTrace();
                System.out.println("Cluster Class Not Found!");
                return;
            }
        } else { 
            // get the clusters
            clusters = getBestCluster();
            try{
                ObjectOutputStream clusterOutStream = new ObjectOutputStream(new FileOutputStream(clusterFile));
                clusterOutStream.writeObject(clusters);
                clusterOutStream.close();
            } catch(IOException e){
                e.printStackTrace();
                return;
            }
        }

        gui = new GUI();

        gui.initialize(parsedPages);

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

        ArrayList<ParsePage> tempList = new ArrayList<ParsePage>(parsedPages);
        // has a keyword
        if(!keyword.equals("")){
            for(int i = 0; i < tempList.size(); i++){
                ParsePage tempPage = tempList.get(i);
                if(tempPage.wordMap.get(keyword) == null){
                    tempList.remove(i);
                    i--;
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
            ParsePage tempPage = null;
            for(int i = 0; i < parsedPages.size(); i++){
                if(parsedPages.get(i).title.equals(website)){
                    tempPage = parsedPages.get(i);
                }
            }
            // page was found -could be assumed
            if(tempPage != null){
                // remove the page from temp list if it exists
                for(int i = 0; i < tempList.size(); i++){
                    if(tempList.get(i).title.equals(tempPage.title)){
                        tempList.remove(i);
                        break;
                    }
                }

                // create a list of cosine similarity values
                ArrayList<Double> simArrayList = new ArrayList<Double>();
                for(int j = 0; j < tempList.size(); j++){
                    simArrayList.add(tempPage.wordMap.cosSimilarity(tempList.get(j).wordMap));
                }

                // remove all but the highest 2 values
                while(tempList.size() > 2){
                    int lowest = 0;
                    for(int k = 0; k < tempList.size(); k++){
                        if(simArrayList.get(k) < simArrayList.get(lowest)){
                            lowest = k;
                        }
                    }
                    simArrayList.remove(lowest);
                    tempList.remove(lowest);
                }
            }
        }
        
        gui.displayResults(tempList);
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
            importWebsites();
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

    public static void setTFIDF(){
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
    }

    private static void fillImportFile(){
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

    private static void createImportFile(){
        File newFile = new File(IMPORT_FILE);
        try{
            newFile.createNewFile();
        } catch (IOException e){
            System.out.println("Error Creating File");
            e.printStackTrace();
        }
    }

    private static boolean importPages(){
        int i = 0;
        File importFile = new File(PAGE_FILE_PREFIX + i);
        if(!importFile.exists()) return false;
        do{
            try{
                parsedPagesFileLocations.add(i, importFile);
                ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(importFile));
                parsedPages.add(i, (ParsePage)inStream.readObject());
                inStream.close();
            } catch(IOException e){
                e.printStackTrace();
                return false;
            } catch(ClassNotFoundException e){
                e.printStackTrace();
                return false;
            }
            i++;
            importFile = new File(PAGE_FILE_PREFIX + i);
        }while(importFile.exists());
        return true;
    }

    private static ArrayList<File> writeParsedPages(){
        ArrayList<File> result = new ArrayList<File>();

        File tempFolder = new File(PAGE_FILE_PREFIX);
        tempFolder.mkdir();

        for(int i = 0; i < parsedPages.size(); i++){
            try{
                File temp = new File(PAGE_FILE_PREFIX + "\\page" + i);
                ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(temp));
                outStream.writeObject(parsedPages.get(i));
                outStream.close();
                result.add(i, temp);
            } catch(IOException e){
                e.printStackTrace();
            }
        }

        return result;
    }

    private static SimCluster getBestCluster(){
        SimCluster result = null;
        SimCluster tempCluster;
        for(int i = 0; i < CLUSTER_CYCLE_COUNT; i++){
            tempCluster = new SimCluster(CLUSTER_NODE_COUNT);
            for(int k = 0; k < CLUSTER_NODE_COUNT; k++){
                ParsePage thePage = parsedPages.get((int)(Math.random() * parsedPages.size()) + 1);
                if(!tempCluster.isCenter(thePage)){
                    tempCluster.addCenter(thePage);
                } else {
                    k--;
                }
            }
            for(int j = 0; j < parsedPagesFileLocations.size(); j++){
                tempCluster.insert(parsedPagesFileLocations.get(j));
            }
            if(result == null || tempCluster.isBetter(result)){
                result = tempCluster;
            }
        }
        return result;
    }
}
