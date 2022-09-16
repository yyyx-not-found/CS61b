package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private BSTNode root;
    private int size;

    private class BSTNode {
        K key;
        V val;
        BSTNode left;
        BSTNode right;

        BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
        }

    }

    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public void clear() {
        clearHelper(root);
        root = null;
        size = 0;
    }

    private void clearHelper(BSTNode node) {
        if (node == null) {
            return;
        }

        clearHelper(node.left);
        node.left = null;
        clearHelper(node.right);
        node.right = null;
    }

    @Override
    public boolean containsKey(K key) {
        return containKeyHelper(root, key);
    }

    private boolean containKeyHelper(BSTNode node, K key) {
        if (node == null) {
            return false;
        }

        if ((node.key).compareTo(key) == 0) {
            return true;
        } else if ((node.key).compareTo(key) > 0) {
            return containKeyHelper(node.left, key);
        } else {
            return containKeyHelper(node.right, key);
        }
    }

    @Override
    public V get(K key) {
        return getHelper(root, key);
    }

    private V getHelper(BSTNode node, K key) {
        if (node == null) {
            return null;
        }

        if ((node.key).compareTo(key) == 0) {
            return node.val;
        } else if ((node.key).compareTo(key) > 0) {
            return getHelper(node.left, key);
        } else {
            return getHelper(node.right, key);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = putHelper(root, key, value);
    }

    private BSTNode putHelper(BSTNode node, K key, V value) {
        if (node == null) {
            size++;
            return new BSTNode(key, value);
        } else if ((node.key).compareTo(key) == 0) {
            node.val = value;
        } else if ((node.key).compareTo(key) > 0) {
            node.left = putHelper(node.left, key, value);
        } else {
            node.right = putHelper(node.right, key, value);
        }
        return node;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new TreeSet<>();
        for (K key : this) {
            keys.add(key);
        }

        return keys;
    }

    @Override
    public V remove(K key) {
        return removeHelper(root, key, null, 0);
    }

    @Override
    public V remove(K key, V value) {
        return removeHelper(root, key, value, 1);
    }

    private V removeHelper(BSTNode node, K key, V val, int mode) {
        if (node == null) {
            return null;
        } else if ((node.key).compareTo(key) == 0) {
            if (mode == 0 || (mode == 1 && node.val == val)) {
                root = delete(root, node.key);
                return node.val;
            } else {
                return null;
            }
        } else if ((node.key).compareTo(key) > 0) {
            return removeHelper(node.left, key, val, mode);
        } else {
            return removeHelper(node.right, key, val, mode);
        }
    }

    private BSTNode delete(BSTNode node, K key) {
        if ((node.key).compareTo(key) > 0) {
            node.left = delete(node.left, key);
        } else if ((node.key).compareTo(key) < 0) {
            node.right = delete(node.right, key);
        } else {
            /* Deletion */
            size--;
            if (node.left == null && node.right == null) {
                return null;
            } else if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            } else {
                node.key = findMinNodeKey(node.right);
            }
        }
        return node;
    }

    /** Find successor in BST and delete it. */
    private K findMinNodeKey(BSTNode node) {
        if (node.left == null) {
            root = delete(root, node.key);
            return node.key;
        }

        return findMinNodeKey(node.left);
    }

    @Override
    public Iterator<K> iterator() {
        return new keyIterator();
    }

    private class keyIterator implements Iterator<K> {
        BSTNode curNode;
        Stack<BSTNode> stack;

        keyIterator() {
            curNode = root;
            stack = new Stack<>();
        }

        @Override
        public boolean hasNext() {
            return !(stack.empty() && curNode == null);
        }

        @Override
        public K next() {
            if (!hasNext()) {
                return null;
            } else if (curNode == null) {
                curNode = stack.pop();
            }

            if (curNode != null && curNode.right != null) {
                stack.push(curNode.right);
            }

            BSTNode res = curNode;
            curNode = curNode.left;
            return res.key;
        }
    }

    public void printInOrder() {
        printInOrderHelper(root);
        System.out.println();
    }

    private void printInOrderHelper(BSTNode node) {
        if (node == null) {
            return;
        }
        printInOrderHelper(node.left);
        System.out.print(node.key + " ");
        printInOrderHelper(node.right);
    }
}
