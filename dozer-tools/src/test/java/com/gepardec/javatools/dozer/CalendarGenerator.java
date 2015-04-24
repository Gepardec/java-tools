package com.gepardec.javatools.dozer;

import java.util.Calendar;

import com.gepardec.javatools.dozer.MappingChecker.CustomGenerator;

public class CalendarGenerator implements CustomGenerator<Calendar> {

	@Override
	public Calendar generate() {
		Calendar c = Calendar.getInstance();
		c.set(2000, 01, 01, 12, 30);
		return c;
	}

}
