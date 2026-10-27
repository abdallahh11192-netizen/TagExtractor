import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Lab 08 - Tag Extractor.
 * Lets the user pick a text file and a stop word file, extracts the
 * tags (words) with their frequencies, displays them, and saves them.
 */
public class TagExtractor extends JFrame
{
    private final JLabel textFileLabel = new JLabel("Text file: (none selected)");
    private final JLabel stopFileLabel = new JLabel("Stop word file: (none selected)");
    private final JTextArea outputArea = new JTextArea(25, 40);
    private final JButton chooseTextBtn = new JButton("Choose Text File");
    private final JButton chooseStopBtn = new JButton("Choose Stop Word File");
    private final JButton extractBtn = new JButton("Extract Tags");
    private final JButton saveBtn = new JButton("Save Tags");
    private final JButton quitBtn = new JButton("Quit");

    private File textFile;
    private File stopFile;
    private final Set<String> stopWords = new TreeSet<>();
    private final Map<String, Integer> tags = new TreeMap<>();

    public TagExtractor()
    {
        super("Tag Extractor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // top: file names
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(textFileLabel);
        top.add(stopFileLabel);
        top.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(top, BorderLayout.NORTH);

        // center: tags + frequencies
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // bottom: buttons
        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(chooseTextBtn);
        bottom.add(chooseStopBtn);
        bottom.add(extractBtn);
        bottom.add(saveBtn);
        bottom.add(quitBtn);
        add(bottom, BorderLayout.SOUTH);

        extractBtn.setEnabled(false);
        saveBtn.setEnabled(false);

        chooseTextBtn.addActionListener(e -> chooseTextFile());
        chooseStopBtn.addActionListener(e -> chooseStopFile());
        extractBtn.addActionListener(e -> extractTags());
        saveBtn.addActionListener(e -> saveTags());
        quitBtn.addActionListener(e -> System.exit(0));

        pack();
        setLocationRelativeTo(null);
    }

    private File pickFile(String title)
    {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private void chooseTextFile()
    {
        File f = pickFile("Select the text file to scan");
        if (f != null)
        {
            textFile = f;
            textFileLabel.setText("Text file: " + f.getName());
            updateExtractEnabled();
        }
    }

    private void chooseStopFile()
    {
        File f = pickFile("Select the stop word file");
        if (f != null)
        {
            stopFile = f;
            stopFileLabel.setText("Stop word file: " + f.getName());
            updateExtractEnabled();
        }
    }

    private void updateExtractEnabled()
    {
        extractBtn.setEnabled(textFile != null && stopFile != null);
    }

    private void loadStopWords() throws IOException
    {
        stopWords.clear();
        try (Scanner in = new Scanner(stopFile))
        {
            while (in.hasNextLine())
            {
                String w = in.nextLine().trim().toLowerCase();
                if (!w.isEmpty())
                {
                    stopWords.add(w);
                }
            }
        }
    }

    private void extractTags()
    {
        try
        {
            loadStopWords();
            tags.clear();

            try (Scanner in = new Scanner(textFile))
            {
                while (in.hasNextLine())
                {
                    // remove non-letters and force lowercase
                    String line = in.nextLine().toLowerCase().replaceAll("[^a-z]", " ");
                    for (String word : line.split("\\s+"))
                    {
                        if (word.isEmpty() || stopWords.contains(word))
                        {
                            continue;
                        }
                        tags.merge(word, 1, Integer::sum);
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Tags extracted from: ").append(textFile.getName()).append("\n");
            sb.append("Unique tags: ").append(tags.size()).append("\n\n");
            for (Map.Entry<String, Integer> entry : tags.entrySet())
            {
                sb.append(String.format("%-25s %d%n", entry.getKey(), entry.getValue()));
            }
            outputArea.setText(sb.toString());
            outputArea.setCaretPosition(0);
            saveBtn.setEnabled(!tags.isEmpty());
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTags()
    {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle("Save tags to a file");
        chooser.setSelectedFile(new File("tags.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File out = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(out))
            {
                for (Map.Entry<String, Integer> entry : tags.entrySet())
                {
                    pw.println(entry.getKey() + " : " + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Saved " + tags.size() + " tags to " + out.getName());
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new TagExtractor().setVisible(true));
    }
}
