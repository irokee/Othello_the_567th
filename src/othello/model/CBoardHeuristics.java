package othello.model;

import java.util.ArrayList;

public class CBoardHeuristics
{
	public static int getFieldEvaluation(int i)
	{
		if ( i >= 0 && i < m_FieldEvaluations.length ) return m_FieldEvaluations[i];
		else return 0;
	}

	public static void scoreMoves(ArrayList<CMove> moves)
	{
		/* for each move */
		for ( CMove mv : moves ) {
			mv.setEvaluation(m_FieldEvaluations[mv.getFieldIndex()]);
		}
	}

	public static float evaluateSituation(int player, CBoard board)
	{
		float value = 0.0f;
		int phase = board.getPhase();
		if ( phase == CBoard.PHASE_OPENING ) {
			value = FACTOR_OPENING_MOBILITY * getMobilityDiff(player, board) 
				+ FACTOR_OPENING_MOBILITY_POT * getMobilityPotentialDiff(player, board) 
				+ FACTOR_OPENING_CORNER_ITEMS * getCornerItemsDiff(player, board)
				- FACTOR_OPENING_CROSSC_ITEMS * getBadCrossCornerItemsDiff(player, board)
				- FACTOR_OPENING_STRAIGHT_ITE * getBadStraightCornerItemsDiff(player, board);
		} else if ( phase == CBoard.PHASE_MIDGAME ) {
			value = FACTOR_MIDGAME_MOBILITY  * getMobilityDiff(player, board)
				+ FACTOR_MIDGAME_MOBILITY_POT * getMobilityPotentialDiff(player, board) 
				+ FACTOR_MIDGAME_CORNER_ITEMS * getCornerItemsDiff(player, board) 
				- FACTOR_MIDGAME_CROSSC_ITEMS * getBadCrossCornerItemsDiff(player, board) 
				- FACTOR_MIDGAME_STRAIGHT_ITE * getBadStraightCornerItemsDiff(player, board);
		} else if ( phase == CBoard.PHASE_ENDGAME ) {
			value = getItemsDiff(player, board);
		} else {
			System.out.println("Heuristic error: indeterminate phase");
		}
		return value;
	}

	public static float getFinalScore(CBoard board, int currentPlayer)
	{
		int blackItemsCount = board.countItems(CBoard.BLACK);
		int whiteItemsCount = board.countItems(CBoard.WHITE);
		//debug("ended");
		if ( (blackItemsCount > whiteItemsCount)
			&& currentPlayer == CBoard.BLACK ) {
			return INFINITY / 10;
		} else if ( (blackItemsCount < whiteItemsCount)
			&& currentPlayer == CBoard.BLACK ) {
			return -INFINITY / 10;
		} else if ( (blackItemsCount > whiteItemsCount)
			&& currentPlayer == CBoard.WHITE ) {
			return -INFINITY / 10;
		} else if ( (blackItemsCount < whiteItemsCount)
			&& currentPlayer == CBoard.WHITE ) {
			return INFINITY / 10;
		} else {
			return 0.0f;
		}
	}
	
	public static float getBestScore()
	{
		return INFINITY;
	}
	
	public static float getWorstScore()
	{
		return -INFINITY;
	}
	
	protected static float INFINITY = Float.MAX_VALUE / 1000;

	private static final int FACTOR_OPENING_MOBILITY = 100;
	private static final int FACTOR_OPENING_MOBILITY_POT = 100;
	private static final int FACTOR_OPENING_CORNER_ITEMS = 800;
	private static final int FACTOR_OPENING_CROSSC_ITEMS = 200;
	private static final int FACTOR_OPENING_STRAIGHT_ITE = 200;
	
	private static final int FACTOR_MIDGAME_MOBILITY = 100;
	private static final int FACTOR_MIDGAME_MOBILITY_POT = 100;
	private static final int FACTOR_MIDGAME_CORNER_ITEMS = 900;
	private static final int FACTOR_MIDGAME_CROSSC_ITEMS = 250;
	private static final int FACTOR_MIDGAME_STRAIGHT_ITE = 200;
	
	/**
	 * contains the corner indexes from m_SquareValues
	 */
	private static final int[] m_CornerIndexes = new int[] { 11, 18, 81, 88 };
	/**
	 * contains the indexes of the fields diagonally before the corners
	 */
	private static final int[] m_CrossCornerIndexes = new int[] { 22, 27, 72, 77 };
	/**
	 * contains the fields horizontally or vertically beside the corner
	 */
	private static final int[] m_StraightCornerIndexes = new int[] { 12, 21, 17, 28, 71, 82, 78, 87 };
	/**
	 * contains the Evaluation of the board fields
	 */
	private static final int[] m_FieldEvaluations = new int[] {
		0,	0,		0,		0,		0,		0,		0,		0,		0,		0,
		0,	120,	-20,	20,		5,		5,		20,		-20,	120,	0,
		0,	-20,	-40,	-5,		-5,		-5,		-5,		-40,	-20,	0,
		0,	20,		-5,		15,		3,		3,		15,		-5,		20,		0,
		0,	5,		-5,		3,		3,		3,		3,		-5,		5,		0,
		0,	5,		-5,		3,		3,		3,		3,		-5,		5,		0,
		0,	20,		-5,		15,		3,		3,		15,		-5,		20,		0,
		0,	-20,	-40,	-5,		-5,		-5,		-5,		-40,	-20,	0,
		0,	120,	-20,	20,		5,		5,		20,		-20,	120,	0,
		0,	0,		0,		0,		0,		0,		0,		0,		0,		0,
	};

	private static int getMobility(int player, CBoard board)
	{
		return board.getAllPossibleMoves(player).size();
	}

	private static int getMobilityDiff(int player, CBoard board)
	{
		return getMobility(player, board)
			- getMobility(board.getOpponent(player), board);
	}

	/**
	 * return the potential moves count
	 * count all the fields which are empty and behind an opponent field
	 * @param player
	 * @param board
	 * @return
	 */
	private static int getMobilityPotential(int player, CBoard board)
	{
		int opponent = board.getOpponent(player);
		int[] cells = board.getCells();
		int potentalMoves = 0;
		/* for each fields (top and bottom walls excluded) */
		for ( int i = 10; i < 90; i ++ ) {
			/* exclude side walls and only check opponent fields */
			if ( cells[i] != CBoard.WALL && cells[i] == opponent ) {
				/* for each direction */
				for ( int dir : CBoard.DIRECTIONS ) {
					int targetFieldIndex = dir + i;
					if ( cells[targetFieldIndex] == CBoard.EMPTY && cells[targetFieldIndex] != CBoard.WALL ) {
						//System.out.println("PotentialMove: " + i + ": " + dir + " because " + targetFieldIndex + " is empty");
						potentalMoves ++;
						break;
					}
				}
			}
		}
		return potentalMoves;
	}

	private static int getMobilityPotentialDiff(int player, CBoard board)
	{
		return getMobilityPotential(player, board)
			- getMobilityPotential(board.getOpponent(player), board);
	}


	/**
	 * returns the count of corner items
	 * @param int player
	 * @param CBoard board
	 * @return
	 */
	private static int getCornerItems(int player, CBoard board)
	{
		int corners = 0;
		int[] cells = board.getCells();
		for ( int i = 0; i < m_CornerIndexes.length; i ++ ) {
			if ( cells[m_CornerIndexes[i]] == player ) {
				corners ++;
			}
		}
		return corners;
	}


	/**
	 * returns the difference between the given player and the opponent
	 * @param int player
	 * @param CBoard board
	 * @return
	 */
	private static int getCornerItemsDiff(int player, CBoard board)
	{
		return getCornerItems(player, board)
			- getCornerItems(board.getOpponent(player), board);
	}

	/**
	 * returns the count of the bad items diagonally before the corner
	 * bad items are bad, if the player does not own the corner
	 * @param int player
	 * @param CBoard board
	 * @return
	 */
	private static int getBadCrossCornerItems(int player, CBoard board)
	{
		int x = 0;
		int[] cells = board.getCells();
		for ( int i = 0; i < 4; i ++ ) {
			if ( (cells[m_CornerIndexes[i]] != player)
				&& (cells[m_CrossCornerIndexes[i]] == player) ) {
				x ++;
			}
		}
		return x;
	}

	/**
	 * returns the difference between the given player and the opponent
	 * @param int player
	 * @param CBoard board
	 * @return
	 */
	private static int getBadCrossCornerItemsDiff(int player, CBoard board)
	{
		return getBadCrossCornerItems(player, board)
			- getBadCrossCornerItems(board.getOpponent(player), board);
	}

	/**
	 * returns the count of the bad items straight beside the corner
	 * bad items are bad, if the player does not own the corner
	 * @param int player
	 * @param CBoard board
	 * @return
	 */
	private static int getBadStraightCornerItems(int player, CBoard board)
	{
		int c = 0;
		int[] cells = board.getCells();
		int corner = 0;
		for (int i = 0; i < m_StraightCornerIndexes.length; i += 2) {
			if (cells[m_CornerIndexes[corner ++]] != player) {
				if (cells[m_StraightCornerIndexes[i]] == player) {
					c ++;
				}
				if (cells[m_StraightCornerIndexes[i + 1]] == player) {
					c ++;
				}
			}
		}
		return c;
	}

	/**
	 * returns the difference between the given player and the opponent
	 * @param int player
	 * @param CBoard board
	 * @return
	 */
	private static int getBadStraightCornerItemsDiff(int player, CBoard board)
	{
		return getBadStraightCornerItems(player, board)
			- getBadStraightCornerItems(board.getOpponent(player), board);
	}

	private static int getItemsDiff(int player, CBoard board)
	{
		int playerItemsCount = board.countItems(player);
		int opponentItemsCount = board.countItems(board.getOpponent(player));
		return playerItemsCount - opponentItemsCount;
	}
}
