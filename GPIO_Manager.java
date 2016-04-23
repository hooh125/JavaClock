import java.io.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.*;
import com.pi4j.io.gpio.event.*;

public class GPIO_Manager {

private static GPIO_Manager instance = null;
private GpioPinDigitalOutput beeper;
private GpioController controller;
private boolean pomodoro = false;
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
  beeper = controller.provisionDigitalOutputPin(RaspiPin.GPIO_03, "beeper" ,PinState.LOW);
  final GpioPinDigitalInput buttonLeft = controller.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_DOWN);
  final GpioPinDigitalInput buttonMiddle = controller.provisionDigitalInputPin(RaspiPin.GPIO_06, PinPullResistance.PULL_DOWN);
  final GpioPinDigitalInput buttonRight = controller.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
  beeper.setShutdownOptions(true, PinState.LOW);

  GpioPinListenerDigital buttonListener = new GpioPinListenerDigital() {
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
      Clock clock = Clock.getInstance();
      Weather weather = Weather.getInstance();
      Serial_Manager serial_manager = Serial_Manager.getInstance();
      if(event.getState() == PinState.LOW) {
        String boton = "";
        if(event.getPin().getPin() == RaspiPin.GPIO_05) {
          boton = "Actualizando...";
          try {
            error = false;
            clock.updateTime(Main.TIMEZONE);
            weather.updateWeather(Main.CITY, Main.COUNTRY);
          } catch (IOException ex) {
            error = true;
          }
          serial_manager.sendToArduino(Main.UPDATE_TIME, error);
        } else if(event.getPin().getPin() == RaspiPin.GPIO_06) {
          boton = "Iniciando/Parando pomodoro...";
          serial_manager.sendToArduino(Main.POMODORO, error);
          if(!pomodoro) {
            pomodoro = true;
            clock.runChrono();
          } else {
            pomodoro = false;
            clock.stopChrono();
          }
        } else if(event.getPin().getPin() == RaspiPin.GPIO_04) {
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

public void beep() {
  try {
    Double period = 1.0 / 800;
    int delay = new Double((period / 2) * 1000).intValue();
    int cycles = new Double(Main.BEEP_DURATION * 400).intValue();
    for(int j = 0; j<3; j++) {
      for(int i = 0; i<cycles; i++) {
        beeper.high();
        Thread.sleep(delay);
        beeper.low();
        Thread.sleep(delay);
      }
      Thread.sleep(500);
    }
  } catch (Exception ex) {
    System.out.println("Error en el beeper -> " + ex.toString());
  }

}

}
