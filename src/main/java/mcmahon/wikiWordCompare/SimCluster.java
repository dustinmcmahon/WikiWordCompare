package mcmahon.wikiWordCompare;

import java.io.File;

public class SimCluster {
    
    static final class Node{
        double similarity;
        String title;
        String url;
        File dataLocation;

        Node(ParsePage page){
            title = page.title;
            url = page.url;
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

    public void addCenter(ParsePage thePage){
        if(centerCount < numCenterNodes - 1){
            centerNodes[centerCount] = thePage;
            centerNodeTrees[centerCount] = new ObjBTree(5);
            centerCount++;
        }
    }
    
    public void insert(ParsePage newPage){
        if(centerCount == numCenterNodes -1){
            double[] similarities = new double[numCenterNodes];
            for(int i = 0; i < numCenterNodes; i++){
                similarities[i] = centerNodes[i].wordMap.cosSimilarity(newPage.wordMap);
            }
            int lowIndex = 0;
            for(int i = 1; i < numCenterNodes; i++){
                if(similarities[lowIndex] > similarities[i]){
                    lowIndex = i;
                }
            }
            centerNodeTrees[lowIndex].insert(new Node(newPage));
        } else {
            System.out.println("Not enough center Nodes");
        }
    }

}
