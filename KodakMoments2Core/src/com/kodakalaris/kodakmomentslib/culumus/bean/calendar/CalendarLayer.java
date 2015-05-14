package com.kodakalaris.kodakmomentslib.culumus.bean.calendar;

import com.kodakalaris.kodakmomentslib.culumus.bean.product.Layer;

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
	
}
