package mcmahon.wikiWordCompare;

import java.util.ArrayList;

import java.awt.*;

import javax.swing.*;

public class GUI extends JFrame {
    int WIDTH = 700, HEIGHT = 300;
    JTextField category;
    JComboBox<String> website;
    JButton clearBTN,okBTN;
    JPanel resultsPanel;
    JLabel simResult1, simResult2, clusterCenterLabel, clusterClosestLabel;
    ArrayList<String> titleList = new ArrayList<String>();

    public void initialize(ArrayList<String> pages){

        JPanel initialPanel = new JPanel();
        initialPanel.setLayout(new BorderLayout());
        initialPanel.add(inputPanel(pages), BorderLayout.NORTH);
        resultsPanel = resultPanel();
        initialPanel.add(resultsPanel, BorderLayout.CENTER);
        initialPanel.add(actionButtons(),BorderLayout.SOUTH);
        
        add(initialPanel);
        setTitle("Find Wiki Pages: KeyWord or Website");
        setSize(WIDTH, HEIGHT);      
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // display the results in the results frame
    public void displaySimResults(ArrayList<ParsePage> results){  
        simResult1.setText("");      
        simResult2.setText("");      
        if(results.size() == 0){
            simResult1.setText("No Result Found");
        } 
        if(results.size() >= 1){
            simResult1.setText(results.get(0).title + " :: " + results.get(0).url);
        } 
        if (results.size() >= 2) {
            simResult2.setText(results.get(1).title + " :: " + results.get(1).url);
        }
    }

    public void displayClusterResults(ParsePage clusterCenter, ParsePage closest){
        clusterCenterLabel.setText("");
        clusterClosestLabel.setText("");        
        if(clusterCenter == null){
            clusterCenterLabel.setText("No Cluster Found");
        } else if(clusterCenter != null){
            clusterCenterLabel.setText("Cluster Center: " + clusterCenter.title + " :: " + clusterCenter.url);
        } 
        if(closest != null) {
            clusterClosestLabel.setText("Closest in Cluster: " + closest.title + " :: " + closest.url);
        }
    }

    public void clear(){
        simResult1.setText("");
        simResult2.setText("");
        clusterCenterLabel.setText("");
        clusterClosestLabel.setText("");
        category.setText("");
        website.setSelectedIndex(0);
    }

    //This needs to be updated, right now it is just writing the location
    private JPanel inputPanel(ArrayList<String> pages){
        JPanel inputPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        JLabel catLabel = new JLabel("Key Word:");
        catLabel.setLayout(new BorderLayout(10,10));
        JLabel siteLabel = new JLabel("Website:");
        siteLabel.setLayout(new BorderLayout(10,10));

        category = new JTextField();
        category.setLayout(new BorderLayout(10,10));
        website = new JComboBox<String>();
        website.setLayout(new BorderLayout(10,10));
        website.addItem("");
        for(int i = 0; i < pages.size(); i++){
            website.addItem(pages.get(i));
        }

        inputPanel.add(catLabel);
        inputPanel.add(category);
        inputPanel.add(siteLabel);
        inputPanel.add(website);

        return inputPanel;
    }

    private JPanel resultPanel(){
        JPanel resultPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        simResult1 = new JLabel("");
        simResult2 = new JLabel("");

        resultPanel.add(simResult1);
        resultPanel.add(simResult2);

        clusterCenterLabel = new JLabel("");
        clusterClosestLabel = new JLabel("");

        resultPanel.add(clusterCenterLabel);
        resultPanel.add(clusterClosestLabel);

        return resultPanel;
    }

    private JPanel actionButtons(){
        JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,5));

        clearBTN = new JButton("Clear");
        okBTN = new JButton("Ok");
        
        buttonPanel.add(clearBTN);
        buttonPanel.add(okBTN);

        return buttonPanel;
    }
}
