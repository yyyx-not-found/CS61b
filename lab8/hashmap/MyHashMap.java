package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int numOfNodes = 0;
    private double maxLoadFactor = 0.75;
    private int sizeOfTable = 16;

    @Override
    public void clear() {
        buckets = createTable(sizeOfTable);
        numOfNodes = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    @Override
    public V get(K key) {
        Node targetNode = findNode(key);
        if (targetNode == null) {
            return null;
        } else {
            return targetNode.value;
        }
    }

    private Node findNode(K key) {
        for (Node item : buckets[getBucketIndex(key)]) {
            if ((item.key).equals(key)) {
                return item;
            }
        }
        return null;
    }

    private int getBucketIndex(K key) {
        return Math.floorMod(key.hashCode(), sizeOfTable);
    }

    @Override
    public int size() {
        return numOfNodes;
    }

    @Override
    public void put(K key, V value) {
        /* Check if key exists in bucket. */
        Node targetNode = findNode(key);
        if (targetNode != null) {
            targetNode.value = value;
            return;
        }

        /* Insert new node */
        Collection<Node> targetBucket = buckets[getBucketIndex(key)];
        Node newNode = createNode(key, value);
        targetBucket.add(newNode);
        numOfNodes++;

        /* Check load factor */
        if (getLoadFactor() >= maxLoadFactor) {
            resize();
        }
    }

    private double getLoadFactor() {
        return (double) numOfNodes / sizeOfTable;
    }

    private void resize() {
        MyHashMap<K, V> newHushMap = new MyHashMap<>(sizeOfTable * 2);
        for (int i = 0; i < sizeOfTable; i++) {
            for (Node item : buckets[i]) {
                newHushMap.put(item.key, item.value);
            }
        }

        /* Update buckets */
        buckets = newHushMap.buckets;
        sizeOfTable = newHushMap.sizeOfTable;
        numOfNodes = newHushMap.numOfNodes;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (K key : this) {
            keys.add(key);
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        return removeHelper(key, null, 0);
    }

    @Override
    public V remove(K key, V value) {
        return removeHelper(key, value, 1);
    }

    private V removeHelper(K key, V value, int mode) {
        Collection<Node> targetBucket = buckets[getBucketIndex(key)];
        Node targetNode = findNode(key);
        if (targetNode != null && (mode == 0 || (mode == 1 && (targetNode.value).equals(value)))) {
            targetBucket.remove(targetNode);
            numOfNodes--;
            return targetNode.value;
        } else {
            return null;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new keyIterator();
    }

    private class keyIterator implements Iterator<K> {
        private int bucketIndex = 0;
        private Iterator<Node> curIterator = buckets[bucketIndex].iterator();

        @Override
        public boolean hasNext() {
            while (bucketIndex < sizeOfTable && !curIterator.hasNext()) {
                curIterator = buckets[bucketIndex++].iterator();
            }
            return bucketIndex < sizeOfTable;
        }

        @Override
        public K next() {
            if (!curIterator.hasNext()) {
                curIterator = buckets[++bucketIndex].iterator();
            }
            K res = (curIterator.next()).key;
            return res;
        }
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /**
     * Constructors
     */
    public MyHashMap() {
        buckets = createTable(sizeOfTable);
    }

    public MyHashMap(int initialSize) {
        sizeOfTable = initialSize;
        buckets = createTable(sizeOfTable);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        sizeOfTable = initialSize;
        maxLoadFactor = maxLoad;
        buckets = createTable(sizeOfTable);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

}
