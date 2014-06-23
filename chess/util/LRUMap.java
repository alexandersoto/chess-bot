package chess.util;

import java.util.LinkedHashMap;
import java.util.Map;

// Adapted from - http://amix.dk/blog/post/19465
public class LRUMap<K,V> extends LinkedHashMap<K,V> {

	private static final long serialVersionUID = 1L;
	private int max_cap;

    public LRUMap(int initial_cap, int max_cap, float loadFactor) {
        super(initial_cap, loadFactor, true);
        this.max_cap = max_cap;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > this.max_cap;
    }
}