package exeter;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class StringTranslate {
    private static final String DICTIONARY_FILE = "input/french_dictionary.csv";
    private static final String BOOK_FILE = "input/t8.shakespeare.txt";
    private static final String WORD_FREQ_FILE = "output/frequency.csv";
    private static final String TRANSLATED_BOOK = "output/t8.shakespearetranslated.txt";
    private static final String PERFORMANCE_FILE = "output/performance.txt";
    private static String TIMETAKEN = "Time to process: {{minute}} minutes {{second}} seconds";
    private static String MEMORY_USED = "Memory used: {{memused}} MB";
    private static final Runtime runTimeIns = Runtime.getRuntime();

    public static void main(String[] args) throws IOException {
        long startMem = runTimeIns.totalMemory() - runTimeIns.freeMemory();
        long startTime = System.currentTimeMillis();
        Map<String, String> wordsMap = parseDictionaryFile();//dictionary CSV parsing
        translateBook(wordsMap);//translate book
        createWordFreqFile(wordsMap); //create words frequency CSV
        performanceFileCreation(startTime, startMem);//performanceFile creation
        System.out.println("Completed : " + Duration.ofMillis(System.currentTimeMillis() -  startTime).getSeconds() + "seconds");
    }

    private static void translateBook(Map<String, String> wordsMap) {
        PrintWriter translatedFile = null;
        Scanner fileScanner = null;
        try {
            File bookFile = new File(BOOK_FILE);
            translatedFile = new PrintWriter(TRANSLATED_BOOK);
            fileScanner = new Scanner(bookFile);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] words = line.split(" ");
                for (String englishWord : words) {
                    englishWord = stripSpclChars(englishWord.trim().toLowerCase());
                    if (wordsMap.containsKey(englishWord)) {
                        String frenchWord = wordsMap.get(englishWord).split("::")[0];
                        int currentFreq = Integer.parseInt(wordsMap.get(englishWord).split("::")[1]);
                        wordsMap.put(englishWord, frenchWord +  "::" + (currentFreq + 1));
                        line = line.replaceAll("\\b(?i)" + englishWord + "\\b", frenchWord);
                    }
                }
                translatedFile.write(line + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fileScanner != null) {
                fileScanner.close();
            }
            if (translatedFile != null) {
                translatedFile.close();
            }
        }
    }

    private static Map<String, String> parseDictionaryFile() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        Scanner fileScanner = null;
        try {
            File file = new File(DICTIONARY_FILE);
            fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                String englishWord = line.substring(0, line.indexOf(",")).toLowerCase();
                String frenchWord = line.substring(line.indexOf(",") + 1, line.length());
                int frquency = 0;
                map.put(englishWord, frenchWord + "::" + frquency);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fileScanner != null) {
                fileScanner.close();
            }
        }
        return map;
    }

    private static void createWordFreqFile(Map<String, String> wordsMap) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(WORD_FREQ_FILE);
            for (Map.Entry<String, String> r : wordsMap.entrySet()) {
                String origWord = r.getKey();
                String frenchWord = r.getValue().split("::")[0];
                Integer freq = Integer.parseInt(r.getValue().split("::")[1]);
                writer.write(origWord + "," + frenchWord + "," + freq + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void performanceFileCreation(long startTime, long startMem) {
        PrintWriter writer = null;
        long endMem = runTimeIns.totalMemory() - runTimeIns.freeMemory();
        int memUsage = (int) ((endMem - startMem) / (1024 * 1024));
        try {
            writer = new PrintWriter(PERFORMANCE_FILE);
            TIMETAKEN = TIMETAKEN.replace("{{minute}}" , 0 + "")
                                 .replace("{{second}}",
                                     Duration.ofMillis(System.currentTimeMillis() - startTime).getSeconds() + "");
            MEMORY_USED = MEMORY_USED.replace("{{memused}}", memUsage + "");
            writer.write(TIMETAKEN + "\n" + MEMORY_USED);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
     
    }

    static String stripSpclChars(String s) {
        int index;
        for (index = 0; index < s.length(); index++) {
            if (Character.isLetterOrDigit(s.charAt(index))) {
                break;
            }
        }
        s = s.substring(index);
        for (index = s.length() - 1; index >= 0; index--) {
            if (Character.isLetterOrDigit(s.charAt(index))) {
                break;
            }
        }
        return s.substring(0, index + 1);
    }
}
          
          
           
