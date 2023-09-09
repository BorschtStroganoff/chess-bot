import java.util.Locale;
import java.util.Scanner;

/**
*  This class deals with determining what move to make in a given position.
*  Also known as the chess engine.
*/

public class Computer {
   private int positionsExamined = 0; //this counts how many positions were examined
   private int[] map = new int[64]; // this is a temporary buffer threatMap that is used in determining the legal moves in a position
   private int move; // this variable stores the computer's move after doing the alphaBeta search algorithm

   public int getMove() {
       return move;
   }

   public int getPositionsExamined() {
       return positionsExamined;
   }

   /**
    * This is the search algorithm in determining what move to play in a certain position.
    * It uses ideas from minimax and alpha-beta pruning to find the move
    *
    * @param p
    *      This is the position that the computer is to search from.
    * @param depth
    *      This is how many plies ("moves") deep the algorithm will search.
    * @param alpha
    *      This is the alpha value. It is assigned as negative infinity, and it increases in value.
    *      Its purpose is to indicate when it is pointless to continue searching a certain branch
    *      as the minimizing player.
    * @param beta
    *      This is the beta value. It is assigned positive infinity, and it decreases in value.
    *      Its purpose is to indicate when it is pointless to continue searching a certain branch
    *      as the maximizing player.
    * @param maximizingPlayer
    *      This tells the computer whether it's trying to maximize its score (play for white),
    *      or whether it is trying to minimize its score (play for black).
    * @return
    *      Returns the evaluation of the position.
    *      A positive score means the computer thinks white is ahead.
    *      A negative score means the computer things black is ahead.
    *      An equal score means the computer thinks the position is equal.
    *      Returned values around +/- 10000 indicate that there's a forced mate.
    */
   public double alphaBetaA(Position p, int depth, double alpha, double beta, boolean maximizingPlayer) {
       this.positionsExamined++;
       int[] legalMoves;
       if (p.getTurn() == Const.WHITE)
           legalMoves = p.findLegalMoves(p,Const.WHITE, p.threatMap(p,Const.BLACK,map));
       else legalMoves = p.findLegalMoves(p,Const.BLACK, p.threatMap(p,Const.WHITE,map));

       legalMoves = orderMoves(p,legalMoves);

       // if there are no legal moves, returns 10000-offset (White won), -10000+offset (Black won), or 0 (stalemate)
       if (legalMoves[0] == 0) {
           return status(p);
       }

       // if the end of the depth is reached, return the static evaluation of the position
       if (depth == 0)
           return staticEvaluation(p);

       // is a draw if the 50-move rule is surpassed
       // the rule is that 50-moves happen in a row where no pawn moves nor captures occur, someone can claim a draw
       // Technically, players can keep playing if both agree, but here, I just assume that someone will claim a draw
       // The number is 100 because the halfmove clock increases every ply, and 50 "moves" is equal to 100 "plies"
       if (p.getHalfMoveClock() >= 100)
           return 0.0;

       double value;
       if (maximizingPlayer) {
           value = Const.NEGATIVE_INFINITY;
           for (int i = 0; i<Const.BUFFER_SIZE; i++) {

               if (legalMoves[i] == 0)
                   break;

               double eval = alphaBetaA(p.getMovedBoard(p,legalMoves[i]),depth-1,alpha, beta,false);
               if (eval > value && depth == Const.DEPTH)
                   this.move = legalMoves[i];
               value = Double.max(value, eval);

               if (value > beta)
                   break;

               alpha = Double.max(alpha, value);
           }
       }

       else {
           value = Const.POSITIVE_INFINITY;
           for (int i = 0; i<Const.BUFFER_SIZE; i++) {
               if (legalMoves[i] == 0)
                   break;

               double eval = alphaBetaA(p.getMovedBoard(p,legalMoves[i]),depth-1,alpha, beta,true);
               if (eval < value && depth == Const.DEPTH)
                   this.move = legalMoves[i];
               value = Double.min(value,eval);

               if (value < alpha)
                   break;

               beta = Double.min(beta, value);
           }
       }
       return value;
   }

   /**
    * This function orders the moves so that more promising moves are evaluated first
    * Sets moves that are checks and captures first
    * Sets moves that are just checks second
    * Sets moves that are just captures third
    * Sets quiet moves last
    *
    * @param p
    *      The position that is being branched.
    * @param moves
    *      These are the legal moves in the given position.
    * @return
    *      Returns the legal move list in an order that is more likely to have better moves in the front
    *      and worse moves in the back.
    */
   public int[] orderMoves(Position p,int[] moves) {

       // This array associates each legal move with a classification as to what kind of move it is:
       // quiet move, capture, check, or check+capture
       int[][] classifiedMoves = new int[Const.BUFFER_SIZE][2];

       for (int i = 0; i < Const.BUFFER_SIZE; i++) {
           int move = moves[i];
           classifiedMoves[i][0] = move;
           int start = move /100;
           int end = move%100;


           Pieces endPiece = p.getBoard()[end];
           // if the ending square contains a piece (is not empty)
           boolean isCapture = endPiece.piece != Const.EMPTY;

           int defendingColor;
           int attackingColor = p.getTurn();
           if (attackingColor == Const.WHITE)
               defendingColor = Const.BLACK;
           else defendingColor = Const.WHITE;


           int [] threatMap = new int[64];
           threatMap = p.threatMap(p.getMovedBoard(p,move),attackingColor,threatMap);
           boolean isKingThreatened = false;
           for (int j = 0; j<64; j++) {
               // if the piece is a king, it's the defending color, and it's under direct attack, the king is threatened
               if (p.getBoard()[j].piece == Const.KING_PIECE && p.getBoard()[j].color == defendingColor) {
                   if (threatMap[j] == Const.DIRECT_ATTACK)
                       isKingThreatened = true;
                   break;
               }

           }

           classifiedMoves[i][0] = move;
           if (move == 0)
               classifiedMoves[i][1] = 0;
           else if (!isKingThreatened && !isCapture)
               classifiedMoves[i][1] = Const.QUIET_MOVE;
           else if (isKingThreatened && !isCapture)
               classifiedMoves[i][1] = Const.CHECK_MOVE;

               //note: the value is multiplied/added by the value of the piece because moves that capture higher-valued pieces should be evaluated first
               //note: it shouldn't matter whether the value of the piece is added or multiplied
           else if (!isKingThreatened && isCapture)
               classifiedMoves[i][1] = Const.CAPTURE_MOVE * (int) Math.abs(getPieceValue(endPiece));
           else if (isKingThreatened && isCapture)
               classifiedMoves[i][1] = Const.CHECK_AND_CAPTURE_MOVE + (int) Math.abs(getPieceValue(endPiece)) ;

       }
       return sortMoves(classifiedMoves,moves);
   }

   // sorts the 2D array based on what its classifications are
   public int[] sortMoves(int[][] classifiedMoves, int[]moves) {

       // this is the sort algorithm that orders the move based on how strong its classification is
       // 1. Checks+captures
       // 2. Checks
       // 3. Captures
       // 4. Quiet moves
       for (int i = 0; i<Const.BUFFER_SIZE; i++) {
           int max=-1;
           int index = 0;
           for (int j = i; j<Const.BUFFER_SIZE; j++) {
               if (classifiedMoves[j][1] > max) {
                   index = j;
                   max = classifiedMoves[j][1];
               }
           }
           int tempMove = classifiedMoves[index][0];
           int tempValue = classifiedMoves[index][1];

           classifiedMoves[index][0] = classifiedMoves[i][0];
           classifiedMoves[index][1] = classifiedMoves[i][1];

           classifiedMoves[i][0] = tempMove;
           classifiedMoves[i][1] = tempValue;

       }

       //converts the 2D array back to the 1D array with the part that contained the moves
       for (int i = 0; i< Const.BUFFER_SIZE; i++) {
           moves[i] = classifiedMoves[i][0];
       }
       return moves;
   }

   // this function determines if the game is over
   public boolean isGameOver(Position p) {
       int[] legalMoves;
       int[] threatMap = new int[64];

       legalMoves = p.findLegalMoves(p, p.getTurn(), threatMap);
       return legalMoves[0] == 0;
   }

   // this function determines the result of a given position
   // is only called if the game is over
   public void endingSequence(Position p) {
       double result = status(p);
       if (result > 0)
           System.out.println("White is victorious");
       else if (result < 0)
           System.out.println("Black is victorious");
       else
           System.out.println("It's a draw");
   }

   /**
    * this function returns an array of bytes (size 8) that will be sent to the robotic arm via it's I/O box
    *      the command will look something like this
    *      {5,2,5,4,0,0,0,0}
    *      the first byte represents the x coordinate of the start square (in this example, 5 is the e column)
    *      the second byte represents the y coordinate of the start square (in this example, 2 is row 2
    *      the third byte represents the x coordinate of the end square (in this example, 5 is the e column)
    *      the fourth byte represent the y coordinate of the end square (in this example, 4 is row 4)
    *      the fifth byte represents the "special move"
    *          0 means it is a normal move
    *          1 means it is a capture
    *          2 means it is a casting move
    *          3 means it is en passant
    *      the sixth byte represents if the move is a promotion
    *          0 means no promotion
    *          1 means promotion
    *      the seventh byte represents if the starting piece is tall
    *          0 for short piece
    *          1 for tall piece
    *          (this is required so that the arm gripper doesn't crash into tall piece or come up short on grabbing short pieces)
    *      the eighth byte represents if the ending/captured piece is tall
    *          0 for short piece
    *          1 for tall piece
    *          (this is the same idea as the seventh byte, but for pieces that are getting captured)
    *
    *      note: the position is supposed to be in the position before the move is made
    */

   public byte[] determineArduinoCommand(Position p, int move) {
       byte[] command = new byte[8];

       //these are the variables, in order, that will be a part of the command
       int startX;
       int startY;
       int endX;
       int endY;
       int specialMove;
       int isPromotion;
       int isTall;
       int isCaptureTall;


       int start = move / 100;
       int end = move % 100;

       // converts the indexes into coordinates
       startX = (start % 8) + 1; // e.g. (16 % 8) + 1 = 0 + 1 = 1 which is the a-file
       startY = (start / 8) + 1; // e.g. (16 / 8) + 1 = 2 + 1 = 3 which is the 3rd rank
       endX = (end % 8) + 1;
       endY = (end / 8) + 1;

       int startPiece = p.getBoard()[start].piece;
       int endPiece = p.getBoard()[end].piece;

       // determines the special move
       // if the end square is the enpassant square, and a pawn is moving there, the special move is an en passant
       if (end == p.getEnPassant() && startPiece == Const.PAWN_PIECE)
           specialMove = Const.ENPASSANT_COMMAND;
           // if the starting piece is a king, and it is traveling two squares away, the special move is castling
       else if (startPiece == Const.KING_PIECE
               && Math.abs(start-end) == 2)
           specialMove = Const.CASTLING_COMMAND;
           // if the ending square contains a piece, the move is a capture
       else if (endPiece != Const.EMPTY)
           specialMove = Const.CAPTURE_COMMAND;
       else
           specialMove = Const.NORMAL_MOVE_COMMAND;

       // determines if move is a promotion
       // if the end square is on the first or eighth ranks, and the piece moved is a pawn, isPromotion is true
       if (((end / 8) + 1 == 1 || (end / 8) + 1 == 8)
               && startPiece == Const.PAWN_PIECE)
           isPromotion = Const.TRUE_COMMAND;
       else isPromotion = Const.FALSE_COMMAND;

       // determines if the piece being moved is tall
       if (startPiece == Const.QUEEN_PIECE || startPiece == Const.KING_PIECE)
           isTall = Const.TRUE_COMMAND;
       else isTall = Const.FALSE_COMMAND;

       // determines if the piece being captured (if it is) is tall
       if (endPiece == Const.QUEEN_PIECE) //we don't need to check the king because capturing the king is an illegal move
           isCaptureTall = Const.TRUE_COMMAND;
       else isCaptureTall = Const.FALSE_COMMAND;

       command[0] = (byte) startX;
       command[1] = (byte) startY;
       command[2] = (byte) endX;
       command[3] = (byte) endY;
       command[4] = (byte) specialMove;
       command[5] = (byte) isPromotion;
       command[6] = (byte) isTall;
       command[7] = (byte) isCaptureTall;
       return command;
   }

   // this function should only be called when there are no legal moves
   // returns whether white won, black won, or is stalemate
   public double status(Position p) {


       int turnColor = p.getTurn();
       int attackingColor;
       if (turnColor == Const.WHITE)
           attackingColor = Const.BLACK;
       else attackingColor = Const.WHITE;

       int c; // coefficient that will determine whether white or black won (won't matter if it is stalemate)
       if (attackingColor == Const.BLACK)
           c = -1;
       else c = 1;

       // this value is important in mating when there's few pieces on a board (e.g. KQ v k).
       // Essentially, this value will make moves that go towards a mate slightly more valuable than moves that simply stall the mate count.
       // In other words, it will cause a mate in 2 position to be more valuable than a mate in 3 position...
       // ...because the mate in three position will have a higher half move clock value
       double halfMoveOffset = -c * 0.05 * p.getHalfMoveClock();

       int[] map = new int[64];

       int[] threatMap = p.threatMap(p,attackingColor,map);
       boolean isKingUnderDirectAttack = false;
       for (int i = 0; i <64; i++) {
           // if the piece is a king ,and it's the color of the player who is supossed to move
           if (p.getBoard()[i].piece == Const.KING_PIECE
                   && p.getBoard()[i].color == turnColor
                   && threatMap[i] == Const.DIRECT_ATTACK)
               return c * Const.POSITIVE_INFINITY + halfMoveOffset; //the halfmove offset is an arbitrarily small number that will make postions with quicker checkmate have higher value than postion with lower checkmates
       }
       // if the king isn't under direct attack, it's a stalemate.
       return 0.0;
   }

   // This function evaluates the position statically
   // The evaluation considers how many piece each side has,
   // and the position of those pieces according based on the piece-square tables
   public double staticEvaluation(Position p) {
       double eval = 0.0;

       //first, considers the material values of how many pieces each side has
       for (int i = 0; i<64; i++) {
           Pieces currentPiece = p.getBoard()[i];

           // this coefficient determines will either make the values increase (better for white) or decrease (better for black) depending on the kind of piece it is
           int c;
           if (currentPiece.color == Const.WHITE)
               c = 1; //positive direction (white)
           else
               c = -1; // negative direction (black)

           //adds the material value of the piece times the direction (white/positive direction or black/negative direction)
           eval += c * getPieceValue(currentPiece);

           // uses the piece-square tables to evaluate how effective the pieces' positions on the board are
           switch (currentPiece.piece) {
               case Const.EMPTY:
                   break;

               case Const.PAWN_PIECE:
                   if (currentPiece.color == Const.BLACK) {
                       eval += c * Const.PAWN_TABLE[63-i];
                   }
                   else eval += c* Const.PAWN_TABLE[i];
                   break;

               case Const.KNIGHT_PIECE:
                   if (currentPiece.color == Const.WHITE) {
                       eval += c * Const.KNIGHT_TABLE[i];
                   }
                   else eval += c * Const.KNIGHT_TABLE[63-i];
                   break;

               case Const.BISHOP_PIECE:
                   if (currentPiece.color == Const.WHITE) {
                       eval += c * Const.BISHOP_TABLE[i];
                   }
                   else eval += c* Const.BISHOP_TABLE[63-i];
                   break;

               case Const.ROOK_PIECE:
                   if (currentPiece.color == Const.WHITE) {
                       eval += c * Const.ROOK_TABLE[i];
                   }
                   else eval += c* Const.ROOK_TABLE[63-i];
                   break;

               //note: the queen table requires two different tables according to the colors because
               // queens tend to be best positioned on c2-b3 (white) and c7-b6 (black)
               // those squares cause the tables to not be symmetric, so it requires two different tables
               case Const.QUEEN_PIECE:
                   if (currentPiece.color == Const.WHITE) {
                       eval += c* Const.WHITE_QUEEN_TABLE[i];
                   }
                   else eval += c * Const.BLACK_QUEEN_TABLE[63-i];
                   break;

               // note: there are two different piece-square tables for the king because
               // in the early stages of the game, the king is better protected,
               // but in the later stages of the game, the king is better centralized
               case Const.KING_PIECE:
                   if (p.getFullMoveClock() < 35) {
                       if (currentPiece.color == Const.WHITE)
                           eval += c* Const.KING_MIDDLEGAME_TABLE[i];
                       else eval += c * Const.KING_MIDDLEGAME_TABLE[63-i];
                   }
                   else {// if fullmoveclock >=35
                       if (currentPiece.color == Const.WHITE)
                           eval += c* Const.KING_ENDGAME_TABLE[i];
                       else eval += c * Const.KING_ENDGAME_TABLE[63-i];
                   }
           }
       }
       return eval;
   }

   public double getPieceValue(Pieces piece) {

       switch (piece.piece) {
           case Const.EMPTY:
               return 0;
           case Const.PAWN_PIECE:
               return Const.PAWN_VALUE;
           case Const.KNIGHT_PIECE:
               return Const.KNIGHT_VALUE;
           case Const.BISHOP_PIECE:
               return Const.BISHOP_VALUE;
           case Const.ROOK_PIECE:
               return Const.ROOK_VALUE;
           case Const.QUEEN_PIECE:
               return Const.QUEEN_VALUE;
           case Const.KING_PIECE:
               return Const.KING_VALUE;
       }
       return Const.ERROR;
   }
}
