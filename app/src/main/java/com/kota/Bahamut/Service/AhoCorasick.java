package com.kota.Bahamut.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AhoCorasick {
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
        TrieNode fail;
        String word;
    }

    private TrieNode root;

    public AhoCorasick(Set<String> dictionary) {
        root = new TrieNode();
        // construct the trie
        for (String word : dictionary) {
            insert(word);
        }
        // construct the fail links
        constructFailLinks();
    }

    private void insert(String word) {
        TrieNode current = root;
        for (char ch: word.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }
        current.isEndOfWord = true;
        current.word = word;
    }

    private void constructFailLinks() {
        Queue<TrieNode> queue = new LinkedList<>();

        // set the fail links of the root's children
        for (TrieNode node: root.children.values()) {
            node.fail = root;
            queue.offer(node);
        }

        // set the fail links of the rest of the nodes
        while(!queue.isEmpty()) {
            TrieNode current = queue.poll();

            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                TrieNode child = entry.getValue();

                // find suitable failure
                TrieNode fail = current.fail;
                while (fail!=null && !fail.children.containsKey(ch)) {
                    fail = fail.fail;
                }

                child.fail = (fail == null) ? root : fail.children.get(ch);
                queue.offer(child);
            }
        }
    }

    public List<String> search(String text) {
        List<String> found = new ArrayList<>();
        TrieNode current = root;

        for (char ch : text.toCharArray()) {
            // if we can't find the character, go to the fail link
            while (current!=root && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            // if we reached the root, start from the beginning
            current = current.children.getOrDefault(ch, root);

            // check if we found a word
            TrieNode temp = current;
            while (temp != root) {
                if (temp.isEndOfWord) {
                    found.add(temp.word);
                }
                temp = temp.fail;
            }
        }

        return found;
    }
}
