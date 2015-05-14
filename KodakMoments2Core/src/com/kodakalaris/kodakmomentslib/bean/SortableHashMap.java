package com.kodakalaris.kodakmomentslib.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class SortableHashMap<T1, T2> implements Serializable{
	private static final long serialVersionUID = 1L;
	private ArrayList<T1> keyList = new ArrayList<T1>();
	private HashMap<T1, T2> map = new HashMap<T1, T2>();
	private transient KeyComparator<T1> comparator = new KeyComparator<T1>();
	public SortableHashMap() {};
	public SortableHashMap(SortableHashMap<T1, T2> original) {
		keyList = new ArrayList<T1>(original.keys());
		map = new HashMap<T1, T2>(original.values());
	};

	public synchronized void put(T1 key, T2 obj) {
		if (!map.containsKey(key)) {
			keyList.add(key);
		}
		map.put(key, obj);
	}
	
	public synchronized T2 get(T1 key) {
		if (key != null && map.containsKey(key)&&keyList.contains(key)) {
			return map.get(key);
		}
		else {
			return null;
		}
	}

	public synchronized T2 remove(T1 key) {
		if (key != null) {
			keyList.remove(key);
			return map.remove(key);
		}
		return null;
	}

	public synchronized T2 removeObject(Iterator<T1> keyIT) {
		T1 key = keyIT.next();
		T2 object = map.get(key);
		map.remove(key);
		keyIT.remove();
		return object;
	}

	public synchronized boolean containsKey(T1 key) {
		 return map.containsKey(key);
	}

	public synchronized ArrayList<T1> keys() {
		return keyList;
	}
	
	public synchronized HashMap<T1, T2> values() {
		return map;
	}
	
	public synchronized T2 valueAt(int index) {
		if (index < keyList.size() && index > -1) {
			return map.get(keyList.get(index));
		}
		return null;
	}
	
	public synchronized T1 keyAt(int index) {
		return keyList.get(index);
	}

	public synchronized int size() {
		return keyList.size();
	}
	
	public synchronized void clear() {
		map.clear();
		keyList.clear();
	}
	
	public void sortByValue(ValueComparator<T2> c) {
		comparator.setValueComparator(c);
		Collections.sort(keyList, comparator);
	}
	
	public static abstract class ValueComparator<T2> implements Comparator<T2> {
	}
	private class KeyComparator<T1> implements Comparator<T1> {
		ValueComparator<T2> valueComparator;
		
		public KeyComparator() {
		}
		public void setValueComparator(ValueComparator<T2> cv) {
			valueComparator = cv;
		}
		
		@Override
		public int compare(T1 object1, T1 object2) {
			return valueComparator.compare(map.get(object1), map.get(object2));
		}
		
	}
}
