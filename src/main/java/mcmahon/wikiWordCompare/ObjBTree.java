package mcmahon.wikiWordCompare;


/**
 * This class is built to eager split the nodes when the node is full
 */
public class ObjBTree implements java.io.Serializable {
    
    static final class Node{
        
        public Object[] keys;
        public Node[] children;
        public Node parent = null;
        public int order = 1;
        public int count = 0;
        // build the node and put an object in it
        public Node(Object key, int order){
            this(order);
            keys[0] = key;
            count++;
        }
        // build an n-size node
        public Node(int order){
            this.order = order;
            keys = new Object[order-1];
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
        public boolean isFullNode(){
            return count >= keys.length;
        }
        public boolean childrenFull(){
            for(Node e: children){
                if(e != null && !e.isFull()) return false;
            }
            return true;
        }

        public int biggestHash(){
            if(count == 0) return 0;
            if(isLeaf()) return keys[count-1].hashCode();
            return children[count].biggestHash();
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
        public int smallestHash(){
            if(count == 0) return 0;
            if(isLeaf()) return keys[0].hashCode();
            return children[0].smallestHash();
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
        public void insertKey(Object key){
            Object temp1 = key,temp2;
            boolean added = false;
            for(int i = 0; i < count && !added; i++){
                if(key.hashCode() < keys[i].hashCode()){
                    for(int j = i; j <= count; j++){
                        temp2 = keys[j];
                        keys[j] = temp1;
                        temp1 = temp2;
                    }
                    count++;
                    added = true;
                }
            }
            if(!added){
                keys[count] = key;
                count++;
            }
            /* moving this out a layer
            if(count == keys.length){
                split();
            }
            */
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
        /* 
        public void split(){
            if(parent == null) {
                //System.out.println("Split children");
                splitToChildren();
            } else if(parent.count < keys.length){
                if(parent.childrenFull()){
                    System.out.println("Split Parents");
                    splitToParent();
                } else {
                    System.out.println("Rotate");
                    rotateToSibling();
                }
            } 
        }
        public void rotateToSibling(){
            int nodeIndex = -1, closestIndex = -1;
            for(int i = 0; i < parent.children.length; i++){
                if(nodeIndex == -1 && this.equals(parent.children[i])){
                    nodeIndex = i;
                }
                if(parent.children[i] != null && !parent.children[i].isFull()){
                    if(nodeIndex == -1 || closestIndex == -1){
                        closestIndex = i;
                    } else if(Math.abs(nodeIndex - closestIndex) > Math.abs(nodeIndex - i)){
                        closestIndex = i;
                    }
                }
            }
            if(nodeIndex == -1 || closestIndex == -1) return;
            Object temp1,temp2;
            if(nodeIndex > closestIndex){
                //rotate right
                System.out.println("Rotate Right");
                //smallest from this node
                temp1 = keys[0];
                //shift keys left 1 and clear last
                for(int i = 1; i < keys.length; i++){
                    keys[i-1] = keys[i];
                }
                keys[keys.length-1] = null;
                count--;

                temp2 = parent.keys[nodeIndex-1];
                parent.keys[nodeIndex-1] = temp1;
                parent.children[nodeIndex-1].insertKey(temp2);
            } else {
                //rotate left
                System.out.println("Rotate Left");
                temp1 = keys[keys.length-1];
                keys[keys.length-1] = null;
                count--;
                temp2 = parent.keys[nodeIndex];
                parent.keys[nodeIndex] = temp1;
                parent.children[nodeIndex+1].insertKey(temp2);
            }
        }

        public void splitToChildren(){
            Node left = new Node(order),right = new Node(order);
            Object mid = null;
            left.insertChild(children[0]);
            children[0] = null;
            for(int i = 0; i < keys.length; i++){
                if(i == keys.length/2){
                    //System.out.println("Mid Found");
                    mid = keys[i];
                    right.insertChild(children[i+1]);
                } else if(i < keys.length/2){
                    left.insertKey(keys[i]);
                    left.insertChild(children[i+1]);
                    children[i+1] = null;
                } else {
                    right.insertKey(keys[i]);
                    right.insertChild(children[i+1]);
                }
                children[i+1] = null;
                keys[i] = null;
            }
            children[children.length-1] = null;
            count = 1;
            keys[0] = mid;
            //System.out.println(mid);
            insertChild(left);
            insertChild(right);
        }

        public void splitToParent(){
            Node newRightNode = new Node(order);
            
            int middleIndex = order/2;
            for(int i = 0; i < keys.length; i++){
                if(i == middleIndex){
                    parent.insertKey(keys[i]);
                    keys[i] = null;
                    count--;
                } else if(i > middleIndex) {
                    newRightNode.insertKey(keys[i]);
                    keys[i] = null;
                    count--;
                }
            }
            parent.insertChild(newRightNode);
        }
        */
        
        private void insertChild(Node newNode) {
            // no children
            // containing children
                // what are the bounds here?
            if(newNode == null) {
                return;
            }
            newNode.parent = this; 
            if(isLeaf()){
                children[0] = newNode;
            } else {
                int newHash = newNode.biggestHash();
                for(int i = 0; i < children.length; i++){
                    if(children[i] == null || newHash < children[i].smallestHash()){
                        Node temp1 = children[i], temp2;
                        for(int k = i+1; k < children.length; k++){
                            temp2 = children[k];
                            children[k] = temp1;
                            temp1 = temp2;
                        }
                        children[i] = newNode;
                        return;
                    }
                }
            }
        }
    }

    Node root;
    int order;

    public ObjBTree(int order){
        this.order = order;
        this.root = new Node(this.order);
    }

    public ObjBTree(){
        this(1);
    }

    public void insert(Object item){
        // insert into the table recursivly
        insert(item, this.root);
        // rebalance the tree
        if(root.isFull()){
            splitTree(root);
        }
    }

    private void insert(Object item, Node location){
        if(location.isLeaf() && location.isFull() ){
            int current = -1;
            if(location.parent == null){
                splitToChildren(location);
            }
            // sibling with room
            else if(!location.parent.childrenFull()){
                // get the needed information to decide on rotate direction
                int closest = -1;
                for(int i = 0; i < location.parent.children.length && location.parent.children[i] != null; i++){
                    if(location.parent.children[i] == location){
                        current = i;
                    } else if(!location.parent.children[i].isFull()){
                        if(closest == -1 || current == -1 || Math.abs(i-current) < Math.abs(closest-current)){
                            closest = i;
                        }
                    }
                }
                if(closest > current){
                    // rotate right
                    // if the object is greater than the item that would be rotated right
                    // this created an infinite loop at one point
                    if(location.biggestHash() < item.hashCode()){
                        Object temp = location.getBiggest();
                        location.removeBiggest();
                        location.insertKey(item);
                        insert(temp, location.parent);
                    } else {
                        rotateRight(location, current);
                    }
                } else {
                    // rotate left
                    // if the object is greater than the item that would be rotated right
                    // this created an infinite loop at one point
                    if(location.smallestHash() > item.hashCode()){
                        Object temp = location.getSmallest();
                        location.removeSmallest();
                        location.insertKey(item);
                        insert(temp, location.parent);
                    } else {
                        rotateLeft(location, current);
                    }
                }

            } 
            // ancestor with room
            else {
                splitToParent(location);
            }

            // split if the leaf node is full and restart the algorithm
            /* split(location);
            insert(item); */
        } else if(location.isLeaf()){
            // add the item into this leaf
            location.insertKey(item);
        } else {
            Node nextNode = null;
            for(int i = 0; i < location.keys.length && location.keys[i] != null; i++){
                if(nextNode == null && location.keys[i].hashCode() >= item.hashCode()){
                    nextNode = location.children[i];
                }
            }
            if(nextNode == null){
                nextNode = location.children[location.count];
            }
            insert(item, nextNode);
        }
    }
    /* 
    private void split(Node e){
        if(e.parent == null){
            // split to children
            //System.out.println("Split to Children");
            splitToChildren(e);
        } else if(!e.parent.childrenFull()){
            int current = -1, closest = -1;
            for(int i = 0; i < e.parent.children.length && e.parent.children[i] != null; i++){
                if(e.parent.children[i] == e){
                    current = i;
                } else if(!e.parent.children[i].isFull()){
                    if(closest == -1 || current == -1 || Math.abs(i-current) < Math.abs(closest-current)){
                        closest = i;
                    }
                }
            }
            if(closest > current){
                //rotate right
                //System.out.println("Rotate Right");
                rotateRight(e, current);
            } else {
                //rotate left
                //System.out.println("Rotate Left");
                rotateLeft(e, current);
            }
        } else if(!e.parent.isFull()){
            // bubble to parent
            splitToParent(e);
        } else {
            split(e.parent);
            split(e);
        }
    }
    */
    private void splitToChildren(Node e){
        Node left = new Node(e.order);
        Node right = new Node(e.order);
        Object middle = null;
        for(int i = 0; i < e.keys.length; i++){
            if(i < e.order / 2){
                left.insertKey(e.keys[i]);
                left.insertChild(e.children[i]);
            } else if(i == e.order / 2){
                middle = e.keys[i];
                left.insertChild(e.children[i]);
            } else {
                right.insertKey(e.keys[i]);
                right.insertChild(e.children[i]);
            }
            e.keys[i] = null;
            e.children[i] = null;
        }
        right.insertChild(e.children[e.children.length -1]);
        e.children[e.children.length -1] = null;
        e.count = 0;
        e.insertKey(middle);
        e.insertChild(left);
        e.insertChild(right);
    }

    private void rotateRight(Node e, int index){
        if(e.parent.children[index+1].isFull()){
            rotateRight(e.parent.children[index+1], index + 1);
        }
        Object biggest = e.getBiggest();
        Object newInsert = e.parent.keys[index];
        e.parent.keys[index] = biggest;
        e.removeBiggest();
        insert(newInsert, e.parent.children[index+1]);
    }

    private void rotateLeft(Node e, int index){
        if(e.parent.children[index-1].isFull()){
            rotateLeft(e.parent.children[index-1], index-1);
        }
        Object smallest = e.getSmallest();
        Object newInsert = e.parent.keys[index-1];
        e.parent.keys[index-1] = smallest;
        e.removeSmallest();
        insert(newInsert, e.parent.children[index-1]);
    }

    private void splitToParent(Node e){
        Node right = new Node(e.order);
        Object middle = null;
        if(e.parent.isFullNode()){
            splitToParent(e.parent);
        }
        for(int i = 0; i < e.keys.length; i++){
            if(i > e.order / 2){
                right.insertKey(e.keys[i]);
                right.insertChild(e.children[i]);
                e.children[i] = null;
                e.keys[i] = null;
                e.count--;
            } else if(i == e.order / 2){
                middle = e.keys[i];
                //left.insertChild(e.children[i]);
                e.keys[i] = null;
                e.count--;
            }
        }
        right.insertChild(e.children[e.children.length -1]);
        e.children[e.children.length -1] = null;
        e.parent.insertKey(middle);
        /*
        left.parent = e.parent;
        e = left;
        */
        e.parent.insertChild(right);
    }

    private void splitTree(Node e){
        if(e.isLeaf()){
            splitToChildren(e);
        } else {
            for(Node n: e.children){
                if(n != null) splitTree(n);
            }
            if(e.parent != null){
                splitToParent(e);  
            } else {
                splitToChildren(e);
            }
        }
    }
    
    public void printAll(){
        print(root);
    }
    private void print(Node theNode){
        System.out.print("This Node has " + theNode.count + " nodes: ");
        for(int i = 0; i < theNode.keys.length; i++){
            if(theNode.keys[i] != null){
                System.out.print(theNode.keys[i] + " ");
            }
        }
        System.out.println("");
        for(int i = 0; i < theNode.children.length; i++){
            if(theNode.children[i] != null){
                print(theNode.children[i]);
            }
        }
    }

    // public call into recursive call
    public Object getSmallest(){
        return root.getSmallest();
    }

    public Object getBiggest(){
        return root.getBiggest();
    }

    public Node getNode(Object item){
        return getNode(root, item);
    }

    private Node getNode(Node loc, Object item){
        Node result = null;

        for(int i = 0; i < loc.count; i++){
            if(loc.keys[i].hashCode() == item.hashCode()){
                result = loc;
            }
        }
        if(result == null && !loc.isLeaf()){
            for(int i = 0; i < loc.count; i++){
                if(loc.keys[i].hashCode() > item.hashCode()){
                    result = loc.children[i];
                }
            }
            if(result == null){
                result = loc.children[loc.count];
            }
            result = getNode(result, item);
        }

        return result;
    }
}
