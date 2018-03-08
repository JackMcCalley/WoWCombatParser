import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by John McCalley.
 */
public class DPSMeter extends JFrame
{
    final int windowHeight = 450;
    final int windowWidth = 700;
    private JLabel nameMessage;
    private JPanel options = new JPanel();
    private JPanel output = new JPanel();
    private JPanel nameEntry = new JPanel();
    private JPanel load = new JPanel();
    private JTextField enterName;
    private JLabel introMessage;
    private String name;
    String paraDMG = "SPELL_DAMAGE,Player";
    String periodic = "SPELL_PERIODIC_DAMAGE,Player";
    String healing = "SPELL_HEAL,Player";
    String absorbed = "SPELL_ABSORBED";
    String dmgTaken = "DAMAGE,Creature";

    JTextArea outputText;

    File combatLog = new File("C:\\Program Files (x86)\\World of Warcraft\\Logs\\WoWCombatLog.txt");
    File charSift = new File("C:\\Users\\Jack\\IdeaProjects\\WoWCombatLogger\\src\\charSift.txt");
    File nameSearch = new File("C:\\Users\\Jack\\IdeaProjects\\WoWCombatLogger\\src\\nameSearch.txt");
    File damage = new File("C:\\Users\\Jack\\IdeaProjects\\WoWCombatLogger\\src\\damage.txt");
    File HealingDone = new File("C:\\Users\\Jack\\IdeaProjects\\WoWCombatLogger\\src\\HealingDone.txt");
    File DamageReceived = new File("C:\\Users\\Jack\\IdeaProjects\\WoWCombatLogger\\src\\DamageReceived.txt");

    /**
     * Constructor
     */

    public DPSMeter()
    {
        //Set the title
        setTitle("World of Warcraft Combat Logger");

        //Set the size of the window.
        setSize(windowWidth, windowHeight);

        //Close Operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //build the panels
        buildPanels();

        //Add panel to content pane
        add(nameEntry);
        add(options);
        add(output);
        add(load);

        //Display the window.
        setVisible(true);
    }

    /**
     * This method builds the panels into the content pane
     */
    private void buildPanels()
    {
        //Add a BorderLayout manager to the content pane
        setLayout(new BorderLayout());

        //Get name of character
        nameMessage = new JLabel("Please enter your character's name: ");
        enterName = new JTextField(15);
        JButton enter = new JButton("ENTER");

        //add to name entry panel
        nameEntry.add(nameMessage);
        nameEntry.add(enterName);
        nameEntry.add(enter);

        //add an Action listener to the enter button
        enter.addActionListener(new enterListener());

        //FlowLayout
        setLayout(new FlowLayout(FlowLayout.CENTER));

        //Create the Buttons
        JButton dps = new JButton("Damage Per Second");
        dps.addActionListener(new dpsListener());
        JButton totalDamage = new JButton("Total Damage");
        totalDamage.addActionListener(new totListener());
        JButton dmgReceived = new JButton("Damage Received");
        dmgReceived.addActionListener(new dmgRecListener());
        JButton Healing = new JButton("Total Healing");
        Healing.addActionListener(new healingListener());

        //Add buttons to box
        Box buttonBox = Box.createVerticalBox();
        buttonBox.add(dps);
        buttonBox.add(totalDamage);
        buttonBox.add(dmgReceived);
        buttonBox.add(Healing);
        options.add(buttonBox);

        //create output text field
        outputText = new JTextArea();
        outputText.setPreferredSize(new Dimension(500,200));
        output.add(outputText);

        //Create the load file button
        JButton fileLoad = new JButton("Load Combat Log File");
        load.add(fileLoad);
        fileLoad.addActionListener(new loadListener());

        //Add panels to content pane
        add(nameEntry, BorderLayout.NORTH);
        add(options, BorderLayout.WEST);
        add(output, BorderLayout.CENTER);
        add(load, BorderLayout.SOUTH);

    }


    /**
     * Constructor for the Combat Log loader
     */
    private class loadListener implements ActionListener
    {
        public void actionPerformed(ActionEvent ld)
        {
            try
            {
                loadFile();
            }
            catch(IOException ex)
            {

            }
        }
    }

    /**
     * This method copies data from the WoWCombatLog to the charSift.txt file
     * @throws IOException
     */

    public void loadFile() throws IOException
    {
        InputStream logFile;
        OutputStream sift;

        try
        {
            PrintWriter reset = new PrintWriter(charSift);
            reset.print("");
            reset.close();

            logFile = new FileInputStream(combatLog);
            sift = new FileOutputStream(charSift);
            byte[] b = new byte [1024];
            int length;

            while ((length = logFile.read(b)) > 0)
            {
                sift.write(b,0, length);
            }
            logFile.close();
            sift.close();
            String message = "\nCombat Log Loaded. Please enter your character name.";
            outputText.append(message);

            PrintWriter clear = new PrintWriter(combatLog);
            clear.print("");
            clear.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * enterListener adds an action listener to the enter button
     */
    private class enterListener implements ActionListener
    {
        /**
         * actionPerformed method executes when the user clicks the Enter button
         */
        public void actionPerformed(ActionEvent e)
        {
            String input;
            input = enterName.getText();
            name = input;
            boolean valid = false;
            Scanner scan;
            Scanner scan2;
            String word;
            String transfer;

            try
            {
                PrintWriter clear = new PrintWriter(nameSearch);
                clear.print("");
                clear.close();
                scan = new Scanner(new FileReader(charSift));
                scan2 = new Scanner(new FileReader(charSift));
                PrintWriter copy = new PrintWriter(nameSearch);
                while (scan2.hasNextLine())
                {
                    transfer = scan2.nextLine();
                    if (transfer.contains(name.trim()))
                    {
                        copy.println(transfer);
                    }
                }

                while (scan.hasNext() && !valid)
                {
                    word = scan.nextLine();
                    if (word.contains(name.trim()))
                    {
                        valid = true;
                    }
                    else
                    {
                       valid = false;
                    }
                }

                copy.close();
            }
            catch(IOException iv)
            {

            }
            if(valid)
            {
                String message = "\nWCL will now show data for: " + name;
                outputText.append(message);
            }
            else
            {
                outputText.append("\nName not found. Please enter your character name correctly.");
            }
        }
    }

    /**
     * This method creates an ActionListener for the Total Damage button, and
     * contains the algorithm for parsing damage from the damage.txt file
     */

    private class totListener implements ActionListener
    {
        public void actionPerformed(ActionEvent d)
        {
            String line;
            Scanner scan;
            Scanner scan2;
            boolean valid = false;
            int totalDamage = 0;
            int tot = 0;

            try
            {
                PrintWriter clear = new PrintWriter(damage);
                clear.print("");
                clear.close();
                PrintWriter copy = new PrintWriter(damage);
                scan = new Scanner(new FileReader(nameSearch));
                scan2 = new Scanner(new FileReader(nameSearch));
                while (scan.hasNextLine())
                {
                    line = scan.nextLine();
                    if(line.contains(paraDMG.trim()) || line.contains(periodic))
                    {
                        copy.println(line);
                    }
                }
                copy.close();

                while (scan2.hasNext() && !valid)
                {
                    line = scan2.nextLine();
                    if(line.contains(paraDMG) || line.contains(periodic))
                    {
                        valid = true;
                    }
                    else
                    {
                        valid = false;
                    }
                }

                //Create ArrayList of all the damage done
                Scanner scan3 = new Scanner(damage);
                double intline;
                ArrayList<Double> dmgList = new ArrayList<>();
                while(scan3.hasNextLine())
                {
                    line = scan3.nextLine();
                    intline = Double.parseDouble(line.split(",")[25]);
                    dmgList.add(intline);
                }
                for(int i = 0; i < dmgList.size(); i++)
                {
                    totalDamage += dmgList.get(i);
                }
                tot = totalDamage;

            }
            catch(IOException dmg) {}

            if(valid)
            {
                DecimalFormat myformat = new DecimalFormat("#,###");
                String message = "\nTotal damage during encounter is: " + (myformat.format(tot));
                outputText.append(message);
            }
            else
            {
                String message = "\nNo damage found in file. Please upload a file that includes combat information.";
                outputText.append(message);
            }
        }
    }

    /**
     * This is a constructor for the dmgReceived class
     */

    private class dmgRecListener implements ActionListener
    {
        public void actionPerformed(ActionEvent a)
        {

        }
    }

    private class dpsListener implements ActionListener
    {
        public void actionPerformed(ActionEvent t)
        {
            try
            {
                dmgReceived();
            }
            catch(IOException rec)
            {

            }
        }
    }

    public void dmgReceived() throws IOException
    {
        PrintWriter write = new PrintWriter(DamageReceived);
        Scanner scan = new Scanner(nameSearch);
        String line;
        boolean valid = false;
        int totDmgRec = 0;
        int total;

        while (scan.hasNextLine())
        {
            line = scan.nextLine();
            if(line.contains(dmgTaken) || line.contains(absorbed))
            {
                write.println(line);
                valid = true;
            }

        }
    }

    /**
     * This is a constructor for the getHealing method, and
     * implements an Action Listener to the Healing Done button
     */

    private class healingListener implements ActionListener
    {
        public void actionPerformed(ActionEvent h)
        {
            try
            {
                getHealing();
            }
            catch(IOException he)
            {

            }
        }
    }

    /**
     * This method contains the algorithm for calculating the
     * total healing done from the HealingDone.txt file
     * @throws IOException
     */

    public void getHealing() throws IOException
    {
        PrintWriter write = new PrintWriter(HealingDone);
        Scanner scan = new Scanner(nameSearch);
        String line;
        boolean valid = false;
        int totalHealing = 0;
        int total;
        while(scan.hasNextLine())
        {
            line = scan.nextLine();
            if(line.contains(healing))
            {
                write.println(line);
                valid = true;
            }
        }
        write.close();

        //Create an array of the healing done
        double intline;
        ArrayList<Double> healingDone = new ArrayList<>();
        Scanner scan2 = new Scanner(HealingDone);
        while(scan2.hasNextLine())
        {
            line = scan2.nextLine();
            intline = Double.parseDouble(line.split(",")[25]);
            healingDone.add(intline);
        }
        for(int i = 0; i < healingDone.size(); i++)
                {
                    totalHealing += healingDone.get(i);
                }
        total = totalHealing;

        if(valid)
        {
            DecimalFormat myFormat = new DecimalFormat("#,###");
            String message = "\nTotal healing for the encounter is: " + myFormat.format(total);
            outputText.append(message);
        }
        else
        {
            String message = "\nNo healing was done for this encounter.";
            outputText.append(message);
        }
    }

    /**
     * main method
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException
    {
        new DPSMeter();
    }
}