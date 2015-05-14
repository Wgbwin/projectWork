package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import android.net.Uri;
import android.provider.BaseColumns;

public class GreetingCardDatabase {
	public static final String AUTHORITY = "com.kodak.provider.GreetingCard";
	
	public static final class CardPreview implements BaseColumns{
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cardpreviews");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.kodak.cardpreview";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.kodak.cardpreview";
        
        public static final String DEFAULT_SORT_ORDER = "create_time DESC";
        
        public static final String PATH = "path";
        public static final String CREATE_TIME = "create_time";
        public static final String PREVIEW_DATA = "cardpreview_data";
	}
}
