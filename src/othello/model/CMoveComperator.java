package othello.model;

import java.util.Comparator;

public class CMoveComperator implements Comparator<CMove>
{
	public int compare(CMove move1, CMove move2)
	{
		if ( move1.getEvalualtion() > move2.getEvalualtion() )
			return 1;
		else if ( move1.getEvalualtion() < move2.getEvalualtion() )
			return -1;
		else
			return 0;
	}
}