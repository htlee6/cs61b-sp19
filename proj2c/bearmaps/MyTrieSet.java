package bearmaps;

import java.util.*;

public class MyTrieSet implements TrieSet61B {
    private Node root;

    private class Node {
        private boolean isKey;
        private HashMap<Character, Node> next;

        Node(boolean k) {
            isKey = k;
            next = new HashMap<>();
        }
    }

    public MyTrieSet() {
        root = new Node(false);
    }

    /**
     * Clears all items out of Trie
     */
    @Override
    public void clear() {
        root = new Node(false);
    }

    /**
     * Returns true if the Trie contains KEY, false otherwise
     */
    @Override
    public boolean contains(String key) {
        List<String> collection = collection();
        for (String aString : collection) {
            if (aString.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts string KEY into Trie
     */
    @Override
    public void add(String key) {
        // if (contains(key)) return;
        if (key == null || key.length() < 1) return;
        // TODO
        Node currentNode = root;
        for (int i = 0; i < key.length(); i += 1) {
            char c = key.charAt(i);
            if (!currentNode.next.containsKey(c)) {
                currentNode.next.put(c, new Node(false));
            }
            currentNode = currentNode.next.get(c);
        }
        currentNode.isKey = true;
    }

    /**
     * Returns a list of all words that start with PREFIX
     */
    @Override
    public List<String> keysWithPrefix(String prefix) {
        try {
            List<String> keysWithPrefix = new LinkedList<>();
            Node currentNode = root;
            for (int i = 0; i < prefix.length(); i += 1) {
                char c = prefix.charAt(i);
                currentNode = currentNode.next.get(c);
            }
            for (char c : currentNode.next.keySet()) {
                colHelper(prefix + c, keysWithPrefix, currentNode.next.get(c));
            }
            return keysWithPrefix;
        } catch (NullPointerException e) {
            return new LinkedList<>();
        }
    }

    private List<String> collection() {
        List<String> result = new LinkedList<>();
        colHelper("", result, root);
        return result;
    }

    private void colHelper(String s, List<String> x, Node n) {
        if (n == null) {
            return;
        }
        if (n.isKey == true) {
            x.add(s);
        }
        for (char c : n.next.keySet()) {
            colHelper(s + c, x, n.next.get(c));
        }
    }

    /**
     * Returns the longest prefix of KEY that exists in the Trie
     * Not required for Lab 9. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public String longestPrefixOf(String key) {
        int i = 0;
        try {
            for (; i < key.length(); i += 1) {
                if (keysWithPrefix(key.substring(0, i)).size() == 0) {
                    break;
                }
            }
        } catch (NullPointerException e) {
            return key.substring(0, i - 1);
        }
        return key;
    }
}