package com.kodak.rss.tablet.facebook;

import java.util.Comparator;

public class Comparater implements Comparator<Object>{
	@Override
	public int compare(Object o1, Object o2) {
		FbkObject user0=(FbkObject)o1;
		FbkObject user1=(FbkObject)o2;
		int flag = user0.name.compareTo(user1.name);		
		return flag;		 
	}
}
