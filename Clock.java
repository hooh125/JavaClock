
import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class Clock {

	private final int HOUR_LIMIT = 23;
	private final int MIN_LIMIT = 59;
	private final int SEC_LIMIT = 59;
	private int hour = 0;
	private int min = 0;
	private int sec = 0;
	private int day = 0;
	private int month = 0;
	private int year = 0;

	public Clock() {

	}

	public String getTime() {
		return hour + ":" + min + ":" + sec + ":" + day + ":" + month + ":" + year;
	}

	public void updateTime(String timezone) {
		Calendar calendar = getNTPCalendar(timezone);
		if(calendar != null) {
			hour = calendar.get(Calendar.HOUR_OF_DAY);
			min = calendar.get(Calendar.MINUTE);
			sec = calendar.get(Calendar.SECOND);
			day = calendar.get(Calendar.DAY_OF_MONTH);
			month = calendar.get(Calendar.MONTH);
			year = calendar.get(Calendar.YEAR);
			System.out.println("La hora se ha actualizado a " + hour + ":" + min + ":" + sec + ":" + day + ":" + month + ":" + year);
		} else {
			//Ha habido un error al actualizar la hora y fecha
		}
	}

	private static Calendar getNTPCalendar(String timezone) {
		String[] hosts = new String[]{
			"ntp02.oal.ul.pt", "ntp04.oal.ul.pt",
			"ntp.xs4all.nl"};

			NTPUDPClient client = new NTPUDPClient();
			// We want to timeout if a response takes longer than 5 seconds
			client.setDefaultTimeout(5000);
			Calendar calendar = null;
			for (String host : hosts) {
				try {
					InetAddress hostAddr = InetAddress.getByName(host);
					//System.out.println("> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
					TimeInfo info = client.getTime(hostAddr);
					Date date = new Date(info.getMessage().getTransmitTimeStamp().getTime());
					calendar = new GregorianCalendar(TimeZone.getTimeZone(timezone));
					calendar.setTime(date);
					return calendar;
				}
				catch (IOException e) {
					//Ha habido un error
				}
			}
			client.close();
			return null;

		}

	}
