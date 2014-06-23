package chess.playchess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.engine.Engine;


import static chess.board.ArrayBoard.*;

public class TextChess {

  public static void main(String[] args) throws IOException {
    InputStreamReader stdin = new InputStreamReader(System.in);
    String command, prompt;

    // Redefine these variables if you want to substitute your own board.
    List<ArrayMove> moveList;
    ArrayBoard b;
    ArrayMove m = new ArrayMove();

    while (true)
    {
      Engine player = new Engine(5 * 60 * 1000, 0, Engine.MEDIUM); // time control of 5 0, not
                                                    // that
      // it matters much
      System.out.println("Computer player: " + player.getName());

      while (true)
      {
        b = player.getBoard();
        if (b.toPlay() == WHITE)
          prompt = "White";
        else
          prompt = "Black";
        System.out.println("Position (" + prompt + " to move):\n" + b);
        moveList = b.generateMoves();
        List<String> moveListAsString = new ArrayList<String>(moveList.size());
        for (ArrayMove move : moveList)
        {
          moveListAsString.add(b.moveToSANString(move, moveList));
        }
        Collections.sort(moveListAsString);

        if (moveList.size() == 0)
        {
          if (b.inCheck())
            System.out.println("Checkmate");
          else
            System.out.println("Stalemate");
          break;
        }
        System.out.println("Moves:");
        System.out.print("   ");
        for (int i = 0; i < moveListAsString.size(); i++)
        {
          if ((i % 10) == 0 && i > 0)
            System.out.print("\n   ");
          System.out.print(moveListAsString.get(i) + " ");
        }
        System.out.println();
        while (true)
        {
          System.out.print(prompt + " move (or \"go\" or \"search\" or \"quit\")> ");
          command = readCommand(stdin);
          if (command.equals("search"))
          {
            String tmp =
              b.moveToSANString(
                  player.computeMove(1 * 60 * 1000, 0),
                  moveList    
              );
            System.out.println("Computer Recommends: " + tmp);
          }
          else if (command.equals("go"))
          {
            m = player.computeMove(1 * 60 * 1000, 0);
            System.out.println("Computer Moves: " + m);
            break;
          } else if (command.equals("quit"))
          {
            System.exit(1);
          } else
          {
            m = null;
            for (int i = 0; i < moveList.size(); i++)
            {
              if (b.moveToSANString(moveList.get(i), moveList).equalsIgnoreCase(command))
              {
                m = moveList.get(i);
                break;
              }
            }
            if (m != null)
              break;
            System.out.println("\"" + command + "\" is not a legal move");
          }
        }
        player.applyMove(m);
      }

      while (true)
      {
        System.out.print("Play again? (y/n):");
        command = readCommand(stdin);
        if (command.equals("n"))
          System.exit(1);
        if (command.equals("y"))
          break;
      }
    }
  }

  static String readCommand(InputStreamReader stdin) throws IOException
  {
    final int MAX = 100;
    int len;
    char[] cbuf = new char[MAX];
    len = stdin.read(cbuf, 0, MAX);
    if (len == -1)
      System.exit(1);

    StringBuilder sb = new StringBuilder();

    /* Drop the newline in both unix and windows */
    for (int i = 0; i < cbuf.length; ++i)
    {
      if (cbuf[i] == '\r')
        continue;

      if (cbuf[i] == '\n')
        break;

      sb.append(cbuf[i]);
    }

    return sb.toString();
  }
}