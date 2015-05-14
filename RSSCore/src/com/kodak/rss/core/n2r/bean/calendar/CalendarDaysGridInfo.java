package com.kodak.rss.core.n2r.bean.calendar;

import java.io.Serializable;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.prints.Data;

public class CalendarDaysGridInfo implements Serializable{
	private static final long serialVersionUID = 2482474225009965373L;
	public String monthName;
	public int year;
	public int month;
	public boolean acceptsContent;
	public ROI firstCellLocation;
	public int cellColumns;
	public int cellRows;
	public int firstDayCellIndex;
	public int daysInMonth;
	public int readingDirection;
	/*
	public static final String TYPE_CALENDAR_GRID = "CalendarGrid";
	public static final String FLAG_DATA_MONTH_NAME = "MonthName";
	public static final String FLAG_DATA_YEAR = "Year";
	public static final String FLAG_DATA_MONTH = "Month";
	public static final String FLAG_DATA_ACCEPTS_CONTENT = "AcceptsContent";
	public static final String FLAG_DATA_FIRST_CELL_LOCATION = "FirstCellLocation";
	public static final String FLAG_DATA_CELL_COLUMNS = "CellColumns";
	public static final String FLAG_DATA_CELL_ROWS = "CellRows";
	public static final String FLAG_DATA_FIRST_DAY_CELL_INDEX = "FirstDayCellIndex";
	public static final String FLAG_DATA_DAYS_IN_MONTH = "DaysInMonth";
	public static final String FLAG_DATA_READING_DIRECTION = "ReadingDirection";
	*/
	public CalendarDaysGridInfo(CalendarLayer layer) {
		if (layer != null && layer.data != null) {
			for (int i = 0; i < layer.data.length; i++) {
				String name = layer.data[i].name;
				if (CalendarLayer.FLAG_DATA_MONTH_NAME.equals(name)) {
					monthName = (String) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_YEAR.equals(name)) {
					year = (Integer) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_MONTH.equals(name)) {
					month = (Integer) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_ACCEPTS_CONTENT.equals(name)) {
					acceptsContent = (Boolean) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_FIRST_CELL_LOCATION.equals(name)) {
					firstCellLocation = (ROI) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_CELL_COLUMNS.equals(name)) {
					cellColumns = (Integer) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_CELL_ROWS.equals(name)) {
					cellRows = (Integer) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_FIRST_DAY_CELL_INDEX.equals(name)) {
					firstDayCellIndex = (Integer) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_DAYS_IN_MONTH.equals(name)) {
					daysInMonth = (Integer) layer.data[i].value;
				} else if (CalendarLayer.FLAG_DATA_READING_DIRECTION.equals(name)) {
					readingDirection = (Integer) layer.data[i].value;
				}
			}
		}
	}
}
