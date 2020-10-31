//runs on ESP32 Adafruit Huzzah Board

#include "AsyncUDP.h"
#include <WiFi.h>
#include <WiFiClient.h>
AsyncUDP udp;
//WiFiUDP udp;
WiFiServer server(80);

unsigned long lastTimeStamp = 0;
unsigned long totalCount = 0;
void setup() {
  Serial.begin(115200);
  Serial.println("server");
  pinMode(13, OUTPUT);
  Serial.println(WiFi.softAP("your_udp_SSID", "your_udp_PW", 1, 1, 8)); //ssid,pw,ch,hid,conn
  IPAddress myIP = WiFi.softAPIP();
  Serial.print("AP IP address: ");
  Serial.println(myIP);
  server.begin();

  if (udp.listen(1234)) {
    //    Serial.print("UDP Listening on IP: ");
    //    Serial.println(WiFi.localIP());
    udp.onPacket([](AsyncUDPPacket packet) {
      digitalWrite(13, !digitalRead(13));
      //      Serial.print("UDP Packet Type: ");
      //      Serial.print(packet.isBroadcast() ? "Broadcast" : packet.isMulticast() ? "Multicast" : "Unicast");
      //      Serial.print(", From: ");
      //      Serial.print(packet.remoteIP());
      //      Serial.print(":");
      //      Serial.print(packet.remotePort());
      //      Serial.print(", To: ");
      //      Serial.print(packet.localIP());
      //      Serial.print(":");
      //      Serial.print(packet.localPort());
      //      Serial.print(", Length: ");
      //      Serial.print(packet.length());
      //      Serial.print(", Data: ");

      unsigned long timeSinceLast = int((millis() - lastTimeStamp));
      if (timeSinceLast > 200) {
//        totalCount++;
//        Serial.print(totalCount);
//        Serial.print("  ");
        Serial.write(packet.data(), packet.length());
        Serial.println("");
//        Serial.print(timeSinceLast / 1000);
//        Serial.print("sec ");
//        if (timeSinceLast > 20000)
//          Serial.print("ERROR");
//
//        Serial.println();
      }
      lastTimeStamp = millis();
    });
  }
}
void loop() {


}
