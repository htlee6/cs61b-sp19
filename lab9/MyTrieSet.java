import java.util.*;

public class MyTrieSet implements TrieSet61B {
    // private static final int R = 128;
    private Node root;

    private class Node {
        // private char ch;
        private boolean isKey;
        private HashMap<Character, Node> next;

        public Node(boolean k, int R) {
            // ch = c;
            isKey = k;
            next = new HashMap<>();
        }
    }

    /**
     * Clears all items out of Trie
     */
    @Override
    public void clear() {
        root = null;
    }

    /**
     * Returns true if the Trie contains KEY, false otherwise
     */
    @Override
    public boolean contains(String key) {
        List<String> collection = keysWithPrefix(key);
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
        if (contains(key)) return;
        // TODO

    }

    /**
     * Returns a list of all words that start with PREFIX
     */
    @Override
    public List<String> keysWithPrefix(String prefix) {
        List<String> collection = new LinkedList<>();
        for (String akey : root.next.keys()) {

        }
        return collection;
    }

    private MyTrieSet keysWithPrefixHelper(String s, List<String> x, DataIndexedCharMap n) {
        if (n.isKey) {
            x.add(s);
        }

    }

    /**
     * Returns the longest prefix of KEY that exists in the Trie
     * Not required for Lab 9. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public String longestPrefixOf(String key) {
        return null;
    }

    /* GLOBAL HELPER */
}
