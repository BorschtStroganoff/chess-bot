/**
* This class holds all of this project's constant values
*/
public class Const {

   // error value
   public static final int ERROR = -1;

   // these are the values for the commands to send to the Arduino UNO
   // also found on the code for the Arduino Uno
   public static final int NORMAL_MOVE_COMMAND = 0;
   public static final int CAPTURE_COMMAND = 1;
   public static final int CASTLING_COMMAND = 2;
   public static final int ENPASSANT_COMMAND = 3;
   public static final int TRUE_COMMAND = 1;
   public static final int FALSE_COMMAND = 0;

   // size of buffers such as the move list array
   public static final int BUFFER_SIZE = 70;

   //constant for search depth in the alphaBeta minimax algorithm
   public static final int DEPTH = 5;

   //constant for the starting position fen
   public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

   // values that represent the pieces
   public static final int EMPTY = 0;
   public static final int PAWN_PIECE = 1;
   public static final int KNIGHT_PIECE = 2;
   public static final int BISHOP_PIECE = 3;
   public static final int ROOK_PIECE = 4;
   public static final int QUEEN_PIECE = 5;
   public static final int KING_PIECE = 6;

   // values that represent the color
   public static final int NO_COLOR = 0;
   public static final int WHITE = 1;
   public static final int BLACK = 2;

   // characters that represent the pieces
   public static final char EMPTY_SYMBOL = '-';
   public static final char PAWN_SYMBOL = 'p';
   public static final char KNIGHT_SYMBOL = 'n';
   public static final char BISHOP_SYMBOL = 'b';
   public static final char ROOK_SYMBOL = 'r';
   public static final char QUEEN_SYMBOL = 'q';
   public static final char KING_SYMBOL = 'k';

   //unit directions for pieces
   //note: the pawn moves are not here because they are not used
   public static final int[] KNIGHT_MOVES =
           {10, 17, 15, 6,
                   -10, -17, -15, -6};
   //note: the bishop, rook, and queen arrays are simply unit vectors.
   // that means that these only contain the steps in one direction
   public static final int[] BISHOP_MOVES =
           {9,7,-9,-7};
   public static final int[] ROOK_MOVES =
           {1,8,-1,-8};
   public static final int[] QUEEN_MOVES =
           {1,9,8,7,-1,-9,-8,-7};
   public static final int[] KING_MOVES =
           {1,9,8,7,-1,-9,-8,-7};

   // constants used with LinearMoveCheck
   public static final int TERMINATE = 0;
   public static final int CAPTURE = 1;
   public static final int CONTINUE = 2;

   // constants unique to LinearThreatCheck
   public static final int SAME_COLOR = 3;
   public static final int DIFFERENT_COLOR = 4;

   // constants used with the threat map
   public static final int NO_ATTACK = 0;
   public static final int DIRECT_ATTACK = 1;
   public static final int DISCOVERED_ATTACK = 2;

   //constants used in evaluating static positions
   // these values were taken from this article
   // https://www.chessprogramming.org/Simplified_Evaluation_Function
   public static final double PAWN_VALUE = 100;
   public static final double KNIGHT_VALUE = 320;
   public static final double BISHOP_VALUE = 330;
   public static final double ROOK_VALUE = 500;
   public static final double QUEEN_VALUE = 900;
   public static final double KING_VALUE = 20000;

   //constants used in ordering moves
   public static final int QUIET_MOVE = 1;
   public static final int CHECK_MOVE = 2000;
   public static final int CAPTURE_MOVE = 2;
   public static final int CHECK_AND_CAPTURE_MOVE = 2000;

   // these are the arbitrarily large numbers used in evaluating the position
   public static final double NEGATIVE_INFINITY = -10000;
   public static final double POSITIVE_INFINITY = 10000;

   // piece-square tables that are based on the tables found at https://www.chessprogramming.org/Simplified_Evaluation_Function
   //These tables are for White
   //To make them for black, reverse the order of the for loop (63 to 0)
   public static final int[] PAWN_TABLE =

           // These arrays are reflected over x-axis (flipped vertically) compared to a regular board orientation.
           // this is done so that the array is compatible with the index form of the board
           //top left represents a1, top right represents h1, bottom left represents a8, bottom right reps h8
           {
                   0,  0,  0,  0,  0,  0,  0,  0,
                   5, 10, 10,-20,-20, 10, 10,  5,
                   5, -5,-10,  0,  0,-10, -5,  5,
                   0,  0,  0, 20, 20,  0,  0,  0,
                   5,  5, 10, 25, 25,  10, 5,  5,
                   10, 10, 20, 30, 30, 20, 10, 10,
                   50, 50, 50, 50, 50, 50, 50, 50,
                   0,  0,  0,  0,  0,  0,  0,  0
           };

   public static final int[] KNIGHT_TABLE =
           {
                   -50,-40,-30,-30,-30,-30,-40,-50,
                   -40,-20,  0,  5,  5,  0,-20,-40,
                   -30,  5, 10, 15, 15, 10,  5,-30,
                   -30,  0, 15, 20, 20, 15,  0,-30,
                   -30,  5, 15, 20, 20, 15,  5,-30,
                   -30,  0, 10, 15, 15, 10,  0,-30,
                   -40,-20,  0,  0,  0,  0,-20,-40,
                   -50,-40,-30,-30,-30,-30,-40,-50
           };

   public static final int[] BISHOP_TABLE =
           {
                   -20,-10,-10,-10,-10,-10,-10,-20,
                   -10,  5,  0,  0,  0,  0,  5,-10,
                   -10, 10, 10, 10, 10, 10, 10,-10,
                   -10,  0, 10, 10, 10, 10,  0,-10,
                   -10,  5,  5, 10, 10,  5,  5,-10,
                   -10,  0,  5, 10, 10,  5,  0,-10,
                   -10,  0,  0,  0,  0,  0,  0,-10,
                   -20,-10,-10,-10,-10,-10,-10,-20
           };

   public static final int[] ROOK_TABLE =
           {
                   0,  0,  0,  5,  5,  0,  0,  0,
                   -5, 0,  0,  0,  0,  0,  0, -5,
                   -5, 0,  0,  0,  0,  0,  0, -5,
                   -5, 0,  0,  0,  0,  0,  0, -5,
                   -5, 0,  0,  0,  0,  0,  0, -5,
                   -5, 0,  0,  0,  0,  0,  0, -5,
                   5, 10, 10, 10, 10, 10, 10,  5,
                   0,  0,  0,  0,  0,  0,  0,  0
           };


   public static final int[] WHITE_QUEEN_TABLE =
           {
                   -20,-10,-10, -5, -5,-10,-10,-20,
                   -10,  0,  5,  0,  0,  0,  0,-10,
                   -10,  5,  5,  5,  5,  5,  0,-10,
                     0,  0,  5,  5,  5,  5,  0, -5,
                    -5,  0,  5,  5,  5,  5,  0, -5,
                   -10,  0,  5,  5,  5,  5,  0,-10,
                   -10,  0,  0,  0,  0,  0,  0,-10,
                   -20,-10,-10, -5, -5,-10,-10,-20
           };

   // This table is different from WHITE_QUEEN_TABLE[] in that the squares c7 and b6 are promoted rather than c2 and b3
   // This will be read backwards, so the extra valued squares changed from indexes (10 and 17) to (20 and 29)
   public static final int[] BLACK_QUEEN_TABLE =
           {
                   -20,-10,-10, -5, -5,-10,-10,-20,
                   -10,  0,  0,  0,  0,  5,  0,-10,
                   -10,  0,  5,  5,  5,  5,  5,-10,
                     0,  0,  5,  5,  5,  5,  0, -5,
                    -5,  0,  5,  5,  5,  5,  0, -5,
                   -10,  0,  5,  5,  5,  5,  0,-10,
                   -10,  0,  0,  0,  0,  0,  0,-10,
                   -20,-10,-10, -5, -5,-10,-10,-20
           };

   //this table is used for positions early in the game because it's better to have a safe king at the start of the game
   public static final int[] KING_MIDDLEGAME_TABLE =
           {
                    20, 30, 10,  0,  0, 10, 30, 20,
                    20, 20,  0,  0,  0,  0, 20, 20,
                   -10,-20,-20,-20,-20,-20,-20,-10,
                   -20,-30,-30,-40,-40,-30,-30,-20,
                   -30,-40,-40,-50,-50,-40,-40,-30,
                   -30,-40,-40,-50,-50,-40,-40,-30,
                   -30,-40,-40,-50,-50,-40,-40,-30,
                   -30,-40,-40,-50,-50,-40,-40,-30
           };

   // this table is used for positions later in the game, because you want an active king during the endgame
   public static final int[] KING_ENDGAME_TABLE =
           {
                   -50,-30,-30,-30,-30,-30,-30,-50,
                   -30,-30,  0,  0,  0,  0,-30,-30,
                   -30,-10, 20, 30, 30, 20,-10,-30,
                   -30,-10, 30, 40, 40, 30,-10,-30,
                   -30,-10, 30, 40, 40, 30,-10,-30,
                   -30,-10, 20, 30, 30, 20,-10,-30,
                   -30,-20,-10,  0,  0,-10,-20,-30,
                   -50,-40,-30,-20,-20,-30,-40,-50
           };
}
