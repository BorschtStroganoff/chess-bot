#define BUFFER_SIZE 128

//these are the values that could be sent from the java program
#define NORMAL_MOVE_COMMAND 0
#define CAPTURE_COMMAND 1
#define CASTLING_COMMAND 2
#define ENPASSANT_COMMAND 3
#define TRUE_COMMAND 1
#define FALSE_COMMAND 0

// pwm out pins to robot arm
const int YOutPin = 3;
const int XOutPin = 9;

// digital out pins to robot arm
const int isCapturePin = 12;
const int isEnPassantPin = 8;
const int isCastlingPin = 7; 
const int isPromotionPin = 12;
const int isTallPin = 8;
const int isCaptureTallPin = 7;

//digital out pin to the arm indicating that this arduino’s output signals are ready
const int arduinoReadyPin = 4;

//digital out pin to arduinoMega indicating that the arm is ready
const int arduinoMegaPin = 5;

// digital in pin
const int armReadyPin = 2;

// initialization of temporary values (will constantly change)
int outputValue = 0;

// method that maps pwm output values from 0-8 to 0-255
int maps(int value) {
  return map(value, 0, 8, 0, 255);
}

void setup() {
  Serial.begin(9600);

  //this pin reads when the robotic arm is ready
  pinMode(armReadyPin, INPUT_PULLUP);

  //this tells the arm that this arduino UNO is ready
  pinMode(arduinoReadyPin, OUTPUT);

  //this tells the arduino MEGA that the arm is finished moving
  pinMode(arduinoMegaPin, OUTPUT);

  //these are the pins that go to the robot arm I/O box
  pinMode(isCapturePin, OUTPUT);
  pinMode(isEnPassantPin, OUTPUT);
  pinMode(isCastlingPin, OUTPUT);
  pinMode(isPromotionPin, OUTPUT);
  pinMode(isTallPin, OUTPUT);
  pinMode(isCaptureTallPin, OUTPUT);
  
  //these pins output info on what squares the arm has to move to
  pinMode(XOutPin, OUTPUT);
  pinMode(YOutPin, OUTPUT); 
}

//this is the buffer that reads commands from the java program
char readBuffer[BUFFER_SIZE];


void loop() {
  
// waits for a command from the java program
  while(Serial.available() < 8) {
    delay(10);
  }
  Serial.readBytes(readBuffer, 8);

  delay(1000);

  //The information sent is translated into individual variables
  int startX = (int) readBuffer[0]; //this is the x-coordinate of the starting square
  int startY = (int) readBuffer[1]; //this is the y-coordinate of the starting square
  int endX = (int) readBuffer[2]; //this is the x-coordinate of the ending square
  int endY = (int) readBuffer[3]; //this is the y-coordinate of the ending square
  
  //This contains the special move:
  // 0 "NORMAL_MOVE_COMMAND" means the move doesn't require any crazy movements
  // 1 "CAPTURE_COMMAND" means the move is a capture
  // 2 "CASTLING_COMMAND" means the move is castling the king
  // 3 "ENPASSANT_COMMAND" means the move is an en passant capture
  int specialMove = (int) readBuffer[4]; 

  //for these three variables, the value will either be:
  // 0 "FALSE_COMMAND" or
  // 1 "TRUE_COMMAND"
  int isPromotion = (int) readBuffer[5]; //does the move involve promoting a pawn
  int isTall = (int) readBuffer[6]; // is the starting piece tall
  int isCaptureTall = (int) readBuffer[7]; // is the piece that is being captured tall

// writes special moves
  digitalWrite(isCapturePin, LOW);
  digitalWrite(isCastlingPin, LOW);
  digitalWrite(isEnPassantPin, LOW);
  if (specialMove == CAPTURE_COMMAND) {
    digitalWrite(isCapturePin, HIGH);
  }
  if (specialMove == CASTLING_COMMAND) {
    digitalWrite(isCastlingPin, HIGH);
  }
  if (specialMove == ENPASSANT_COMMAND) {
    digitalWrite(isEnPassantPin, HIGH);
  }

  //writes the info about the starting square
  analogWrite(XOutPin, maps(startX));
  analogWrite(YOutPin, maps(startY));

  digitalWrite(arduinoReadyPin, HIGH); // tells the arm that the first set of inputs are ready
  delay(300);
  digitalWrite(arduinoReadyPin, LOW);
  
// waits for the arm to be ready to accept extra inputs
// I do this so that it limits the number of inputs I have 
// to physically build to the arm.
// Also, there are only two analog inputs to the arm anyway, so I have to do this.
  while(true) {
    if (digitalRead(armReadyPin) == 1)
      break;
    delay(10);
  }
  
// is promotion
  if(isPromotion == TRUE_COMMAND) {
    digitalWrite(isPromotionPin, HIGH);
  } else {
    digitalWrite(isPromotionPin, LOW);
  }

//  isTall
  if(isTall == TRUE_COMMAND) {
    digitalWrite(isTallPin, HIGH);
  } else {
    digitalWrite(isTallPin, LOW);
  }

// isCaptureTall
  if(isCaptureTall == TRUE_COMMAND) {
    digitalWrite(isCaptureTallPin, HIGH);
  } else {
    digitalWrite(isCaptureTallPin, LOW);
  }

  //writes the info about the end square
  analogWrite(XOutPin, maps(endX));
  analogWrite(YOutPin, maps(endY));

  //tells the arm that the second set of inputs are ready
  //note: the delay times are fairly arbitrary. 
  //The time in total below should at least be 5 seconds to avoid any conflicts with the delay times in the arm's code
  //If the time is set too short, the following lines of code in the while loop could be executed before the arm begins moving
  //That would cause the whole system to softlock (halt) or crash
  digitalWrite(arduinoReadyPin, HIGH);
  delay(5000);
  digitalWrite(arduinoReadyPin, LOW);
  delay(3000);

  // waits for the arm to finish its movement before accepting extra inputs
  while(true) {
    if (digitalRead(armReadyPin) == 1) {
      delay (200);
      if (digitalRead(armReadyPin) == 1)
        break;
    }
    delay(10);
  }

  //Resets the pwm outputs
  //note: these two lines aren't necessary for the program to work
  //It is simply an organizes it so that pointless signals aren't being sent
  analogWrite(XOutPin, 0);
  analogWrite(YOutPin, 0);

  Serial.write(3); // sends an arbitrary number. When the java program recieves this, it knows the arm is ready
  
  //Indicates to the arduino MEGA that the arm has finished its move, and that it is free to start recording the board position
  digitalWrite(arduinoMegaPin, HIGH);
  delay (1000);
  digitalWrite(arduinoMegaPin, LOW);
}
