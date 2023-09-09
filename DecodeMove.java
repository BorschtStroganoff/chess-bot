import java.util.Arrays;

/**
* This class is dedicated to determining the move made by the player on the physical chess board
* It takes the position before the move was made, the position after it was made,
* and the squares that experienced change during the move.
* With that information, it decodes what move must have taken place.
* This class is a solution to the problem: "How can I determine the player's move if I'm only given a binary value of the position?"
* In other words, "I only know whether a piece occupies a square or not. How do I determine the moves without knowing what the pieces are?"
* Note: this a very unstable program and crashes if the player fumbles with the pieces.
*      For example, if the player initially makes one move, but then changes his mind and makes a capture move,
*      the program will crash because the "count" number will be wrong, and an IndexArrayOutOfBounds error occurs
*/
public class DecodeMove {

   // this returns the move that was played on the physical board by the player
   public int determineMove(byte[] startPos, byte[] endPos, byte[] changedSquares, int color) {
       int start = -1; //original square of piece's move
       int end = -1; //destination square of piece's move

       // first, checks to see if move was a simple move or promotion without capturing (no captures, enpassants or castling)
       int count =0;
       //counts how many differences are in the position
       for (int i=0; i<64; i++) {
           if (startPos[i] != endPos[i])
               count++;
       }
       if (count == 0) //if no change was made (no move was made), return an error of -1
           return Const.ERROR;

       if (count == 2) { //simple move was made (one square turned off, one square turned on)
           for (int i=0; i<64; i++) {
               if(startPos[i]==1 && endPos[i]==0) //if the square was on before the move, and off after the move, that is the start square
                   start = i;
               else if(startPos[i] ==0 && endPos[i]==1) //if square was off before the move, and on after the move, that is the end square
                   end = i;
           }
           if (start == -1 || end == -1) //if either one of the values doesn't update, return an error
               return Const.ERROR;
           return start*100 + end; //return the move in the 4-digit format
       }

       if (count == 1) { // move was a capture (one square turned off, one square remains on)
           for (int i=0; i<64; i++) {
               if (startPos[i] == 1 && endPos[i] == 0) //if the square was on before the move, and off after the move, that is the start square
                   start = i;
           }
           for (int i=0; i<64;i++) {
               if (changedSquares[i] == 1 && i != start) { //if the square experienced change during the move, and it wasn't the start square, that is the end square
                   end = i;
                   break;
               }
           }
           return start*100 + end; //return the move in the 4-digit format
       }

       if (count==3) { //move was enpassant (two squares turned off, one square turned on)
           int[] changedCoordinates = {-1, -1, -1}; //this variable will hold the three squares that change occured
           for (int i = 0, j = 0; i<64; i++) { //assigns changedCoordinates[] with the squares that changed
               if (changedSquares[i] == 1) {
                   changedCoordinates[j] = i;
                   j++;
               }
           }

           // if not all three squares were assigned, return an error
           Arrays.sort(changedCoordinates);
           if (changedCoordinates[0] == -1)
               return Const.ERROR;

           //this is a lengthy section that determines how the en passant happened
           // note: a change of 7 and a change of 9 indicate diagonal captures
           // those are the direction values that we check to determine pawn captures
           if (color == Const.WHITE) {
               if (changedCoordinates[0] + 7 == changedCoordinates[2]) { //enpassant move where white pawn captures leftwards
                   start = changedCoordinates[0];
                   end = changedCoordinates[2];
               } else

               if (changedCoordinates[0] + 9 == changedCoordinates[2]) { //enpassant move where white pawn captures rightwards
                   start = changedCoordinates[0];
                   end = changedCoordinates[2];
               } else

               if (changedCoordinates[1] + 7 == changedCoordinates[2]) { //enpassant move where white pawn captures leftwards
                   start = changedCoordinates[1];
                   end = changedCoordinates[2];
               } else

               if (changedCoordinates[1] + 9 == changedCoordinates[2]) { //enpassant move where white pawn captures leftwards
                   start = changedCoordinates[1];
                   end = changedCoordinates[2];
               } else
                   return Const.ERROR;
           }

           if (color == Const.BLACK) {
               if (changedCoordinates[1] - 9 == changedCoordinates[0]) { //enpassant move where black pawn captures leftwards relative to white perspective
                   start = changedCoordinates[1];
                   end = changedCoordinates[0];
               } else

               if (changedCoordinates[1] - 7 == changedCoordinates[0]) { //enpassant move where black pawn captures rightwards relative to white perspective
                   start = changedCoordinates[1];
                   end = changedCoordinates[0];
               } else

               if (changedCoordinates[2] - 9 == changedCoordinates[0]) { //enpassant move where black pawn captures leftwards relative to white perspective
                   start = changedCoordinates[2];
                   end = changedCoordinates[0];
               } else

               if (changedCoordinates[2] - 7 == changedCoordinates[0]) { //enpassant move where black pawn captures rightwards relative to white perspective
                   start = changedCoordinates[2];
                   end = changedCoordinates[0];
               } else
                   return Const.ERROR;
           }

           if (color != Const.WHITE && color != Const.BLACK) // if we are evaluating an empty square
               return Const.ERROR;

           return start*100 + end; //return move in 4-digit format
       }

       if (count >= 4) { // if is castling (two squares turn on, two squares turn off)
           int[] changedCoordinates = {-1, -1, -1, -1}; //this array will hold the four squares that changed
           for (int i = 0, j = 0; i<64; i++) {
               if (changedSquares[i] == 1) {
                   changedCoordinates[j] = i;
                   j++;
               }
           }
           Arrays.sort(changedCoordinates);
           if (changedCoordinates[0] == -1) //if there aren't 4 changes, return an error
               return Const.ERROR;

           if (color == Const.WHITE && changedCoordinates[1] == 2) { //if it was white's turn and move involved c1 square
               start = 4; // e1 square
               end = 2; // c1 square meaning queenside castling
           } else
           if (color == Const.WHITE && changedCoordinates[2] == 6) {// if it was white's turn and move involved g1 square
               start = 4; // e1 square
               end = 6; // g1 square meaning kingside castling
           } else
           if (color == Const.BLACK && changedCoordinates[1] == 58) { // if it was black's turn and move involved c8 square
               start = 60; // e8 square
               end = 58; // c8 square meaning queenside castling
           } else
           if (color == Const.BLACK && changedCoordinates[2] == 62) { // if it was black's turn and move involved g8 square
               start = 60; // e8 square
               end = 62; // g8 square meaning kingside castling
           } else
               return Const.ERROR;

           return start*100 + end; //return move in 4-digit format
       }
       return Const.ERROR; //return an error if a move couldn't be determined
   }
} 
