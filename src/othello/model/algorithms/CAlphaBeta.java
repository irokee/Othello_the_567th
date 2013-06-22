package othello.model.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import othello.model.CBoard;
import othello.model.CBoardHeuristics;
import othello.model.CMove;
import othello.model.CMoveComperator;

public class CAlphaBeta extends CAbstract
{
	public CAlphaBeta(CBoard board, int maxDepth, boolean solve)
	{
		super(board, maxDepth, solve);
		m_Name = "AlphaBeta";
	}

	public float startAlgorithm()
	{
		return processAlgorithm(m_Board, CBoardHeuristics.getWorstScore(), CBoardHeuristics.getBestScore(), 0);
	}

	private Comparator<CMove> m_Comparator = Collections
		.reverseOrder(new CMoveComperator());

	private float processAlgorithm(CBoard board, float alpha, float beta, int depth)
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
			//debug("Max depth of " + m_MaxDepth + " reached with " + result);
			return result;
		}
		/* get moves*/
		ArrayList<CMove> moves = board.getAllPossibleMoves(currentPlayer);
		if ( moves.size() > 1 ) {
			CBoardHeuristics.scoreMoves(moves);
			Collections.sort(moves, m_Comparator);
		}
		/* for each moves */
		for ( CMove currentMove : moves ) {
			board.makeMove(currentMove);
			float score = -processAlgorithm(board, -beta, -alpha, depth + 1);
			//debug("score:" + score + " in depth" + depth);
			board.undoMove(currentMove);
			if ( score > alpha ) {
				alpha = score;
				if ( depth == 0 ) {
					m_BestFound.setFlipSquares(currentMove.getFlipSquares());
					m_BestFound.setFieldIndex(currentMove.getFieldIndex());
					m_BestFound.setPlayer(currentMove.getPlayer());
				}
				if ( alpha >= beta ) {
					break;
				}
			}
		}
		return alpha;
	}
}
