package mcmahon.wikiWordCompare;

//import java.util.*;
import java.io.*;

public class ObjFreqHashMap implements java.io.Serializable {

    static final class Node {
        public Object key;
        public int freq;
        public Node next;

        public Node(Object key, Node next){
            this.next = next;
            this.key = key;
            this.freq = 1;
        }
        public String toString(){
            return key.toString() + "appears " + freq + " times.";
        }
    }

    Node[] map;
    int count;

    public ObjFreqHashMap(){
        this(8);
    }

    public ObjFreqHashMap(int initialSize){
        this.map = new Node[initialSize];
        count = 0;
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
            count++;
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
        s.writeInt(count);
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
}