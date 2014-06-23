chess-bot
=========

Chess game and engine written in Java. Supports human vs. bot and bot vs. bot games.

The main function is under chess.playchess.GuiChess. To run the jar file, just run java -jar chess.jar. Compiled for Java 6.

The engine uses negamax with iterative deepening, transposition tables, quiescence search, move ordering, and check extensions. It also has support for Polyglot opening books. More information can be found at http://alexander.soto.io/chess-bot.
