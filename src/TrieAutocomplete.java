import java.util.*;

/**
 * General trie/priority queue algorithm for implementing Autocompletor
 * 
 * @author Austin Lu
 * @author Jeff Forbes
 */
public class TrieAutocomplete implements Autocompletor {

	/**
	 * Root of entire trie
	 */
	protected Node myRoot; // only can be accessed from inside TriAutocomplete

	/**
	 * Constructor method for TrieAutocomplete. Should initialize the trie
	 * rooted at myRoot, as well as add all nodes necessary to represent the
	 * words in terms.
	 * 
	 * @param terms
	 *            - The words we will autocomplete from
	 * @param weights
	 *            - Their weights, such that terms[i] has weight weights[i].
	 * @throws NullPointerException
	 *             if either argument is null
	 * @throws IllegalArgumentException
	 *             if terms and weights are different weight
	 */
	public TrieAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null)
			throw new NullPointerException("One or more arguments null");
		// 2. Length of terms and weights are equal
		if (terms.length != weights.length)
			throw new IllegalArgumentException("Terms and weights are not the same length");
		HashSet<String> words = new HashSet<String>();
		// Represent the root as a dummy/placeholder node
		myRoot = new Node('-', null, 0);

		for (int i = 0; i < terms.length; i++) {
			if (words.contains(terms[i]))
				throw new IllegalArgumentException("Duplicate term " + terms[i]);
			words.add(terms[i]);
			add(terms[i], weights[i]);
		}
		if (words.size() != terms.length) 
			throw new IllegalArgumentException("Duplicate terms");
	}

	/**
	 * Add the word with given weight to the trie. If word already exists in the
	 * trie, no new nodes should be created, but the weight of word should be
	 * updated.
	 * 
	 * In adding a word, this method should do the following: Create any
	 * necessary intermediate nodes if they do not exist. Update the
	 * subtreeMaxWeight of all nodes in the path from root to the node
	 * representing word. Set the value of myWord, myWeight, isWord, and
	 * mySubtreeMaxWeight of the node corresponding to the added word to the
	 * correct values
	 * 
	 * @throws a
	 *             NullPointerException if word is null
	 * @throws an
	 *             IllegalArgumentException if weight is negative.
	 */
	private void add(String word, double weight) {
		Node current = myRoot;
		// find the node (creating new Nodes where necessary) for word
		// set mySubtreeMaxWeight and myInfo for each current
		for (char ch : word.toCharArray()) {
			if (current.mySubtreeMaxWeight < weight)
				current.mySubtreeMaxWeight = weight;
			if (!current.children.containsKey(ch)) // if current's children map does not contain key ch
				current.children.put(ch, new Node(ch, current, weight));
			current = current.children.get(ch);
			current.myInfo = "" + ch;
		}
		// set current to be a word
		current.isWord = true;
		// set current's myWord
		current.myWord = word;
		// set current's myWeight
		current.myWeight = weight;
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the
	 * k words in the trie with the largest weight which match the given prefix,
	 * in descending weight order. If less than k words exist matching the given
	 * prefix (including if no words exist), then the array instead contains all
	 * those words. e.g. If terms is {air:3, bat:2, bell:4, boy:1}, then
	 * topKMatches("b", 2) should return {"bell", "bat"}, but topKMatches("a",
	 * 2) should return {"air"}
	 * 
	 * @param prefix
	 *            - A prefix which all returned words must start with
	 * @param k
	 *            - The (maximum) number of words to be returned
	 * @return An Iterable of the k words with the largest weights among all
	 *         words starting with prefix, in descending weight order. If less
	 *         than k such words exist, return all those words. If no such words
	 *         exist, return an empty Iterable
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		// prefix cannot be null
		if (prefix == null) throw new NullPointerException();
		if (k <= 0) return new ArrayList<String>();
		Node current = myRoot;
		PriorityQueue<Node> pq = new PriorityQueue<Node>(k, new Node.ReverseSubtreeMaxWeightComparator());
		ArrayList<String> arr = new ArrayList<>();
		
		// navigate current to prefix node
		for (char ch : prefix.toCharArray()) {
			if (!current.children.containsKey(ch)) return new ArrayList<String>();
			current = current.children.get(ch);
		}
		pq.add(current);
		// while pq is not empty and top k matches haven't been found
		// continue to search through trie for matching words and adding to String ArrayList
		while (pq.peek() != null && arr.size() != k) {
			current = pq.remove();
			if (current.isWord == true)
				arr.add(current.myWord);
			// add every child of current to pq to continue searching through trie
			for (Node child : current.children.values()) {
				pq.add(child);
			}
		}
		return arr;
	}

	/**
	 * Given a prefix, returns the largest-weight word in the trie starting with
	 * that prefix.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from with the largest weight starting with prefix, or an
	 *         empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 */
	public String topMatch(String prefix) {
		if (prefix == null) throw new NullPointerException();
		Node current = myRoot;
		// loop through characters in prefix to get to node corresponding to prefix
		for (char ch : prefix.toCharArray()) {
			if (!current.children.containsKey(ch)) return "";
			current = current.children.get(ch);
		}
		// navigate down tree until mySubtreeMaxWeight is equal to myWeight
		while (current.mySubtreeMaxWeight != current.myWeight) {
			for (char x : current.children.keySet()) {
				if (current.mySubtreeMaxWeight == current.children.get(x).mySubtreeMaxWeight) {
					current = current.children.get(x);
					break;
				}
			}
		}
		return current.myWord;
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary,
	 * return 0.0
	 */
	public double weightOf(String term) {
		Node current = myRoot;
		// loop through every char in term and check if that char is in current's children
		for (char ch : term.toCharArray()) {
			// if char is not in current's children, return 0.0
			if (!current.children.containsKey(ch)) return 0.0;
			else current = current.children.get(ch);
		}
		return current.myWeight;
	}

	/**
	 * Optional: Returns the highest weighted matches within k edit distance of
	 * the word. If the word is in the dictionary, then return an empty list.
	 * 
	 * @param word
	 *            The word to spell-check
	 * @param dist
	 *            Maximum edit distance to search
	 * @param k
	 *            Number of results to return
	 * @return Iterable in descending weight order of the matches
	 */
	public Iterable<String> spellCheck(String word, int dist, int k) {
		return null;
	}
}
