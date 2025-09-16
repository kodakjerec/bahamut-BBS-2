package com.kota.Bahamut.Service

import java.util.LinkedList
import java.util.Queue

class AhoCorasick(dictionary: MutableSet<String?>) {
    private class TrieNode {
        var children: MutableMap<Char?, TrieNode> = HashMap<Char?, TrieNode>()
        var isEndOfWord: Boolean = false
        var fail: TrieNode? = null
        var word: String? = null
    }

    private val root: TrieNode

    init {
        root = TrieNode()
        // construct the trie
        for (word in dictionary) {
            if (word == null || word.isEmpty()) continue  // empty strings are not allowed

            insert(word)
        }
        // construct the fail links
        constructFailLinks()
    }

    private fun insert(word: String) {
        var current = root
        for (ch in word.toCharArray()) {
            current.children.putIfAbsent(ch, TrieNode())
            current = current.children.get(ch)
        }
        current.isEndOfWord = true
        current.word = word
    }

    private fun constructFailLinks() {
        val queue: Queue<TrieNode?> = LinkedList<TrieNode?>()

        // set the fail links of the root's children
        for (node in root.children.values) {
            node.fail = root
            queue.offer(node)
        }

        // set the fail links of the rest of the nodes
        while (!queue.isEmpty()) {
            val current = queue.poll()

            for (entry in current!!.children.entries) {
                val ch: Char = entry.key!!
                val child = entry.value

                // find suitable failure
                var fail = current.fail
                while (fail != null && !fail.children.containsKey(ch)) {
                    fail = fail.fail
                }

                child.fail = if (fail == null) root else fail.children.get(ch)
                queue.offer(child)
            }
        }
    }

    fun search(text: String): MutableList<String?> {
        val found: MutableList<String?> = ArrayList<String?>()
        var current = root

        for (ch in text.toCharArray()) {
            // if we can't find the character, go to the fail link
            while (current !== root && !current.children.containsKey(ch)) {
                current = current.fail
            }

            // if we reached the root, start from the beginning
            current = current.children.getOrDefault(ch, root)

            // check if we found a word
            var temp: TrieNode? = current
            while (temp !== root) {
                if (temp!!.isEndOfWord) {
                    found.add(temp.word)
                    return found
                }
                temp = temp.fail
            }
        }

        return found
    }
}
