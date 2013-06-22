package othello.model.algorithms;

import othello.model.CBoard;
import othello.model.CMove;

public abstract class CAbstract
{
	
	public CAbstract(CBoard board, int maxDepth, boolean solve)
	{
		m_Board = board;
		m_BestFound = new CMove();
		m_BestFound.setPlayer(board.getCurrentPlayer());
		m_MaxDepth = maxDepth;
		m_Solve = solve;
	}

	public CMove getBestFound()
	{
		return m_BestFound;
	}

	public void calculate()
	{
		float score = startAlgorithm();
		debug("calls: " + m_Calls);
		debug("score: " + score);
	}

	protected String m_Name = "Algorithm";
	protected int m_MaxDepth;
	protected int m_Calls;
	protected CMove m_BestFound;
	protected CBoard m_Board;
	protected boolean m_Solve = false;

	protected void debug(String message)
	{
		System.out.println("[" + m_Name + "] " + message);
	}

	abstract protected float startAlgorithm();
}
