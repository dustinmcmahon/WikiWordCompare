package mcmahon.wikiWordCompare;

import java.util.ArrayList;

import java.awt.*;
import javax.swing.*;

public class GUI extends JFrame {
    int WIDTH = 700, HEIGHT = 300;
    JTextField category;
    JComboBox<String> website;
    JButton clearBTN,okBTN;
    JLabel result1, result2;

    public void initialize(ArrayList<Long> pages){

        JPanel initialPanel = new JPanel();
        initialPanel.setLayout(new BorderLayout());
        initialPanel.add(inputPanel(pages), BorderLayout.NORTH);
        initialPanel.add(resultPanel(), BorderLayout.CENTER);
        initialPanel.add(actionButtons(),BorderLayout.SOUTH);
        
        add(initialPanel);
        setTitle("Find Wiki Pages: KeyWord or Website");
        setSize(WIDTH, HEIGHT);      
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // display the results in the results frame
    public void displayResults(ArrayList<ParsePage> results){
        if(results.size() == 0){
            result1.setText("No Result Found");
        } 
        if(results.size() >= 1){
            result1.setText(results.get(0).title + " :: " + results.get(0).url);
        } 
        if (results.size() >= 2) {
            result2.setText(results.get(1).title + " :: " + results.get(1).url);
        }
    }

    public void clear(){
        result1.setText("");
        result2.setText("");
        category.setText("");
        website.setSelectedIndex(0);
    }

    //This needs to be updated, right now it is just writing the location
    private JPanel inputPanel(ArrayList<Long> pages){
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
            website.addItem(pages.get(i).toString());
        }

        inputPanel.add(catLabel);
        inputPanel.add(category);
        inputPanel.add(siteLabel);
        inputPanel.add(website);

        return inputPanel;
    }

    private JPanel resultPanel(){
        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        result1 = new JLabel("");
        result2 = new JLabel("");

        resultPanel.add(result1);
        resultPanel.add(result2);

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
