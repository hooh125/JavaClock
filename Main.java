import java.io.*;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import org.json.JSONException;


public class Main {

	public static final Double 	BEEP_DURATION = 0.5;
	public static final int 	BAUD_RATE = 9600;
	public static final int		UPDATE_TIME = 0;
	public static final int		CHANGE_BACKLIGHT = 1;
	public static final int		POMODORO = 2;
	public static final int		SLEEP_TIME = 30*60*1000;
	public static final String SERIAL_PORT = "/dev/ttyACM0";
	public static final String API_KEY = "cd09d3ceae1378f89e6df28af53aedb9";
	public static final String TIMEZONE = "GMT+2";
	public static final String CITY = "Madrid";
	public static final String COUNTRY = "es";

	private static Clock clock;
	private static Weather weather;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Iniciando aplicacion...");
		Serial_Manager serial_manager = Serial_Manager.getInstance();
		GPIO_Manager gpio_manager = GPIO_Manager.getInstance();
		serial_manager.init();
		gpio_manager.init();
		clock = Clock.getInstance();
		weather = Weather.getInstance();
		while(true) {
		gpio_manager.error = false;
			try {
				System.out.println("Actualizando...");
				clock.updateTime(TIMEZONE);
				weather.updateWeather(CITY, COUNTRY);
				serial_manager.sendToArduino(UPDATE_TIME, gpio_manager.error);
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException|JSONException|IOException ex) {
				gpio_manager.error = true;
				serial_manager.sendToArduino(UPDATE_TIME, gpio_manager.error);
			}
		}
	}




}
