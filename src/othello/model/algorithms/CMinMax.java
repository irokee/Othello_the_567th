package othello.model.algorithms;

import java.util.ArrayList;

import othello.model.CBoard;
import othello.model.CBoardHeuristics;
import othello.model.CMove;


public class CMinMax extends CAbstract
{
	public CMinMax(CBoard board, int maxDepth, boolean solve)
	{
		super(board, maxDepth, false);
		m_Name = "MinMax";
	}

	public float startAlgorithm()
	{
		return processAlgorithm(m_Board, 0);
	}

	private float processAlgorithm(CBoard board, int depth)
	{
		m_Calls ++;
		int currentPlayer = board.getCurrentPlayer();
		/* only if no move is possible / no cell is empty */
		if ( board.isGameEnded() ) {
			return CBoardHeuristics.getFinalScore(board, currentPlayer);
		}
		/* if max depth reached and not in the end game phase get move by heuristic */
		if ( ! m_Solve && depth == m_MaxDepth ) {
			float result = CBoardHeuristics.evaluateSituation(currentPlayer, board);
			return result;
		}
		/* score moves and sort them to start with best scored move */
		ArrayList<CMove> moves = board.getAllPossibleMoves(currentPlayer);
		/* for each moves */
		float value = CBoardHeuristics.getWorstScore();
		for ( CMove currentMove : moves ) {
			board.makeMove(currentMove);
			float score = -processAlgorithm(board, depth + 1);
			board.undoMove(currentMove);
			if ( score > value ) {
				value = score;
				if ( depth == 0 ) {
					m_BestFound.setFlipSquares(currentMove.getFlipSquares());
					m_BestFound.setFieldIndex(currentMove.getFieldIndex());
					m_BestFound.setPlayer(currentMove.getPlayer());
				}
			}
		}
		return value;
	}
}
