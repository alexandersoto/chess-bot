package chess.gui;
//package com.chessclub.easychess;

/* 2/23/98 - Modified to retain native-size versions.
 */

import java.awt.Color;
import java.awt.Graphics;

public final class Painter {
	// size at which the polygons where designed
	private final static int native_size = 75;

	// pawn
	private static final int pawnOutlineNative[][] = { { 13, 63 }, { 13, 54 },
			{ 18, 46 }, { 30, 39 }, { 25, 35 }, { 25, 27 }, { 29, 23 },
			{ 32, 22 }, { 29, 18 }, { 29, 13 }, { 33, 9 }, { 36, 8 },
			{ 39, 8 }, { 42, 9 }, { 46, 13 }, { 46, 18 }, { 43, 22 },
			{ 46, 23 }, { 50, 27 }, { 50, 35 }, { 45, 39 }, { 57, 46 },
			{ 62, 54 }, { 62, 63 }, { 13, 63 } };

	// rook
	private static final int rookOutlineNative[][] = { { 11, 69 }, { 11, 65 },
			{ 22, 54 }, { 22, 24 }, { 15, 17 }, { 15, 6 }, { 25, 6 },
			{ 25, 12 }, { 32, 12 }, { 32, 6 }, { 43, 6 }, { 43, 12 },
			{ 50, 12 }, { 50, 6 }, { 60, 6 }, { 60, 17 }, { 53, 24 },
			{ 53, 54 }, { 64, 65 }, { 64, 69 }, { 11, 69 } };

	private static final int rookHighlightNative[][] = { { 22, 54 },
			{ 22, 24 }, { 53, 24 }, { 53, 54 }, { 22, 54 } };

	// knight
	private static final int knightOutlineNative[][] = { { 21, 69 },
			{ 68, 69 }, { 68, 55 }, { 62, 29 }, { 53, 19 }, { 43, 14 },
			{ 37, 6 }, { 35, 6 }, { 36, 14 }, { 28, 6 }, { 26, 6 }, { 29, 14 },
			{ 25, 13 }, { 18, 19 }, { 5, 46 }, { 12, 54 }, { 20, 46 },
			{ 15, 53 }, { 17, 55 }, { 34, 40 }, { 37, 45 }, { 21, 62 },
			{ 21, 69 } };

	private static final int knightManeNative[][] = { { 64, 65 }, { 57, 31 },
			{ 45, 20 }, { 42, 20 }, { 54, 31 }, { 61, 65 }, { 64, 65 } };

	private static final int knightEyeNative[][] = { { 19, 29 }, { 23, 26 },
			{ 24, 22 }, { 20, 25 }, { 19, 29 } };

	private static final int knightNoseNative[][] = { { 8, 46 }, { 10, 48 },
			{ 13, 43 }, { 11, 41 }, { 8, 46 } };

	// bishop
	private static final int bishopOutlineNative[][] = { { 6, 71 }, { 9, 65 },
			{ 28, 65 }, { 22, 38 }, { 22, 29 }, { 24, 25 }, { 30, 19 },
			{ 35, 15 }, { 33, 10 }, { 35, 6 }, { 40, 6 }, { 42, 10 },
			{ 40, 15 }, { 45, 19 }, { 51, 25 }, { 53, 29 }, { 53, 38 },
			{ 47, 65 }, { 66, 65 }, { 69, 71 }, { 6, 71 } };

	private static final int bishopCrossNative[][] = { { 36, 28 }, { 36, 31 },
			{ 33, 31 }, { 33, 34 }, { 36, 34 }, { 36, 40 }, { 39, 40 },
			{ 39, 34 }, { 42, 34 }, { 42, 31 }, { 39, 31 }, { 39, 28 },
			{ 36, 28 } };

	private static final int bishopBandNative[][] = { { 27, 60 }, { 35, 59 },
			{ 40, 59 }, { 48, 60 }, { 50, 51 }, { 41, 50 }, { 34, 50 },
			{ 25, 51 }, { 27, 60 } };

	// queen
	private static final int queenOutlineNative[][] = { { 27, 69 }, { 17, 68 },
			{ 17, 62 }, { 16, 52 }, { 8, 22 }, { 5, 19 }, { 5, 15 }, { 8, 12 },
			{ 12, 12 }, { 15, 15 }, { 15, 19 }, { 12, 22 }, { 22, 39 },
			{ 26, 16 }, { 23, 13 }, { 23, 9 }, { 26, 6 }, { 30, 6 }, { 33, 9 },
			{ 33, 13 }, { 30, 16 }, { 37, 35 }, { 38, 35 }, { 45, 16 },
			{ 42, 13 }, { 42, 9 }, { 45, 6 }, { 49, 6 }, { 52, 9 }, { 52, 13 },
			{ 49, 16 }, { 53, 39 }, { 63, 22 }, { 60, 19 }, { 60, 15 },
			{ 63, 12 }, { 67, 12 }, { 70, 15 }, { 70, 19 }, { 67, 22 },
			{ 59, 52 }, { 58, 62 }, { 58, 68 }, { 48, 69 }, { 27, 69 } };

	private static final int queenBandNative[][] = { { 29, 60 }, { 17, 62 },
			{ 16, 52 }, { 29, 50 }, { 46, 50 }, { 59, 52 }, { 58, 62 },
			{ 46, 60 }, { 29, 60 } };

	// king
	private static final int kingOutlineNative[][] = { { 27, 69 }, { 21, 66 },
			{ 16, 47 }, { 11, 45 }, { 6, 40 }, { 4, 35 }, { 4, 32 }, { 6, 27 },
			{ 11, 22 }, { 16, 20 }, { 19, 20 }, { 24, 22 }, { 28, 26 },
			{ 30, 26 }, { 30, 23 }, { 36, 17 }, { 36, 13 }, { 33, 13 },
			{ 33, 10 }, { 36, 10 }, { 36, 6 }, { 39, 6 }, { 39, 10 },
			{ 42, 10 }, { 42, 13 }, { 39, 13 }, { 39, 17 }, { 45, 23 },
			{ 45, 26 }, { 47, 26 }, { 51, 22 }, { 56, 20 }, { 59, 20 },
			{ 64, 22 }, { 69, 27 }, { 71, 32 }, { 71, 35 }, { 69, 40 },
			{ 64, 45 }, { 59, 47 }, { 54, 66 }, { 48, 69 }, { 27, 69 } };

	private static final int kingJewelNative[][] = { { 36, 23 }, { 34, 27 },
			{ 36, 31 }, { 39, 31 }, { 41, 27 }, { 39, 23 }, { 36, 23 } };

	private static final int kingLeftLobeNative[][] = { { 15, 25 }, { 21, 25 },
			{ 34, 38 }, { 34, 44 }, { 20, 45 }, { 15, 42 }, { 10, 36 },
			{ 10, 31 }, { 15, 25 } };

	private static final int kingRightLobeNative[][] = { { 60, 25 },
			{ 54, 25 }, { 41, 38 }, { 41, 44 }, { 55, 45 }, { 60, 42 },
			{ 65, 36 }, { 65, 31 }, { 60, 25 } };

	private static final int kingBandNative[][] = { { 19, 57 }, { 33, 54 },
			{ 42, 54 }, { 56, 57 }, { 54, 63 }, { 40, 60 }, { 35, 60 },
			{ 21, 63 }, { 18, 57 } };

	private int[][] bishopBand, bishopCross, bishopOutline, kingBand,
			kingJewel, kingLeftLobe, kingOutline, kingRightLobe, knightEye,
			knightMane, knightNose, knightOutline, pawnOutline, queenBand,
			queenOutline, rookHighlight, rookOutline;

	public Painter() {
		// initialize local versions of polygons
		bishopBand = new int[bishopBandNative.length][2];
		bishopCross = new int[bishopCrossNative.length][2];
		bishopOutline = new int[bishopOutlineNative.length][2];

		kingBand = new int[kingBandNative.length][2];
		kingJewel = new int[kingJewelNative.length][2];
		kingLeftLobe = new int[kingLeftLobeNative.length][2];
		kingOutline = new int[kingOutlineNative.length][2];
		kingRightLobe = new int[kingRightLobeNative.length][2];

		knightEye = new int[knightEyeNative.length][2];
		knightMane = new int[knightManeNative.length][2];
		knightNose = new int[knightNoseNative.length][2];
		knightOutline = new int[knightOutlineNative.length][2];

		pawnOutline = new int[pawnOutlineNative.length][2];

		queenBand = new int[queenBandNative.length][2];
		queenOutline = new int[queenOutlineNative.length][2];

		rookHighlight = new int[rookHighlightNative.length][2];
		rookOutline = new int[rookOutlineNative.length][2];

		copy(bishopBandNative, bishopBand);
		copy(bishopCrossNative, bishopCross);
		copy(bishopOutlineNative, bishopOutline);

		copy(kingBandNative, kingBand);
		copy(kingJewelNative, kingJewel);
		copy(kingLeftLobeNative, kingLeftLobe);
		copy(kingOutlineNative, kingOutline);
		copy(kingRightLobeNative, kingRightLobe);

		copy(knightEyeNative, knightEye);
		copy(knightManeNative, knightMane);
		copy(knightNoseNative, knightNose);
		copy(knightOutlineNative, knightOutline);

		copy(pawnOutlineNative, pawnOutline);

		copy(queenBandNative, queenBand);
		copy(queenOutlineNative, queenOutline);

		copy(rookHighlightNative, rookHighlight);
		copy(rookOutlineNative, rookOutline);
	}

	private void copy(int[][] src, int[][] dest) {
		int i;
		for (i = 0; i < src.length; i++) {
			dest[i][0] = src[i][0];
			dest[i][1] = src[i][1];
		}
	}

	public void drawPawn(Graphics g, Color piece, Color outline, int scale,
			int x, int y) {
		drawScaledPoly(g, piece, outline, scale, x, y, pawnOutline);
	}

	public void drawRook(Graphics g, Color piece, Color outline, int scale,
			int x, int y) {
		drawScaledPoly(g, piece, outline, scale, x, y, rookOutline);
		drawScaledPoly(g, piece, outline, scale, x, y, rookHighlight);
	}

	public void drawKnight(Graphics g, Color piece, Color outline, int scale,
			int x, int y) {
		drawScaledPoly(g, piece, outline, scale, x, y, knightOutline);
		drawScaledPoly(g, piece, outline, scale, x, y, knightMane);
		drawScaledPoly(g, piece, outline, scale, x, y, knightEye);
		drawScaledPoly(g, piece, outline, scale, x, y, knightNose);
	}

	public void drawBishop(Graphics g, Color piece, Color outline, int scale,
			int x, int y) {
		drawScaledPoly(g, piece, outline, scale, x, y, bishopOutline);
		drawScaledPoly(g, piece, outline, scale, x, y, bishopCross);
		drawScaledPoly(g, piece, outline, scale, x, y, bishopBand);
	}

	public void drawQueen(Graphics g, Color piece, Color outline, int scale,
			int x, int y) {
		drawScaledPoly(g, piece, outline, scale, x, y, queenOutline);
		drawScaledPoly(g, piece, outline, scale, x, y, queenBand);
	}

	public void drawKing(Graphics g, Color piece, Color outline, int scale,
			int x, int y) {
		drawScaledPoly(g, piece, outline, scale, x, y, kingOutline);
		drawScaledPoly(g, piece, outline, scale, x, y, kingJewel);
		drawScaledPoly(g, piece, outline, scale, x, y, kingLeftLobe);
		drawScaledPoly(g, piece, outline, scale, x, y, kingRightLobe);
		drawScaledPoly(g, piece, outline, scale, x, y, kingBand);
	}

	private int current_scale = 75;

	public void rescalePolygons(int newsize) {
		double factor = (double) newsize / native_size;
		rescalePolygon(pawnOutlineNative, pawnOutline, factor);
		rescalePolygon(rookOutlineNative, rookOutline, factor);
		rescalePolygon(rookHighlightNative, rookHighlight, factor);
		rescalePolygon(knightOutlineNative, knightOutline, factor);
		rescalePolygon(knightManeNative, knightMane, factor);
		rescalePolygon(knightEyeNative, knightEye, factor);
		rescalePolygon(knightNoseNative, knightNose, factor);
		rescalePolygon(bishopOutlineNative, bishopOutline, factor);
		rescalePolygon(bishopCrossNative, bishopCross, factor);
		rescalePolygon(bishopBandNative, bishopBand, factor);
		rescalePolygon(queenOutlineNative, queenOutline, factor);
		rescalePolygon(queenBandNative, queenBand, factor);
		rescalePolygon(kingOutlineNative, kingOutline, factor);
		rescalePolygon(kingJewelNative, kingJewel, factor);
		rescalePolygon(kingLeftLobeNative, kingLeftLobe, factor);
		rescalePolygon(kingRightLobeNative, kingRightLobe, factor);
		rescalePolygon(kingBandNative, kingBand, factor);
		current_scale = newsize;
	}

	public static void rescalePolygon(int[][] nativepoly, int[][] poly,
			double factor) {
		for (int i = 0; i < poly.length; i++) {
			poly[i][0] = (int) (factor * nativepoly[i][0]);
			poly[i][1] = (int) (factor * nativepoly[i][1]);
		}
	}

	// draw part of a piece from polygon info
	public void drawScaledPoly(Graphics g, Color fill, Color outline, int size,
			int x, int y, int src[][]) {

		if (size != current_scale)
			rescalePolygons(size);

		int xp[] = new int[src.length];
		int yp[] = new int[src.length];
		for (int i = 0; i < src.length; i++) {
			xp[i] = x + src[i][0];
			yp[i] = y + src[i][1];
		}

		g.setColor(fill);
		g.fillPolygon(xp, yp, src.length);
		g.setColor(outline);
		g.drawPolygon(xp, yp, src.length);
	}

	public void drawPiece(Graphics g, char piece, int scale, int x, int y) {

		Color piececolor, outlinecolor;

		if (piece == '-')
			return; // blank square

		if (Board.isWhite(piece)) {
			piececolor = Config.colorWhitePiece;
			outlinecolor = Config.colorWhiteOutline;
		} else {
			piececolor = Config.colorBlackPiece;
			outlinecolor = Config.colorBlackOutline;
		}

		switch (piece) {
		case 'p':
		case 'P':
			drawPawn(g, piececolor, outlinecolor, scale, x, y);
			break;
		case 'r':
		case 'R':
			drawRook(g, piececolor, outlinecolor, scale, x, y);
			break;
		case 'n':
		case 'N':
			drawKnight(g, piececolor, outlinecolor, scale, x, y);
			break;
		case 'b':
		case 'B':
			drawBishop(g, piececolor, outlinecolor, scale, x, y);
			break;
		case 'q':
		case 'Q':
			drawQueen(g, piececolor, outlinecolor, scale, x, y);
			break;
		case 'k':
		case 'K':
			drawKing(g, piececolor, outlinecolor, scale, x, y);
			break;
		default:
			// error, shouldn't get here
			System.out.println("drawPiece: unknown piece:" + piece);
		}
	}

}