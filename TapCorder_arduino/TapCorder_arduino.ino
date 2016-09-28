#include <SoftwareSerial.h>

SoftwareSerial BTSerial(4, 7); //Connect HC-06. Use your (TX, RX) settings

int  recordTime = 10000;     // default 녹음 시간 12초
long previousMillis = 0;     // 상태가 변경된 마지막 시간을 저장하는 변수. 밀리 초 저장 위한 long 타입
long currentMillis  = 9000;     // 현재 시각을 밀리 초 단위로 저장하는 변수

void setup() {
  
  Serial.begin(9600);
  BTSerial.begin(9600);  
  
  Serial.println("Start");
  pinMode(12,INPUT_PULLUP);                                                             
}

void loop() {
  
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
    
      // 블루투스 송신 값 쓰기
      BTSerial.write("R");
    
  }
}
