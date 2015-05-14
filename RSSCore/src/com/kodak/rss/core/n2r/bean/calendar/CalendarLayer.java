package com.kodak.rss.core.n2r.bean.calendar;

import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;

public class CalendarLayer extends Layer{
	private static final long serialVersionUID = 1L;
	
	public static final String FLAG_LAYERS = "Layers";
	
	public static final String TYPE_CALENDAR_GRID = "CalendarGrid";
	public static final String FLAG_DATA_MONTH_NAME = "MonthName";
	public static final String FLAG_DATA_YEAR = "Year";
	public static final String FLAG_DATA_MONTH = "Month";
	public static final String FLAG_DATA_DAY = "Day";
	public static final String FLAG_DATA_ACCEPTS_CONTENT = "AcceptsContent";
	public static final String FLAG_DATA_FIRST_CELL_LOCATION = "FirstCellLocation";
	public static final String FLAG_DATA_CELL_COLUMNS = "CellColumns";
	public static final String FLAG_DATA_CELL_ROWS = "CellRows";
	public static final String FLAG_DATA_FIRST_DAY_CELL_INDEX = "FirstDayCellIndex";
	public static final String FLAG_DATA_DAYS_IN_MONTH = "DaysInMonth";
	public static final String FLAG_DATA_READING_DIRECTION = "ReadingDirection";
	
	public static final String FLAG_DATA_CELL_INDEX = "CellIndex";//For sub layer
	
	public CalendarLayer[] sublayers;
	
	/**
	 * DataName:TYPE_YEAR\TYPE_MONTH\TYPE_CELL_COLUMNS\TYPE_CELL_ROWS\TYPE_FIRST_DAY_CELL_INDEX\TYPE_DAYS_IN_MONTH\TYPE_READING_DIRECTION
	 */
	public int getDataValue(String DataName) {
		int dataValue = -1;
		if(data == null) return dataValue;
		if(TYPE_CALENDAR_GRID.equals(type)){						
			for(Data d : data){					
				String name = d.name == null ? "" : d.name;
				if(DataName.equals(name)){
					dataValue = Integer.valueOf(d.getValue());
					break;
				}
			}						
		}
		return dataValue;
	}
	
	public int getDataValueWithoutType(String dataName){
		int dataValue = -1;
		if(data == null) return dataValue;
								
		for(Data d : data){					
			String name = d.name == null ? "" : d.name;
			if(dataName.equals(name)){
				dataValue = Integer.valueOf(d.getValue());
				break;
			}
		}						
		
		return dataValue;
	}
	
	
 	public boolean isCalendarDaysGridLayer() {
		if (data == null || !CalendarLayer.TYPE_CALENDAR_GRID.equals(type)) {
			return false;
		}
		
		for (int i = 0; i < data.length; i++) {
			if (CalendarLayer.FLAG_DATA_FIRST_DAY_CELL_INDEX.equals(data[i].name)) {
				return true;
			}
		}
	
		return false;
	}	
			
	
}
