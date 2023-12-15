package mcmahon.wikiWordCompare;

import java.util.ArrayList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class GUI extends JFrame {
    int WIDTH = 1600,HEIGHT = 800;
    JTextField category;
    JComboBox<String> website, simWebsite1, simWebsite2;
    JButton clearBTN, okBTN, graphButton, simButton, clearSimBTN, okSimBTN;
    JPanel resultsPanel, graphResultPanel;
    JLabel simResult1, simResult2, clusterCenterLabel, clusterClosestLabel;
    ArrayList<String> titleList = new ArrayList<String>();

    JPanel simPanel,graphPanel;

    boolean graphShown;

    public void initializeSim(ArrayList<String> pages){
        graphShown = false;
        if(graphPanel != null) graphPanel.setVisible(false);
        if(simPanel != null){
            simPanel.setVisible(true);
        } else {
            simPanel = new JPanel();
            simPanel.setLayout(new BorderLayout());
            simPanel.add(inputPanel(pages), BorderLayout.NORTH);
            resultsPanel = resultPanel();
            simPanel.add(resultsPanel, BorderLayout.CENTER);
            simPanel.add(actionButtons(),BorderLayout.SOUTH);
            
            add(simPanel);
            setTitle("Find Wiki Pages: KeyWord or Website");
            setSize(WIDTH, HEIGHT);      
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
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
        JPanel buttonPanel = new JPanel(new GridLayout(1,3,5,5));

        clearBTN = new JButton("Clear");
        okBTN = new JButton("Ok");
        graphButton = new JButton("Graph Info");

        okBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                App.okButtonPressed();
            }
        });

        clearBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                clear();
            }
        });

        graphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                initializeGraph(App.graph, App.siteTitleList);
            }
        });
        
        buttonPanel.add(graphButton);
        buttonPanel.add(clearBTN);
        buttonPanel.add(okBTN);

        return buttonPanel;
    }

    public void initializeGraph(WikiGraph g, ArrayList<String> pages){
        setVisible(false);
        graphShown = true;

        if(simPanel != null) simPanel.setVisible(false);
        if(graphPanel != null){
            graphPanel.setVisible(true);
        } else {
            graphPanel = new JPanel();
            graphPanel.setLayout(new BorderLayout());

            // insert into the panel
            graphPanel.add(graphInputs(pages), BorderLayout.NORTH);
            graphResultPanel = graphPanel(null);
            graphPanel.add(graphResultPanel, BorderLayout.CENTER);
            graphPanel.add(graphButtons(), BorderLayout.SOUTH);

            add(graphPanel);
            setTitle("Wiki Graph Information");
            setSize(WIDTH, HEIGHT);      
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }

        setVisible(true);
    }

    private JPanel graphInputs(ArrayList<String> pages){
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        simWebsite1 = new JComboBox<String>();
        simWebsite1.setLayout(new BorderLayout(10,10));
        simWebsite1.addItem("");
        simWebsite2 = new JComboBox<String>();
        simWebsite2.setLayout(new BorderLayout(10,10));
        simWebsite2.addItem("");

        for(String s: pages){
            simWebsite1.addItem(s);
            simWebsite2.addItem(s);
        }

        inputPanel.add(simWebsite1);
        inputPanel.add(simWebsite2);

        return inputPanel;
    }

    private JPanel graphPanel(ArrayList<String> results){
        // initial state
        if(results == null) return new JPanel();

        int rows = (results.size()*2) - 1;
        if(results.size() == 0) rows = 1;
        JPanel resultPanel = new JPanel(new GridLayout(rows, 1, 5, 5));
        JLabel label;
        if(results.size() == 0){
            label = new JLabel("No Results");
            label.setHorizontalAlignment(JLabel.CENTER);
            resultPanel.add(label);
        } else {
            for(int i = 0; i < results.size(); i++){
                label = new JLabel(results.get(i));
                label.setHorizontalAlignment(JLabel.CENTER);
                resultPanel.add(label);
                if(i < results.size()-1)
                    label = new JLabel("to");
                    label.setHorizontalAlignment(JLabel.CENTER);
                    resultPanel.add(label);
            }
        }
        return resultPanel;
    }

    private JPanel graphButtons(){
        JPanel buttonPanel = new JPanel(new GridLayout(1,3,5,5));

        clearSimBTN = new JButton("Clear");
        okSimBTN = new JButton("Ok");
        simButton = new JButton("Similarities");

        okSimBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                ArrayList<String> res = App.getPath(simWebsite1.getSelectedItem().toString(), simWebsite2.getSelectedItem().toString());
                graphPanel.setVisible(false);
                graphPanel.remove(graphResultPanel);
                graphResultPanel = graphPanel(res);
                graphPanel.add(graphResultPanel, BorderLayout.CENTER);
                graphPanel.setVisible(true);
            }
        });

        clearSimBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                clearGraph();
            }
        });
        
        simButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                initializeSim(App.siteTitleList);
            }
        });
        
        buttonPanel.add(simButton);
        buttonPanel.add(clearSimBTN);
        buttonPanel.add(okSimBTN);

        return buttonPanel;
    }

    public void clearGraph(){
        graphPanel.setVisible(false);
        simWebsite1.setSelectedIndex(0);
        simWebsite2.setSelectedIndex(0);
        graphPanel.remove(graphResultPanel);
        graphPanel.setVisible(true);
    }
}
