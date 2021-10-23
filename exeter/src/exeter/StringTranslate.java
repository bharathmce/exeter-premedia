package exeter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class StringTranslate {
    private static final String DICTIONARY_FILE = "input/french_dictionary.csv";
    private static final String BOOK_FILE = "input/t8.shakespeare.txt";
    private static final String WORD_FREQ_FILE = "output/frequency.csv";
    private static final String TRANSLATED_BOOK = "output/t8.shakespeare.translated.txt";
    private static final String PERFORMANCE_FILE = "output/performance.txt";
    private static final String REGEX_FOR_STRING_MATCH =  "\\b(?i){{word}}\\b";//To get exact matched case insensitive words


    private static String TIMETAKEN = "Time to process: {{minute}} minutes {{second}} seconds";
    private static String MEMORY_USED = "Memory used: {{memused}} MB";
    private static final Runtime runTimeIns = Runtime.getRuntime();

    public static void main(String[] args) {
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
                    englishWord = trimSymbols(englishWord.trim());
                    String englishWordLowerCase = englishWord.toLowerCase();
                    if (wordsMap.containsKey(englishWordLowerCase)) {
                        String frenchWord = wordsMap.get(englishWordLowerCase).split("::")[0];
                        int currentFreq = Integer.parseInt(wordsMap.get(englishWordLowerCase).split("::")[1]);
                        wordsMap.put(englishWordLowerCase, frenchWord +  "::" + (currentFreq + 1));
                        frenchWord = casingWord(englishWord, frenchWord); //String Capitalization handling
                        line = line.replaceAll(REGEX_FOR_STRING_MATCH.replace("{{word}}", englishWord), frenchWord);
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

    private static String casingWord(String englishWord, String frenchWord) {
        if (englishWord.toLowerCase().equals(englishWord)) {
            frenchWord = frenchWord.toLowerCase();
        } else if (englishWord.toUpperCase().equals(englishWord)) {
            frenchWord = frenchWord.toUpperCase();
        } else if (Character.isUpperCase(englishWord.charAt(0))) {
            frenchWord = Character.toUpperCase(frenchWord.charAt(0)) + frenchWord.substring(1);
        }
        return frenchWord;
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
                String frenchWord = line.substring(line.indexOf(",") + 1);
                //To convert french word to UTF-8
                frenchWord = new String(frenchWord.getBytes(), "UTF-8");
                int frquency = 0;
                map.put(englishWord, frenchWord + "::" + frquency);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {

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

    private static String removeLeadingSymbols(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 1 && !Character.isLetterOrDigit(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    private static  String removeTrailingSymbols(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() > 1 && !Character.isLetterOrDigit(sb.charAt(sb.length() - 1))) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String trimSymbols(String s) {
        s = removeLeadingSymbols(s);
        return removeTrailingSymbols(s);
    }
}