package mcmahon.wikiWordCompare;

//import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class ObjFreqHashMap implements java.io.Serializable {

    static final class Node {
        public Object key;
        public int freq;
        public Node next;
        public double tf;
        public double idf;
        public double tfidf;

        public Node(Object key, Node next){
            this.next = next;
            this.key = key;
            this.freq = 1;
            this.tf = 0;
            this.idf = 0;
            this.tfidf = 0;
        }
        public String toString(){
            return key.toString() + " appears " + freq + " times and has a term frequency of " + tf + ", an idf of " + idf + ", and a tfidf of " + tfidf +".";
        }
    }

    Node[] map;
    int totalWords;
    int uniqueWords;

    public ObjFreqHashMap(){
        this(8);
    }

    public ObjFreqHashMap(int initialSize){
        this.map = new Node[initialSize];
        totalWords = 0;
        uniqueWords = 0;
    }

    /**
     * 
     * @param key
     * @return either null or the Node containing the key
     */
    public Node get(Object key){

        for(Node e = map[getIndex(key)]; e != null; e = e.next){
            if(e.key.equals(key)) return e;
        }
        return null;
    }

    public boolean contains(Object key){

        for(Node e = map[getIndex(key)]; e != null; e = e.next){
            if(e.key.equals(key)) return true;
        }
        return false;
    }

    /**
     * 
     * @param key
     * 
     * the way i am using this is to count unique words
     * if a key exists inclement that keys freq
     * if it does not exist, add a new one to the front of that index
     */
    public void add(Object key){
        Node tempNode = get(key);
        if (tempNode != null){
            tempNode.freq++;
        } else {
            int i = getIndex(key);
            tempNode = new Node(key, map[i]);
            map[i] = tempNode;
            uniqueWords++;
        }
        totalWords++;
        if ((float)uniqueWords/map.length >= 0.75f){
            resizeV2();
        }
    }

    public void remove(Object key){
        int i = getIndex(key);
        Node current = map[i], prior = null;
        while(current != null){
            if(key.equals(current.key)){
                if(prior == null) map[i] = map[i].next;
            } else {
                prior.next = current.next;
                break;
            }
            prior = current;
            current = current.next;
        }
    }

    // do not use this for complex hash maps
    void resize() {
        Node[] oldTable = map;
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity << 1;
        Node[] newTable = new Node[newCapacity];
        for (int i = 0; i < oldCapacity; ++i) {
            for (Node e = oldTable[i]; e != null; e = e.next) {
            int h = e.key.hashCode();
            int j = h & (newTable.length - 1);
            newTable[j] = new Node(e.key, newTable[j]);
            }
        }
        map = newTable;
    }
    void resizeV2() { // avoids unnecessary creation
        Node[] oldTable = map;
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity << 1;
        Node[] newTable = new Node[newCapacity];
        for (int i = 0; i < oldCapacity; ++i) {
                Node e = oldTable[i];
                while (e != null) {
                    Node next = e.next;
            int h = e.key.hashCode();
            int j = h & (newTable.length - 1);
                    e.next = newTable[j];
            newTable[j] = e;
                    e = next;
            }
        }
        map = newTable;
    }

    public void addAll(ObjFreqHashMap theMap){
        for (int i = 0; i < theMap.map.length; i++){
            for (Node e = theMap.map[i]; e != null; e = e.next){
                Node temp = this.get(e.key);
                if (temp == null){
                    add(e.key);
                    temp = get(e.key);
                    temp.freq = e.freq;
                } else { 
                    temp.freq += e.freq;
                }
            }
        }
    }

    public void printAll(){
        for(int i = 0; i < map.length; i++ ){
            for(Node e = map[i]; e != null; e = e.next){
                System.out.println(e.toString());
            }
        }
    }

    private int getIndex(Object key){
        return key.hashCode() & (map.length - 1);
    }

    private void writeObject(ObjectOutputStream s) throws Exception {
        s.defaultWriteObject();
        s.writeInt(uniqueWords);
        for(int i = 0; i < map.length; i++ ){
            for(Node e = map[i]; e != null; e = e.next){
                s.writeObject(e);
            }
        }
    }

    private void readObject(ObjectInputStream s) throws Exception {
        s.defaultReadObject();
        int n = s.readInt();
        for(int i = 0; i < n; i++){
            add(s.readObject());
        }
    }

    /**
     * set the term frequency for this document
     */
    public void generateTF(){
        if(totalWords > 0){
            for(int i = 0; i < map.length; i++ ){
                for(Node e = map[i]; e != null; e = e.next){
                    e.tf = (float) e.freq / totalWords;
                }
            }
        }
    }

    public double[] getTFIDFVector(){
        double[] result = new double[uniqueWords];
        int index = 0;
        for(int i = 0; i < map.length; i++ ){
            for(Node e = map[i]; e != null; e = e.next){
                result[index] = e.tfidf;
                index++;
            }
        }
        return result;
    }

    public double cosSimilarity(ObjFreqHashMap b){
        ArrayList<Double> aVect = new ArrayList<Double>();
        ArrayList<Double> bVect = new ArrayList<Double>();

        for(int i = 0; i < map.length; i++){
            for(Node e = map[i]; e != null; e = e.next){
                Node bTemp = b.get(e.key);
                if(bTemp != null){
                    bVect.add(bTemp.tfidf);
                } else {
                    bVect.add(0.0);
                }
                aVect.add(e.tfidf);
            }
        }

        for(int j = 0; j < b.map.length; j++){
            for(Node k = b.map[j]; k != null; k = k.next){
                Node aTemp = get(k.key);
                if(aTemp == null){
                    aVect.add(0.0);
                    bVect.add(k.tfidf);
                }
            }
        }
        if(aVect.size() == bVect.size()){
            System.out.println();
        }
        double dotProd = dotProduct(aVect, bVect);

        return dotProd / (magnitude(aVect) * magnitude(bVect));
    }

    public static double dotProduct(ArrayList<Double> a, ArrayList<Double> b){
        double result = 0;
        if(a.size() >= b.size()){
            for(int i = 0; i < a.size(); i++){
                result += a.get(i) * b.get(i % b.size());
            }
        } else {
            for(int i = 0; i < b.size(); i++){
                result += b.get(i) * a.get(i % a.size());
            }
        }
        return result;
    }

    public static double magnitude(ArrayList<Double> a){
        double result = 0;
        for(int i = 0; i < a.size(); i++){
            result += a.get(i) * a.get(i);
        }
        return Math.sqrt(result);
    }
}