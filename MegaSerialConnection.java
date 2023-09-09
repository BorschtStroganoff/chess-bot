import com.fazecast.jSerialComm.SerialPort;


/**
* This class does the Serial communication between the Laptop and the Arduino MEGA
* The Arduino MEGA is the controller that records the physical board position and any changes that are made
* So, this class is used when the player's move is recorded
*/
public class MegaSerialConnection {

   // returns the three arrays that comes from the arduino mega
   // those three arrays are the board positions
   public byte[][] serialRead()  {
       // open the serial port
       SerialPort port = SerialPort.getCommPort("COM11"); // make sure this is the correct port name before executing
       port.openPort();
       port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

//        // Flushes buffer so that old bytes don't mess with new bytes
//        port.flushIOBuffers();

       //this delay ensures that opening the port doesn't affect the Serial communication
       // 2 seconds is about how long the time between opening the port and reading/writing data has to be in order to work
       try {
           Thread.sleep(2000);
       } catch (Exception e) {}

       //waits for bytes to be available
       //this is finished when the player presses the button
       while (port.bytesAvailable() <=0) {
           try {
               Thread.sleep(10);
           } catch (Exception e) {}
       }

       // read the three arrays
       byte[] startPos = readBytes(port, 64);
       byte[] endPos = readBytes(port, 64);
       byte[] changedSquares = readBytes(port, 64);

       // print the arrays
       System.out.println("Start Position Array:");
       printBytes(startPos);
       System.out.println("End Position Array");
       printBytes(endPos);
       System.out.println("Changed Squares Array");
       printBytes(changedSquares);

       byte[][] arrays = new byte[3][64];
       for (int i=0; i<64; i++) {
           arrays[0][i] = startPos[i];
           arrays[1][i] = endPos[i];
           arrays[2][i] = changedSquares[i];
       }

       // close the serial port
       port.closePort();

       return arrays;
   }

   // this method was written with help from ChatGPT
   // reads bytes coming from the arduinoMega
   private static byte[] readBytes(SerialPort port, int numBytes) {
       byte[] buffer = new byte[numBytes];
       int bytesRead = 0;

       while (bytesRead < numBytes) {
           int numRead = port.readBytes(buffer, numBytes - bytesRead);
           if (numRead == -1) {
               throw new RuntimeException("Error reading from serial port");
           }
           bytesRead += numRead;
       }

       return buffer;
   }

   // this is a method used in testing
   // prints the arrays recorded from the Arduino MEGA
   private static void printBytes(byte[] arr) {
       for (int i = 0; i < arr.length; i++) {
           System.out.print(arr[i] + " ");
       }
       System.out.println();

       System.out.println("--------");
       System.out.println();

       int i,j;
       for (i=7; i>=0; i--) {
           for (j=i*8; j< i*8+8; j++)
               System.out.print(arr[j]);
           System.out.println();
       }


       System.out.println();
       System.out.println("--------");
   }

}
