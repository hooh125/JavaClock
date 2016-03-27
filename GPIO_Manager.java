import java.io.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.*;
import com.pi4j.io.gpio.event.*;

public class GPIO_Manager {

private static GPIO_Manager instance = null;
private GpioController controller;
public static boolean error = false;

protected GPIO_Manager() {
  controller = GpioFactory.getInstance();
}

public static GPIO_Manager getInstance() {
  if(instance == null) {
    instance = new GPIO_Manager();
  }
  return instance;
}

public void init() {
  final GpioPinDigitalInput buttonLeft = controller.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
  final GpioPinDigitalInput buttonMiddle = controller.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
  final GpioPinDigitalInput buttonRight = controller.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
  GpioPinListenerDigital buttonListener = new GpioPinListenerDigital() {
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
      Clock clock = Clock.getInstance();
      Weather weather = Weather.getInstance();
      Serial_Manager serial_manager = Serial_Manager.getInstance();
      if(event.getState() == PinState.LOW) {
        String boton = "";
        if(event.getPin().getPin() == RaspiPin.GPIO_00) {
          boton = "Actualizando...";
          try {
            error = false;
            clock.updateTime(Main.TIMEZONE);
            weather.updateWeather(Main.CITY, Main.COUNTRY);
          } catch (IOException ex) {
            error = true;
          }
          serial_manager.sendToArduino(Main.UPDATE_TIME, error);
        } else if(event.getPin().getPin() == RaspiPin.GPIO_01) {
          boton = "Iniciando/Parando pomodoro...";
          serial_manager.sendToArduino(Main.POMODORO, error);
          clock.runChrono();
        } else if(event.getPin().getPin() == RaspiPin.GPIO_02) {
          boton = "Cambiando el estado del backlight...";
          serial_manager.sendToArduino(Main.CHANGE_BACKLIGHT, error);
        }
        System.out.println("Se ha pulsado: " + boton);
      }
    }
  };
  buttonLeft.addListener(buttonListener);
  buttonMiddle.addListener(buttonListener);
  buttonRight.addListener(buttonListener);
}

}
