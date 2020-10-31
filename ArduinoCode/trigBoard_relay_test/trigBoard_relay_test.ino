//runs on ATMEGA328P "Mini"

void setup() {
  Serial.begin(115200);
  // put your setup code here, to run once:
  pinMode(2, OUTPUT);

}

void loop() {
  // put your main code here, to run repeatedly:

  if (Serial.available()) {
    if (Serial.read() == 'O') {
      digitalWrite(2, LOW);
    }
    else{
      digitalWrite(2, HIGH);
    }
  }
  }
