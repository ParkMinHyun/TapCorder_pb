#include <SoftwareSerial.h>

SoftwareSerial BTSerial(4, 7); //Connect HC-06. Use your (TX, RX) settings

void setup() {
  
  Serial.begin(9600);
  BTSerial.begin(9600);  
  
  Serial.println("Start");
  pinMode(12,INPUT_PULLUP);                                                             
}

void loop() {
  //int button = digitalRead(12);
  //Serial.println(button);
  
  if (BTSerial.available()) {
    Serial.write(BTSerial.read());
    
  }
  if (Serial.available()) {
    BTSerial.write(Serial.read());
  }
}
