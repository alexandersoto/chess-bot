package chess.gui;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.util.List;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;

public final class Board extends Canvas implements chess.board.Board<ArrayMove, ArrayBoard> {

    private static final long serialVersionUID = 9083239188755001093L;
	
	private Color colorDarkSquare = Config.colorDarkSquareIdle;
	private Color colorLightSquare = Config.colorLightSquareIdle;
	private Color colorHighlight = Config.colorHighlight;	
	private Color colorBestMove = Config.colorBestMove;

	private boolean isFlipped = false;
	private char pieces[][]; // the board, 8x8 array of chars
	private static final String initialPosition = "rnbqkbnrpppppppp--------------------------------PPPPPPPPRNBQKBNR";
	private int squareSize = 1; // size in pixels of each square

	private Point highlit1 = null, highlit2 = null;
	private boolean playing = false; // mouse disabled when false
	private boolean humanPlaying = false;

	private transient Painter painter;

	// these vars represent a move made by the user that hasn't been
	// acknowledged yet by the server. they are necessary to provide
	// immediate feedback that a move has been entered.
	private boolean isPending = false;

	private Point pendingFrom = null, pendingTo = null;
	
	/** Represents the current best move during the search */
	private Point bestMove1 = null, bestMove2 = null;
	
	/** The width of the line that will be drawn to show the current
	 *  best move as the search progresses */
	private float bestMoveWidth = 3f;
	
	private chess.board.ArrayBoard boardState;
	private GamePanel gamePanel;
		
	public Board(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		pieces = new char[8][8];
		painter = new Painter();

		// initialize board
		newGame();
	}

	// double-buffering code
	private transient Image offscreen;

	private transient Graphics  offscreeng;

	private int offwidth, offheight;

	public void repaint() {
		int w = getSize().width, h = getSize().height;
		if (offscreen == null || w != offwidth || h != offheight) {
			offscreen = createImage(w, h);
			offscreeng = offscreen.getGraphics();
			offwidth = w;
			offheight = h;
		}

		update(offscreeng);
		getGraphics().drawImage(offscreen, 0, 0, this);
	}

	public void startGame() {
		playing = true;
		colorDarkSquare = Config.colorDarkSquareActive;
		colorLightSquare = Config.colorLightSquareActive;
		setHighlights(null);
	}

	public void endGame() {
		playing = false;
		colorDarkSquare = Config.colorDarkSquareIdle;
		colorLightSquare = Config.colorLightSquareIdle;
	}

	/** Reset the board to starting position. */
	public void newGame() {
		int row, col;
		resetStateVariables();
		boardState = ArrayBoard.FACTORY.create().init(chess.board.Board.STARTING_POSITION);	

		for (row = 0; row < 8; row++) {
			for (col = 0; col < 8; col++) {
				pieces[row][col] = initialPosition.charAt(row * 8 + col);
			}
		}
		
		setBestMove(null);
	}

	public void setFlipped(boolean b) {
		if (isFlipped != b) {
			isFlipped = b;
			
			// reverse highlights
			if (highlit1 != null) {
				highlit1 = new Point(7 - highlit1.x, 7 - highlit1.y);
			}
			if (highlit2 != null) {
				highlit2 = new Point(7 - highlit2.x, 7 - highlit2.y);
			}
		}
	}

	public void paint(Graphics g) {
		int row, col;

		// calculate squareSize
		squareSize = getSize().width;
		if (getSize().height < squareSize) {
			squareSize = getSize().height;
		}
		squareSize /= 8;

		// draw even squares
		g.setColor(colorLightSquare);
		for (row = 0; row < 8; row++) {
			for (col = row % 2; col < 8; col += 2) {
				g.fillRect(col * squareSize, row * squareSize, squareSize,
						squareSize);
			}
		}
		// draw odd squares
		g.setColor(colorDarkSquare);
		for (row = 0; row < 8; row++) {
			for (col = (row + 1) % 2; col < 8; col += 2) {
				g.fillRect(col * squareSize, row * squareSize, squareSize,
						squareSize);
			}
		}
		
		// draw highlights
		g.setColor(colorHighlight);
		Graphics2D g2 = (Graphics2D) g;
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(3));
		if (highlit1 != null) {
			g.drawRect(highlit1.x * squareSize, highlit1.y * squareSize,
					squareSize - 1, squareSize - 1);
		}
		if (highlit2 != null) {
			g.drawRect(highlit2.x * squareSize, highlit2.y * squareSize,
					squareSize - 1, squareSize - 1);
		}
		g2.setStroke(oldStroke);		

		// draw the best move at this point in the search
		if(bestMove1 != null && bestMove2 != null) {
			g.setColor(colorBestMove);
			// Can only set the stroke (line thickness) with Graphics2D
			if(g instanceof Graphics2D) {
				Graphics2D g2d = ((Graphics2D)g);
				Stroke prevStroke = g2d.getStroke();
				g2d.setStroke(new BasicStroke(bestMoveWidth,
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
				
				g.drawLine(bestMove1.x, bestMove1.y, bestMove2.x, bestMove2.y);
				
				g2d.setStroke(prevStroke);
			}
			// Otherwise draw a line with the default width
			else {
				g.drawLine(bestMove1.x, bestMove1.y, bestMove2.x, bestMove2.y);
			}
		}

		// draw pieces
		for (row = 0; row < 8; row++) {
			for (col = 0; col < 8; col++) {
				painter.drawPiece(g, getPiece(col, row), squareSize, col
						* squareSize, row * squareSize);
			}
		}

		// draw pending move, i.e. made by user but not confirmed by server
		if (isPending) {
			// clear square piece was moved from
			if ((pendingFrom.x + pendingFrom.y) % 2 == 0) // even square,
				// light
				g.setColor(colorLightSquare);
			else
				g.setColor(colorDarkSquare);
			g.fillRect(pendingFrom.x * squareSize, pendingFrom.y * squareSize,
					squareSize, squareSize);

			// clear square piece was moved to, if necessary
			if (getPiece(pendingTo) != '-') {
				if ((pendingTo.x + pendingTo.y) % 2 == 0) // even square,
					// light
					g.setColor(colorLightSquare);
				else
					g.setColor(colorDarkSquare);
				g.fillRect(pendingTo.x * squareSize, pendingTo.y * squareSize,
						squareSize, squareSize);
			}

			// draw piece at square it was moved to
			painter.drawPiece(g, getPiece(pendingFrom), squareSize, pendingTo.x
					* squareSize, pendingTo.y * squareSize);
		}
	}

	public char getPiece(int col, int row) {
		if (isFlipped) {
			return pieces[7 - row][7 - col];
		} else {
			return pieces[row][col];
		}
	}

	public char getPiece(Point p) {
		return getPiece(p.x, p.y);
	}

	public void setPiece(int col, int row, char p) {
		if (isFlipped) {
			pieces[7 - row][7 - col] = p;
		} else {
			pieces[row][col] = p;
		}
	}

	public void setPiece(Point pt, char pc) {
		setPiece(pt.x, pt.y, pc);
	}

	private boolean shortCastleOK, longCastleOK;

	private int enPassantFile;

	private void resetStateVariables() {
		shortCastleOK = true;
		longCastleOK = true;
		enPassantFile = -1;
		isFlipped = false;
		highlit1 = null;
		highlit2 = null;
	}

	// keeps track of en passant file and castles
	// this is called *before* the move is actually made
	private void setStateVariables(String smith) {
		String algFrom = smith.substring(0, 2);
		Point bdFrom = algebraicToBoard(algFrom);
		String algTo = smith.substring(2, 4);
		Point bdTo = algebraicToBoard(algTo);
		char piece = getPiece(bdFrom);

		enPassantFile = -1;
		if (!isFlipped) {
			// I am white, care about my rook/king moves, opp's pawn pushes
			if (piece == 'p') {
				if (bdFrom.y == 1 && bdTo.y == 3) 
					enPassantFile = bdTo.x;
			}
			if (piece == 'K') {
				shortCastleOK = false;
				longCastleOK = false;
			}
			if (piece == 'R') {
				if ("a1".equals(algFrom))
					longCastleOK = false;
				if ("h1".equals(algFrom))
					shortCastleOK = false;
			}
		} else {
			// I am black
			if (piece == 'P') {
				if (bdFrom.y == 1 && bdTo.y == 3)
					enPassantFile = bdTo.x;
			}
			if (piece == 'k') {
				shortCastleOK = false;
				longCastleOK = false;
			}
			if (piece == 'r') {
				if ("a8".equals(algFrom))
					longCastleOK = false;
				if ("h8".equals(algFrom))
					shortCastleOK = false;
			}
		}
	}

	// performs a simplistic, liberal check to see if a move is legal
	// does not determine Check discovered check

	// NOTE!!! assumes check is being done for lower player, and that
	// board is appropriately flipped. Do not use for opponent's moves.
	public boolean isLegalMove(Point bdFrom, Point bdTo) {
		char piece = getPiece(bdFrom);
		boolean white = isWhite(piece);
		int dx = bdTo.x - bdFrom.x;
		int dy = bdTo.y - bdFrom.y;
		int absdx = (dx > 0) ? dx : -dx;
		int absdy = (dy > 0) ? dy : -dy;
		char captured;

		// first, eliminate the obvious
		if (white == isFlipped)
			return false; // grabbed opponent's piece

		captured = getPiece(bdTo);
		if (captured != '-' && white == isWhite(captured))
			return false; // capture same color

		piece = Character.toLowerCase(piece);

		switch (piece) {
		case '-':
			return false;
		case 'n':
			if ((absdx == 2 && absdy == 1) || (absdx == 1 && absdy == 2))
				return true;
			else
				return false;
		case 'r':
			if (dx != 0 && dy != 0)
				return false;
			return clearPath(bdFrom, bdTo);
		case 'b':
			if (absdx != absdy)
				return false;
			return clearPath(bdFrom, bdTo);
		case 'q':
			if (dx == 0 || dy == 0 || absdx == absdy)
				return clearPath(bdFrom, bdTo);
			else
				return false;
		case 'k':
			if (absdx <= 1 && absdy <= 1)
				return true;
			if (absdx == 2 && dy == 0 && bdFrom.y == 7) {
				// check for castle
				if (!isFlipped) {
					// white castle
					if (bdFrom.x != 4)
						return false;
					if (dx == -2) {
						// long
						return (longCastleOK && getPiece(0, 7) == 'R' && clearPath(
								bdFrom, new Point(1, 7)));
					} else {
						// short
						return (shortCastleOK && getPiece(7, 7) == 'R' && clearPath(
								bdFrom, bdTo));
					}
				} else {
					// black castle
					if (bdFrom.x != 3)
						return false;
					if (dx == -2) {
						// short
						return (shortCastleOK && getPiece(0, 7) == 'r' && clearPath(
								bdFrom, bdTo));
					} else {
						// long
						return (longCastleOK && getPiece(7, 7) == 'r' && clearPath(
								bdFrom, new Point(6, 7)));
					}
				}
			}
			return false;
		case 'p':
			if (dx == 0 && dy == -1) {
				if (captured == '-')
					return true; // normal push
				else
					return false;
			}
			if (dx == 0 && dy == -2 && bdFrom.y == 6)
				return clearPath(bdFrom, bdTo); // double push
			if (absdx == 1 && dy == -1) {
				captured = getPiece(bdTo);
				if (captured != '-')
					return true; // simple capture
				// *** check for en passant ***
				if (bdFrom.y != 3)
					return false;
				if (enPassantFile != bdTo.x)
					return false;
				captured = getPiece(bdTo.x, bdFrom.y);
				if (white == isWhite(captured))
					return false;
				if (captured == 'p' || captured == 'P') {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private boolean clearPath(Point from, Point to) {
		int x, y;
		int dx = signum(to.x - from.x);
		int dy = signum(to.y - from.y);
		for (x = from.x + dx, y = from.y + dy; x != to.x || y != to.y; x += dx, y += dy) {
			if (getPiece(x, y) != '-')
				return false;
		}
		return true;
	}

	private int signum(int n) {
		return (n == 0) ? 0 : ((n > 0) ? 1 : -1);
	}

	public void setHighlights(String smith) {
		if (smith == null) {
			highlit1 = null;
			highlit2 = null;
			return;
		}

		// get the 2 highlight squares from the smith notation
		highlit1 = algebraicToBoard(smith.substring(0, 2));
		highlit2 = algebraicToBoard(smith.substring(2, 4));
	}

	public void setHighlights(Point bd1, Point bd2) {
		highlit1 = bd1;
		highlit2 = bd2;
	}

	// coordinate transforms
	/*
	 * defs: * "Screen" coords are pixels (relative to upper-left of board), as
	 * might * be returned by a mouse event. * "Board" coords are (row, column)
	 * pairs starting from upper-left. They * do *not* include the effects of
	 * "flipping". * "Algebraic" coords are 2-character chess notation, e.g.
	 * "e2", "d7". * They *do* include "flipping", i.e. "e2" is always white's
	 * king pawn.
	 */
	public Point algebraicToBoard(String s) {
		char c1 = s.charAt(0);
		char c2 = s.charAt(1);

		if (isFlipped) {
			return new Point(7 - (int) (c1 - 'a'), (int) (c2 - '1'));
		}
		return new Point((int) (c1 - 'a'), 7 - (int) (c2 - '1'));
	}

	public String boardToAlgebraic(Point p) {
		char s[] = new char[2];

		if (isFlipped) {
			s[0] = (char) ('a' + 7 - p.x);
			s[1] = (char) ('1' + p.y);
		} else {
			s[0] = (char) ('a' + p.x);
			s[1] = (char) ('1' + 7 - p.y);
		}
		return new String(s);
	}

	public Point screenToBoard(int x, int y) {
		return new Point(x / squareSize, y / squareSize);
	}

	public Point screenToBoard(Point p) {
		return screenToBoard(p.x, p.y);
	}

	public String screenToAlgebraic(int x, int y) {
		return boardToAlgebraic(screenToBoard(x, y));
	}

	public String screenToAlgebraic(Point p) {
		return screenToAlgebraic(p.x, p.y);
	}
	
	/**
	 * Algebraic coords to screen coords
	 * @param s
	 * @return the coordiantes of the center of the specified square
	 */
	public Point algebraicToScreen(String s) {
		return boardToScreen(algebraicToBoard(s));
	}
	
	/**
	 * Board coords to screen coords
	 * @param p
	 * @return the coordiantes of the center of the specified square
	 */
	public Point boardToScreen(Point p) {
		return new Point(p.x * squareSize + (squareSize / 2), 
							p.y * squareSize + (squareSize / 2));
	}

	public void setPending(Point p1, Point p2) {
		isPending = true;
		pendingFrom = p1;
		pendingTo = p2;
	}

	public void clearPending() {
		isPending = false;
	}

	public void setHumanPlaying(boolean humanPlaying) {
		this.humanPlaying = humanPlaying;
	}
	
	private Point mouseFrom = null, mouseTo = null; // in Board coords

	// press the mouse button
	public boolean mouseDown(Event evt, int x, int y) {
		if (humanPlaying) {
			if (!playing) {
				return true;
			}
			mouseTo = null;
			if (x < 8 * squareSize && y < 8 * squareSize) {
				mouseFrom = screenToBoard(x, y);
				if (getPiece(mouseFrom) == '-') { // clicked on empty square
					mouseFrom = null;
					return true;
				}
				highlit1 = mouseFrom;
				highlit2 = null;
			} else {
				mouseFrom = null;
			}
			repaint();
		}
		return true;
	}

	public boolean mouseDrag(Event evt, int x, int y) {
		if (humanPlaying) {
			if (!playing || mouseFrom == null)
				return true;
			if (x < 0 || y < 0 || x >= 8 * squareSize || y >= 8 * squareSize)
				return true;
			Point p2 = screenToBoard(x, y);
			if (!p2.equals(mouseTo)) {
				mouseTo = p2;
				if (boardState.isLegalMove(getMove(mouseFrom, mouseTo))) {
					colorHighlight = Config.colorLegalMove;
				} else {
					colorHighlight = Config.colorIllegalMove;
				}
				highlit2 = mouseTo;
			}
			repaint();
		}
		return true;
	}

	// leave the mouse button
	public boolean mouseUp(Event evt, int x, int y) {
		if (humanPlaying) {
			if (!playing || mouseFrom == null) {
				return true;
			}
			setHighlights(null);
			if (x < 8 * squareSize && y < 8 * squareSize) {
				String algFrom = boardToAlgebraic(mouseFrom);
				String algTo = screenToAlgebraic(x, y);
				if (algFrom.equals(algTo)) {
					mouseFrom = null;
				}
				else {
					Point p1 = mouseFrom;
					Point p2 = screenToBoard(x, y);

					// check for legal move
					if (!isLegalMove(p1, p2)) {
						repaint();
						mouseFrom = null;
						return true;
					}
					
					ArrayMove move = getMove(p1, p2);
					if (!boardState.isLegalMove(move)) {
						repaint();						
						mouseFrom = null;
						return true;
					}
					setPending(p1, p2);
					setHighlights(p1, p2);
					gamePanel.move(move);
				}
			}
			repaint();
		}
		return true;
	}

	public void doSmithMove(String smith) {		
		// assumes move is generated by server, i.e. legal
		Point from, to;
		char piece;
		int len = smith.length();

		if (len < 4) {
			return; // malformed move
		}

		colorHighlight = Config.colorLegalMove;
		setHighlights(smith);
		setStateVariables(smith);
		
		// When a new move is applied, no longer need to show the best move
		// found during the search
		setBestMove(null);

		from = algebraicToBoard(smith.substring(0, 2));
		to = algebraicToBoard(smith.substring(2, 4));
		piece = getPiece(from);

		char end = smith.charAt(len - 1);
		if (len == 4 || "pnbrqk".indexOf(end) != -1) {
			// simple move or simple capture
			setPiece(from, '-');
			setPiece(to, piece);
			return;
		}

		// check for promotions
		if ("QNBR".indexOf(end) != -1) {
			if (Character.isUpperCase(piece))
				piece = end;
			else
				piece = Character.toLowerCase(end);
			setPiece(from, '-');
			setPiece(to, piece);
			return;
		}

		// check for short castle
		if (end == 'c') {
			setPiece(from, '-');
			setPiece(to, piece);
			if (from.x == 3) {
				// leftside
				piece = getPiece(0, from.y);
				setPiece(0, from.y, '-');
				setPiece(2, from.y, piece);
			} else {
				// rightside
				piece = getPiece(7, from.y);
				setPiece(7, from.y, '-');
				setPiece(5, from.y, piece);
			}
			return;
		}

		// check for long castle
		if (end == 'C') {
			setPiece(from, '-');
			setPiece(to, piece);
			if (from.x == 4) {
				// leftside
				piece = getPiece(0, from.y);
				setPiece(0, from.y, '-');
				setPiece(3, from.y, piece);
			} else {
				// rightside
				piece = getPiece(7, from.y);
				setPiece(7, from.y, '-');
				setPiece(4, from.y, piece);
			}
			return;
		}

		// check for En Passant
		if (end == 'E') {
			setPiece(from, '-');
			setPiece(to, piece);
			// capture
			setPiece(to.x, from.y, '-');
			return;
		}

		// shouldn't get here
		System.out.println("bogus smith move: " + smith);
	}
	
	// This function assumes the player requesting will always be the lower one
	public ArrayMove getMove(Point p1, Point p2) {		
		String algFrom = boardToAlgebraic(p1);
		String algTo = boardToAlgebraic(p2);
		String move = algFrom + algTo;
		
		// Check for enpasssant (append 'E')
		// Moving a pawn on the same file that a previous pawn double pushed
		char piece1 = Character.toUpperCase(getPiece(p1));
		if (piece1 == 'P' && enPassantFile == p2.x) {

	    	// Check if we're moving to a different file left or right
	    	if ((p2.x == p1.x + 1) || (p2.x == p1.x - 1)) {
	    		
	    		// We're behind the double pushed pawn, so capture
	    		if (p2.y == 2) {
	    	    	move += 'E';
	    		}
	    	}
	    }
	    				
		// Check for pawn promotion
		if ((p2.y == 0 || p2.y == 7)
			&& Character.toUpperCase(getPiece(p1)) == 'P') {			
			move += Character.toString(gamePanel.getPromote()).toUpperCase();			
		}	
		
		return boardState.createMoveFromString(move);		
	}

	/*
	 * public void undoSmithMove(String smith) { // assumes move is generated by
	 * server, i.e. legal Point from, to; char piece; int len = smith.length();
	 * 
	 * if (len < 4) return; // malformed move
	 * 
	 * from = algebraicToBoard(smith.substring(0, 2) ); to =
	 * algebraicToBoard(smith.substring(2, 4) ); piece = getPiece(to);
	 * 
	 * char end = smith.charAt(len - 1); if (len == 4) { // simple move
	 * setPiece(to, '-'); setPiece(from, piece); return; } if
	 * ("pnbrqk".indexOf(end) != -1) { // simple capture setPiece(from, piece);
	 * if (Character.isLowerCase(piece) ) setPiece(to,
	 * Character.toUpperCase(end) ); else setPiece(to, end); } // check for
	 * promotions if ("QNBR".indexOf(end) != -1) { if
	 * (Character.isLowerCase(piece) ) piece = 'p'; else piece = 'P';
	 * setPiece(to, '-'); setPiece(from, piece); return; } // check for short
	 * castle if (end == 'c') { setPiece(to, '-'); setPiece(from, piece); if
	 * (from.x == 3) { // leftside piece = getPiece(2, from.y); setPiece(2,
	 * from.y, '-'); setPiece(0, from.y, piece); } else { // rightside piece =
	 * getPiece(5, from.y); setPiece(5, from.y, '-'); setPiece(7, from.y,
	 * piece); } return; } // check for long castle if (end == 'C') {
	 * setPiece(to, '-'); setPiece(from, piece); if (from.x == 4) { // leftside
	 * piece = getPiece(3, from.y); setPiece(3, from.y, '-'); setPiece(0,
	 * from.y, piece); } else { // rightside piece = getPiece(4, from.y);
	 * setPiece(4, from.y, '-'); setPiece(7, from.y, piece); } return; } //
	 * check for En Passant if (end == 'E') { setPiece(to, '-'); setPiece(from,
	 * piece); // un-capture if (piece == 'p') piece = 'P'; else piece = 'p';
	 * setPiece(to.x, from.y, piece); return; } // shouldn't get here
	 * System.out.println("bogus smith move: " + smith); }
	 */

	public static boolean isWhite(char piece) {
		return Character.isUpperCase(piece);
	}

	public static boolean isBlack(char piece) {
		return Character.isLowerCase(piece);
	}

	public Dimension preferredSize() {
		return new Dimension(128, 128);
	}

	public Dimension minimumSize() {
		return new Dimension(80, 80);
	}
	
	public void setBestMove(String smithBestMove) {
		if(smithBestMove == null) {
			bestMove1 = null;
			bestMove2 = null;
		}
		else {
			
			// Get the best move squares from the smith notation
			bestMove1 = algebraicToScreen(smithBestMove.substring(0, 2));
			bestMove2 = algebraicToScreen(smithBestMove.substring(2, 4));
		}
	}

	// These methods allow this call to implement Board	
	@Override
	public ArrayBoard create() {
		return boardState.create();
	}

	@Override
	public ArrayBoard copy() {
		return boardState.copy();
	}

	@Override
	public ArrayBoard init(String fen) {
		return boardState.init(fen);
	}

	@Override
	public List<ArrayMove> generateMoves() {
		return boardState.generateMoves();
	}

	public void applyMove(ArrayMove move) {
		clearPending();
		boardState.applyMove(move);
		doSmithMove(move.smithString());
	}

	public ArrayMove createMoveFromString(String move) {
		return boardState.createMoveFromString(move);
	}

	@Override
	public List<ArrayMove> generatePseudoMoves() {
		return boardState.generatePseudoMoves();
	}

	public boolean isLegalPseudoMove(ArrayMove move) {
		return isLegalPseudoMove(move);
	}

	public boolean isLegalMove(ArrayMove move) {
		return boardState.isLegalMove(move);
	}

	@Override
	public void undoMove() {
		boardState.undoMove();
	}

	@Override
	public boolean inCheck() {
		return boardState.inCheck();
	}

	@Override
	public int toPlay() {
		return boardState.toPlay();
	}

	@Override
	public int plyCount() {
		return boardState.plyCount();
	}

	@Override
	public long signature() {
		return boardState.signature();
	}

	@Override
	public String fen() {
		return boardState.fen();
	}
}