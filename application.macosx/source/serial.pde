import processing.serial.*;
import static javax.swing.JOptionPane.*;//needed by the message box for selecting port
String SerialString = null;      // Data received from the serial port
Serial relaySerialPort; 
Serial gatewaySerialPort; 
final boolean debug = true;

//void serialEvent(Serial myPort) {
void serialGet() {

  SerialString = gatewaySerialPort.readStringUntil(0x0D);
  //trigBoard Name Contact Still Open
  //trigBoard Name Contact has Closed
  //trigBoard Name Button Was Pressed
  println(SerialString);
  if (SerialString != null) {
    if (SerialString.contains("trigBoard Name Contact Still Open") && timerState==0 && currentState==3) {//first one to catch after flash
      println("STILL OPEN TIMER PASS");
      //String[] splitStrings = new String[3];
      String[] splitSerialString = split(SerialString, ',');

      //need to get rid of space and V at end
      splitSerialString[1] = splitSerialString[1].substring(1);
      splitSerialString[1] = splitSerialString[1].substring(0, splitSerialString[1].length()-2);
      println(splitSerialString[1]);//should be the voltage
      float batteryVoltage = float(splitSerialString[1]);

      if (batteryVoltage>2.5 && batteryVoltage<3.5) {
        println("BATTERY PASS");
        relaySerialPort.write("C");//this will trigger a has closed, but we will ignore
        timerState=1;
        currentState++;
        timerCheckStart=millis();
      } else {
        failureMode = "BATTERY FAIL";
        failure=true;
      }
    }
    if (SerialString.contains("trigBoard Name Contact Still Closed") && timerState==1 && currentState==4) {
      println(millis()-timerCheckStart);
      if (millis()-timerCheckStart>4000 && millis()-timerCheckStart<10000) {
        println("STILL CLOSED TIMER PASS");
        timerCheckStart=millis();
        relaySerialPort.write("O");//this will trigger a has closed, but we will ignore
        timerCheckStart=millis();
        timerState=2;
        currentState++;
      } else {
        failureMode = "TIMER OUT OF TOL";
        failure=true;
      }
    }   
    if (SerialString.contains("trigBoard Name Contact has Closed") && contactState==1 && currentState==6) {
      println("CONTACT CLOSE PASS");
      timerCheckStart=millis();
      contactState=2;
    }
    if (SerialString.contains("trigBoard Name Contact has Opened") && contactState==3 && currentState==6) {
      println("CONTACT OPEN PASS");
      timerCheckStart=millis();
      contactState=4;
      currentState++;
    }

    if (SerialString.contains("trigBoard Name Button Was Pressed") && currentState==8) {
      println("WAKE BUTTON PASS");
      timerCheckStart=millis();
      currentState++;
    }

    gatewaySerialPort.clear();
  }
}

String serialSelection(String nameofPort) {
  String COMx, COMlist = "";
  /*
  Other setup code goes here - I put this at
   the end because of the try/catch structure.
   */
  try {
    //if(debug) printArray(Serial.list());
    int i = Serial.list().length;
    if (i != 0) {
      if (i >= 2) {
        // need to check which port the inst uses -
        // for now we'll just let the user decide
        for (int j = 0; j < i; ) {
          COMlist += char(j+'a') + " = " + Serial.list()[j];
          if (++j < i) COMlist += "\n";
        }
        COMx = showInputDialog(nameofPort + " (a,b,..):\n"+COMlist);
        if (COMx == null) exit();
        if (COMx.isEmpty()) exit();
        i = int(COMx.toLowerCase().charAt(0) - 'a') + 1;
      }
      return Serial.list()[i-1];
      //trigBoardPort = ;

      //myPort = new Serial(this, trigBoardPort, 19200); // change baud rate to your liking
      //myPort.bufferUntil(0x0A); // buffer until CR/LF appears, but not required..
    } else {
      showMessageDialog(frame, "Device is not connected to the PC");
      exit();
    }
  }
  catch (Exception e)
  { //Print the type of error
    showMessageDialog(frame, "COM port is not available (may\nbe in use by another program)");
    //println("Error:", e);
    exit();
  }
  return "";
}
