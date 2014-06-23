package chess.tests.internal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chess.board.Board;
import chess.board.Move;
import chess.evaluation.NoEvaluator;
import chess.search.DFS;
import chess.search.Searcher;


import static org.junit.Assert.*;

/**
 * This class aids in testing the Board interface.
 * 
 * You do not need to edit this file.  It is provided
 * in case you want to write your own board and use
 * similar tests on it.
 * 
 * FrontDesk will ignore any changes you make to this
 * file.
 */
public class PerftTestUtil
{
	public static final String PERFT_SUITE_FILE = "chess/util/perftsuite.epd";
	
	public static
	<
		M extends Move<M>,
		B extends Board<M,B>
	>
	void perftAll(B board, int depth)
	{
		List<Map.Entry<String,String[]>> entries = new ArrayList<Map.Entry<String,String[]>>(database.entrySet());
		//Collections.shuffle(entries);
		
		for(Map.Entry<String,String[]> entry : entries)
		{
			String   fen            = entry.getKey();
			String[] expectedCounts = entry.getValue();

			//System.out.println("Processing fen: " + fen);
			
			for(int d = 1; d <= depth && d < expectedCounts.length; ++d)
			{
				perft(board, fen, d, expectedCounts[d-1]);
				//System.out.println("\tDepth " + d + " completed successfully.");
			}
		}
	}
	
	private static
	<
		M extends Move<M>,
		B extends Board<M,B>
	>
	void perft(B b, String fen, int depth, String expectedCount)
	{
		Searcher<M,B> s     = new DFS<M,B>();
		B             board = b.create().init(fen);
		
		s.setEvaluator(new NoEvaluator<B>());
		s.setFixedDepth(depth);
		s.getBestMove(board, 1, 1);
		
		String count = new Long(s.leafCount()).toString();
		
		assertEquals("depth " + depth + " perft count on board \""+fen+"\" failed.\n",expectedCount,count);
	}
	
	public static Map<String,String[]> database = new HashMap<String,String[]>();
	static {
	    String s = "";
	    
	    try
        {
            s = readFileAsString(PERFT_SUITE_FILE);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        String[] tests = s.split("\\n");
        for(String test : tests)
        {
            String[]     parts  = test.split(";");
            String       fen    = parts[0];
            List<String> counts = new ArrayList<String>();
            
            for(int d = 1; d < parts.length; ++d)
            {
                String count = parts[d].split("\\s")[1];
                counts.add(count);
            }
            
            addPerft(fen,counts.toArray(new String[0]));
        }
	}
	
	private static void addPerft(String fen, String... expectedCounts)
	{
		database.put(fen,expectedCounts);
	}
	
	private static String readFileAsString(String filePath)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
