package chess.gui;
//package com.chessclub.easychess;

import java.awt.Color;

//This is the class for containing various global variables and settings

public final class Config {

	public static final boolean debug = false;
	
	// msec between clock updates
	public static final int clockSleepTime = 200;
	
	// msec between best move updates
	public static final int delayBetweenBestMoves = 500;
	
	public final static Color colorWhitePiece = Color.white;
	public final static Color colorWhiteOutline = Color.black;
	public final static Color colorBlackPiece = Color.black;
	public final static Color colorBlackOutline = Color.white;
	public final static Color colorBackground = Color.darkGray;
	public final static Color colorGame = Color.gray;
	public final static Color colorDarkSquareActive = Color.blue;
	public final static Color colorDarkSquareIdle = Color.darkGray;
	public final static Color colorLightSquareActive = Color.lightGray;
	public final static Color colorLightSquareIdle = Color.lightGray;
	public final static Color colorHighlight = Color.red;
	public final static Color colorLegalMove = new Color (0, 125, 0); //Color.green;
	public final static Color colorIllegalMove = Color.red;
	public final static Color colorClockRunning = Color.cyan;
	public final static Color colorClockIdle = Color.black;	
	public final static Color colorBestMove = Color.red;
}