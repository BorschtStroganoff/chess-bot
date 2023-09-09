#define PUSH_BUTTON 50
#define ARM_FINISHED 51

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  //a and b columns
  for(int i = A0; i<=A15; i++)
    pinMode(i, INPUT);

  for(int i=2; i<=13; i++)
    pinMode(i, INPUT);

  for(int i=14; i<=49; i++)
    pinMode(i, INPUT);

  pinMode(PUSH_BUTTON, INPUT); //button that player presses after finishing move
  pinMode(ARM_FINISHED, INPUT); // signal that will tell the mega (this arduino) that the arm has finished its move
  
}

void loop() {
  
byte startPos[64]; //stores the starting position
byte endPos[64]; //stores the ending position
byte changedSquares[64]; //stores the squares that changed states during the move

recordMove(startPos, endPos, changedSquares);

  // waits for a Serial connection to be established between this arduino and the java program
  while(!Serial) {
    delay(10);
  }
  
  writeBytes(startPos, endPos, changedSquares);

  //This delay time is arbitrary
  // Five seconds works because it takes the arm over five seconds to move the piece
  delay(5000);

  //waits for Arm to finish its move before moving on to record
  while (true) {
    if (digitalRead(ARM_FINISHED) == HIGH)
      break;
    delay(100);
  }

}

//This is the function that writes the three byte arrays startPos, endPos, and changedSquares
//to the java program that decodes the move and moves the move in its stored position
void writeBytes(byte arr1[], byte arr2[], byte arr3[]) {
  Serial.write(arr1, 64);
  Serial.write(arr2, 64);
  Serial.write(arr3, 64);
}

// Records the starting position, the ending position, and all the coordinates that experienced change
void recordMove(byte startPos[], byte endPos[], byte changedSquares[]) {
  
  int i;
  byte bufferBoard[64];

  
  // clear the changedSquared buffer
  for (i=0; i<64; i++)
    changedSquares[i] = 0;
  

  // reads the position before the move is made
  readBoard(startPos);

  //this will fill changedSquares[] with the squares that change until player presses the button
  while (true) {
    readBoard(bufferBoard);
    compareBoards(startPos, bufferBoard, changedSquares);

    if (digitalRead(PUSH_BUTTON) == HIGH) {
      readBoard(endPos);
      break;
    }
      
    delay(30);
  }
}

// compares the current position with the starting position, and records the changes in changedSquares[]
void compareBoards(byte startPos[], byte bufferBoard[], byte changedSquares[]) {
  for (int i=0; i<64; i++) {
    if (bufferBoard[i] != startPos[i])
      changedSquares[i] = 1;
  }
}

void readBoard(byte board[]) {
  int i; // index variable for board[]
  int j; // index variable representing the pins
//  int board[64];
  for (i=0; i<64; i++)
    board[i] = 0;

  // a column - [A1 to A7]
  for (i=0, j=A0;j<=A7; i+=8, j++) {
    board[i] = digitalRead(j);
  }

  //b column - [A8 to A15]
  for (i=1, j=A8; i<64; i+=8, j++) {
    board[i] = digitalRead(j);
  }

  //c column [13 to 6]
  for (i=2, j=13; i<64; i+=8, j--) {
    board[i] = digitalRead(j);
  }

  //d column [49 to 42]
  for (i=3, j=49; i<64; i+=8, j--) {
    board[i] = digitalRead(j);
  }

  //e column [18 to 25]
  for (i=4, j=18; i<64; i+=8, j++) {
    board[i] = digitalRead(j);
  }
  // f column [41 to 34]
  for (i = 5, j=41; i<64; i+=8, j--) {
    board[i] = digitalRead(j);
  }

  // g column [5 to 2 and then 14 to 17]
  for (i=6, j=5; j>=2; i+=8, j--) {
    board[i] = digitalRead(j);
  }
  for (i=i, j=14; j<=17; i+=8, j++) {
    board[i] = digitalRead(j);
  }

  // h column [33 to 26]
  for (i=7, j=33; i<= 64; i+=8, j--) {
    board[i] = digitalRead(j);
  }
}


// command used in testing
void printBoard(byte board[]) {
  Serial.println("--------");
  Serial.println();

  int i,j;
  for (i=7; i>=0; i--) {
    for (j=i*8; j< i*8+8; j++)
      Serial.print(board[j]);
    Serial.println();
  }


  Serial.println();
  Serial.println("--------");
}
