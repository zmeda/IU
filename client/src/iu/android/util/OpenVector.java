package iu.android.util;

import java.util.Arrays;

public class OpenVector<T>
{
	// Array that holds the data - it is public - thats why the name is 'Open Vector'
	public T[]	data;

	// Holds the current size of the array
	private int	capacity;

	// Increment for size of the array
	private int	increment;

	// Index of the last element in the array
	private int	nextIndex;

	public OpenVector()
	{
		this.data = (T[]) new Object[10];
		// this.data = (T[]) Array.newInstance(this.data.getClass(), 10);

		// (V[]) Array.newInstance(elementType, DEFAULT_LENGTH);

		this.capacity = 10;
		this.increment = 10;
		this.nextIndex = 0;

		System.out.println("New open vector : " + this.data.length);
	}

	public OpenVector(final int capacity)
	{
		this.data = (T[]) new Object[capacity];
		this.increment = 10;
		this.capacity = capacity;
		this.nextIndex = 0;
	}

	public OpenVector(final int capacity, final int increment)
	{
		this.data = (T[]) new Object[capacity];
		this.increment = increment;
		this.capacity = capacity;
		this.nextIndex = 0;
	}

	public void add(T t)
	{
		int cap = this.capacity;

		// Enough space
		if (this.nextIndex < cap)
		{
			this.data[this.nextIndex++] = t;
		}
		else
		// Not enough space
		{
			final int newCap = this.capacity + this.increment;

			T[] newData = (T[]) new Object[newCap];

			System.arraycopy(this.data, 0, newData, 0, cap);

			newData[this.nextIndex++] = t;

			this.data = newData;

			this.capacity = newCap;
		}
	}

	/**
	 * Same as add()
	 * 
	 * @param t -
	 *            element to add
	 */
	public void addElement(T t)
	{
		int cap = this.capacity;

		// Enough space
		if (this.nextIndex < cap)
		{
			this.data[this.nextIndex++] = t;
		}
		else
		// Not enough space
		{
			final int newCap = this.capacity + this.increment;

			T[] newData = (T[]) new Object[newCap];

			System.arraycopy(this.data, 0, newData, 0, cap);

			newData[this.nextIndex++] = t;

			this.data = newData;

			this.capacity = newCap;
		}
	}

	public void removeAllElements()
	{
		Arrays.fill(this.data, null);
	}

	public void remove(final int idx)
	{
		int last = this.nextIndex - 1;

		if (idx == last)
		{
			this.data[last] = null;
		}
		else
		{
			System.arraycopy(this.data, idx + 1, this.data, idx, this.nextIndex - idx);
			this.data[last] = null;
			this.nextIndex--;
		}
	}

	// public static void main(String[] args)
	// {
	// OpenVector<Integer> vec = new OpenVector<Integer>();
	//
	// vec.data = new Integer[5];
	//
	// System.out.println("Lenght : " + vec.data.length);
	// //
	// // vec.data[0] = new Integer(5);
	// // vec.data[1] = new Integer(15);
	// // vec.data[2] = new Integer(25);
	// //
	// // // Fill
	// // // for (int i = 0; i < 5; i++)
	// // // {
	// // // vec.add(new Integer(i));
	// // // }
	// //
	// // // Display
	// // for (int i = 0; i < 3; i++)
	// // {
	// // System.out.println(i + ": " + vec.data[i].intValue());
	// // }
	// }
}
