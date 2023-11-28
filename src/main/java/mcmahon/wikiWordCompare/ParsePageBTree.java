package mcmahon.wikiWordCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ParsePageBTree implements java.io.Serializable {
    static final class Node{
        
        public String[] keys;
        public Node[] children;
        public Node parent = null;
        public int order = 1;
        public int count = 0;
        // build the node and put an object in it
        public Node(String key, int order){
            this(order);
            keys[0] = key;
            count++;
        }
        // build an n-size node
        public Node(int order){
            this.order = order;
            keys = new String[order-1];
            children = new Node[order];
        }
        // binary tree node is the base case
        public Node(){
            this(2);
        }

        public boolean isLeaf(){
            for(Node e: children){
                if(e != null) return false;
            }
            return true;
        }

        public boolean isFull(){
            if(isLeaf()) return count >= keys.length;
            for(int i = 0; i < children.length && children[i] != null; i++){
                if(!children[i].isFull()){
                    return false;
                }
            }
            return count >= keys.length;
        }
        
        public boolean childrenFull(){
            for(Node e: children){
                if(e != null && !e.isFull()) return false;
            }
            return true;
        }
        
        public Object getBiggest(){
            if(count == 0) return null;
            if(isLeaf()) return keys[count-1]; 
            return children[count].getBiggest();
        }

        public void removeBiggest(){
            if(count != 0){
                if(isLeaf()){
                    keys[count-1] = null;
                    count--;
                } else {
                    children[count].removeBiggest();
                }
            }
        }
        
        public Object getSmallest(){
            if(count == 0) return null;
            if(isLeaf()) return keys[0];
            return children[0].getSmallest();
        }
        public void removeSmallest(){
            if(count != 0){
                if(isLeaf()){
                    for(int i = 1; i < keys.length;i++){
                        keys[i-1] = keys[i];
                    }
                    keys[keys.length-1] = null;
                    count--;
                } else {
                    children[0].removeSmallest();
                }
            }
        }
        // add a new key into te key array
        // will split if there are too many objects
        public void insertKey(String key){
            String temp1 = key,temp2;
            ParsePage p2, p1 = getPage(temp1);
            boolean added = false;
            /* 
            for(int i = 0; i < count && !added; i++){
                p2 = getPage(keys[i]);
                if(p1.wordMap. < keys[i].hashCode()){
                    for(int j = i; j <= count; j++){
                        temp2 = keys[j];
                        keys[j] = temp1;
                        temp1 = temp2;
                    }
                    count++;
                    added = true;
                }
            }
            */
            if(!added){
                keys[count] = key;
                count++;
            }
        }

        private ParsePage getPage(String inString){
            ParsePage result = null;
            try{
                ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(new File(inString)));
                result = (ParsePage)inStream.readObject();
                inStream.close();
            } catch(IOException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
            return result;
        }

        public void depthPrint(){
            for(Node e: children){
                if(e != null){
                    e.depthPrint();
                }
            }
            print();
        }

        public void breadthPrint(){
            print();
            for(Node e: children){
                if(e != null){
                    e.breadthPrint();
                }
            }
        }

        public void print(){
            for(int i = 0; i < keys.length; i++){
                if(keys[i] != null) System.out.print(keys[i] + " ");
            }
            System.out.println("");
        }
    }
}
