
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class Clock {

	private static Clock instance = null;
	private Timer timer;
	private final int HOUR_LIMIT = 23;
	private final int MIN_LIMIT = 59;
	private final int SEC_LIMIT = 59;
	private int hour = 0;
	private int min = 0;
	private int sec = 0;
	private int day = 0;
	private int month = 0;
	private int year = 0;

	protected Clock() {

	}

	public static Clock getInstance() {
		if(instance == null) {
			instance = new Clock();
		}
		return instance;
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
			month = calendar.get(Calendar.MONTH) + 1;
			year = calendar.get(Calendar.YEAR);
			System.out.println("La hora se ha actualizado a " + hour + ":" + min + ":" + sec + ":" + day + ":" + month + ":" + year);
			//Actualizamos la hora del módulo RTC
			String line;
			try {
				String date = String.format("%1$04d%2$02d%3$02d%4$02d%5$02d%6$02d", year, month, day, hour, min, sec);
				String command = "sudo ./rtc-pi " + date;
				System.out.println("Ejecutando el comando: "  + command);
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

		public void runChrono() {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					GPIO_Manager gpio_manager = GPIO_Manager.getInstance();
					gpio_manager.beep();
				}
			}, 25 * 60 * 1000);
		}

		public void stopChrono() {
			if(timer != null)
			timer.cancel();
		}

	}
