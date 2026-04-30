//imports
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;

//Main class
public class TypingRaceGUI extends JFrame
{
    private int passageLength;
    private String passageText;

    private ArrayList<Typist> typists;
    private ArrayList<JTextPane> typistPanes;

    private JPanel typistPanel;

    private JLabel winnerLabel;

    private JComboBox<String> passageDropdown;
    private JTextField customPassageField;

    private JComboBox<Integer> typistCountDropdown;

    private JCheckBox autoCorrectBox;
    private JCheckBox caffeineBox;
    private JCheckBox nightShiftBox;

    private JComboBox<String> keyboardDropdown;

    // Task 12: custom symbols + colours
    private HashMap<String, Character> customSymbols;
    private HashMap<String, Color> typistColors;

    // WPM leaderboard
    private HashMap<String, Double> leaderboard;

    // Points + stats leaderboards
    private HashMap<String, Integer> pointsLeaderboard;
    private HashMap<String, Integer> consecutiveWins;
    private HashMap<String, Integer> racesWithoutBurnout;
    private HashMap<String, Integer> racesCompleted;

    // Burnout tracker (per race)
    private HashMap<String, Boolean> burntOutThisRace;

    // Performance metrics per race
    private HashMap<String, Integer> keystrokesAttempted;
    private HashMap<String, Integer> correctKeystrokes;
    private HashMap<String, Integer> mistypeCount;
    private HashMap<String, Integer> burnoutCount;

    // Personal best WPM
    private HashMap<String, Double> personalBestWPM;

    // Store accuracy rating before race (to show change)
    private HashMap<String, Double> oldAccuracyRating;

    // Full history (each typist -> list of results strings)
    private HashMap<String, ArrayList<String>> raceHistory;

    private long raceStartTime;
    private long raceEndTime;

    private Timer timer;

    public TypingRaceGUI()
    {
        // Default passage
        this.passageText = "The quick brown fox jumps over the lazy dog.";
        this.passageLength = passageText.length();

        typists = new ArrayList<>();
        typistPanes = new ArrayList<>();

        leaderboard = new HashMap<>();

        pointsLeaderboard = new HashMap<>();
        consecutiveWins = new HashMap<>();
        racesWithoutBurnout = new HashMap<>();
        racesCompleted = new HashMap<>();

        burntOutThisRace = new HashMap<>();

        keystrokesAttempted = new HashMap<>();
        correctKeystrokes = new HashMap<>();
        mistypeCount = new HashMap<>();
        burnoutCount = new HashMap<>();

        personalBestWPM = new HashMap<>();
        oldAccuracyRating = new HashMap<>();

        raceHistory = new HashMap<>();

        // Task 12 maps
        customSymbols = new HashMap<>();
        typistColors = new HashMap<>();

        // Window settings
        setTitle("Typing Race Simulator");
        setSize(950, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(30, 30, 30));

        // -------------------------
        // TOP CONTROL PANEL
        // -------------------------
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(6, 2));
        topPanel.setBackground(new Color(45, 45, 45));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Passage dropdown
        String[] passages = {"Short", "Medium", "Long", "Custom"};
        passageDropdown = new JComboBox<>(passages);

        // Custom passage field
        customPassageField = new JTextField();
        customPassageField.setEnabled(false);

        passageDropdown.addActionListener(e -> {
            String choice = (String) passageDropdown.getSelectedItem();
            customPassageField.setEnabled(choice.equals("Custom"));
        });

        // Typist count dropdown (2–6)
        Integer[] counts = {2, 3, 4, 5, 6};
        typistCountDropdown = new JComboBox<>(counts);

        // Keyboard dropdown
        String[] keyboards = {"Mechanical", "Membrane", "Touchscreen", "Stenography"};
        keyboardDropdown = new JComboBox<>(keyboards);

        // Modifiers
        autoCorrectBox = new JCheckBox("Autocorrect (less slide back)");
        caffeineBox = new JCheckBox("Caffeine Mode (faster but burnout)");
        nightShiftBox = new JCheckBox("Night Shift (harder)");

        autoCorrectBox.setBackground(new Color(45, 45, 45));
        caffeineBox.setBackground(new Color(45, 45, 45));
        nightShiftBox.setBackground(new Color(45, 45, 45));

        autoCorrectBox.setForeground(Color.WHITE);
        caffeineBox.setForeground(Color.WHITE);
        nightShiftBox.setForeground(Color.WHITE);

        JLabel passageLabel = new JLabel("Select Passage:");
        passageLabel.setForeground(Color.WHITE);

        JLabel customLabel = new JLabel("Custom Passage:");
        customLabel.setForeground(Color.WHITE);

        JLabel countLabel = new JLabel("Number of Typists:");
        countLabel.setForeground(Color.WHITE);

        JLabel keyboardLabel = new JLabel("Keyboard Type:");
        keyboardLabel.setForeground(Color.WHITE);

        JLabel modLabel = new JLabel("Modifiers:");
        modLabel.setForeground(Color.WHITE);

        topPanel.add(passageLabel);
        topPanel.add(passageDropdown);

        topPanel.add(customLabel);
        topPanel.add(customPassageField);

        topPanel.add(countLabel);
        topPanel.add(typistCountDropdown);

        topPanel.add(keyboardLabel);
        topPanel.add(keyboardDropdown);

        topPanel.add(modLabel);
        topPanel.add(new JLabel(""));

        topPanel.add(autoCorrectBox);
        topPanel.add(caffeineBox);

        topPanel.add(nightShiftBox);
        topPanel.add(new JLabel(""));

        add(topPanel, BorderLayout.NORTH);

        // -------------------------
        // CENTER PANEL (Typists)
        // -------------------------
        typistPanel = new JPanel();
        typistPanel.setLayout(new GridLayout(6, 1));
        typistPanel.setBackground(new Color(30, 30, 30));
        typistPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JScrollPane(typistPanel), BorderLayout.CENTER);

        // -------------------------
        // BOTTOM PANEL
        // -------------------------
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(6, 1));
        bottomPanel.setBackground(new Color(45, 45, 45));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        winnerLabel = new JLabel("Winner: None");
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton startButton = new JButton("Start Race");
        JButton pauseButton = new JButton("Pause");
        JButton resetButton = new JButton("Reset");
        JButton leaderboardButton = new JButton("View Leaderboard");
        JButton historyButton = new JButton("View History");

        startButton.setBackground(new Color(0, 150, 80));
        startButton.setForeground(Color.WHITE);

        pauseButton.setBackground(new Color(200, 140, 0));
        pauseButton.setForeground(Color.WHITE);

        resetButton.setBackground(new Color(180, 50, 50));
        resetButton.setForeground(Color.WHITE);

        leaderboardButton.setBackground(new Color(70, 70, 200));
        leaderboardButton.setForeground(Color.WHITE);

        historyButton.setBackground(new Color(120, 0, 200));
        historyButton.setForeground(Color.WHITE);

        startButton.setFocusPainted(false);
        pauseButton.setFocusPainted(false);
        resetButton.setFocusPainted(false);
        leaderboardButton.setFocusPainted(false);
        historyButton.setFocusPainted(false);

        bottomPanel.add(winnerLabel);
        bottomPanel.add(startButton);
        bottomPanel.add(pauseButton);
        bottomPanel.add(resetButton);
        bottomPanel.add(leaderboardButton);
        bottomPanel.add(historyButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // -------------------------
        // TIMER LOOP
        // -------------------------
        timer = new Timer(200, e -> {

            for (Typist t : typists)
            {
                advanceTypist(t);
            }

            updateDisplay();

            if (raceFinished())
            {
                raceEndTime = System.currentTimeMillis();

                awardPoints();

                Typist winner = getWinner();
                winnerLabel.setText("Winner: " + winner.getName());

                showResults(winner);

                timer.stop();
            }
        });

        // -------------------------
        // PAUSE/RESUME BUTTON
        // -------------------------
        pauseButton.addActionListener(ev -> {
            if (timer.isRunning())
            {
                timer.stop();
                pauseButton.setText("Resume");
            }
            else
            {
                timer.start();
                pauseButton.setText("Pause");
            }
        });

        // -------------------------
        // RESET BUTTON
        // -------------------------
        resetButton.addActionListener(ev -> {
            timer.stop();

            for (Typist t : typists)
            {
                t.resetToStart();
            }

            winnerLabel.setText("Winner: None");
            pauseButton.setText("Pause");

            updateDisplay();
        });

        // -------------------------
        // LEADERBOARD BUTTON
        // -------------------------
        leaderboardButton.addActionListener(ev -> {
            JOptionPane.showMessageDialog(this,
                    "--- BEST WPM LEADERBOARD ---\n" + getLeaderboardText()
                            + "\n\n--- GLOBAL POINTS LEADERBOARD ---\n"
                            + getPointsLeaderboardText(),
                    "Leaderboards",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // -------------------------
        // HISTORY BUTTON
        // -------------------------
        historyButton.addActionListener(ev -> {

            String text = "";

            for (String name : raceHistory.keySet())
            {
                text += "==== " + name + " ====\n";

                for (String entry : raceHistory.get(name))
                {
                    text += entry + "\n";
                }

                text += "\n";
            }

            if (text.length() == 0)
            {
                text = "No race history yet.";
            }

            JOptionPane.showMessageDialog(this, text, "Race History", JOptionPane.INFORMATION_MESSAGE);
        });

        // -------------------------
        // START BUTTON
        // -------------------------
        startButton.addActionListener(ev -> {

            setupPassage();
            setupTypists();

            winnerLabel.setText("Winner: None");
            pauseButton.setText("Pause");

            updateDisplay();

            raceStartTime = System.currentTimeMillis();
            timer.start();
        });
    }

    private void setupPassage()
    {
        String choice = (String) passageDropdown.getSelectedItem();

        if (choice.equals("Short"))
        {
            passageText = "Fast typing race!";
        }
        else if (choice.equals("Medium"))
        {
            passageText = "The quick brown fox jumps over the lazy dog.";
        }
        else if (choice.equals("Long"))
        {
            passageText = "Typing is a useful skill that improves speed, accuracy, and productivity over time.";
        }
        else if (choice.equals("Custom"))
        {
            passageText = customPassageField.getText();

            if (passageText.length() == 0)
            {
                passageText = "Default custom passage.";
            }
        }

        passageLength = passageText.length();
    }

    // -------------------------
    // UPDATED SETUP TYPISTS (TASK 12)
    // -------------------------
    private void setupTypists()
    {
        typists.clear();
        typistPanes.clear();
        typistPanel.removeAll();
        burntOutThisRace.clear();

        int count = (int) typistCountDropdown.getSelectedItem();

        for (int i = 1; i <= count; i++)
        {
            String name = "TYPER_" + i;

            // Ask for symbol only once
            if (!customSymbols.containsKey(name))
            {
                String symbolInput = JOptionPane.showInputDialog(this,
                        "Enter a symbol for " + name + " (example: @, #, ★, ☺):");

                char symbol;

                if (symbolInput == null || symbolInput.length() == 0)
                {
                    symbol = (char) ('①' + (i - 1));
                }
                else
                {
                    symbol = symbolInput.charAt(0);
                }

                customSymbols.put(name, symbol);
            }

            // Ask for colour only once
            if (!typistColors.containsKey(name))
            {
                Color chosenColor = JColorChooser.showDialog(this,
                        "Choose a colour for " + name,
                        Color.GREEN);

                if (chosenColor == null)
                {
                    chosenColor = Color.GREEN;
                }

                typistColors.put(name, chosenColor);
            }

            char symbol = customSymbols.get(name);

            double accuracy = 0.3 + (Math.random() * 0.6);

            int pts = pointsLeaderboard.getOrDefault(name, 0);
            if (pts >= 10)
            {
                accuracy -= 0.05;
            }
            if (accuracy < 0.1)
            {
                accuracy = 0.1;
            }

            Typist t = new Typist(symbol, name, accuracy);
            t.resetToStart();

            typists.add(t);

            burntOutThisRace.put(name, false);

            keystrokesAttempted.put(name, 0);
            correctKeystrokes.put(name, 0);
            mistypeCount.put(name, 0);
            burnoutCount.put(name, 0);

            oldAccuracyRating.put(name, accuracy);

            if (!personalBestWPM.containsKey(name))
            {
                personalBestWPM.put(name, 0.0);
            }

            if (!raceHistory.containsKey(name))
            {
                raceHistory.put(name, new ArrayList<>());
            }

            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            pane.setBackground(new Color(25, 25, 25));
            pane.setFont(new Font("Consolas", Font.PLAIN, 16));
            pane.setForeground(Color.WHITE);

            typistPanes.add(pane);
            typistPanel.add(new JScrollPane(pane));
        }

        typistPanel.revalidate();
        typistPanel.repaint();
    }

    private void advanceTypist(Typist theTypist)
    {
        if (theTypist.isBurntOut())
        {
            theTypist.recoverFromBurnout();
            return;
        }

        double effectiveAccuracy = theTypist.getAccuracy();

        if (nightShiftBox.isSelected())
        {
            effectiveAccuracy -= 0.15;
            if (effectiveAccuracy < 0.0) effectiveAccuracy = 0.0;
        }

        if (caffeineBox.isSelected())
        {
            effectiveAccuracy += 0.15;
            if (effectiveAccuracy > 1.0) effectiveAccuracy = 1.0;
        }

        String keyboardType = (String) keyboardDropdown.getSelectedItem();

        double speedBoost = 0.0;
        double mistakeBoost = 0.0;

        if (keyboardType.equals("Mechanical"))
        {
            speedBoost = 0.10;
            mistakeBoost = -0.05;
        }
        else if (keyboardType.equals("Membrane"))
        {
            speedBoost = 0.0;
            mistakeBoost = 0.0;
        }
        else if (keyboardType.equals("Touchscreen"))
        {
            speedBoost = -0.10;
            mistakeBoost = 0.10;
        }
        else if (keyboardType.equals("Stenography"))
        {
            speedBoost = 0.25;
            mistakeBoost = 0.15;
        }

        double typingChance = effectiveAccuracy + speedBoost;
        if (typingChance > 1.0) typingChance = 1.0;
        if (typingChance < 0.0) typingChance = 0.0;

        keystrokesAttempted.put(theTypist.getName(),
                keystrokesAttempted.get(theTypist.getName()) + 1);

        if (Math.random() < typingChance)
        {
            theTypist.typeCharacter();

            correctKeystrokes.put(theTypist.getName(),
                    correctKeystrokes.get(theTypist.getName()) + 1);
        }

        int slideAmount = 2;
        if (autoCorrectBox.isSelected())
        {
            slideAmount = 1;
        }

        double mistypeChance = (1 - effectiveAccuracy) * 0.3 + mistakeBoost;
        if (mistypeChance < 0.0) mistypeChance = 0.0;

        if (Math.random() < mistypeChance)
        {
            theTypist.slideBack(slideAmount);

            mistypeCount.put(theTypist.getName(),
                    mistypeCount.get(theTypist.getName()) + 1);
        }

        double burnoutChance = 0.05 * effectiveAccuracy * effectiveAccuracy;

        if (caffeineBox.isSelected())
        {
            burnoutChance *= 2;
        }

        if (Math.random() < burnoutChance)
        {
            theTypist.burnOut(3);
            burntOutThisRace.put(theTypist.getName(), true);

            burnoutCount.put(theTypist.getName(),
                    burnoutCount.get(theTypist.getName()) + 1);
        }
    }

    private boolean raceFinished()
    {
        for (Typist t : typists)
        {
            if (t.getProgress() >= passageLength)
            {
                return true;
            }
        }
        return false;
    }

    private Typist getWinner()
    {
        Typist winner = typists.get(0);

        for (Typist t : typists)
        {
            if (t.getProgress() > winner.getProgress())
            {
                winner = t;
            }
        }

        return winner;
    }

    private double calculateRealAccuracy(String name)
    {
        int attempts = keystrokesAttempted.getOrDefault(name, 0);
        int correct = correctKeystrokes.getOrDefault(name, 0);

        if (attempts == 0)
        {
            return 0.0;
        }

        return (correct * 100.0) / attempts;
    }

    private void awardPoints()
    {
        ArrayList<Typist> results = new ArrayList<>(typists);
        Collections.sort(results, (a, b) -> Integer.compare(b.getProgress(), a.getProgress()));

        double totalSeconds = (raceEndTime - raceStartTime) / 1000.0;
        double minutes = totalSeconds / 60.0;

        for (int i = 0; i < results.size(); i++)
        {
            Typist t = results.get(i);
            String name = t.getName();

            int basePoints = 0;

            if (i == 0) basePoints = 3;
            else if (i == 1) basePoints = 2;
            else if (i == 2) basePoints = 1;

            double wpm = (t.getProgress() / 5.0) / minutes;
            int wpmBonus = (int)(wpm / 20);

            int burnoutPenalty = 0;
            if (burntOutThisRace.getOrDefault(name, false))
            {
                burnoutPenalty = 1;
            }

            int totalPoints = basePoints + wpmBonus - burnoutPenalty;

            if (totalPoints < 0)
            {
                totalPoints = 0;
            }

            pointsLeaderboard.put(name, pointsLeaderboard.getOrDefault(name, 0) + totalPoints);

            racesCompleted.put(name, racesCompleted.getOrDefault(name, 0) + 1);

            if (burntOutThisRace.getOrDefault(name, false))
            {
                racesWithoutBurnout.put(name, 0);
            }
            else
            {
                racesWithoutBurnout.put(name, racesWithoutBurnout.getOrDefault(name, 0) + 1);
            }

            if (i == 0)
            {
                consecutiveWins.put(name, consecutiveWins.getOrDefault(name, 0) + 1);
            }
            else
            {
                consecutiveWins.put(name, 0);
            }
        }
    }

    private void updateDisplay()
    {
        for (int i = 0; i < typists.size(); i++)
        {
            updateTypingPane(typistPanes.get(i), typists.get(i));
        }
    }

    // UPDATED: typed section uses typist's chosen colour
    private void updateTypingPane(JTextPane pane, Typist t)
    {
        StyledDocument doc = pane.getStyledDocument();

        try
        {
            doc.remove(0, doc.getLength());

            int progress = t.getProgress();
            if (progress > passageLength) progress = passageLength;

            String typed = passageText.substring(0, progress);
            String remaining = passageText.substring(progress);

            Style nameStyle = pane.addStyle("nameStyle", null);
            StyleConstants.setForeground(nameStyle, new Color(100, 180, 255));
            StyleConstants.setBold(nameStyle, true);

            Style typedStyle = pane.addStyle("typedStyle", null);
            Color c = typistColors.getOrDefault(t.getName(), Color.GREEN);
            StyleConstants.setForeground(typedStyle, c);

            Style cursorStyle = pane.addStyle("cursorStyle", null);
            StyleConstants.setForeground(cursorStyle, Color.RED);
            StyleConstants.setBold(cursorStyle, true);

            Style remainingStyle = pane.addStyle("remainingStyle", null);
            StyleConstants.setForeground(remainingStyle, Color.LIGHT_GRAY);

            Style burnoutStyle = pane.addStyle("burnoutStyle", null);
            StyleConstants.setForeground(burnoutStyle, Color.ORANGE);
            StyleConstants.setBold(burnoutStyle, true);

            doc.insertString(doc.getLength(), t.getName() + " (" + t.getSymbol() + ")\n", nameStyle);

            doc.insertString(doc.getLength(), typed, typedStyle);
            doc.insertString(doc.getLength(), "|", cursorStyle);
            doc.insertString(doc.getLength(), remaining, remainingStyle);

            if (t.isBurntOut())
            {
                doc.insertString(doc.getLength(),
                        "\nBURNT OUT (" + t.getBurnoutTurnsRemaining() + " turns)",
                        burnoutStyle);
            }
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
    }

    private String getLeaderboardText()
    {
        if (leaderboard.isEmpty())
        {
            return "No races completed yet.";
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(leaderboard.entrySet());
        Collections.sort(list, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        String text = "";
        int rank = 1;

        for (Map.Entry<String, Double> entry : list)
        {
            text += rank + ". " + entry.getKey() + " : "
                    + String.format("%.2f", entry.getValue()) + " WPM\n";
            rank++;
        }

        return text;
    }

    private String getPointsLeaderboardText()
    {
        if (pointsLeaderboard.isEmpty())
        {
            return "No races completed yet.";
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(pointsLeaderboard.entrySet());
        Collections.sort(list, (a, b) -> Integer.compare(b.getValue(), a.getValue()));

        String text = "";
        int rank = 1;

        for (Map.Entry<String, Integer> entry : list)
        {
            String name = entry.getKey();
            int pts = entry.getValue();

            text += rank + ". " + name + " : " + pts + " pts"
                    + "   [" + getTitle(name) + "]\n";
            rank++;
        }

        return text;
    }

    private String getTitle(String name)
    {
        int wins = consecutiveWins.getOrDefault(name, 0);
        int noBurnout = racesWithoutBurnout.getOrDefault(name, 0);
        int completed = racesCompleted.getOrDefault(name, 0);

        if (wins >= 3)
        {
            return "Speed Demon";
        }
        else if (noBurnout >= 5)
        {
            return "Iron Fingers";
        }
        else if (completed >= 10)
        {
            return "Veteran Typer";
        }
        else
        {
            return "Rookie";
        }
    }

    private void showResults(Typist winner)
    {
        double totalSeconds = (raceEndTime - raceStartTime) / 1000.0;
        double minutes = totalSeconds / 60.0;

        String message = "Race Finished!\n\n";
        message += "Time: " + String.format("%.2f", totalSeconds) + " seconds\n\n";

        ArrayList<Typist> results = new ArrayList<>(typists);
        Collections.sort(results, (a, b) -> Integer.compare(b.getProgress(), a.getProgress()));

        for (int i = 0; i < results.size(); i++)
        {
            Typist t = results.get(i);
            String name = t.getName();

            int position = i + 1;

            double wpm = (t.getProgress() / 5.0) / minutes;
            double realAccuracy = calculateRealAccuracy(name);

            int burnouts = burnoutCount.getOrDefault(name, 0);

            double beforeAcc = oldAccuracyRating.getOrDefault(name, t.getAccuracy());
            double afterAcc = t.getAccuracy();
            double change = afterAcc - beforeAcc;

            double best = personalBestWPM.getOrDefault(name, 0.0);
            if (wpm > best)
            {
                personalBestWPM.put(name, wpm);
                best = wpm;
            }

            raceHistory.get(name).add(
                    "Pos " + position +
                    " | WPM " + String.format("%.2f", wpm) +
                    " | Accuracy " + String.format("%.2f", realAccuracy) + "%" +
                    " | Burnouts " + burnouts
            );

            message += position + ". " + name + "\n";
            message += "   WPM: " + String.format("%.2f", wpm) + "\n";
            message += "   Accuracy: " + String.format("%.2f", realAccuracy) + "%\n";
            message += "   Burnouts: " + burnouts + "\n";
            message += "   Accuracy Rating Change: " + String.format("%+.2f", change) + "\n";
            message += "   Personal Best WPM: " + String.format("%.2f", best) + "\n\n";
        }

        message += "--- BEST WPM LEADERBOARD ---\n" + getLeaderboardText();
        message += "\n\n--- GLOBAL POINTS LEADERBOARD ---\n" + getPointsLeaderboardText();

        JOptionPane.showMessageDialog(this, message, "Race Results", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void startRaceGUI()
    {
        TypingRaceGUI gui = new TypingRaceGUI();
        gui.setVisible(true);
    }

    public static void main(String[] args)
    {
        startRaceGUI();
    }
}