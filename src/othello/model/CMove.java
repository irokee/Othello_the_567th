package othello.model;

import java.util.ArrayList;

public class CMove
{
	public ArrayList<Integer> getFlipSquares()
	{
		return m_FlipSquares;
	}

	public void setFlipSquares(ArrayList<Integer> flipSquares)
	{
		this.m_FlipSquares = flipSquares;
	}

	public int getEvalualtion()
	{
		return this.m_Evaluation;
	}

	public void setEvaluation(int eval)
	{
		this.m_Evaluation = eval;
	}

	public int getPlayer()
	{
		return this.m_Player;
	}

	public void setPlayer(int player)
	{
		this.m_Player = player;
	}

	public int getFieldIndex()
	{
		return m_FieldIndex;
	}

	public void setFieldIndex(int i)
	{
		this.m_FieldIndex = i;
	}

	private ArrayList<Integer> m_FlipSquares;
	private int m_FieldIndex;
	private int m_Player;
	private int m_Evaluation;
}
