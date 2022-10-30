// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

/**
 * Dictionary of <code>Count</code> key-value pairs.
 * A key is an integer between zero and <code>Count-1</code>. A value is an int.
 * The dictionary always contains exactly <code>Count</code> mappings. The values are initially all zeros.
 * The pairs are ordered by their last usage, from Most Recently Used (MRU) to Least Recently Used (LRU).
 * When you add or retrieve a value, it becomes the Most Recently Used.
 * When you add a value, the Least Recently Used is discarded.
 */
class RecentInts
{
	RecentInts()
	{
		for (int i = 0; i < 128; i++) {
			this.prev[i] = (byte) ((i + 1) & 127);
			this.next[i] = (byte) ((i - 1) & 127);
		}
	}

	private static final int COUNT = 128;
	private final int[] value = new int[128];
	private final byte[] prev = new byte[128];
	private final byte[] next = new byte[128];
	private int head = 0;

	/**
	 * Store the value.
	 * Will use the Least Recently Used key.
	 * Its previous value is discarded and now it maps to <code>value</code>
	 * and becomes the Most Recently Used.
	 */
	final void add(int value)
	{
		this.head = this.prev[this.head] & 0xff;
		this.value[this.head] = value;
	}

	/**
	 * Retrieve a value by key.
	 */
	final int get(int key)
	{
		if (key != this.head) {
			int prev = this.prev[key] & 0xff;
			int next = this.next[key] & 0xff;
			this.next[prev] = (byte) next;
			this.prev[next] = (byte) prev;
			int tail = this.prev[this.head] & 0xff;
			this.next[tail] = (byte) key;
			this.prev[key] = (byte) tail;
			this.prev[this.head] = (byte) key;
			this.next[key] = (byte) this.head;
			this.head = key;
		}
		return this.value[key];
	}
}
