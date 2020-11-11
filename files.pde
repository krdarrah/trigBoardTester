void getFiles() {
  //this will get the folder with all of the files, then is automatically parsed
  selectFolder("Folder with Files", "folderSelection");
  while (BINfileName.equals(" ")) {
    println("waiting...");
    delay(1000);
  }
}


// THIS is teh function to get the folder and find the files containing the various titles:
File folder;
String [] filenames;
void folderSelection(File selection) {
  java.io.File folder = new java.io.File(dataPath(selection.getAbsolutePath()));

  filenames = folder.list();
  println(filenames.length + ".properties.ser");

  for (int i = 0; i <= filenames.length; i++)
  {
    println(filenames[i]);

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

//OLD way just for reference
//void binfileSelection(File selection) {
//  if (selection == null) {
//    println("Window was closed or the user hit cancel.");
//  } else {
//    println("User selected " + selection.getAbsolutePath());
//    BINfileName = selection.getAbsolutePath();
//  }
//}

//void partitionfileSelection(File selection) {
//  if (selection == null) {
//    println("Window was closed or the user hit cancel.");
//  } else {
//    println("User selected " + selection.getAbsolutePath());
//    partitionsfileName = selection.getAbsolutePath();
//  }
//}
