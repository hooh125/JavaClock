
import java.io.*;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.*;
import com.pi4j.io.gpio.event.*;
import org.json.JSONException;


public class Main {

	private static final int		UPDATE_TIME = 0;
	private static final int 		CHANGE_BACKLIGHT = 1;
	private static final int 		POMODORO = 2;
	private static final int 		SLEEP_TIME = 30*60*1000;
	private static final int 		BAUD_RATE = 9600;
	private static final String SERIAL_PORT = "/dev/ttyACM0";
	private static final String API_KEY = "cd09d3ceae1378f89e6df28af53aedb9";
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
		initGpio();
		clock = new Clock();
		weather = new Weather(API_KEY);
		//clock.start();
		while(true) {
		error = false;
			try {
				System.out.println("Actualizando...");
				clock.updateTime(TIMEZONE);
				weather.updateWeather(CITY, COUNTRY);
				sendToArduino(UPDATE_TIME);
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException|JSONException|IOException ex) {
				error = true;
				sendToArduino(UPDATE_TIME);
				//ex.printStackTrace();
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

	private static void initGpio() {
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalInput buttonLeft = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
		final GpioPinDigitalInput buttonMiddle = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
		final GpioPinDigitalInput buttonRight = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
		GpioPinListenerDigital buttonListener = new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					if(event.getState() == PinState.LOW) {
						String boton = "";
						if(event.getPin().getPin() == RaspiPin.GPIO_00) {
							boton = "Actualizando...";
							try {
								error = false;
								clock.updateTime(TIMEZONE);
								weather.updateWeather(CITY, COUNTRY);
							} catch (IOException ex) {
								error = true;
							}
							sendToArduino(UPDATE_TIME);
						} else if(event.getPin().getPin() == RaspiPin.GPIO_01) {
							boton = "Iniciando/Parando pomodoro...";
							sendToArduino(POMODORO);
						} else if(event.getPin().getPin() == RaspiPin.GPIO_02) {
							boton = "Cambiando el estado del backlight...";
							sendToArduino(CHANGE_BACKLIGHT);
						}
						System.out.println("Se ha pulsado: " + boton);
					}
			}
		};
		buttonLeft.addListener(buttonListener);
		buttonMiddle.addListener(buttonListener);
		buttonRight.addListener(buttonListener);
	}

	private static void sendToArduino(int operation) {
		if(serial.isOpen()) {
			try {
				String data = clock.getTime() + ":" + (error ? 1 : 0) + ":" + weather.getWeather() + ":" + weather.getLocalWeather() + ":"  + operation + "/";
				System.out.println("Enviando -> " + data);
				serial.write(data);
			} catch (IllegalStateException ex) {
				System.out.println(ex.toString());
				ex.printStackTrace();
			}
		}
	}
}
