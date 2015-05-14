package com.kodak.quickbook.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class QuickBook {
	public static final String AUTHORITY = "com.kodak.provider.QuickBook";

	
	public QuickBook() { }
	
	public static final class Thumbnail implements BaseColumns{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/thumbnails");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.kodak.thumbnail";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.kodak.thumbnail";
        
        public static final String DEFAULT_SORT_ORDER = "create_time DESC";
        
        public static final String PATH = "path";
        public static final String CREATE_TIME = "create_time";
        public static final String THUMBNAIL_DATA = "thumbnail_data";
        
	}
	
	public static final class Mini implements BaseColumns{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/minis");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.kodak.mini";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.kodak.mini";
        
        public static final String DEFAULT_SORT_ORDER = "create_time DESC";
        
        public static final String PATH = "path";
        public static final String CREATE_TIME = "create_time";
        public static final String MINI_DATA = "mini_data";
	}
}
