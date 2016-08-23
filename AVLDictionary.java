import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

public class AVLDictionary<E extends Comparable<E>> implements Dictionary<E> {

	// Set this to true to enable debugging statements
	private final boolean DEBUG = true;

	private class Node {
		//Preferred to have left and right variables instead of array since they can use generics
		private E item;
		private int height;
		private Node parent;
		private Node left;
		private Node right;
		
		public Node(E i, Node left, Node right, Node p) {
			item = i;
			parent = p;
			this.left = left;
			this.right = right;
			setHeight(this);
		}
	}
	
	/**
	*My own implementation of StringBuilder
	**/
	private class MyStringBuilder {
		private char[] buffer;
		private int end;
		
		public MyStringBuilder(String initial) {
			buffer = new char[64];
			end = 0;
			append(initial);
		}
		
		public void append(String s) {
			for (int i = 0; i < s.length(); i++) {
				if (end >= buffer.length) {
					char[] tmp = new char[buffer.length * 2];
					for (int j = 0; j < end; j++) {
						tmp[j] = buffer[j];
					}
					buffer = tmp;
				}
				buffer[end++] = s.charAt(i);
			}
		}
		
		public String toString() {
			return new String(buffer);
		}
	}
	
	private Node beforeRoot;
	private int size;
	private int modCount;
	//Use this variable to keep track of how many comparisons are made, since some methods require helper methods
	private int counter;
	private MyStringBuilder logString;

	public AVLDictionary() {
		beforeRoot = new Node(null, null, null, null);
		size = 0;
		modCount = 0;
		logString = new MyStringBuilder("");
	}

	/**
	*Checks to see whether the Dictionary is empty
	*@return true if and only if the Dictionary is Empty
	**/
	public boolean isEmpty() { 
		counter++;
		return beforeRoot.left == null;
	}
	
	public int getHeight() {
		if (isEmpty()) return 0;
		return beforeRoot.left.height;
	}
	public int getSize() { return size; }
	
	private void setHeight(Node node) {
		if (node.left == null && node.right == null) {
			node.height = 0;
		} else if (node.left == null) {
			node.height = node.right.height + 1;
		} else if (node.right == null) {
			node.height = node.left.height + 1;
		} else {
			node.height = (node.left.height >= node.right.height? node.left.height : node.right.height) + 1;
		}
		counter++;
	}
	
	private void debugNode(Node node) {
		if (!DEBUG) return;
		System.out.println("------------------------");
		if (node == null) {
			System.out.println("---DEBUG FOR NULL NODE---");
			return;
		}
		System.out.println("Node - Item: "+node.item.toString()+",");
		if (node.parent.item != null)
			System.out.println("Parent: "+node.parent.item.toString());
		if (node.left != null)
			System.out.println("Left item: "+node.left.item.toString());
		if (node.right != null)
			System.out.println("Right item: "+node.right.item.toString());
		System.out.println("Height: "+node.height);
		System.out.println("Balance factor: "+getBalanceFactor(node));
		System.out.println("------------------------");
	}
	
	/**
	*Method useful for testing, prints debug info on all nodes via an inorder traversal
	**/
	public void debugAllNodes() {
		inorderDebug(beforeRoot.left);
	}
	
	private void inorderDebug(Node node) {
		if (node == null) return;
		inorderDebug(node.left);
		debugNode(node);
		inorderDebug(node.right);
	}

	/**
	*Checks to see if an element is contained in the Dictionary
	*@param item the item to be checked.
	*@return true if and only if the Dictionary contains something equal to item.
	**/
	public boolean contains(E item) {
		counter = 0;
		Node s = beforeRoot.left;
		while (s != null && !s.item.equals(item)) {
			if ((Integer)item == 6)
				System.out.println(s.item.toString());
			if (s.item.compareTo(item) > 0)
				s = s.left;
			else
				s = s.right;
			counter++;
		}
		logString.append("Operation contains() completed using "+counter+" comparisons\n");
		counter = 0;
		if (s == null) return false;
		return true;
	}

	/**
	*Checks to see if an element has a predecessor in the dictionary
	*O(lgn)
	*@return true if and only if there is an element strictly less than item in the Dictionary
	*@param item the item to be checked
	**/ 
	public boolean hasPredecessor(E item) {
		//item will only have a predecessor if and only if the minimum element in the tree precedes E
		if (!isEmpty()) {
			return min().compareTo(item) < 0;
		}
		return false;
	}

	/**
	*Checks to see if an element has a successor in the dictionary
	*O(lgn)
	*@return true if and only if there is an element strictly greater than item in the Dictionary
	*@param item the item to be checked
	**/ 
	public boolean hasSuccessor(E item) {
		//item will only have a predecessor if and only if the maximum element in the tree succeeds E
		if (!isEmpty())
			return max().compareTo(item) > 0;
		return false;
	}

	/**
	*Find the greatest element less than the specified element
	*@return the element strictly less than item in the Dictionary
	*@param item the item to be checked
	*@throws NoSuchElementException if there is no lesser element.
	**/ 
	public E predecessor(E item) throws NoSuchElementException {
		counter = 0;
		Node s = beforeRoot.left;
		E predecessor = null;
		//Loop until we reach an external node
		while (s != null) {
			counter++;
			//If the current node is >= to the item, we go down into the left sub-tree
			//Any predecessors will be less than this node and therefore lie in this area
			if (s.item.compareTo(item) >= 0 )
				s = s.left;
			//If the current node is < than the item, then it must be a new smallest predecessor
			//(See report for proof)
			//Any bigger predecessors will be in the right sub-tree
			else {
				predecessor = s.item;
				s = s.right;
			}
			counter++;
		}
		//If predecessor is null then we never found any nodes that were < item
		counter++;
		if (predecessor == null)
			throw new NoSuchElementException("Could not find predecessor of item");
		logString.append("Operation predecessor() completed using "+counter+" operations\n");
		counter = 0;
		return predecessor;
	}

	/**
	*Find the least element greater than the specified element
	*@return the element strictly greater than item in the Dictionary
	*@param item the item to be checked
	*@throws NoSuchElementException if there is no greater element.
	**/ 
	public E successor(E item) {
		counter = 0;
		Node s = beforeRoot.left;
		E successor = null;
		//Loop until we reach an external node
		while (s != null) {
			counter++;
			//If the current node is <= to the item, we go down into the right sub-tree
			//Any successors will be greater than this node and therefore lie in this area
			if (s.item.compareTo(item) <= 0 )
				s = s.right;
			//If the current node is > than the item, then it must be a new smallest successor
			//(See report for proof)
			//Any smaller successors will be in the left sub-tree
			else {
				successor = s.item;
				s = s.left;
			}
			counter++;
		}
		counter++;
		//If successor is null then we never found any nodes that were > item
		if (successor == null)
			throw new NoSuchElementException("Could not find successor of item");
		logString.append("Operation successor() completed using "+counter+" operations\n");
		counter = 0;
		return successor;
	}

	/**
	*Return the least item in the Dictionary
	*@return the least element in the Dictionary
	*@throws NoSuchElementException if the Dictionary is empty.
	**/ 
	public E min() throws NoSuchElementException {
		if (isEmpty()) throw new NoSuchElementException("No minimum element in empty dictionary");
		int n = 0;
		Node s = beforeRoot.left;
		while (s.left != null) {
			s = s.left;
			n++;
		}
		logString.append("Operation min() completed using "+n+" comparisons\n");
		return s.item;
	}
	
	/**
	*Return the greatest element in the dictionary
	*@return the greatest element in the Dictionary
	*@throws NoSuchElementException if the Dictionary is empty.
	**/ 
	public E max() throws NoSuchElementException {
		if (isEmpty()) throw new NoSuchElementException("No maximum element in empty dictionary");
		int n = 0;
		Node s = beforeRoot.left;
		while (s.right != null) {
			s = s.right;
			n++;
		}
		logString.append("Operation max() completed using "+n+" comparisons\n");
		return s.item;
	}
	
	/**
	*A helper method to determine the balance of a node.
	*A negative balance factor indicates the node is right-heavy, positive indicates left-heavy.
	*@return int the balance factor
	**/
	private int getBalanceFactor(Node node) {
		counter++;
		if (node == null) return 0;
		int left = -1;
		int right = -1;
		counter++;
		if (node.left != null)
			left = node.left.height;
		counter++;
		if (node.right != null)
			right = node.right.height;
		return left - right;
	}
	
	/**
	*Performs the necessary rotations around node that rebalance the sub-tree rooted at node.
	*@return the new root of the sub-tree
	**/
	private Node rotate(Node node) {
	
		boolean rotated = false;
		
		// DEBUG
		if (DEBUG) {
			System.out.println("---------NODE BEFORE---------");
			debugNode(node);
		}
		
		counter++;
		if (node == null) return null;
		Node newRoot = node;
		//If node is right-heavy
		if (getBalanceFactor(node) < -1) {
			rotated = true;
			//If right sub-tree is left-heavy
			if (getBalanceFactor(node.right) > 0) {
				// DEBUG
				if (DEBUG)
					System.out.println("---DOUBLE LEFT ROTATION---");
				
				//Double left rotation
				node.right = rightRotate(node.right);
				newRoot = leftRotate(node);
			} else {
				// DEBUG
				if (DEBUG)
					System.out.println("-----LEFT ROTATION-----");
				
				//Single left rotation
				newRoot = leftRotate(node);
			}
		//If left-heavy
		} else if (getBalanceFactor(node) > 1) {
			rotated = true;
			//If left sub-tree is right-heavy
			if (getBalanceFactor(node.left) < 0) {
				// DEBUG
				if (DEBUG)
					System.out.println("---DOUBLE RIGHT ROTATION---");
				
				//Double right rotation
				node.left = leftRotate(node.left);
				newRoot = rightRotate(node);
			} else {
				// DEBUG
				if (DEBUG)
					System.out.println("-----RIGHT ROTATION-----");
				
				//Single right rotation
				newRoot = rightRotate(node);
			}
		}
		counter += 2;
		
		if (DEBUG && rotated) {
			// DEBUG
			System.out.println("---------NODE AFTER---------");
			debugNode(node);
			System.out.println("-------NEWROOT AFTER-------");
			debugNode(newRoot);
			debugNode(newRoot.left);
			debugNode(newRoot.right);
		}
		
		return newRoot;
	}
	
	private Node leftRotate(Node node) {
		Node newRoot = node.right;
		node.right = newRoot.left;
		if (node.right != null) node.right.parent = node;
		
		counter++;
		
		newRoot.left = node;
		newRoot.parent = node.parent;
		
		counter++;
		
		if (node.parent.left == node) node.parent.left = newRoot;
		else if (node.parent.right == node) node.parent.right = newRoot;
		node.parent = newRoot;
		setHeight(node);
		setHeight(newRoot);
		return newRoot;
	}
	
	public void testLeftRotate() {
	}
	
	private Node rightRotate(Node node) {
		Node newRoot = node.left;
		node.left = newRoot.right;
		
		counter++;
		
		if (node.left != null) node.left.parent = node;
		newRoot.right = node;
		newRoot.parent = node.parent;
		
		counter++;
		
		if (node.parent.left == node) node.parent.left = newRoot;
		else if (node.parent.right == node) node.parent.right = newRoot;
		node.parent = newRoot;
		setHeight(node);
		setHeight(newRoot);
		return newRoot;
	}
	
	public void testRightRotate() {
	
	}
	
	/**
	*Adds a new element to the Dictionary 
	*If there is an equal element already in the table, or the item is null it returns false.
	*@param item the item to be added.
	*@return true if the item is not null, and not already in the dictionary.
	**/
	public boolean add(E item) {
		if (DEBUG)
			System.out.println("Inserting "+item.toString());
		counter++;
		//Can't add null item to tree
		if (item == null) return false;
		//Empty tree is special case, root needs to become a new node containing item
		counter++;
		if (isEmpty()) {
			beforeRoot.left = new Node(item, null, null, beforeRoot);
		} else {
			//Start at root
			Node s = beforeRoot.left;
			//Loop until we reach node that we insert after, or we reach a node that matches the item itself, in which case we don't need to insert item
			while (!s.item.equals(item)) {
				counter += 2;
				//If item is less than current node, we need to insert it into left sub-tree
				if (s.item.compareTo(item) > 0) {
					if (s.left != null)
						s = s.left;
					else {
						s.left = new Node(item, null, null, s);
						break;
					}
				}
				//Otherwise go into right sub-tree
				else if (s.item.compareTo(item) < 0) {
					if (s.right != null)
						s = s.right;
					else {
						s.right = new Node(item, null, null, s);
						break;
					}
				}
			}
			counter++;
			//If we've found a match, don't insert and return false
			if (s != null && s.item.equals(item)) return false;
			//Starting at s and traversing up the tree, rebalance using rotate
			while (s.parent != null) {
				counter++;
				setHeight(s);
				/*
				debugNode(s);
				*/
				Node next = s.parent;
				counter++;
				if (next.left == s)
					next.left = rotate(s);
				else if (next.right == s)
					next.right = rotate(s);
				// DEBUG
				else
					System.out.println("----THIS LINE SHOULD NEVER BE PRINTED----");
				/*
				debugNode(s);
				*/
				s = next;
			}
		}
		/*
		System.out.println("Height of tree is "+getHeight());
		System.out.println("-------DEBUG FOR ROOT-------");
		debugNode(beforeRoot.left);
		debugNode(beforeRoot.left.left);
		debugNode(beforeRoot.left.right);
		*/
		modCount++;
		size++;
		logString.append("Operation add(item) completed using "+counter+" comparisons\n");
		counter = 0;
		return true;
	}

	/**
	*Deletes the specified element from the Dictionary if it is present.
	*@param item the element to be removed
	*@return true if the element was in the Dictionary and has now been removed. False otherwise.
	**/
	public boolean delete(E item) {
		counter = 0;
		if (DEBUG) {
			System.out.println("Deleting "+item.toString());
		}
		//If trying to delete null item or tree is empty, return false
		if (item == null || isEmpty()) return false;
		counter++;
		//Start at root
		Node s = beforeRoot.left;
		//Loop until we reach external node, or we reach a node that matches the item itself
		while (s != null && !s.item.equals(item)) {
			//If item is less than current node, we go into the left sub-tree
			if (s.item.compareTo(item) > 0)
				s = s.left;
			//Otherwise go into right sub-tree
			else
				s = s.right;
			counter++;
		}
		//If s is null at this point, then item is not in the tree, as we've reached an external node
		if (s == null) return false;
		counter++;
		//Deleting a leaf node is straightforward
		if (s.left == null && s.right == null) {
			if (s.parent.left == s)
				s.parent.left = null;
			else
				s.parent.right = null;
		//If it only has a right child, connect it to parent
		} else if (s.left == null) {
			if (s.parent.left == s) {
				s.parent.left = s.right;
				s.parent.left.parent = s.parent;
			}
			else {
				s.parent.right = s.right;
				s.parent.right.parent = s.parent;
			}
		//Only left child
		} else if (s.right == null) {
			if (s.parent.left == s) {
				s.parent.left = s.left;
				s.parent.left.parent = s.parent;
			}
			else {
				s.parent.right = s.left;
				s.parent.right.parent = s.parent;
			}
		//Has 2 children
		} else {
			s = deleteMinOfSubtree(s.right);
		}
		counter += 2;
		while (s.parent != beforeRoot) {
			counter++;
			s = s.parent;
			setHeight(s);
			rotate(s);
		}
		if (DEBUG)
			debugNode(beforeRoot.left);
		modCount++;
		size--;
		logString.append("Operation delete(item) completed using "+counter+" comparisons\n");
		counter = 0;
		return true;
	}
	
	/**
	* A helper method to find the minimum element of a right sub-tree
	* Since this is only to be used by myself, I assume I have provided a valid node
	* @return minimum element in the sub-tree with root n
	**/
	private Node deleteMinOfSubtree(Node node) {
		Node s = node;
		while (s.left != null) {
			counter++;
			s = s.left;
		}
		//Special case is if node is the minimum
		if (s.parent == node.parent) {
			s.parent.right = s.right;
		} else {
			s.parent.left = s.right;
		}
		counter++;
		if (s.right != null) s.right.parent = s.parent;
		counter++;
		node.parent.item = s.item;
		return s;
	}

	/**
	*Provides a fail fast iterator for the Dictionary, starting at the least element
	*The iterator should implement all methods of the iterator class including remove
	*@return an iterator whose next element is the least element in the dictionary, and which will iterate through all the elements in the Dictionary in ascending order. 
	*/
	public Iterator<E> iterator() {
		return new BackingStructure();
	}

	/**
	*Provides a fail fast iterator for the Dictionary, starting at the least element greater than or equal to start
	*The iterator should implement all methods of the iterator class including remove
	*@param start the element at which to start iterating at.
	*@return an iterator whose next element is the least element greater than or equal to start in the dictionary, and which will iterate through all the elements in the Dictionary in ascending order. 
	*/
	public Iterator<E> iterator(E start) {
		return new BackingStructure(start);
	}
	
	/**
	*A private class that acts as a backing structure for the iterator.
	**/
	private class BackingStructure implements Iterator<E> {
		
		private Object[] array;
		private int index;
		private int mCount;
		
		private BackingStructure(E start) {
			mCount = modCount;
			array = new Object[size];
			index = 0;
			counter = 0;
			fillArray(beforeRoot.left, start);
			logString.append("Operation iterator() completed using "+counter+" comparisons\n");
			counter = 0;
			index = 0;
		}
		
		private BackingStructure() {
			this(min());
		}
		
		public E next() {
			checkSync();
			if (hasNext())
				return (E)array[index++];
			else
				throw new NoSuchElementException("Iterator has iterated over all elements");
		}
		
		public boolean hasNext() {
			checkSync();
			return index < array.length;
		}
		
		public void remove() {
			checkSync();
			if (index > 0 && index <= array.length && array[index-1] != null)
				throw new IllegalStateException("Either have not called next() or this element has already been deleted");
			delete((E)array[index-1]);
			array[index-1] = null;
			mCount++;
		}
		
		private void checkSync() {
			if (mCount != modCount)
				throw new ConcurrentModificationException("This iterator is no longer synchronized with the original data structure");
		}
		
		private void fillArray(Node node, E start) {
			if (node == null) return;
			counter++;
			if (node.item.compareTo(start) >= 0) {
				fillArray(node.left, start);
				array[index++] = node.item;
			}
			counter++;
			fillArray(node.right, start);
		}
	}

	/**
	*Provides a string describing all operations performed on the table since its construction, or since the last time getLogString was called
	* As each operation returns (either called directly on the Dictionary, or on an iterator generated by the dictionary) append a new line to the String:"Operation <name of op>(<parameter values>) completed using [n] comparisons". 
	*@return A sting listing all operations called on the Dictionary, and how many comparisons were required to complete each operation.
	**/ 
	public String getLogString() {
		return logString.toString();
	}

	/**
	*Provides a String representation of the Dictionary, where the representation is simply a newline-separated list of the elements in order
	*@return a String representation of the Dictionary
	**/
	public String toString() {
		counter = 0;
		MyStringBuilder string = new MyStringBuilder("");
		inorderTraversal(string, beforeRoot.left);
		logString.append("Operation toString() completed using "+counter+" comparisons\n");
		counter = 0;
		return string.toString();
	}
	
	private void inorderTraversal(MyStringBuilder string, Node node) {
		counter++;
		if (node == null) return;
		inorderTraversal(string, node.left);
		string.append(node.item.toString()+"\n");
		inorderTraversal(string, node.right);
	}
}