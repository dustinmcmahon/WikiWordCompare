package mcmahon.wikiWordCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SimCluster {
    
    static final class Node implements java.io.Serializable {
        double similarity;
        String title;
        String url;
        File dataLocation;

        Node(File pageLocation, double sim, String title, String url){
            similarity = sim;
            this.title = title;
            this.url = url;
            dataLocation = pageLocation;
        }

        private void writeObject(ObjectOutputStream s) throws Exception {
            s.defaultWriteObject();
            s.writeDouble(similarity);
            s.writeObject(title);
            s.writeObject(url);
            s.writeObject(dataLocation);
        }

        private void readObject(ObjectInputStream s) throws Exception {
            s.defaultReadObject();
            similarity = s.readDouble();
            title = (String)s.readObject();
            url = (String)s.readObject();
            dataLocation = (File)s.readObject();
        }

        // this is not right but it will work for now
        // because i built an object b-tree rather than one based off this class
        // i generalized it to use a hashCode as the comparison
        public int hashCode(){
            return Double.hashCode(similarity);
        }
    }

    protected int numCenterNodes;
    protected ParsePage[] centerNodes;
    protected ObjBTree[] centerNodeTrees;
    private int centerCount = 0;
    
    SimCluster(int countCenterNodes){
        numCenterNodes = countCenterNodes;
        centerNodes = new ParsePage[numCenterNodes];
        centerNodeTrees = new ObjBTree[numCenterNodes];
    }

    public boolean isCenter(ParsePage thePage){
        for(int i = 0; i < centerCount; i++){
            if(thePage == centerNodes[i]) return true;
        }
        return false;
    }

    public void addCenter(ParsePage thePage){
        if(centerCount < numCenterNodes){
            centerNodes[centerCount] = thePage;
            centerNodeTrees[centerCount] = new ObjBTree(5);
            centerCount++;
        }
    }
    
    public void insert(File inFile){
        ParsePage temp;
        try{
            ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(inFile));
            temp = (ParsePage)inStream.readObject();
            inStream.close();
        } catch(IOException e){
            e.printStackTrace();
            return;
        } catch(ClassNotFoundException e){
            e.printStackTrace();
            return;
        }
        // we have all center nodes and this is not a center node
        if(centerCount == numCenterNodes && !isCenter(temp)){
            int bestIndex = -1;
            double bestSim = -1;
            for(int i = 0; i < centerNodes.length; i++){
                double tempSim = centerNodes[i].wordMap.cosSimilarity(temp.wordMap);
                if(tempSim > bestSim){
                    bestIndex = i;
                    bestSim = tempSim;
                }
            }
            Node pageContainer = new Node(inFile, bestSim, temp.title, temp.url);
            centerNodeTrees[bestIndex].insert(pageContainer);
        }
    }

    public boolean isBetter(SimCluster cluster){
        return this.getDistance() > cluster.getDistance();
    }

    private double getDistance(){
        double avgRange = 0;
        if(centerCount == 0) return 0;
        for(int i = 0; i < centerNodeTrees.length; i++){
            avgRange += centerNodeTrees[i].getBiggest().hashCode() - centerNodeTrees[i].getSmallest().hashCode();
        }
        return avgRange / centerCount;
    }
}
