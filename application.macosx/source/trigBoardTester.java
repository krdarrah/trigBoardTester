import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.*; 
import java.io.File; 
import processing.serial.*; 
import static javax.swing.JOptionPane.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class trigBoardTester extends PApplet {



//import interfascia.*;



//IFButton startOverButton, nextButton;
//GUIController GUIcont;
String trigBoardPort = "";
String relayPort = "";
String gatewayPort = "";

//these are the 5 files needed to flash
String BINfileName = " ";
String partitionsfileName = " ";
String espToolName = " ";
String bootAppBinName = " ";
String bootLoaderBinName = " ";

boolean eraseSuccess = false;
boolean flashSuccess = false;
int currentState = 0;
boolean failure = false;
String failureMode = "";
//IFLookAndFeel  redLook, greenLook;
PFont iceland;
long timerCheckStart;
int timerState = 0;
int contactState = 0;
long testStartTime;
int totalTestTimeSeconds;
public void setup() {
  iceland = createFont("Iceland-Regular.ttf", 32);
  textFont(iceland);

  
  println("setting up serial");

  getFiles();
  trigBoardPort = serialSelection("trigBoard Port");
  relayPort = serialSelection("relay Port");
  gatewayPort = serialSelection("gateway Port");
  relaySerialPort = new Serial(this, relayPort, 115200);
  gatewaySerialPort = new Serial(this, gatewayPort, 115200);
}




public void draw() {
  if (!failure) {
    if (currentState==0) {//starting point
      background(255);
      fill(0);
      textSize(25);
      text("NEW BOARD-PRESS RESET AND CHECK CURRENT\nCLOSE TO 0.000?", 10, 30);
    }
    if (currentState==1) {//flashing firmware message
      background(255);
      fill(0);
      textSize(25);
      text("FLASHING FIRMWARE PLEASE WAIT", 10, 30);
      currentState=2;
    } else if (currentState==2) {//where we actually flash
      try {
        erase();
      }
      catch(IOException ex) {
      }
      if (eraseSuccess==false) {
        failureMode = "ERASE FAILED";
        failure=true;
        println("SOMETHING WENT WRONG");
      }
      if (eraseSuccess) {
        try {
          flash();
        }
        catch(IOException ex) {
        }
        if (flashSuccess==false) {
          failureMode = "FLASH FAILED";
          failure=true;
          println("SOMETHING WENT WRONG");
        } else {
          timerCheckStart=millis();
        }
      }
    }
    if (currentState==3) {//flash was a success, waiting for reset
      background(255);
      fill(0);
      textSize(25);

      if (millis()-timerCheckStart >30000) {
        failureMode = "TIMER FAIL";
        failure=true;
      }
      if (PApplet.parseInt((millis()-timerCheckStart)/1000) % 2 ==0) {
        background(0, 255, 0);
      }



      text("FLASH SUCCESS - PRESS RESET BUTTON " + (30-PApplet.parseInt((millis()-timerCheckStart)/1000))+"seconds", 10, 30);
      if (gatewaySerialPort.available()>0) {
        serialGet();
      }
    }
    if (currentState==4) {// TIMER CHECKS

      fill(0);
      textSize(25);

      if (millis()-timerCheckStart >20000) {
        failureMode = "TIMER FAIL";
        failure=true;
      }

      if (millis()-timerCheckStart <2000) {//just flash this up for 2 seconds, since it checked battery voltage in order to get here
        background(0, 255, 0);
        text("BATTERY SUCCESS", 10, 30);
      } else {
        background(255);
        text("TESTING TIMER", 10, 30);
      }


      if (gatewaySerialPort.available()>0) {
        serialGet();
      }
    }
    if (currentState==5) {// TIMER SUCCESS
      background(0, 255, 0);
      fill(0);
      textSize(25);
      text("TIMER SUCCESS", 10, 30);
      if (millis()-timerCheckStart >2000) {
        currentState++;
        timerCheckStart=millis();
        contactState=0;
      }
    }
    if (currentState==6) {// contact state
      background(255);
      fill(0);
      textSize(25);
      if (millis()-timerCheckStart >20000) {
        failureMode = "CONTACT FAIL";
        failure=true;
      }

      if (contactState==0) {
        contactState=1;
        relaySerialPort.write("C");
      }
      if (contactState==2 && millis()-timerCheckStart >2000) {
        relaySerialPort.write("O");//open back up
        contactState=3;
      }


      text("TESTING CONTACTS", 10, 30);
      if (gatewaySerialPort.available()>0) {
        serialGet();
      }
    }
    if (currentState==7) {// CONTACT SUCCESS
      background(0, 255, 0);
      fill(0);
      textSize(25);
      text("CONTACT SUCCESS", 10, 30);
      if (millis()-timerCheckStart >2000) {
        currentState++;
        timerCheckStart=millis();
      }
    }
    if (currentState==8) {// wake button
      background(255);
      fill(0);
      textSize(25);
      if (millis()-timerCheckStart >30000) {
        failureMode = "WAKE FAIL";
        failure=true;
      }
      if (PApplet.parseInt((millis()-timerCheckStart)/1000) % 2 ==0) {
        background(255, 0, 0);
        fill(255);
      }

      text("PRESS AND HOLD WAKE BUTTON " + (30-PApplet.parseInt((millis()-timerCheckStart)/1000))+"seconds left", 10, 30);


      if (gatewaySerialPort.available()>0) {
        serialGet();
      }
    }

    if (currentState==9) {// WAKE SUCCESS
      background(0, 255, 0);
      fill(0);
      textSize(25);
      text("WAKE SUCCESS - NOW SETUP CONFIGURATOR", 10, 30);
    }
    if (currentState==10) {// METER CHECK
      background(255);
      fill(0);
      textSize(25);
      text("Check Meter for 0.000?", 10, 30);
    }

    if (currentState==11) {// ALL DONE
      background(0, 255, 0);
      fill(0);
      textSize(25);

      text(totalTestTimeSeconds + "seconds total - GREAT JOB! YOU EARNED $$$", 10, 30);
      if (millis()-timerCheckStart >5000) {
        currentState=0;
      }
    }
  } else {
    background(255, 0, 0);
    fill(255);
    textSize(20);
    text(failureMode, 10, 30);
  }


  //draw two buttons at the end here
  stroke(255);//whiteoutline
  fill(158, 10, 10);//dark red
  rect(5, 65, 85, 30);//restart button
  fill(255);//white text
  textSize(20);
  text("RESTART", 12, 85);

  //only show if current state allows
  if (currentState==0 ||currentState==9 || currentState==10) {
    fill(77, 158, 106);//darkgreen
    rect(width-5-60, 65, 60, 30);//next button
    fill(255);//white text
    textSize(20);
    text("NEXT", width-5-60+10, 85);
  }
}
public void getFiles() {
  selectFolder("Folder with Files", "folderSelection");
  while (BINfileName.equals(" ")) {
    println("waiting...");
    delay(1000);
  }




  //  selectInput("BIN FILE:", "binfileSelection");
  //  println("waiting for selection");
  //  while (BINfileName.equals(" ")) {
  //    println("waiting...");
  //    delay(1000);
  //  }
  //  println("getting partition next");
  //  selectInput("Partition FILE:", "partitionfileSelection");
  //  println("waiting for selection");
  //  while (partitionsfileName.equals(" ")) {
  //    println("waiting...");
  //    delay(1000);
  //  }
}

File folder;
String [] filenames;

public void folderSelection(File selection) {
  java.io.File folder = new java.io.File(dataPath(selection.getAbsolutePath()));

  filenames = folder.list();
  println(filenames.length + ".properties.ser");

  for (int i = 0; i <= filenames.length; i++)
  {
    println(filenames[i]);
    //    String BINfileName = " ";
    //String partitionsfileName = " ";
    //String espToolName = " ";
    //String bootAppBinName = " ";
    //String bootLoaderBinName = " ";

    if (filenames[i].contains("boot_app0")) {
      bootAppBinName = selection.getAbsolutePath() + "/" + filenames[i];
    }
    if (filenames[i].contains("bootloader")) {
      bootLoaderBinName = selection.getAbsolutePath() + "/" + filenames[i];
    }
    if (filenames[i].contains("esptool")) {
      espToolName = selection.getAbsolutePath() + "/" + filenames[i];
    }
    if (filenames[i].contains("partitions")) {
      partitionsfileName = selection.getAbsolutePath() + "/" + filenames[i];
    }
    if (filenames[i].contains("THEBIN")) {
      BINfileName = selection.getAbsolutePath() + "/" + filenames[i];
    }
  }
}


public void binfileSelection(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    BINfileName = selection.getAbsolutePath();
  }
}

public void partitionfileSelection(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());
    partitionsfileName = selection.getAbsolutePath();
  }
}
public void erase() throws IOException {
  try {

    Process proc = Runtime.getRuntime().exec(espToolName + " --chip esp32 --port " + trigBoardPort + " --baud 921600 --before default_reset --after hard_reset erase_flash");
    relaySerialPort.write("O");
    BufferedReader stdInput = new BufferedReader(new 
      InputStreamReader(proc.getInputStream()));
      
    // Read the output from the command
    System.out.println("Here is the standard output of the command:\n");
    String s = null;
    eraseSuccess = false;
    while ((s = stdInput.readLine()) != null) {
      println(s);
      if (s.contains("Serial port")) {
        relaySerialPort.write("C");
        delay(500);
        relaySerialPort.write("O");
      }

      if (s.equals("Hard resetting via RTS pin...")) {
        eraseSuccess=true;
        println("erase complete - press the reset button");
        relaySerialPort.write("O");
      }
    }
  }

  catch(IOException ex) {
    ex.printStackTrace();
  }
}


public void flash() throws IOException {
  try {

    Process proc = Runtime.getRuntime().exec(espToolName + " --chip esp32 --port " + trigBoardPort + " --baud 921600 --before default_reset --after hard_reset write_flash -z --flash_mode dio --flash_freq 80m --flash_size detect 0xe000 " + bootAppBinName + " 0x1000 " + bootLoaderBinName + " 0x10000 " + BINfileName + " 0x8000 " + partitionsfileName);
    relaySerialPort.write("O");
    BufferedReader stdInput = new BufferedReader(new 
      InputStreamReader(proc.getInputStream()));


    // Read the output from the command
    System.out.println("Here is the standard output of the command:\n");
    String s = null;
    flashSuccess = false;
    while ((s = stdInput.readLine()) != null) {
      println(s);
      if (s.contains("Serial port")) {
        relaySerialPort.write("C");
        delay(500);
        relaySerialPort.write("O");
      }

      if (s.equals("Hard resetting via RTS pin...")) {
        flashSuccess=true;
        println("flashing complete - press the reset button");
        relaySerialPort.write("O");
        currentState++;
        gatewaySerialPort.clear();
        timerCheckStart=millis();
        timerState = 0;
      }
    }
  }

  catch(IOException ex) {
    ex.printStackTrace();
  }
}
public boolean mouseOverRect(int x, int y, int w, int h) {
  return (mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h);
}

public void mousePressed() {

  if (mouseOverRect(5, 65, 85, 30)) {//restart button
    currentState=0;
    failure=false;
  }

  if (mouseOverRect(width-5-60, 65, 60, 30) && (currentState==0 ||currentState==9 || currentState==10)) {
    if (currentState==0 || currentState==9)
      currentState++;
    else if (currentState==10) {
      currentState=11;
      timerCheckStart=millis();
      totalTestTimeSeconds = PApplet.parseInt((millis()-testStartTime)/1000);
    }
    if (currentState==1) {
      testStartTime = millis();
    }
    //if(currentState==4){//enters timer check, so reset things
    //gatewaySerialPort.clear();
    //timerCheckStart=millis();
    //timerState = 0;
    //}
  }
}

//needed by the message box for selecting port
String SerialString = null;      // Data received from the serial port
Serial relaySerialPort; 
Serial gatewaySerialPort; 
final boolean debug = true;

//void serialEvent(Serial myPort) {
public void serialGet() {

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
      float batteryVoltage = PApplet.parseFloat(splitSerialString[1]);

      if (batteryVoltage>2.5f && batteryVoltage<3.5f) {
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

public String serialSelection(String nameofPort) {
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
          COMlist += PApplet.parseChar(j+'a') + " = " + Serial.list()[j];
          if (++j < i) COMlist += "\n";
        }
        COMx = showInputDialog(nameofPort + " (a,b,..):\n"+COMlist);
        if (COMx == null) exit();
        if (COMx.isEmpty()) exit();
        i = PApplet.parseInt(COMx.toLowerCase().charAt(0) - 'a') + 1;
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
  public void settings() {  size(600, 100); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "trigBoardTester" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
