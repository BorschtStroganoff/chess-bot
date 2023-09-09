public class Arm {

    /**
     *  This class handles the interface while playing the robotic arm
     *  Note: this class only works with the player playing white and the arm playing black
     */

    public void playArm() {

        Position position = new Position(); //the board position will be stored in this variable
        Computer computer = new Computer(); //this variable/class is used to determine what move the arm will make

        //this class is used in communicating with the arduino uno, which facilitates voltage I/O communication with the robotic arm
        UnoSerialConnection arduinoUno = new UnoSerialConnection();
        arduinoUno.openPort(); //the port is opened at the beginning of the program and remains open so that the process of opening and closing it doesn't interfere with the Serial communication

        // this class is used in communicating with the arduinoMega, which records the binary position of the physical board
        MegaSerialConnection arduinoMega = new MegaSerialConnection();

        // this class is essentially a function that determines what move the player made
        DecodeMove decodeMove = new DecodeMove();

        // these variable arrays are used with "decodeMove" to determine what move the player made
        byte[] startPos = new byte[64];
        byte[] endPos = new byte[64];
        byte[] changedSquares = new byte[64];
        byte[][] bufferArrays;

        //these variables are used in printing what moves each player made
        int start;
        int end;


        System.out.println("You (white) are playing against the robotic arm. \n");
        position.readFen(Const.STARTING_POSITION); //sets the starting position of a chess game

        // this is the while loop that will loop for every move
        while (true) {

            position.printBoard();
            System.out.println("Make a move on the board, then press the button:");

            // this is section where the information about the board is read and decoded
            bufferArrays = arduinoMega.serialRead(); //reads the three arrays from the arduino mega
            // transfers the information into 1D arrays
            for (int i=0; i<64; i++) {
                startPos[i] = bufferArrays[0][i];
                endPos[i] = bufferArrays[1][i];
                changedSquares[i] = bufferArrays[2][i];
            }
            // determines the move made by the player
            int playerMove = decodeMove.determineMove(startPos, endPos, changedSquares, Const.WHITE);

            //prints out the move the player made along with the new updated position
            start = playerMove/100;
            end = playerMove%100;
            System.out.println("Player moved " + position.indexToCoordinate(start)
                    + " to " + position.indexToCoordinate(end));
            position.move(position,playerMove);
            position.printBoard();

            // checks to see if the game is finished
            // continues if game isn't over
            //breaks the loop if it is
            if (computer.isGameOver(position)) {
                computer.endingSequence(position);
                break;
            }

            //Now that the player made his move, the computer will compute a move in response
            double evaluation = computer.alphaBetaA(position, Const.DEPTH, Const.NEGATIVE_INFINITY, Const.POSITIVE_INFINITY, false);
            //after the alphaBeta evaluation, the best move is stored in "computer"'s private variable
            int computerMove = computer.getMove();

            start = computerMove / 100;
            end = computerMove % 100;

            // prints out the computer's move, moves the piece, and prints the new board position
            System.out.println("The computer determined the move: " + position.indexToCoordinate(start)
                    + " to " + position.indexToCoordinate(end));
            System.out.println("Computer evaluation: " + evaluation);

            // sends the move command to the arduino uno
            byte[] command = computer.determineArduinoCommand(position, computerMove);

            position.move(position,computerMove);
            position.printBoard();

            System.out.println("Sending command: \n");
            for (int i = 0; i < 8; i++) {
                System.out.print(command[i]);
            }
            System.out.println();

            arduinoUno.serialLoop(command);

            // This is simply down-time. The arm will be moving the move during this time
            try {
                Thread.sleep(5000);
            } catch (Exception e) {}

            //This waits for the confirmation that the arm finished its move before looping
            arduinoUno.serialRead();
        }

        arduinoUno.closePort();
    }
}
