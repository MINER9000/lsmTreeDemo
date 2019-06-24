import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.*;
import java.util.*;

public class MyFileManager {
    private MyScanner dataIn;
    private MyScanner tabIn;
    private PrintWriter tabOut;
    private int tables;
    private List<MyFile> files;
    
    private final int MAX_FILE_SIZE = 30;
    private final int MERGE_MAX_SIZE = 6;
    private final double error = 0.05;

    MyFileManager(String datafileName) throws IOException {
        dataIn = new MyScanner(datafileName);
        tables = dataIn.nextInt();
        files = new ArrayList<>();
        
        for (int i = 0; i < tables; ++i) {
            String name = dataIn.next();
            int keys = dataIn.nextInt();
            int size = dataIn.nextInt();
            
            tabIn = new MyScanner(name + ".txt");
            FileInputStream fis = new FileInputStream(name + ".bloom");
            BloomFilter<Integer> bloom = BloomFilter.readFrom(fis
                    , Funnels.integerFunnel());
            
            files.add(new MyFile(name, bloom, keys, size));

            fis.close();
            tabIn.close();
        }
        
        dataIn.close();
    }
    
    String findValue(int key) throws IOException{
        for (MyFile file : files) {
            if (file.getBloom().mightContain(key)) {
                tabIn = new MyScanner(file.getPath());
                for (int i = 0; i < file.getKeys(); i++) {
                    int cur_key = tabIn.nextInt();
                    String s = tabIn.next();
                    if (cur_key == key) return s;
                }
                tabIn.close();
            }
        }
        return null;
    }

    /**
     * Remove first pair <key, _>
     * @param key to be deleted
     * @return true if successfully
     */
    boolean removeKey(int key) throws IOException {
        tryMergeFiles();
        boolean founded = false;
        for (MyFile file : files) {
            if (file.getBloom().mightContain(key)) {
                StringBuilder temp = new StringBuilder();
                BloomFilter<Integer> bloomFilter = BloomFilter.create(
                        Funnels.integerFunnel(), MAX_FILE_SIZE, error);
                tabIn = new MyScanner(file.getPath());

                for (int i = 0; i < file.getKeys(); i++) {
                    int k = tabIn.nextInt();
                    bloomFilter.put(k);
                    String val = tabIn.next();

                    if (k != key) {
                        temp.append(k).append(' ').append(val).append('\n');
                    }
                    else {
                        founded = true;
                    }
                }
                file.setBloom(bloomFilter);
                tabIn.close();
                if (founded) {
                    file.keyRemoved();
                    FileWriter f = new FileWriter(file.getPath());
                    PrintWriter pw = new PrintWriter(f);
                    pw.println(temp.toString());
                    f.close();
                    pw.flush();
                    pw.close();
                    break;
                }
            }
        }

        return founded;
    }
    
    private void tryMergeFiles() throws IOException {
        MyFile file1 = null;
        MyFile file2 = null;
        for (MyFile file : files) {
            if (file.getKeys() <= MERGE_MAX_SIZE) {
                if (file1 == null)
                    file1 = file;
                else if (file2 == null)
                    file2 = file;
                else
                    break;
            }
        }
        if (file1 != null && file2 != null) {
            mergeFiles(file1, file2);
        }
    }

    private void mergeFiles(MyFile file1, MyFile file2) throws IOException {
        if (file1.getKeys() == 0 || file2.getKeys() == 0) {
            if (file1.getKeys() == 0) {
                file1.delete();
                files.remove(file1);
            }
            if (file2.getKeys() == 0) {
                file2.delete();
                files.remove(file2);
            }
            return;
        }
        String newFileName = String.valueOf(new Date().getTime());
        MyScanner in1 = new MyScanner(file1.getPath());
        MyScanner in2 = new MyScanner(file2.getPath());
        FileWriter fw = new FileWriter(newFileName + ".txt");
        tabOut = new PrintWriter(fw);
        BloomFilter<Integer> bloomFilter = BloomFilter.create(
                Funnels.integerFunnel(), MAX_FILE_SIZE, error);

        int i = 1;
        int j = 1;
        int keys = 0;
        int key1 = in1.nextInt();
        String val1 = in1.next();
        int key2 = in2.nextInt();
        String val2 = in2.next();
        while (i <= file1.getKeys() && j <= file2.getKeys()) {
            if (val1 == null) {
                key1 = in1.nextInt();
                val1 = in1.next();
            }
            if (val2 == null) {
                key2 = in2.nextInt();
                val2 = in2.next();
            }
            if (key1 == key2) {
                tabOut.println(key1 + " " + val1);
                bloomFilter.put(key1);
                val1 = null;
                val2 = null;
                keys++;
                i++;
                j++;
            }
            else if (key1 < key2) {
                tabOut.println(key1 + " " + val1);
                bloomFilter.put(key1);
                keys++;
                val1 = null;
                i++;
            }
            else {
                tabOut.println(key2 + " " + val2);
                bloomFilter.put(key2);
                val2 = null;
                keys++;
                j++;
            }
        }

        while (i <= file1.getKeys()) {
            if (val1 == null) {
                key1 = in1.nextInt();
                val1 = in1.next();
            }
            tabOut.println(key1 + " " + val1);
            bloomFilter.put(key1);
            keys++;
            i++;
            val1 = null;
        }

        while (j <= file2.getKeys()) {
            if (val2 == null) {
                key2 = in2.nextInt();
                val2 = in2.next();
            }
            tabOut.println(key2 + " " + val2);
            bloomFilter.put(key2);
            keys++;
            j++;
            val2 = null;
        }

        files.add(new MyFile(newFileName, bloomFilter, keys, MAX_FILE_SIZE));
        in1.close();
        in2.close();
        files.remove(file1);
        files.remove(file2);
        tabOut.flush();
        tabOut.close();
        fw.close();
        file1.delete();
        file2.delete();
    }

    void addNewFile(Map<Integer, String> tree) throws IOException {
        String name = String.valueOf(new Date().getTime());
        int keys = tree.size();
        BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), MAX_FILE_SIZE, error);

        FileWriter fw = new FileWriter(name + ".txt");
        tabOut = new PrintWriter(fw);
        
        for (Map.Entry<Integer, String> pair : tree.entrySet()) {
            tabOut.println(pair.getKey() + " " + pair.getValue());
            bloomFilter.put(pair.getKey());
        }

        tables++;
        files.add(new MyFile(name, bloomFilter, keys, MAX_FILE_SIZE));
        tabOut.flush();
        fw.close();
        tabOut.close();
    }
    
    void close(String dataFileName) throws IOException{
        tryMergeFiles();
        if (dataIn != null) dataIn.close();
        if (tabIn != null) tabIn.close();

        FileWriter fw = new FileWriter(dataFileName);
        tabOut = new PrintWriter(fw);
        tabOut.println(files.size());
        for (MyFile file : files) {
            tabOut.print(file.getName() + " ");
            tabOut.print(file.getKeys() + " ");
            tabOut.println(file.getSize());
            
            File bloomFile = new File(file.getName() + ".bloom");
            if (bloomFile.exists()) 
                if (bloomFile.delete())
                    throw new IOException();
            if (bloomFile.createNewFile())  {
                FileOutputStream f = new FileOutputStream(bloomFile);
                file.getBloom().writeTo(f);
                f.close();
            }

        }
        tabOut.flush();
        fw.close();
        tabOut.close();
    }
}
