void erase() throws IOException {
  try {
    //this whole string here copied straight out of the Arduino IDE for a USB upload
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
      if (s.contains("Serial port")) {////toggle the relay to wake up trigBoard
        relaySerialPort.write("C");
        delay(500);
        relaySerialPort.write("O");
      }

      if (s.equals("Hard resetting via RTS pin...")) {//always get this after all goes well
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


void flash() throws IOException {
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
