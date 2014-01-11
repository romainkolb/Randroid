package org.loutr.randroid.data;

import android.provider.BaseColumns;

public class RandoContract {

	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public RandoContract() {
	}
	
	public static abstract class Rando implements BaseColumns {
        public static final String TABLE_NAME = "rando";
        public static final String COLUMN_NAME_DATE = "date";
    }
	
	public static abstract class CheckPoint implements BaseColumns {
		public static final String TABLE_NAME = "latlng";
        public static final String COLUMN_NAME_RANDO_ID = "rando_id";
        public static final String COLUMN_NAME_SEGMENT = "segment";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
	}
	
}
