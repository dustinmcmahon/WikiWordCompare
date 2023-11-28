package mcmahon.wikiWordCompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SimCluster {
    
    static final class SimNode implements java.io.Serializable {
        double similarity;
        String title;
        String url;
        Long pageIndex;

        SimNode(long pageIndex, double sim, String title, String url){
            similarity = sim;
            this.title = title;
            this.url = url;
            this.pageIndex = pageIndex;
        }

        private void writeObject(ObjectOutputStream s) throws Exception {
            s.defaultWriteObject();
            s.writeDouble(similarity);
            s.writeLong(pageIndex);
            s.writeObject(title);
            s.writeObject(url);
        }

        private void readObject(ObjectInputStream s) throws Exception {
            s.defaultReadObject();
            similarity = s.readDouble();
            pageIndex = s.readLong();
            title = (String)s.readObject();
            url = (String)s.readObject();
        }

        // this is not right but it will work for now
        // because i built an object b-tree rather than one based off this class
        // i generalized it to use a hashCode as the comparison
        public int hashCode(){
            return (int)(similarity * 100000);
        }
    }

    public static final double SIMILARITY_THRESHOLD = .000001;
    protected int numCenterNodes;
    protected ParsePage[] centerNodes;
    protected Long[] centerIndexes;
    protected ObjBTree[] centerNodeTrees;
    private int centerCount = 0;
    int failCount = 0;
    
    SimCluster(int countCenterNodes){
        numCenterNodes = countCenterNodes;
        centerNodes = new ParsePage[numCenterNodes];
        centerNodeTrees = new ObjBTree[numCenterNodes];
        centerIndexes = new Long[numCenterNodes];
    }

    public boolean isCenter(long location){
        for(int i = 0; i < centerCount; i++){
            if(location == centerIndexes[i]) return true;
        }
        return false;
    }

    public void addCenter(ParsePage thePage, long location){
        if(centerCount < numCenterNodes){
            centerNodes[centerCount] = thePage;
            centerIndexes[centerCount] = location;
            centerNodeTrees[centerCount] = new ObjBTree(5);
            centerCount++;
        }
    }
    
    public void insert(long index){
        // we have all center nodes and this is not a center node
        if(centerCount == numCenterNodes && !isCenter(index)){
            ParsePage thePage = ParsePage.getPage(new File(App.PAGE_DATA), index);
            int bestIndex = -1;
            double bestSim = -1;
            for(int i = 0; i < centerNodes.length; i++){
                double tempSim = centerNodes[i].wordMap.cosSimilarity(thePage.wordMap);
                if(tempSim > bestSim){
                    bestIndex = i;
                    bestSim = tempSim;
                }
            }
            if(Double.compare(bestSim, SIMILARITY_THRESHOLD) > 0){
                SimNode pageContainer = new SimNode(index, bestSim, thePage.title, thePage.url);
                centerNodeTrees[bestIndex].insert(pageContainer);
            } else {
                failCount++;
            }
        }
    }

    public boolean isBetter(SimCluster cluster){
        if(this.failCount > cluster.failCount) return false;
        else if(this.failCount < cluster.failCount) return true;
        return this.getDistance() > cluster.getDistance();
    }

    private double getDistance(){
        double avgRange = 0;
        if(centerCount == 0) return 0;
        for(int i = 0; i < centerNodeTrees.length; i++){
            if(centerNodeTrees[i].root.count > 0){
                avgRange += centerNodeTrees[i].getBiggest().hashCode() - centerNodeTrees[i].getSmallest().hashCode();
            }
        }
        return avgRange / centerCount;
    }
}
