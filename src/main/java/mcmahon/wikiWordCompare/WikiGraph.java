package mcmahon.wikiWordCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WikiGraph {
    
    static final class MapNode{

        long location;
        MapNode[] edges = new MapNode[4];
        int edgeCount = 0;
        // this is just used for shortest path
        boolean visited = false;
        MapNode parent = null;

        public MapNode(long wikiLocation){
            this.location = wikiLocation;
        }

        public void addEdge(MapNode newEdge){
            if(!isFull()){
                edges[edgeCount] = newEdge;
                edgeCount++;
            }
        }

        public boolean isFull(){
            return edgeCount == 4;
        }

        /**
         * Basic equals function to make sure there arent any duplicates in the map
         * @param e
         * @return
         */
        public boolean equals(Object e){
            if(e.getClass() != this.getClass()) return false;
            MapNode temp = (MapNode)e;
            return this.location == temp.location;
        }

        /**
         * this is a depth first search that seems to be getting me a much longer path than it should be
         * @param destination
         * @return
         */
        public boolean canReach(MapNode destination){
            if(this.equals(destination)) return true;
            if(this.visited) return false;
            this.visited = true;
            for(MapNode n: edges){
                if(n != null && !n.visited){
                    n.parent = this;
                    if(n.canReach(destination)) return true;
                }
            }
            return false;
        }
    }

    static final class EdgeNode{

        long to;
        long from;

        public EdgeNode(long to, long from){
            this.to = to;
            this.from = from;
        }

        public boolean equals(Object e){
            if(e.getClass() != this.getClass()) return false;
            EdgeNode temp = (EdgeNode)e;
            return (this.to == temp.to && this.from == temp.from) || (this.from == temp.to && this.to == temp.from);
        }
    }

    ArrayList<MapNode> nodes;
    ArrayList<EdgeNode> edges;
    int setCount = 0;

    public WikiGraph(){
        nodes = new ArrayList<MapNode>();
        edges = new ArrayList<EdgeNode>();
    }

    public WikiGraph(File indexFile, File siteFile, int size){
        // call the default constructor
        this();

        // build the map
        build(indexFile,siteFile,size);
        
        // set the number of disjoined sets
        setDisjoinedCount();
    }

    /**
     * Public Wrapper Function to catch the exceptions thrown by the private version of this function
     * 
     * @param indexFile
     * @param siteFile
     * @param size
     */
    public void build(File indexFile, File siteFile, int size){
        try{
            _build(indexFile, siteFile, size);
        } catch(FileNotFoundException e){
            System.out.println("Map Input File Does Not Exist");
            e.printStackTrace();
        } catch(IOException e){
            System.out.println("File Read Failed");
            e.printStackTrace();
        }
    }

    /**
     * Private function to do the work of the import without needing to worry about error handling
     * @param indexFile
     * @param siteFile
     * @param size
     * @throws FileNotFoundException,IOException
     */
    private void _build(File indexFile, File siteFile, int size) throws FileNotFoundException,IOException{
        ParsePage current,temp;
        double[] bestSim = new double[4];
        long[] bestIndex = new long[4];
        double currentSim;

        FileInputStream indexStream = new FileInputStream(indexFile);
        FileChannel indexChannel = indexStream.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);

        // get a list of indexes from the indexFile
        ArrayList<Long> fileLocations = new ArrayList<Long>();
        for(int i = 0; i < size; i++){
            bb.position(0);
            indexChannel.read(bb);
            bb.position(0);
            fileLocations.add(bb.getLong());
        }
        indexStream.close();

        // keep going until all nodes have been chosen
        while(fileLocations.size() > 0){
            for(int i = 0; i < 4; i++){
                // clear the best arrays
                // using .000001 because comparing with doubles (0 == 0) is unreliable
                bestSim[i] = .000001;
                bestIndex[i] = -1;
            }
            // pick a random page from the file, read the page from the fileLocation, then remove it from the list
            int index = ThreadLocalRandom.current().nextInt(fileLocations.size());
            long currFileIndex = fileLocations.get(index);
            current = ParsePage.getPage(siteFile, currFileIndex);
            fileLocations.remove(index);

            // node's location if it exists
            MapNode currentNode = new MapNode(currFileIndex);
            int nodeIndex = nodes.indexOf(currentNode);

            // if it exists, use the already created node, or insert the new one
            if(nodeIndex != -1){
                currentNode = nodes.get(nodeIndex);
            } else {
                addNode(currentNode);
            }

            // go through all remaining indexes to find the 4 most similar
            for(int i = 0; i < fileLocations.size(); i++){
                long tempIndex = fileLocations.get(i);
                temp = ParsePage.getPage(siteFile, tempIndex);
                currentSim = current.wordMap.cosSimilarity(temp.wordMap);
                // check to see if this similarity is better than one that was already mapped
                for(int k = 0; k < 3; k++){
                    if(currentSim > bestSim[k]){
                        double tSim1 = bestSim[k];
                        long tIndex1 = bestIndex[k];
                        // shift array over to ensure it is sorted keeping the best 4
                        for(int j = k+1; j < 4; j++){
                            double tSim2 = bestSim[j];
                            long tIndex2 = bestIndex[j];

                            bestSim[j] = tSim1;
                            bestIndex[j] = tIndex1;

                            tSim1 = tSim2;
                            tIndex1 = tIndex2;
                        }
                        bestSim[k] = currentSim;
                        bestIndex[k] = tempIndex;
                        break;
                    }
                }
            }

            for(int i = 0; i < 4 && !currentNode.isFull(); i++){
                if(bestIndex[i] == -1) break;
                MapNode tempMapNode = new MapNode(bestIndex[i]);
                int tempNodeIndex = nodes.indexOf(tempMapNode);
                // if tempnode has been added, retrieve it, if not add it
                if(tempNodeIndex != -1){
                    tempMapNode = nodes.get(tempNodeIndex);
                } else {
                    addNode(tempMapNode);
                }
                // add edges to both Map Nodes
                tempMapNode.addEdge(currentNode);
                currentNode.addEdge(tempMapNode);
                if(tempMapNode.isFull()){
                    // remove this website from possible websites if it is full
                    fileLocations.remove(new Long(bestIndex[i]));
                }
                addEdge(new EdgeNode(currFileIndex, bestIndex[i]));
            }
        }
    }

    /**
     * insert a node into the map
     */
    private void addNode(MapNode node){
        nodes.add(node);
    }

    /**
     * insert an edge into the map
     */
    private void addEdge(EdgeNode edge){
        edges.add(edge);
    }
    
    /**
     * Get and set the number of disjoined sets
     * iterate over all nodes, if they have not been visited yet do a breadth-first traversal starting at the
     */
    private void setDisjoinedCount(){
        for(MapNode e: nodes){
            if(!e.visited){
                ArrayList<MapNode> currentSet = new ArrayList<MapNode>();
                currentSet.add(e);
                while(currentSet.size() > 0){
                    MapNode currentNode = currentSet.get(0);
                    if(!currentNode.visited){
                        currentNode.visited = true;
                        for(int i = 0; i < currentNode.edgeCount; i++){
                            currentSet.add(currentNode.edges[i]);
                        }
                    }
                    currentSet.remove(0);
                }
                this.setCount++;
            }
        }

        clearVisitedNodes();
    }

    public void clearVisitedNodes(){
        for(MapNode e: nodes){
            e.visited = false;
        }
    }

    public void clearParents(){
        for(MapNode e: nodes){
            e.parent = null;
        }
    }

    public void clear(){
        for(MapNode e: nodes){
            e.parent = null;
            e.visited = false;
        }
    }

    /**
     * 
     * @param source
     * @param destination
     * @return if there is a path, true is returned, false if there is no path
     */
    public boolean hasPath(long source, long destination){
        clear();
        MapNode sNode = nodes.get(nodes.indexOf(new MapNode(source)));
        MapNode dNode = nodes.get(nodes.indexOf(new MapNode(destination)));
        return sNode.canReach(dNode);
    }

    public ArrayList<MapNode> shortestPath(long source, long destination){
        ArrayList<MapNode> result = new ArrayList<MapNode>();
        if(!hasPath(source, destination)) return result;
        clear();
        MapNode sNode = nodes.get(nodes.indexOf(new MapNode(source)));
        MapNode dNode = nodes.get(nodes.indexOf(new MapNode(destination)));
        MapNode tempNode;
        ArrayList<MapNode> searchList = new ArrayList<MapNode>();
        searchList.add(sNode);

        while(searchList.size() > 0){
            tempNode = searchList.get(0);
            if(tempNode.equals(dNode)) {
                break;
            }
            for(MapNode e: tempNode.edges){
                if(e != null && e.parent == null){
                    e.parent = tempNode;
                    searchList.add(e);
                }
            }
            searchList.remove(0);
        }

        tempNode = dNode;
        while(!tempNode.equals(sNode)){
            result.add(tempNode);
            tempNode = tempNode.parent;
        }
        result.add(sNode);

        return result;
    }

    /**
     * This function writes the map to a file
     * Wrapper function to catch exceptions
     */
    public void writeToFile(File nodeFile){
        if(nodes.size() == 0){
            // incase there have been no inserts into the map, there is nothing to write
            return;
        }
        try{
            _writeToFile(nodeFile);
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This function writes the map to a file
     */
    private void _writeToFile(File nodeFile) throws FileNotFoundException, IOException{
        FileOutputStream nodeStream = new FileOutputStream(nodeFile);
        FileChannel nodeChannel = nodeStream.getChannel();

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(setCount);
        bb.position(0);
        nodeChannel.write(bb);

        // for each node, enter location, edge count, then the edges
        for(MapNode e: nodes){
            bb = ByteBuffer.allocate(((Long.BYTES * (e.edgeCount + 1)) + Integer.BYTES));
            bb.putLong(e.location);
            bb.putInt(e.edgeCount);
            for(int i = 0; i < e.edgeCount; i++){
                bb.putLong(e.edges[i].location);
            }

            bb.position(0);
            nodeChannel.write(bb);
        }

        nodeStream.close();
    }

    /**
     * This function reads a Graph from a file
     * Wrapper function to catch exceptions
     */
    public static WikiGraph readFromFile(File nodeFile, int mapSize){
        try{
            return _readFromFile(nodeFile, mapSize);
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This function reads a Graph from a file
     */
    private static WikiGraph _readFromFile(File nodeFile, int mapSize) throws FileNotFoundException,IOException{
        WikiGraph result = new WikiGraph();
        
        FileInputStream inStream = new FileInputStream(nodeFile);
        FileChannel inChannel = inStream.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);

        inChannel.read(bb);
        bb.position(0);
        result.setCount = bb.getInt();

        for(int i = 0; i < mapSize; i++){
            bb = ByteBuffer.allocate(Long.BYTES + Integer.BYTES);
            inChannel.read(bb);
            bb.position(0);
            long loc = bb.getLong();
            MapNode n = new MapNode(loc);
            if(result.nodes.contains(n)){
                n = result.nodes.get(result.nodes.indexOf(n));
            } else {
                result.addNode(n);
            }
            int count = bb.getInt();
            bb = ByteBuffer.allocate(Long.BYTES * count);
            inChannel.read(bb);
            bb.position(0);
            for(int j = 0; j < count; j++){
                long e = bb.getLong();
                MapNode t = new MapNode(e);
                if(result.nodes.contains(t)){
                    t = result.nodes.get(result.nodes.indexOf(t));
                } else {
                    result.addNode(t);
                }
                n.addEdge(t);
                EdgeNode tEdge = new EdgeNode(loc, e);
                if(!result.edges.contains(tEdge))result.addEdge(tEdge);
            }
        }

        inStream.close();
        return result;
    }

    // this is just a helper function to see which index is being dropped
    // discovered duplicate sites in my list of pages, had to update some functions to ensure no duplication
    public void checkMissing(ArrayList<Long> pages){
        for(int i = 0; i < pages.size(); i++){
            MapNode temp = new MapNode(pages.get(i));
            int index = nodes.indexOf(temp);
            if(index == -1){
                System.out.println("Missing Index: " + pages.get(i));
            } else {
                //System.out.println(i + ") from pages: " + pages.get(i) + " || " + index + ") from node: " + nodes.get(index).location);
                nodes.remove(index);
            }
        }
        System.out.println(nodes.size());
    }
}
