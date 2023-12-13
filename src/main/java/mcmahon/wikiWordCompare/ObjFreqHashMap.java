package mcmahon.wikiWordCompare;

//import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

public class ObjFreqHashMap implements java.io.Serializable {

    static final class Node {
        public String key;
        public int freq;
        public Node next;
        public double tf, idf, tfidf;

        public Node(String key, Node next){
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
    public void add(String key){
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

    public void remove(String key){
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

    private void writeObject(ObjectOutputStream s) {
        try {
            //s.defaultWriteObject();
            s.writeInt(uniqueWords);
            //System.out.println("Writing");
            for(int i = 0; i < map.length; i++ ){
                for(Node e = map[i]; e != null; e = e.next){
                    //System.out.println(e.key + " x " + e.freq);
                    s.writeObject(e.key);
                    s.writeInt(e.freq);
                    s.writeDouble(e.tf);
                    s.writeDouble(e.idf);
                    s.writeDouble(e.tfidf);
                }
            }
        } catch(IOException e){
            System.out.println("Could not write ObjFreqHashMap");
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream s) {
        //s.defaultReadObject();
        this.map = new Node[8];
        totalWords = 0;
        uniqueWords = 0;
        try {
            int n = s.readInt();
            for(int i = 0; i < n; i++){
                String e = (String)s.readObject();
                int count = s.readInt();
                for(int k = count; k > 0; k--){
                    add(e); 
                }
                Node t = get(e);
                t.tf = s.readDouble();
                t.idf = s.readDouble();
                t.tfidf = s.readDouble();
            }
        } catch(IOException e){
            System.out.println("Could not read ObjFreqHashMap");
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            System.out.println("Could not find class ObjFreqHashMap");
            e.printStackTrace();
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

    /**
     * This is a wrapper to handle any exceptions to make things look a bit cleaner
     * @param fileLocation
     * @param index
     * @return
     */
    public static ObjFreqHashMap getMap(File fileLocation, long index){
        ObjFreqHashMap result = null;
        try{
            result = _getMap(fileLocation, index);
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /* The Read/Write order for this is:
     * 1. word count
     * 3. total words
     * LOOP on word count
     * 1. word length (int)
     * 2. the word (String)
     * 3. word freq (int)
     * 4. tf (double)
     * 5. idf (double)
     * 6. tfidf (double)
     */
    private static ObjFreqHashMap _getMap(File fileLocation, long index) throws FileNotFoundException,IOException{
        ObjFreqHashMap result = new ObjFreqHashMap();
        
        // used for decoding the strings
        Charset cs = Charset.forName("UTF-8");
        CharsetDecoder dec = cs.newDecoder();

        FileInputStream inStream = new FileInputStream(fileLocation);
        FileChannel inChannel = inStream.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES*2);
        String word;
        int length, count, numWords;

        // set the channel to the start of the Object
        inChannel.position(index);
        inChannel.read(bb);
        bb.position(0);
        numWords = bb.getInt();
        count = bb.getInt();

        // get all the data for the next node and insert it into the result
        for(int i = 0; i < numWords; i++){
            // get length
            bb = ByteBuffer.allocate(Integer.BYTES);
            inChannel.read(bb);
            bb.position(0);
            length = bb.getInt();

            // get word
            bb = ByteBuffer.allocate(length);
            inChannel.read(bb);
            bb.position(0);
            word = dec.decode(bb).toString();
            
            // the insert part
            result.add(word);
            Node temp = result.get(word);

            // get count
            bb = ByteBuffer.allocate(Integer.BYTES);
            inChannel.read(bb);
            bb.position(0);
            temp.freq = bb.getInt();

            // get tf
            bb = ByteBuffer.allocate(Double.BYTES);
            inChannel.read(bb);
            bb.position(0);
            temp.tf = bb.getDouble();

            // get idf
            bb.position(0);
            inChannel.read(bb);
            bb.position(0);
            temp.idf = bb.getDouble();

            // get tfidf
            bb.position(0);
            inChannel.read(bb);
            bb.position(0);
            temp.tfidf = bb.getDouble();
        }

        // set the total number of words
        result.totalWords = count;

        inChannel.close();
        inStream.close();
        return result;
    }

    public int insertMap(File fileLocation){
        try{
            return _insertMap(fileLocation, 0);
        } catch (FileNotFoundException e){
            System.out.println("InsertFile Not Found: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("File Stream Issue: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        }
        // should only happen if there was an error inserting into the given file
        return 0;
    }

    public int insertMap(File fileLocation, long index){
        try{
            return _insertMap(fileLocation, index);
        } catch (FileNotFoundException e){
            System.out.println("InsertFile Not Found: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("File Stream Issue: " + fileLocation.getAbsolutePath());
            e.printStackTrace();
        }
        // should only happen if there was an error inserting into the given file
        return 0;
    }

    /* The Read/Write order for this is:
     * 1. word count
     * 3. total words
     * LOOP on word count
     * 1. word length (int)
     * 2. the word (String)
     * 3. word freq (int)
     * 4. tf (double)
     * 5. idf (double)
     * 6. tfidf (double)
     */
    private int _insertMap(File fileLocation, long index) throws FileNotFoundException,IOException{
        // used for decoding the strings
        Charset cs = Charset.forName("UTF-8");
        CharsetEncoder enc = cs.newEncoder();

        int size = Integer.BYTES*2;
        FileOutputStream outStream = new FileOutputStream(fileLocation, true);
        FileChannel outChannel = outStream.getChannel();
        
        ByteBuffer bb1 = ByteBuffer.allocate(size);
        ByteBuffer bb2;

        bb1.putInt(this.uniqueWords);
        bb1.putInt(this.totalWords);

        for(int i = 0; i < map.length; i++ ){
            for(Node e = map[i]; e != null; e = e.next){
                int wordSize = (int)(e.key.length() * 1.1);
                size = bb1.capacity() + (Integer.BYTES * 2) + wordSize + (Double.BYTES * 3);
                bb2 = ByteBuffer.allocate(size);
                bb2.position(0);
                bb1.position(0);
                bb2.put(bb1);
                bb2.putInt(wordSize);
                bb2.put(enc.encode(CharBuffer.wrap(e.key)));
                bb2.putInt(e.freq);
                bb2.putDouble(e.tf);
                bb2.putDouble(e.idf);
                bb2.putDouble(e.tfidf);
                bb1 = bb2;
            }
        }
        if(index == -1) index = outChannel.size();
        bb1.position(0);
        size = outChannel.write(bb1, index);
        outStream.close();

        return size;
    }
}