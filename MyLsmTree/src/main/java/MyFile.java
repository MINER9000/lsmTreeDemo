import com.google.common.hash.BloomFilter;

import java.io.File;

class MyFile {
    private String name;

    public void setBloom(BloomFilter<Integer> bloom) {
        this.bloom = bloom;
    }

    private BloomFilter<Integer> bloom;
    private int keys;
    private int size;

    MyFile(String name, BloomFilter<Integer> bloom, int keys, int size) {
        this.name = name;
        this.bloom = bloom;
        this.keys = keys;
        this.size = size;
    }

    public void delete() {
        File f = new File(getPath());
        File bf = new File(getName() + ".bloom");
        bf.delete();
        f.delete();
    }

    public String getName() {
        return name;
    }

    public String getPath() { return name + ".txt"; }

    public BloomFilter<Integer> getBloom() {
        return bloom;
    }

    public void keyRemoved() {
        this.keys--;
    }

    public int getKeys() {
        return keys;
    }

    public int getSize() {
        return size;
    }
}
