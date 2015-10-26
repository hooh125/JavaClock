
import java.io.*;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import org.json.JSONException;


public class Main {

	private static final int 		SLEEP_TIME = 30*60*1000;
	private static final int 		BAUD_RATE = 9600;
	private static final String SERIAL_PORT = "/dev/ttyACM0";
	private static final String API_KEY = "bd82977b86bf27fb59a04b61b657fb6f";
	private static final String TIMEZONE = "GMT+1";
	private static final String CITY = "Madrid";
	private static final String COUNTRY = "es";
	private static Serial serial;
	private static Clock clock;
	private static Weather weather;
	private static boolean error = false;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Iniciando aplicacion...");
		initSerial();
		clock = new Clock();
		weather = new Weather(API_KEY);
		//clock.start();
		while(true) {
			error = false;
			try {
				clock.updateTime(TIMEZONE);
				weather.updateWeather(CITY, COUNTRY);
				sendToArduino();
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException|JSONException|IOException ex) {
				error = true;
				sendToArduino();
				ex.printStackTrace();
			}
		}
	}

	private static void initSerial() {
		serial = SerialFactory.createInstance();
		serial.addListener(new SerialDataListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				System.out.println(event.getData());
			}
		});

		try {
			serial.open(SERIAL_PORT, BAUD_RATE);
			Thread.sleep(3000);
 		} catch (SerialPortException|InterruptedException ex) {
			System.out.println("Serial error -> " + ex.getMessage());
			return;
		}
	}

	private static void sendToArduino() {
		if(serial.isOpen()) {
			try {
				String data = clock.getTime() + ":" + (error ? 1 : 0) + ":" + weather.getWeather() + ":" + weather.getLocalWeather() + "-";
				System.out.println("Enviando -> " + data);
				serial.write(data);
			} catch (IllegalStateException ex) {
				ex.printStackTrace();
			}
		}
	}
}
