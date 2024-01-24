package com.parkit.parkingsystem.testutil;

import java.util.Calendar;
import java.util.Date;

public class DateUtility {

	public static Date dateModifier(TimeSlot timeSlot, int amount) {
		return dateModifier(new Date(), timeSlot, amount);
	}

	public static Date dateModifier(Date actualDate, TimeSlot timeSlot, int amount) {
		Date newDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(actualDate);
		calendar.add(getTimeSlot(timeSlot), amount);
		newDate = calendar.getTime();

		return newDate;
	}

	private static int getTimeSlot(TimeSlot timeSlot) {
		return switch (timeSlot) {
			case HOUR -> Calendar.HOUR_OF_DAY;
		};
	}
}
