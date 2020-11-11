boolean mouseOverRect(int x, int y, int w, int h) {
  return (mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h);
}

void mousePressed() {

  if (mouseOverRect(5, 65, 85, 30)) {//restart button
    currentState=0;
    failure=false;
  }

//next button
  if (mouseOverRect(width-5-60, 65, 60, 30) && (currentState==0 ||currentState==9 || currentState==10)) {
    if (currentState==0 || currentState==9)
      currentState++;
    else if (currentState==10) {
      currentState=11;
      timerCheckStart=millis();
      totalTestTimeSeconds = int((millis()-testStartTime)/1000);
    }
    if (currentState==1) {
      testStartTime = millis();
    }
  }
}
