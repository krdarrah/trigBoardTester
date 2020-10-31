import java.io.*;
import java.io.File;
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
void setup() {
  iceland = createFont("Iceland-Regular.ttf", 32);
  textFont(iceland);

  size(600, 100);
  println("setting up serial");

  getFiles();
  trigBoardPort = serialSelection("trigBoard Port");
  relayPort = serialSelection("relay Port");
  gatewayPort = serialSelection("gateway Port");
  relaySerialPort = new Serial(this, relayPort, 115200);
  gatewaySerialPort = new Serial(this, gatewayPort, 115200);
}




void draw() {
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
      if (int((millis()-timerCheckStart)/1000) % 2 ==0) {
        background(0, 255, 0);
      }



      text("FLASH SUCCESS - PRESS RESET BUTTON " + (30-int((millis()-timerCheckStart)/1000))+"seconds", 10, 30);
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
      if (int((millis()-timerCheckStart)/1000) % 2 ==0) {
        background(255, 0, 0);
        fill(255);
      }

      text("PRESS AND HOLD WAKE BUTTON " + (30-int((millis()-timerCheckStart)/1000))+"seconds left", 10, 30);


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
