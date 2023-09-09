import com.fazecast.jSerialComm.SerialPort;

/**
* This class deals with Serial Communication with the Arduino UNO
* Whenever a command needs to be sent to the arm, it is first sent to the Arduino UNO
* This class is used to send that command to the arduino
*/

public class UnoSerialConnection {

   private SerialPort port;

   public UnoSerialConnection() {
       port = SerialPort.getCommPort("COM3"); // make sure this is the port used by the UNO
   }

   // this method has written with help from Caleb Hay, a software engineer who used to go to my high school
   // This loop waits for the Arduino UNO to read the command for what move to make
   public void serialLoop(byte[] command) {

       while(true) {

           System.out.println("Sending to Arduino...");
           int bytesWritten = port.writeBytes(command, 8);
           port.flushIOBuffers();

           if (bytesWritten > 0) {
               break;
           }

           try { java.lang.Thread.sleep(200); }
           catch(Exception e) {}

       }
   }

   // this method reads a byte from the Arduino UNO
   // This is used to confirm that the arm finished its move, and that the board is ready to read the next move
   public byte[] serialRead() {
       byte[] buffer = {0};

       while (true) {

           //System.out.println("Looking for bytes to read...");

           if (this.port.bytesAvailable() > 0) {
               port.readBytes(buffer,1);
           }

           if (buffer[0] != 0) {
               break;
           }

           try { java.lang.Thread.sleep(200); }
           catch(Exception e) {}
       }
       System.out.println("serialRead from arduino returned: " + buffer[0]);
       return buffer;
   }

   public void openPort() {
       if(!port.openPort()) {
           System.out.println("Could not open Arduino UNO port");
           return;
       } else {
           System.out.println("Opened Arduino UNO port");
       }
   }

   public boolean closePort() {
       return port.closePort();
   }
}
