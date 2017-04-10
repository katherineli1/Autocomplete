import java.util.*;

/**
 * 
 * Using a sorted array of Term objects, this implementation uses binary search
 * to find the top term(s).
 * 
 * @author Austin Lu, adapted from Kevin Wayne
 * @author Jeff Forbes
 */
public class BinarySearchAutocomplete implements Autocompletor {

	Term[] myTerms;

	/**
	 * Given arrays of words and weights, initialize myTerms to a corresponding
	 * array of Terms sorted lexicographically.
	 * 
	 * This constructor is written for you, but you may make modifications to
	 * it.
	 * 
	 * @param terms
	 *            - A list of words to form terms from
	 * @param weights
	 *            - A corresponding list of weights, such that terms[i] has
	 *            weight[i].
	 * @return a BinarySearchAutocomplete whose myTerms object has myTerms[i] =
	 *         a Term with word terms[i] and weight weights[i].
	 * @throws a
	 *             NullPointerException if either argument passed in is null
	 */
	public BinarySearchAutocomplete(String[] terms, double[] weights) {
		if (terms == null || weights == null)
			throw new NullPointerException("One or more arguments null");
		
		// adds new terms to Term array and sorts lexicographically
		myTerms = new Term[terms.length];
		for (int i = 0; i < terms.length; i++) {
			myTerms[i] = new Term(terms[i], weights[i]);
		}
		Arrays.sort(myTerms);
	}

	/**
	 * Uses binary search to find the index of the first Term in the passed in
	 * array which is considered equivalent by a comparator to the given key.
	 * This method should not call comparator.compare() more than 1+log n times,
	 * where n is the size of a.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the
	 *            values in a and the key.
	 * @return The first index i for which comparator considers a[i] and key as
	 *         being equal. If no such index exists, return -1 instead.
	 */
	public static int firstIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if (a == null) return -1;
		if (key == null) throw new NullPointerException();
		int low = -1;
		int high = a.length - 1;
		// use binary search to continuously cut down on number of values to check until first index is found or loop breaks
		while (high - low > 1) {
			if (low > high) break;
			int mid = (low + high)/2;
			if (comparator.compare(a[mid], key) == 0) high = mid;
			else if (comparator.compare(a[mid], key) < 0) low = mid;
			else if (comparator.compare(a[mid], key) > 0) high = mid;
		}
		// check if first index before breaking is equal to key; if not, return -1 (no index exists)
		if (comparator.compare(a[high], key) == 0) return high;
		else return -1;
	}

	/**
	 * The same as firstIndexOf, but instead finding the index of the last Term.
	 * 
	 * @param a
	 *            - The array of Terms being searched
	 * @param key
	 *            - The key being searched for.
	 * @param comparator
	 *            - A comparator, used to determine equivalency between the
	 *            values in a and the key.
	 * @return The last index i for which comparator considers a[i] and key as
	 *         being equal. If no such index exists, return -1 instead.
	 */
	public static int lastIndexOf(Term[] a, Term key, Comparator<Term> comparator) {
		if (a == null) return -1;
		if (key == null) throw new NullPointerException();
		int low = 0;
		int high = a.length;
		// use binary search to continuously cut down on number of values to check until last index is found or loop breaks
		while (high - low > 1) {
			if (low > high) break;
			int mid = (low + high)/2;
			if (comparator.compare(a[mid], key) == 0) low = mid;
			else if (comparator.compare(a[mid], key) < 0) low = mid;
			else if (comparator.compare(a[mid], key) > 0) high = mid;
		}
		// check if last index before breaking is equal to key; if not, return -1 (no index exists)
		if (comparator.compare(a[low], key) == 0) return low;
		else return -1;
	}

	/**
	 * Required by the Autocompletor interface. Returns an array containing the
	 * k words in myTerms with the largest weight which match the given prefix,
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
	 * @return An array of the k words with the largest weights among all words
	 *         starting with prefix, in descending weight order. If less than k
	 *         such words exist, return an array containing all those words If
	 *         no such words exist, return an empty array
	 * @throws a
	 *             NullPointerException if prefix is null
	 */
	public Iterable<String> topMatches(String prefix, int k) {
		// big-Oh of topMatches is O(log n + m log m) - n elements, m matching terms
		// prefix cannot be null
		if (prefix == null) throw new NullPointerException();
		
		// find first and last occurrence of matches using firstIndexOf and lastIndexOf methods
		int firstIndex = firstIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		int lastIndex = lastIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		
		// if either no first or no last match, return empty String ArrayList
		if (firstIndex == -1 || lastIndex == -1)
			return new ArrayList<String>();
		
		// add each matching term to a Term ArrayList
		ArrayList<Term> arr = new ArrayList<>();
		for (int i = firstIndex; i <= lastIndex; i++) {
			arr.add(myTerms[i]);
		}
		
		// sort ArrayList of matching Terms based on weight using comparator ReverseWeightOrder
		Collections.sort(arr, new Term.ReverseWeightOrder());
		
		// add k (or arr.size()) number of Term words to a String ArrayList to return		
		ArrayList<String> fin = new ArrayList<>();
		int numResults = Math.min(k, arr.size());
		for (int i = 0; i < numResults; i++) {
			fin.add(arr.get(i).getWord());
		}
		
		return fin;
		
//		PriorityQueue<Term> pq = new PriorityQueue<Term>(k, new Term.WeightOrder());
//		for (int i = firstIndex; i <= lastIndex; i++) {
//			System.out.println(pq);
//			if (pq.size() < k) {
//				pq.add(myTerms[i]);
//			}
//			else if (pq.peek().getWeight() < myTerms[i].getWeight()) {
//				pq.remove();
//				pq.add(myTerms[i]);
//			}
//		}
//		
//		ArrayList<String> fin = new ArrayList<>();
//		int numResults = Math.min(k, pq.size());
//		for (int i = 0; i < pq.size(); i++) {
//			fin.add(pq.remove().getWord());
//		}
//		System.out.println(fin);
//		System.out.println("\n");		
	}

	/**
	 * Given a prefix, returns the largest-weight word in myTerms starting with
	 * that prefix. e.g. for {air:3, bat:2, bell:4, boy:1}, topMatch("b") would
	 * return "bell". If no such word exists, return an empty String.
	 * 
	 * @param prefix
	 *            - the prefix the returned word should start with
	 * @return The word from myTerms with the largest weight starting with
	 *         prefix, or an empty string if none exists
	 * @throws a
	 *             NullPointerException if the prefix is null
	 * 
	 */
	public String topMatch(String prefix) {
		// big-Oh of topMatches is O(log n + m)
		// prefix cannot be null
		if (prefix == null) throw new NullPointerException();
		
		// find first and last occurrence of matches using firstIndexOf and lastIndexOf methods
		int firstIndex = firstIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		int lastIndex = lastIndexOf(myTerms, new Term(prefix, 0), new Term.PrefixOrder(prefix.length()));
		
		// if either no first or no last match, return empty string
		if (firstIndex == -1 || lastIndex == -1)
			return "";
		
		// go through matching terms and if weight of term being checked is greater than Term in pq, replace them
		PriorityQueue<Term> pq = new PriorityQueue<Term>(1, new Term.ReverseWeightOrder());
		for (int i = firstIndex; i <= lastIndex; i++) {
			// add first Term to pq
			if (pq.peek() == null) pq.add(myTerms[i]);
			else if (pq.peek().getWeight() < myTerms[i].getWeight()) {
				pq.remove();
				pq.add(myTerms[i]);
			}
		}
		return pq.peek().getWord();
	}

	/**
	 * Return the weight of a given term. If term is not in the dictionary,
	 * return 0.0
	 */
	public double weightOf(String term) {
		int firstIndex = firstIndexOf(myTerms, new Term(term, 0), new Term.PrefixOrder(term.length()));
		int lastIndex = lastIndexOf(myTerms, new Term(term, 0), new Term.PrefixOrder(term.length()));
		for (int i = firstIndex; i <= lastIndex; i++) {
			if (myTerms[i].getWord().equalsIgnoreCase(term))
				return myTerms[i].getWeight();
		}
		// if term is not in the dictionary return 0
		return 0.0;
	}
}
