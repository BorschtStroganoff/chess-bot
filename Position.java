import java.util.Arrays;

public class Position {

   // Array of Pieces of size 64
   private Pieces[] board;

   // whose turn it is
   private int turn;

   // values regarding whether white of black can castle
   private boolean whiteKingside;
   private boolean blackKingside;
   private boolean whiteQueenside;
   private boolean blackQueenside;

   // how many moves have been played
   private int halfMoveClock;
   private int fullMoveClock;

   // if there is a square that can be moved to via enPassant, that square will be stored here
   private int enPassant;

   // creates an empty board
   public Position() {
       board = new Pieces[64];
       for (int i=0; i<64; i++)
           board[i]= new Pieces(Const.EMPTY, Const.NO_COLOR);
   }

   public Pieces[] getBoard() {
       return board;
   }

   public int getTurn() {
       return turn;
   }

   public int getFullMoveClock() {
       return fullMoveClock;
   }

   public int getHalfMoveClock() {
       return halfMoveClock;
   }

   public int getEnPassant() {
       return enPassant;
   }

   // this function will take the piece on the starting square to the ending square
   // it does not check whether the move is legal or not
   public void moveSimple(Position p, int start, int end) {
       p.board[end].piece = p.board[start].piece;
       p.board[end].color = p.board[start].color;
       p.board[start].piece = Const.EMPTY;
       p.board[start].color = Const.NO_COLOR;
   }

   /**
    * @param p
    *      starting position
    * @param move
    *      the move that is to be made on the starting position
    */
   public Position getMovedBoard(Position p, int move) {

       Position newPosition = new Position();

       // copies all the elements of the position onto the new moved position
       for (int i = 0; i<64; i++) {
           newPosition.board[i].piece = p.board[i].piece;
           newPosition.board[i].color = p.board[i].color;
           newPosition.halfMoveClock = p.halfMoveClock;
           newPosition.fullMoveClock = p.fullMoveClock;
           newPosition.turn = p.turn;
           newPosition.blackQueenside = p.blackQueenside;
           newPosition.blackKingside = p.blackKingside;
           newPosition.whiteQueenside = p.whiteQueenside;
           newPosition.whiteKingside = p.whiteKingside;
           newPosition.enPassant = p.enPassant;
       }

       return move(newPosition, move);
   }

   /**
    * This method updates the board by moving the piece and updating the associated information about the position
    * @param p
    *  The starting position
    * @param move
    *  The move that is to be applied on the position
    * @return
    *  Returns the moved position
    */
   public Position move(Position p, int move) {
       int start = move/100;
       int startPiece = p.board[start].piece;
       int color = p.board[start].color;
       int end = move%100;

       /** updates castling rules*/
       if (startPiece == Const.KING_PIECE && color == Const.WHITE) {
           if (end == 6 && start == 4  && p.whiteKingside) {
               moveSimple(p,7,5);
           } else
           if (end == 2 && start == 4 && p.whiteQueenside) {
               moveSimple(p,0,3);
           }
           p.whiteKingside = p.whiteQueenside = false;
       }
       if (startPiece == Const.KING_PIECE && color == Const.BLACK) {
           if (end == 62 && start == 60 && p.blackKingside) {
               moveSimple(p,63,61);
           } else
           if (end == 58 && start == 60 && p.blackQueenside) {
               moveSimple(p,56,59);
           }
           p.blackQueenside = p.blackKingside = false;
       }
       if (startPiece == Const.PAWN_PIECE && end == p.enPassant) {
           if (end >= 40 && end <= 47) {
               p.board[end-8].piece = Const.EMPTY;
               p.board[end-8].color = Const.NO_COLOR;
           }
           if (end >= 16 && end <= 23) {
               p.board[end+8].piece = Const.EMPTY;
               p.board[end+8].color = Const.NO_COLOR;
           }
       }
       // if a piece on [corner square] moved or if a piece on [corner square] was captured, castling isn't allowed anymore
       if (start == 7 || end == 7) // h1
           p.whiteKingside = false;
       if (start == 0 ||end == 0) // a1
           p.whiteQueenside = false;
       if (start == 63 || end == 63) // h8
           p.blackKingside = false;
       if (start == 56 || end == 56) // a8
           p.blackQueenside = false;

       /** sets enpassant */
       if (startPiece == Const.PAWN_PIECE && color == Const.WHITE && end-start == 16)
           p.enPassant = end - 8;
       else if (startPiece == Const.PAWN_PIECE && color == Const.BLACK && end - start == -16)
           p.enPassant = end + 8;
       else
           p.enPassant = Const.EMPTY;


       /** updates halfmove clock*/
       if (startPiece == Const.PAWN_PIECE || p.board[end].piece != Const.EMPTY)
           p.halfMoveClock = -1;
       p.halfMoveClock += 1;

       /** updates the actual move*/
       moveSimple(p,start,end);

       /** updates position if move was a promotion */
       if (p.board[end].piece == Const.PAWN_PIECE && end >= 56 && end <= 63)
           p.board[end].piece = Const.QUEEN_PIECE;
       if (p.board[end].piece == Const.PAWN_PIECE && end <= 7)
           p.board[end].piece = Const.QUEEN_PIECE;

       /** updates whose turn it is*/
       if (p.turn == Const.BLACK) {
           p.turn = Const.WHITE;
           p.fullMoveClock += 1;
       }
       else
           p.turn = Const.BLACK;

       return p;
   }

   //returns the common name of a square given the index
   //e.g. returns "a2" when given index 8
   public String indexToCoordinate(int index) {
       String coordinate = "";

       char[] chars = {'a', 'b','c','d','e','f','g','h'};
       int file = index % 8;
       coordinate += chars[file];

       int rank = index / 8;
       rank++;
       coordinate += Integer.toString(rank);

       return coordinate;
   }

   // returns the index form of a square from its common name
   // e.g. returns 8 when given "a2"
   public int coordinateToIndex(String coordinate) {
       int file;
       int rank;
       char fileChar = coordinate.charAt(0);
       file = columnToNumber(fileChar);
       rank = Character.getNumericValue(coordinate.charAt(1));

       return (rank * 8 - 8 + file);
   }


   /**
    * This method finds all the legal moves in a given position.
    *
    * @param p
    *      Current position
    * @param color
    *      Whose turn it is
    * @param threat
    *      This is the buffer threat map that will be filled with which squares are under attack and discovered attack
    * @return
    *      Returns an array with legal moves.
    *      Note: The returned array will order it so that all the moves appear in the front.
    *      Unused space will be in the back of the array
    */
   public int[] findLegalMoves(Position p, int color, int[] threat) {

       int[] legalMoves = new int[Const.BUFFER_SIZE];

       if (color == Const.WHITE)
           threat = threatMap(p,Const.BLACK,threat);
       else threat = threatMap(p,Const.WHITE, threat);

       legalMoves = findPossibleMoves(p,color,legalMoves);

       boolean isKingThreatened = false;
       if (color == Const.WHITE)

           // if king is under direct or discovered attack, turn isKingThreatened true
           for (int i = 0; i<64; i++) {
               if (p.board[i].piece == Const.KING_PIECE && p.board[i].color == Const.WHITE && threat[i] != Const.NO_ATTACK) {
                   isKingThreatened = true;
                   break;
               }
           }
       else
           // if king is under direct or discovered attack
           for (int i = 0; i<64; i++)
               if (p.board[i].piece == Const.KING_PIECE && p.board[i].color == Const.BLACK && threat[i] != Const.NO_ATTACK) {
                   isKingThreatened = true;
                   break;
               }

       int start,end;
       for (int i = 0; i<Const.BUFFER_SIZE; i++) {
           start = legalMoves[i]/100;
           end = legalMoves[i]%100;
           // remove castling moves where the king has to move over a threatened square
           // and remove moves where the king walks into check.
           if (p.board[start].piece == Const.KING_PIECE) {
               if (start == 4 && end == 6 && (threat[5] == Const.DIRECT_ATTACK || threat[4] == Const.DIRECT_ATTACK))
                   legalMoves[i] = 0;
               if (start == 4 && end == 2 && (threat[3] == Const.DIRECT_ATTACK || threat[4] == Const.DIRECT_ATTACK))
                   legalMoves[i] = 0;
               if (start == 60 && end == 62 && (threat[61] == Const.DIRECT_ATTACK || threat[60] == Const.DIRECT_ATTACK))
                   legalMoves[i] = 0;
               if (start == 60 && end == 58 && (threat[59] == Const.DIRECT_ATTACK || threat[60] == Const.DIRECT_ATTACK))
                   legalMoves[i] = 0;
               if (threat[end] == Const.DIRECT_ATTACK)
                   legalMoves[i] = 0;
           }
       }
       // if king is threatened, check every move to make sure the king doesn't end up in direct attack in following move
       if (isKingThreatened) {
           Position tempPosition;
           int[] tempThreatMap = new int[64];
           for (int i = 0; i<Const.BUFFER_SIZE; i++) {

               if (legalMoves[i] == 0)
                   continue;

               else
               {
                   // creates a new position for every move and makes sure the king is fine
                   tempPosition = getMovedBoard(p,legalMoves[i]);

                   //if is white's turn
                   if (color == Const.WHITE) {
                       // find black's threats
                       tempThreatMap = threatMap(tempPosition,Const.BLACK, tempThreatMap);
                       for (int i1 = 0; i1<64; i1++) {

                           // if white's king is under direct attack, romove the move
                           if (tempPosition.board[i1].piece == Const.KING_PIECE
                                   && tempPosition.board[i1].color == Const.WHITE
                                   && tempThreatMap[i1] == Const.DIRECT_ATTACK) {
                               legalMoves[i] = 0; // removes the move
                               break;
                           }
                       }
                   }
                   else {
                       // finds white's threats
                       tempThreatMap = threatMap(tempPosition,Const.WHITE, tempThreatMap);
                       for (int i1 = 0; i1<64; i1++) {
                           // if black's king is under direct attack, remove the move
                           if (tempPosition.board[i1].piece == Const.KING_PIECE
                                   && tempPosition.board[i1].color == Const.BLACK
                                   && tempThreatMap[i1] == Const.DIRECT_ATTACK) {
                               legalMoves[i] = 0;//removes the move
                               break;
                           }
                       }
                   }
               }
           }
       }

       //sorts the arrays in descending order so that the moves appear in front, and the empty space (integers of value 0) will fill the back
       for (int i = 0; i<64; i++)
           legalMoves[i] = -1*legalMoves[i];
       Arrays.sort(legalMoves);
       for (int i = 0; i<64; i++)
           legalMoves[i] = -1*legalMoves[i];
       return legalMoves;
   }

   /**
    * returns an array with every possible move
    * illegal moves that come from this function will be removed later on
    * note:
    * moves will be stored in this format
    * the start square index will be the first two digits of the integer
    * the end square index will be the final two digits of the integer
    * e.g the move "e2e4" will be stored with this integer: 1228
    * 12 is the index for "e2", and 28 is the index for "e4"
    *
    *  the index map of the board looks like this:
    *
    *  8| 56 57 58 59 60 61 62 63
    *  7| 48 49 50 51 52 53 54 55
    *  6| 40 41 42 43 44 45 46 47
    *  5| 32 33 34 35 36 37 38 39
    *  4| 24 25 26 27 28 29 30 31
    *  3| 16 17 18 19 20 21 22 23
    *  2| 8  9  10 11 12 13 14 15
    *  1| 0  1  2  3  4  5  6  7
    *     a  b  c  d  e  f  g  h
    *    */
   public int[] findPossibleMoves(Position p, int color, int[] buffer) {


       Arrays.fill(buffer,0);

       int count = 0; //count for index in the buffer array

       // this first loop goes through every square on the board to see where the pieces are
       for (int i = 0; i<64; i++) {

           // if the piece is the same color as whose turn it is
           if (p.board[i].color == color) {

               // sees which piece it is and checks its possible moves
               switch (p.board[i].piece) {
                   // if it's a pawn
                   case Const.PAWN_PIECE:
                       //if the pawn is white and the turn is white
                       if (p.board[i].color == Const.WHITE && color == Const.WHITE) {
                           // if the pawn isn't moving off the board and there isn't a piece directly in front of the piece,
                           // moving the pawn up one square is a possible move
                           if (i+8<64 && p.board[i+8].piece == Const.EMPTY) {
                               buffer[count] = i*100 + i+8;
                               count++;
                           }

                           //if the pawn is on the 2nd rank and there are no pieces one and two squares in front of it, it can move two squares ahead
                           if ((i >=8 && i<=15) && (p.board[i+8].piece == Const.EMPTY && p.board[i+16].piece == Const.EMPTY)) {
                               buffer[count] = i*100 + i+16;
                               count++;
                           }

                           // if the pawn is not the leftmost rank, and (if there's a black piece on it's left diagonal or if it can take enpassant), it can capture there
                           if (i % 8 != 0 && (p.board[i+7].color == Const.BLACK || i+7 == p.enPassant)) {
                               buffer[count] = i*100 + i+7;
                               count++;
                           }
                           //if the pawn is not on the rightmost rank, and (if there's a black piece on its right diagonal or if it can take enpassant), it can capture it
                           if ((i % 8 != 7) && (p.board[i+9].color == Const.BLACK || i+9 == p.enPassant)) {
                               buffer[count] = i*100 + i+9;
                               count++;
                           }
                       }
                       if (p.board[i].color == Const.BLACK && color == Const.BLACK) {
                           // if the pawn isn't moving off the board and there isn't a piece directly in front of the piece,
                           // moving the pawn down one square is a possible move
                           if (i - 8 >= 0 && p.board[i-8].piece == Const.EMPTY) {
                               buffer[count] = i*100 + i-8;
                               count++;
                           }

                           //if the pawn is on the 7th rank and there are no pieces one and two squares in front of it, it can move two squares ahead
                           if ((i >=49 && i<=55) && (p.board[i-8].piece == Const.EMPTY && p.board[i-16].piece == Const.EMPTY)) {
                               buffer[count] = i*100 + i-16;
                               count++;
                           }

                           // if the pawn is not the leftmost rank (from black's perspective), and (if there's a white piece on it's left diagonal or if it can take enpassant), it can capture there
                           if (i % 8 != 7 && (p.board[i-7].color == Const.WHITE || i-7 == p.enPassant)) {
                               buffer[count] = i*100 + i-7;
                               count++;
                           }
                           //if the pawn is not on the rightmost rank (from black's perspective), and (if there's a white piece on its right diagonal or if it can take enpassant), it can capture it
                           if ((i % 8 != 0) && (board[i-9].color == Const.WHITE || i-9 == p.enPassant)) {
                               buffer[count] = i*100 + i-9;
                               count++;
                           }
                       }
                       break;

                   case Const.KNIGHT_PIECE:
                       for (int i1 = 0; i1 < 8; i1++) {
                           if (i+Const.KNIGHT_MOVES[i1] < 0
                                   || i+Const.KNIGHT_MOVES[i1] >=64)
                               continue;
                           if (p.board[i+Const.KNIGHT_MOVES[i1]].color != color // if the proposed end square isn't contained by a piece of the same color
                                   && (Math.abs( (i%8) - ((i+Const.KNIGHT_MOVES[i1]) % 8)) <=2)) // if the proposed end square doesn't jump across the board
                           {
                               buffer[count] = i*100 + (i + Const.KNIGHT_MOVES[i1]);
                               count++;
                           }
                       }
                       break;

                   case Const.BISHOP_PIECE:
                       for (int i1 = 0; i1 < 4; i1++) {
                           int direction = Const.BISHOP_MOVES[i1];
                           for (int j = 1; j <9; j++) {
                               int test = linearMoveCheck(p,direction,color,i,j);
                               if (test == Const.TERMINATE)
                                   break;
                               //
                               //if piece of opposite color is on proposed end square, add it to the array of possible moves, and end the current loop on the direction
                               if (test == Const.CAPTURE)
                               {
                                   buffer[count] = i*100 + (i + j*direction);
                                   count++;
                                   break;
                               }

                               else {
                                   buffer[count] = i*100 + (i+ j*direction);
                                   count++;
                               }

                           }
                       }
                       break;
                   case Const.ROOK_PIECE:
                       for (int i1 = 0; i1<4; i1++) {
                           int direction = Const.ROOK_MOVES[i1];
                           for (int j = 1; j <9; j++) {
                               int test = linearMoveCheck(p,direction,color,i,j);
                               if (test == Const.TERMINATE)
                                   break;
                               //
                               //if piece of opposite color is on proposed end square, add it to the array of possible moves, and end the current loop on the direction
                               if (test == Const.CAPTURE)
                               {
                                   buffer[count] = i*100 + (i + j*direction);
                                   count++;
                                   break;
                               }

                               else {
                                   buffer[count] = i*100 + (i+ j*direction);
                                   count++;
                               }

                           }
                       }
                       break;

                   case Const.QUEEN_PIECE:
                       for (int i1 = 0; i1<8; i1++) {
                           int direction = Const.QUEEN_MOVES[i1];
                           for (int j = 1; j <9; j++) {
                               int test = linearMoveCheck(p,direction,color,i,j);
                               if (test == Const.TERMINATE)
                                   break;
                               //
                               //if piece of opposite color is on proposed end square, add it to the array of possible moves, and end the current loop on the direction
                               if (test == Const.CAPTURE)
                               {
                                   buffer[count] = i*100 + (i + j*direction);
                                   count++;
                                   break;
                               }

                               else {
                                   buffer[count] = i*100 + (i+ j*direction);
                                   count++;
                               }

                           }
                       }
                       break;

                   case Const.KING_PIECE:
                       // first checks adjacent moves
                       for (int i1 = 0; i1<8; i1++) {
                           int direction = Const.KING_MOVES[i1];
                           int test = linearMoveCheck(p,direction, color, i, 1);
                           if (test == Const.CAPTURE || test == Const.CONTINUE) {
                               buffer[count] = i*100 + (i + direction);
                               count++;
                           }
                       }
                       // next checks castling
                       if (color == Const.WHITE
                               && p.board[5].piece == Const.EMPTY
                               && p.board[6].piece == Const.EMPTY
                               && whiteKingside)
                       {
                           buffer[count] = i*100 + 6; // e1 to g1
                           count++;
                       }
                       if (color == Const.WHITE
                               && p.board[1].piece == Const.EMPTY
                               && p.board[2].piece == Const.EMPTY
                               && p.board[3].piece == Const.EMPTY
                               && whiteQueenside)
                       {
                           buffer[count] = i*100 + 2; //e1 to c1
                           count++;
                       }
                       if (color == Const.BLACK
                               && p.board[61].piece == Const.EMPTY
                               && p.board[62].piece == Const.EMPTY
                               && blackKingside)
                       {
                           buffer[count] = i*100 + i+2; //e8 to g8
                           count++;
                       }
                       if (color == Const.BLACK
                               && p.board[57].piece == Const.EMPTY
                               && p.board[58].piece == Const.EMPTY
                               && p.board[59].piece == Const.EMPTY
                               && blackQueenside)
                       {
                           buffer[count] = i*100 + i-2; //e8 to c8
                           count++;
                       }
                       break;
               }
           }
       }
       return buffer;
   }

   /**
    * This function is used in checking bishop, rook, queen, and king moves that extend over a length
    * returning 0 "Const.TERMINATE" means that the loop is being terminated
    * returning 1 "Const.CAPTURE" means that the move is a capture move, so it will be the last move in that loop
    * returning 2 "Const.CONTINUE" means that the move is possible and the loop can continue
    */
   public int linearMoveCheck(Position p, int direction,int color, int i, int j) {
       if (Math.abs(((i + (j-1)*direction) % 8) - ((i + j*direction) % 8)) > 1 // if proposed end square wraps around the board horizontally
               || i+ j*direction >=64 // if proposed end square goes out of bounds
               || i+ j*direction < 0 // if the proposed end square goes out of bounds
               || p.board[i+ j*direction].color == color) // if proposed end square is already contained by a piece of the same color
           return Const.TERMINATE;

       //if piece of opposite color is on proposed end square, add it to the array of possible moves, and end the current loop on the direction
       if ((color == Const.WHITE && p.board[i+j*direction].color == Const.BLACK)
               || (color == Const.BLACK && p.board[i+j*direction].color == Const.WHITE))
       {
           return Const.CAPTURE;
       }

       else {
           return Const.CONTINUE;
       }

   }

   /**
    * modified version of LinearMoveCheck()
    * This function returns "Const.CAPTURE" even if it comes across its own colored piece
    * This is to say that the piece is defended*/
   public int LinearThreatCheck (Position p,int direction,int color, int i, int j) {
       if (Math.abs(((i + (j-1)*direction) % 8) - ((i + j*direction) % 8)) > 1 // if proposed end square wraps around board horizontally
               || i+ j*direction >=64 // if proposed end square goes out of bounds
               || i+ j*direction < 0) //if the proposed end square goes out of bounds
           return Const.TERMINATE;

       //if piece of opposite color is on proposed end square, add it to the array of possible moves, and end the current loop on the direction
       if (p.board[i+j*direction].piece != Const.EMPTY)
           if ((color == Const.WHITE && p.board[i+j*direction].color == Const.BLACK)
                   || (color == Const.BLACK && p.board[i+j*direction].color == Const.WHITE))
           {
               return Const.DIFFERENT_COLOR;
           }

       if ((color == Const.WHITE && p.board[i+ j*direction].color == Const.WHITE)
               || (color == Const.BLACK && p.board[i+ j*direction].color == Const.BLACK))
       {
           return Const.SAME_COLOR;
       }

       else {
           return Const.CONTINUE;
       }
   }

   /** returns an array with every square that the specified color is currently attacking.
    *  Squares that are under direct attack have values of 1.
    This is done so that finding illegal moves is more efficient
    */
   public int[] threatMap(Position p, int color, int[] map) {
       Arrays.fill(map,0);

       for (int i = 0; i <64; i++) {

           if (p.board[i].color == color) { //if piece is the color we specified

               switch (p.board[i].piece) {

                   case Const.PAWN_PIECE:
                       if (color == Const.WHITE) {
                           // if the pawn isn't on the left edge (a-file), pawn is threatening its left diagonal
                           if (i%8 != 0)
                               map[i+7] = Const.DIRECT_ATTACK;

                           //if the pawn isn't on the right edge (h-file), pawns is threatening its right diagonal
                           if ((i%8) !=7)
                               map[i+9] = Const.DIRECT_ATTACK;
                       }
                       else if (color == Const.BLACK) {
                           //if the pawn isn't on the a-file, pawn is threatening its right diagonal
                           if (i%8 != 0)
                               map[i-9] = Const.DIRECT_ATTACK;

                           // if the pawn isn't on the h-file, pawn is threatening its left diagonal
                           if ((i%8) != 7)
                               map[i-7] = Const.DIRECT_ATTACK;
                       }
                       break;

                   case Const.KNIGHT_PIECE:
                       for (int i1 = 0; i1 <8; i1++) {
                           if (i+Const.KNIGHT_MOVES[i1] < 0 // if proposed square goes out of bounds
                                   || i+Const.KNIGHT_MOVES[i1] >=64 // if proposed square goes out of bounds
                                   || Math.abs( (i%8) - ((i+Const.KNIGHT_MOVES[i1]) % 8)) > 2) // if the proposed square wraps around the board horizontally
                               continue;
                           map[i+Const.KNIGHT_MOVES[i1]] = Const.DIRECT_ATTACK; // else, add the square to the threat map
                       }
                       break;

                   case Const.BISHOP_PIECE:
                       for (int i1 = 0; i1 <4; i1++) {
                           int direction = Const.BISHOP_MOVES[i1];
                           int level = Const.DIRECT_ATTACK; //the default value is a direct attack. Going past a different colored piece reduces it to Const.DISCOVERED_ATTACK
                           for (int j = 1; j <9; j++) {
                               int test = LinearThreatCheck(p,direction,color,i,j);
                               // if the proposed square goes out of bounds, end the loop now
                               if (test == Const.TERMINATE)
                                   break;

                               // if the proposed square has a piece of the same color, and there is a direct line of sight to it (no obstructions),
                               // add that square to the threat map and end the loop
                               if (test == Const.SAME_COLOR && level == Const.DIRECT_ATTACK) {
                                   map[i+ j*direction] = Const.DIRECT_ATTACK;
                                   break;
                               }
                               // if the proposed square has a piece of the same color, but there isn't a direct line of sight to it (obstructed),
                               // end the loop
                               else if (test == Const.SAME_COLOR && level == Const.DISCOVERED_ATTACK) {
                                   break;
                               }
                               //if the proposed square has a piece of a different color, and piece has a direct line of sight to it,
                               // add that square to the threat map, and continue the loop with a demoted "discovered attack" level
                               else if (test == Const.DIFFERENT_COLOR && level == Const.DIRECT_ATTACK) {
                                   map[i+ j*direction] = Const.DIRECT_ATTACK;
                                   level = Const.DISCOVERED_ATTACK;
                               }
                               // if the proposed square has a piece of a different color, and the piece doesn't have a direct line of sight to it,
                               // add that square and end the loop
                               else if (test == Const.DIFFERENT_COLOR && level == Const.DISCOVERED_ATTACK) {
                                   map[i+ j*direction] = Const.DISCOVERED_ATTACK;
                                   break;
                               }
                               // else, add the square and continue the loop without demoting the level of threat
                               else if (test == Const.CONTINUE) {
                                   map[i+ j*direction] = level;
                               }
                           }
                       }
                       break;

                   case Const.ROOK_PIECE:
                       for (int i1 = 0; i1 <4; i1++) {
                           int direction = Const.ROOK_MOVES[i1];
                           int level = Const.DIRECT_ATTACK; //the default value is a direct attack. Going past a different colored piece reduces it to Const.DISCOVERED_ATTACK
                           for (int j = 1; j <9; j++) {
                               int test = LinearThreatCheck(p,direction,color,i,j);
                               // if the proposed square goes out of bounds, end the loop
                               if (test == Const.TERMINATE)
                                   break;

                               // if the proposed square has a piece of the same color, and there is a direct line of sight to it (no obstructions),
                               // add that square to the threat map and end the loop
                               if (test == Const.SAME_COLOR && level == Const.DIRECT_ATTACK) {
                                   map[i+ j*direction] = Const.DIRECT_ATTACK;
                                   break;
                               }
                               // if the proposed square has a piece of the same color, but there isn't a direct line of sight to it (obstructed),
                               // end the loop
                               else if (test == Const.SAME_COLOR && level == Const.DISCOVERED_ATTACK) {
                                   break;
                               }
                               //if the proposed square has a piece of a different color, and the piece has a direct line of sight to it,
                               // add that square to the threat map, and continue the loop with a demoted "discovered attack" level
                               else if (test == Const.DIFFERENT_COLOR && level == Const.DIRECT_ATTACK) {
                                   map[i+ j*direction] = Const.DIRECT_ATTACK;
                                   level = Const.DISCOVERED_ATTACK;
                               }

                               // if the proposed square has a piece of a different color, and it doesn't have a direct line of sight to it,
                               // add that square and end the loop
                               else if (test == Const.DIFFERENT_COLOR && level == Const.DISCOVERED_ATTACK) {
                                   map[i+ j*direction] = Const.DISCOVERED_ATTACK;
                                   break;
                               }
                               // else, add the square and continue the loop without demoting the level of threat
                               else if (test == Const.CONTINUE) {
                                   map[i+ j*direction] = level;
                               }
                           }
                       }
                       break;

                   case Const.QUEEN_PIECE:
                       for (int i1 = 0; i1 <8; i1++) {
                           int direction = Const.QUEEN_MOVES[i1];
                           int level = Const.DIRECT_ATTACK; //the default value is a direct attack. Going past a different colored piece reduces it to Const.DISCOVERED_ATTACK
                           for (int j = 1; j <9; j++) {
                               int test = LinearThreatCheck(p,direction,color,i,j);

                               // if the proposed square goes out of bounds, end the loop
                               if (test == Const.TERMINATE)
                                   break;

                               // if the proposed square has a piece of the same color, and there is a direct line of sight to it (no obstructions),
                               // add that square to the threat map and end the loop
                               if (test == Const.SAME_COLOR && level == Const.DIRECT_ATTACK) {
                                   map[i+ j*direction] = Const.DIRECT_ATTACK;
                                   break;
                               }

                               // if the proposed square has a piece of the same color, but there isn't a direct line of sight to it (obstructed),
                               // end the loop
                               else if (test == Const.SAME_COLOR && level == Const.DISCOVERED_ATTACK) {
                                   break;
                               }

                               //if the proposed square has a piece of a different color, and the piece has a direct line of sight to it,
                               // add that square to the threat map, and continue the loop with a demoted "discovered attack" level
                               else if (test == Const.DIFFERENT_COLOR && level == Const.DIRECT_ATTACK) {
                                   map[i+ j*direction] = Const.DIRECT_ATTACK;
                                   level = Const.DISCOVERED_ATTACK;
                               }

                               // if the proposed square has a piece of a different color, and it doesn't have a direct line of sight to it,
                               // add that square and end the loop
                               else if (test == Const.DIFFERENT_COLOR && level == Const.DISCOVERED_ATTACK) {
                                   map[i+ j*direction] = Const.DISCOVERED_ATTACK;
                                   break;
                               }

                               // else, add the square and continue the loop without demoting the level of threat
                               else if (test == Const.CONTINUE) {
                                   map[i+ j*direction] = level;
                               }
                           }
                       }
                       break;

                   // all the squares adjacent to the king are under direct attack
                   case Const.KING_PIECE:
                       for (int i1 = 0; i1 <8; i1++) {
                           int direction = Const.KING_MOVES[i1];
                           if (i+direction < 0 || i+direction >=64) //if the square is in-bounds
                               continue;
                           map[i+direction] = Const.DIRECT_ATTACK;
                       }
               }
           }
       }

       return map;
   }

   // turns 'a' to 0, 'b' to 1, ... , 'h' to 7
   public int columnToNumber(char c) {
       return c - 'a';
   }

   // returns the associated piece given a character representation of the piece
   public Pieces characterToPiece(char c) {
       Pieces piece = new Pieces(Const.EMPTY, Const.NO_COLOR);
       switch (Character.toLowerCase(c)) {
           case 'p':
               piece.piece = Const.PAWN_PIECE;
               break;
           case 'n':
               piece.piece = Const.KNIGHT_PIECE;
               break;
           case 'b':
               piece.piece = Const.BISHOP_PIECE;
               break;
           case 'r':
               piece.piece = Const.ROOK_PIECE;
               break;
           case 'q':
               piece.piece = Const.QUEEN_PIECE;
               break;
           case 'k':
               piece.piece = Const.KING_PIECE;
               break;
       }
       if (Character.isUpperCase(c))
           piece.color = Const.WHITE;
       else
           piece.color = Const.BLACK;

       return piece;
   }

   // returns the character representation of a piece
   // e.g. valueToSymbol(PAWN_PIECE) returns PAWN_SYMBOL ('p')
   public char pieceToSymbol(int piece, int color) {
       char symbol;
       switch (piece) {
           case Const.EMPTY:
               symbol = Const.EMPTY_SYMBOL;
               break;
           case Const.PAWN_PIECE:
               symbol = Const.PAWN_SYMBOL;
               break;
           case Const.KNIGHT_PIECE:
               symbol = Const.KNIGHT_SYMBOL;
               break;
           case Const.BISHOP_PIECE:
               symbol = Const.BISHOP_SYMBOL;
               break;
           case Const.ROOK_PIECE:
               symbol = Const.ROOK_SYMBOL;
               break;
           case Const.QUEEN_PIECE:
               symbol = Const.QUEEN_SYMBOL;
               break;
           case Const.KING_PIECE:
               symbol = Const.KING_SYMBOL;
               break;
           default:
               return (char) -1;
       }
       if (color == Const.WHITE)
           return Character.toUpperCase(symbol);
       else //if color is black
           return Character.toLowerCase(symbol);
   }

   //prints the current board position
   public void printBoard() {
       Pieces currentPiece;
       System.out.println("Board Position:\n\n");
       for (int y = 7; y >=0; y--) {
           for (int x = 0; x <= 7; x++) {
               currentPiece = this.board[y*8 + x];
               System.out.print(pieceToSymbol(currentPiece.piece, currentPiece.color));
               System.out.print(" ");
           }
           System.out.println();
       }
   }

   //reads a fen string and sets the board up according to that fen
   public void readFen(String fen) {

       for (int i = 0; i<64; i++) {
           this.board[i].color = Const.NO_COLOR;
           this.board[i].piece = Const.EMPTY;
       }

       int stringIndex = 0;
       int x, y; /** x represents the file, y represents the column */
       char currentChar;

       /** rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 */

       // goes through the first part of the fen ("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR") and assigns each square on the board its piece
       for (y = 7; y >= 0; y--) {
           for (x = 0; x <= 7; x++) {
               currentChar = fen.charAt(stringIndex);

               if (currentChar == '/') {
                   x--; //this essentially "skips" this character
               }
               else if (Character.isDigit(currentChar))
                   x += Character.getNumericValue(currentChar) - 1;
               else if (Character.isAlphabetic(currentChar))
                   this.board[8*y + x] = characterToPiece(currentChar);

               stringIndex++;
           }
       }

       stringIndex++;
       currentChar = fen.charAt(stringIndex);

       /** whose turn it is */
       if (currentChar == 'w')
           this.turn = Const.WHITE;
       else
           this.turn = Const.BLACK;

       /** castling parameters */
       stringIndex += 2;
       currentChar = fen.charAt(stringIndex);
       if (currentChar == '-') {
           this.whiteKingside =
                   this.blackKingside =
                           this.whiteQueenside =
                                   this.blackQueenside = false;
           stringIndex++;
       }
       else
           // reads the castling part of the fen
           for (currentChar = fen.charAt(stringIndex); Character.isAlphabetic(currentChar); currentChar = fen.charAt(stringIndex+1), stringIndex++) {
               switch (currentChar) {
                   case 'K':
                       whiteKingside = true;
                       break;
                   case 'Q':
                       whiteQueenside = true;
                       break;
                   case 'k':
                       blackKingside = true;
                       break;
                   case 'q':
                       blackQueenside = true;
                       break;
               }
           }

       // reads the enpassant square
       stringIndex++;
       currentChar = fen.charAt(stringIndex);
       if (currentChar == '-')
           this.enPassant = Const.EMPTY;
       else {
           int xCoordinate = columnToNumber(currentChar);
           stringIndex++;
           currentChar = fen.charAt(stringIndex);
           int yCoordinate = Character.getNumericValue(currentChar) - 1;

           this.enPassant = xCoordinate + yCoordinate*8;
       }

       // reads the half move clock
       stringIndex += 2;
       int len = 0;
       while (true) {
           currentChar = fen.charAt(stringIndex);
           if (Character.isDigit(currentChar))
               len++;
           else
               break;

           stringIndex++;
       }
       this.halfMoveClock = 0;
       stringIndex -= len;
       for (int i = len; i > 0; i--, stringIndex++) {
           currentChar = fen.charAt(stringIndex);

           // halfMoveClock = 3 * 10^(2-1) + 1 * 10^(1-1) = 31
           this.halfMoveClock += Character.getNumericValue(currentChar) * Math.pow(10,len-1);
       }


       stringIndex += 1;
       len = fen.length() - stringIndex;

       this.fullMoveClock = 0;
       for (int i = len; i > 0; i--, stringIndex++) {
           currentChar = fen.charAt(stringIndex);

           // halfMoveClock = 3 * 10^(2-1) + 1 * 10^(1-1) = 31
           this.fullMoveClock += Character.getNumericValue(currentChar) * Math.pow(10,i-1);
       }
   }

}
