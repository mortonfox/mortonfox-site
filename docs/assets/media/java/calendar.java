// <applet code=calendar.class height=800 width=600>
// </applet>

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;

/**
 * Perpetual Calendar program.
 * Last updated: January 13, 2000
 *
 * @author Po Shan Cheah
 * @version 0.1
 */

public class calendar extends Applet {
    TextArea output;
    TextField yearentry;

    public String getAppletInfo() {
	return "Perpetual Calendar. By Po Shan Cheah (c)2000";
    }

    /**
     * Repeats a character.
     * @param ch Character to repeat.
     * @param count Number of repetitions.
     * @return ch x count in a string.
     */
    String repchar(char ch, int count) {
	String s = "";
	for (int i = 0; i < count; ++i)
	    s += ch;
	return s;
    }

    /**
     * Pads a number to the specified width with spaces.
     * @param num Number to format.
     * @param width Width to which we need to pad.
     * @return Padded number string.
     */
    String padnum(int num, int width) {
	String s = Integer.toString(num);
	return repchar(' ', width - s.length()) + s;
    }

    /**
     * Uses Zeller's congruence to determine the day of the week
     * for a specified date.
     * @param day Day of month.
     * @param month Month of the year.
     * @param year Year.
     * @return 0-6 representing Sun-Sat
     */
    int dayofweek(int day, int month, int year) {
	// Zeller's congruence.
	int century;
	int yr;

	if (month <= 2) {
	    month += 10;
	    --year;
	}
	else
	    month -= 2;

	century = year / 100;
	yr = year % 100;

	return ((26 * month - 2) / 10 + day + yr + yr / 4 + century / 4 +
		203 - 2 * century) % 7;
    }

    /**
     * Is this a leap year?
     * @param year Year to check.
     * @return true if that year is a leap year.
     */
    boolean leapyear(int year) {
	return (year % 400 == 0) || (year % 4 == 0) && (year % 100 != 0);
    }

    /**
     * Get last day of the month.
     * @param month Month of year.
     * @param year The year.
     * @return Last day of that month.
     */
    int getmaxday(int month, int year) {
	int monthlen[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	int maxday = monthlen[month - 1];
	if (month == 2 && leapyear(year))
	    ++maxday;
	return maxday;
    }

    /**
     * Generates a yearly calendar.
     * @param year The year.
     * @return A string containing the formatted calendar.
     */
    String generateCalendar(int year) {
	String[] monthnames = { 
	    "January", "February", "March", "April", "May", "June", "July",
	    "August", "September", "October", "November", "December" 
	};
	String outstr = "";

	outstr += padnum(year, 37) + "\n\n";

	for (int row = 0; row <= 3; ++row) {
	    for (int col = 0; col <= 2; ++col) {
		String monthname = monthnames[row * 3 + col];
		int spacebefore = (19 - monthname.length()) / 2 + 2;
		outstr += 
		    repchar(' ', spacebefore) +
		    monthname +
		    repchar(' ', 24 - monthname.length() - spacebefore);
	    }
	    outstr += "\n\n";

	    for (int col = 0; col <= 2; ++col) 
		outstr += ("  S  M  T  W  T  F  S   ");
	    outstr += "\n";

	    for (int vcell = 0; vcell <= 5; ++vcell) {
		for (int col = 0; col <= 2; ++col) {
		    int month = row * 3 + col + 1;
		    int maxday = getmaxday(month, year);
		    for (int hcell = 0; hcell <= 6; ++hcell) {
			int day = vcell * 7 + hcell + 1 -
			    dayofweek(1, month, year);
			if (day > 0 && day <= maxday)
			    outstr += padnum(day, 3);
			else
			    outstr += "   ";
		    }
		    outstr += "   ";
		}
		outstr += "\n";
	    }
	    outstr += "\n";
	}

	return outstr;
    }

    /**
     * Show the calendar in the textarea.
     * @param yearstr Year string from an input field.
     */
    void showCalendar(String yearstr) {
	int year;
	try {
	    if (yearstr.length() == 0)
		return;
	    year = Integer.parseInt(yearstr);
	    if (year < 1 || year > 9999)
		throw new NumberFormatException();
	    output.setText(generateCalendar(year));
	}
	catch (NumberFormatException e) {
	    output.setText("Invalid year: " + yearstr);
	}
    }

    public void init() {
	Panel toppanel = new Panel();

	toppanel.add("North", new Label("Year:"));

	yearentry = new TextField(5);
	toppanel.add("North", yearentry);

	yearentry.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    showCalendar(e.getActionCommand());
		}
	    }
	);

	setLayout(new BorderLayout());
	add("North", toppanel);

	output = new TextArea();
	output.setFont(new Font("Courier", Font.PLAIN, 12));
	output.setEditable(false);
	add("Center", output);
    }

    public void start() {
	// This will move the focus to the year entry textfield once
	// the applet starts.
	// In standalone mode, the focus already begins in the
	// year entry textfield so no action is required.
	yearentry.requestFocus();
    }

    public static void main(String args[]) {
	calendar cal = new calendar();
	cal.init();
	cal.start();

	final Frame f = new Frame("calendar");

	f.addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    f.setVisible(false);
		    f.dispose();
		    System.exit(0);
		}
	    }
	);
	
	f.add("Center", cal);
	f.setSize(600, 800);
	f.show();
    }
}

// The End
