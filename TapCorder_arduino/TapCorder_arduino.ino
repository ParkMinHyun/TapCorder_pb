#include <SoftwareSerial.h>

SoftwareSerial BTSerial(4, 7); //Connect HC-06. Use your (TX, RX) settings

int  recordTime = 10000;     // default 녹음 시간 12초
long previousMillis = 0;     // 상태가 변경된 마지막 시간을 저장하는 변수. 밀리 초 저장 위한 long 타입
long currentMillis  = 9000;     // 현재 시각을 밀리 초 단위로 저장하는 변수
int analogValue = 0;
float voltage = 0;

void setup() {
  analogReference(DEFAULT);
  
  Serial.begin(9600);
  BTSerial.begin(9600);  

  Serial.println("Start");
  pinMode(12,INPUT_PULLUP);                                                             
}

void loop() {

  analogValue = analogRead(A0);
  voltage = 0.0048*analogValue;

  
  
  int button_press = digitalRead(12);
  currentMillis = millis();    // 현재 시각을 밀리 초 단위로 저장
  //Serial.println((currentMillis - previousMillis)/1000);
  

  // 블루투스 수신 값 받기 및 출력
  if (BTSerial.available()) {
    char serial_value =  BTSerial.read();
    int int_serial_value = (int)serial_value;

    // recordTime = int_serial_value;
    Serial.write(int_serial_value);

    
  }

  // 12초에 한번 누르기만 가능! ( 누른 후 12초안에 막 눌러도 적용 안 됨 )
  if ((currentMillis - previousMillis) > recordTime && 
       button_press == 0  ) {
    
      Serial.println("btn_pressed");
      previousMillis = currentMillis;
      //val = analogRead(7);
      //Serial.println(val);
      //val = val / 2;
    
      // 블루투스 송신 값 쓰기
      //BTSerial.write("R");
      //BTSerial.write(val);

      Serial.println(analogValue);

      if(voltage>=4.0){
        BTSerial.write("a");
        Serial.println("a");
      }
      else if(voltage<4.0 && voltage>=3.8){
        BTSerial.write("b");
        Serial.println("b");
      }
      else if(voltage<3.8 && voltage>=3.6){
        BTSerial.write("c");
        Serial.println("c");
      }
      else if(voltage<3.6 && voltage>=3.4){
        BTSerial.write("d");
        Serial.println("d");
      }
      else if(voltage<3.4 && voltage>=3.2){
        BTSerial.write("e");
        Serial.println("e");
      }
      else if(voltage<3.2 && voltage>=3.0){
        BTSerial.write("f");
        Serial.println("f");
      }
      else if(voltage<3.0 && voltage>=2.8){
        BTSerial.write("B");
        Serial.println("B");
      }
      else if(voltage<2.8 && voltage>=2.6){
        BTSerial.write("C");
        Serial.println("C");
      }
      else if(voltage<2.6 && voltage>=2.4){
        BTSerial.write("D");
        Serial.println("D");
      }
      else if(voltage<2.4 && voltage>=2.2){
        BTSerial.write("E");
        Serial.println("E");
      }
      else if(voltage<2.2 && voltage>=2.0){
        BTSerial.write("F");
        Serial.println("F");
      }
      else if(voltage<2.0 && voltage>=1.8){
        BTSerial.write("G");
        Serial.println("G");
      }
      else if(voltage<1.8 && voltage>=1.6){
        BTSerial.write("H");
        Serial.println("H");
      }
      else if(voltage<1.6 && voltage>=1.4){
        BTSerial.write("i");
        Serial.println("i");
      }
      else if(voltage<1.4 && voltage>=1.2){
        BTSerial.write("J");
        Serial.println("J");
      }
      else if(voltage<1.2 && voltage>=1.0){
        BTSerial.write("K");
        Serial.println("K");
      }
      else if(voltage<1.0 && voltage>=0.8){
        BTSerial.write("L");
        Serial.println("L");
      }
      else if(voltage<0.8 && voltage>=0.6){
        BTSerial.write("M");
        Serial.println("M");
      }
      else if(voltage<0.6 && voltage>=0.4){
        BTSerial.write("N");
        Serial.println("N");
      }
      else if(voltage<0.4 && voltage>=0.2){
        BTSerial.write("O");
        Serial.println("O");
      }
      else if(voltage<0.2 && voltage>=0){
        BTSerial.write("P");
        Serial.println("P");
      }
       
  }
}
