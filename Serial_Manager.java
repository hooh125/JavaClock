import java.io.*;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class Serial_Manager {

  private static Serial_Manager instance = null;
  private Weather weather;
  private Clock clock;
  private Serial serial;

  protected Serial_Manager() {
    serial = SerialFactory.createInstance();
    weather = Weather.getInstance();
    clock = Clock.getInstance();
  }

  public static Serial_Manager getInstance() {
    if(instance == null) {
      instance = new Serial_Manager();
    }
    return instance;
  }

  public void init() {
		serial.addListener(new SerialDataListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				System.out.println(event.getData());
			}
		});

		try {
			serial.open(Main.SERIAL_PORT, Main.BAUD_RATE);
			Thread.sleep(3000);
 		} catch (SerialPortException|InterruptedException ex) {
			System.out.println("Serial error -> " + ex.getMessage());
			return;
		}
	}

  public Serial getSerial() {
    return serial;
  }

  public void sendToArduino(int operation, boolean error) {
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
