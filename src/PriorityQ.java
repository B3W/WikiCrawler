/**
 * Copyright (c) 2019 Weston Berg 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.ArrayList;
import java.util.Collections;

/**
 * A priority queue represented as a max heap
 * @author Weston Berg
 */
public class PriorityQ {

	/**
	 * Inner class symbolizing nodes in the heap
	 * Nodes contain a string and its priority
	 * @author Weston Berg
	 */
	private class HeapNode {
		
		private String value;
		private int key;
		
		/**
		 * Construct a new HeapNode
		 * 
		 * @param value  String value for the node
		 * @param key  priority for the node
		 */
		public HeapNode(String value, int key) {
			this.value = value;
			this.key = key;
		}

	}
	
	// Heap represented as an array
	private ArrayList<HeapNode> heapArr;
	private int heapSize;
	
	/**
	 * Constructs empty priority queue
	 */
	public PriorityQ() {
		heapArr = new ArrayList<HeapNode>();
		heapArr.add(new HeapNode(null, -1)); // Root of heap starting at array index 1
		heapSize = 0;
	}
	
	/**
	 * Adds a new HeapNode to the heap
	 * 
	 * @param s  HeapNode's string value
	 * @param p  HeapNode's priority
	 */
	public void add(String s, int p) {
		heapArr.add(new HeapNode(s, p));
		heapSize++;
		percolateUp();
	} // add
	
	
	/**
	 * Beginning at the last element in the heap, compare the element
	 * to its parent and swap if the element is greater. Repeat this
	 * at the new location of the element. This will restore the max
	 * heap property after a new node is added.
	 */
	private void percolateUp() {
		int curIndex = heapSize;
		int parent = 0;
		while (curIndex > 1) {
			parent = curIndex >> 1;  // Parent = i / 2
			if (heapArr.get(curIndex).key > heapArr.get(parent).key) {
				Collections.swap(heapArr, curIndex, parent);
				curIndex = parent;
			} else {
				return;
			}
		}
	} // percolateUp
	
	
	/**
	 * Peeks at the max value in the heap
	 * 
	 * @return  String whose priority is maximum, or null if queue empty
	 */
	public String returnMax() {
		if (heapSize == 0) {
			return null;
		}
		return heapArr.get(1).value;
	} // returnMax
	
	
	/**
	 * Get the max value from the heap and remove it
	 * 
	 * @return  String whose priority is maximum, or null if queue empty
	 */
	public String extractMax() {
		if (heapSize == 0) {
			return null;
		}
		HeapNode retNode = heapArr.get(1);
		remove(1);
		
		return retNode.value;
	} // extractMin
	
	
	/**
	 * Starting at the index root, the priority of root HeapNode is compared to its
	 * children. If children are larger then root HeapNode is swapped with largest
	 * child and process is repeated at largest child's index. Max heap property
	 * is restored once no swapping is required or a leaf node is reached
	 * 
	 * @param root  Index to begin at
	 */
	private void percolateDown(int root) {
		int curIndex = root;	// Start from the root
		int leftChild = 0;
		int rightChild = 0;
		int maxChild = curIndex;
		
		boolean swap_occurred = false;
		
		do {
			leftChild = (curIndex << 1);	// Left Child = 2i
			rightChild = (curIndex << 1) + 1;	// Right Child = 2i + 1
			
			if (leftChild > heapSize  && rightChild > heapSize) {	// Check if curIndex is leaf
				return;
			} else {
				if ((leftChild <= heapSize) && (heapArr.get(leftChild).key > heapArr.get(maxChild).key)) {	// Check if left child is largest
					maxChild = leftChild;
				}
				if ((rightChild <= heapSize) && (heapArr.get(rightChild).key > heapArr.get(maxChild).key)) {	// Check if right child is largest
					maxChild = rightChild;
				}
				
				if (maxChild != curIndex) {	// Check if swap needed
					Collections.swap(heapArr, curIndex, maxChild);
					curIndex = maxChild;
					swap_occurred = true;
				} else {
					swap_occurred = false;
				}
			}
			
		} while (swap_occurred); // Check if swap occurred, if not max heap property restored so exit loop

	} // percolateDown
	
	
	/**
	 * Removes element from queue at index or nothing if queue
	 * is empty or index is out of bounds
	 * 
	 * @param i  Index to remove node at
	 */
	public void remove(int i) {
		if (heapSize == 0 || i < 1 || i > heapSize) {
			return;
		}
		Collections.swap(heapArr, i, heapSize);	// Swap first and last elements in the heap
		heapArr.remove(heapSize);	// Remove element from the heap
		heapSize--;
		percolateDown(i);
		
	} // remove
	
	
	/**
	 * Decrements the priority of the HeapNode by the specified
	 * amount at the given index. If amount being decremented
	 * greater than current priority then priority set to 0
	 * 
	 * @param i  Index of HeapNode to decrement priority
	 * @param k  Amount to decrement priority
	 */
	public void decrementPriority(int i, int k) {
		if (heapSize == 0 || i < 1 || i > heapSize || k < 0) {
			return;
		}
		
		int curKey = heapArr.get(i).key;
		if (k > curKey) {
			heapArr.get(i).key = 0;
		} else {
			heapArr.get(i).key = curKey - k;
		}
		
		percolateDown(i);	// Restore max heap property
		
	} // decrementPriority
	
	
	/**
	 * Returns array B with following property: B[i] = key(A[i])
	 * 
	 * @return  int[] containing priorities in their respective indeces in heap array
	 */
	public int[] priorityArray() {
		if (heapSize == 0) {
			return null;
		}
		
		int[] priorityArr = new int[heapSize+1];
		for (int i = 1; i <= heapSize; i++) {
			priorityArr[i] = heapArr.get(i).key;
		}
		
		return priorityArr;
	} // priorityArray
	
	
	/**
	 * Get key at an index
	 * 
	 * @param i  Index to get key from
	 * @return  Key at index i, or -1 if index out of bounds or queue empty
	 */
	public int getKey(int i) {
		if (heapSize == 0 || i < 1 || i > heapSize) {
			return -1;
		}
		return heapArr.get(i).key;
	} // getKey
	
	
	/**
	 * Get value at an index
	 * 
	 * @param i  Index to get value from
	 * @return  Value at index i, or null if index out of bounds or queue empty
	 */
	public String getValue(int i) {
		if (heapSize == 0 || i < 1 || i > heapSize) {
			return null;
		}
		return heapArr.get(i).value;
	} // getValue
	
	
	/**
	 * Determines if queue is empty or not
	 * 
	 * @return  true if and only if queue is empty
	 */
	public boolean isEmpty() {
		return heapSize == 0;
	} // isEmpty

}
