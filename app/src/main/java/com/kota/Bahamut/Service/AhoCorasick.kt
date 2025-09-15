package com.kota.Bahamut.Service

class AhoCorasick(dictionary: Set<String>) {
    private class TrieNode {
        val children = mutableMapOf<Char, TrieNode>()
        var isEndOfWord = false
        var fail: TrieNode? = null
        var word: String? = null
    }

    private val root = TrieNode()

    init {
        // construct the trie
        for (word in dictionary) {
            if (word.isNotEmpty()) {
                insert(word)
            }
        }
        // construct the fail links
        constructFailLinks()
    }

    private fun insert(word: String) {
        var current = root
        for (ch in word.toCharArray()) {
            current.children.putIfAbsent(ch, TrieNode())
            current = current.children[ch]!!
        }
        current.isEndOfWord = true
        current.word = word
    }

    private fun constructFailLinks() {
        val queue = mutableListOf<TrieNode>()

        // set the fail links of the root's children
        for (node in root.children.values) {
            node.fail = root
            queue.add(node)
        }

        // set the fail links of the rest of the nodes
        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)

            for ((ch, child) in current.children) {
                // find suitable failure
                var fail = current.fail
                while (fail != null && !fail.children.containsKey(ch)) {
                    fail = fail.fail
                }

                child.fail = fail?.children?.get(ch) ?: root
                queue.add(child)
            }
        }
    }

    fun search(text: String): List<String> {
        val found = mutableListOf<String>()
        var current = root

        for (ch in text.toCharArray()) {
            // if we can't find the character, go to the fail link
            while (current != root && !current.children.containsKey(ch)) {
                current = current.fail!!
            }

            // if we reached the root, start from the beginning
            current = current.children[ch] ?: root

            // check if we found a word
            var temp = current
            while (temp != root) {
                if (temp.isEndOfWord) {
                    found.add(temp.word!!)
                    return found
                }
                temp = temp.fail!!
            }
        }

        return found
    }
}
