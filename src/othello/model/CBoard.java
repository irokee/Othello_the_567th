package othello.model;

import java.util.ArrayList;

public class CBoard
{
	public static final int BLACK = 1;
	public static final int WHITE = 2;
	public static final int EMPTY = 0;
	public static final int WALL = 5;
	public static final int BOARD_SIZE = 8;
	public static final int[] DIRECTIONS = { -11, -10, -9, -1, 1, 9, 10, 11 };
	public static final int PHASE_OPENING = 100;
	public static final int PHASE_MIDGAME = 200;
	public static final int PHASE_ENDGAME = 300;

	public CBoard()
	{
		m_CurrentPlayer = BLACK;

		m_Cells[44] = WHITE;
		m_Cells[55] = WHITE;
		m_Cells[45] = BLACK;
		m_Cells[54] = BLACK;

		m_EmptyCellsCount = BOARD_SIZE * BOARD_SIZE - 4;

		for ( int i = 0; i < m_Cells.length; i++ ) {
			int col = i % 10;
			int row = i / 10;

			if ( col == 0 || col == 9 )
				m_Cells[i] = WALL;
			if ( row == 0 || row == 9 )
				m_Cells[i] = WALL;
		}

		m_Phase = PHASE_OPENING;
	}

	public void toogleCurrentPlayer()
	{
		m_CurrentPlayer = getOpponent(m_CurrentPlayer);
	}

	public void setEmptyCellsCount(int cells)
	{
		m_EmptyCellsCount = cells;
	}

	public boolean isGameEnded()
	{
		boolean isGameEnded = false;
		if ( m_EmptyCellsCount == 0 )
			isGameEnded = true;
		else if ( (getAllPossibleMoves(BLACK).get(0).getFlipSquares() == null)
			&& (getAllPossibleMoves(WHITE).get(0).getFlipSquares() == null) ) {
			isGameEnded = true;
		}

		return isGameEnded;
	}

	public CBoard cloneBoard()
	{
		CBoard b = new CBoard();
		for ( int i = 0; i < this.m_Cells.length; i++ )
			b.m_Cells[i] = this.m_Cells[i];

		b.m_Phase = this.m_Phase;
		b.m_CurrentPlayer = this.m_CurrentPlayer;
		b.m_EmptyCellsCount = this.m_EmptyCellsCount;

		return b;
	}

	public int getEmptyCells()
	{
		return m_EmptyCellsCount;
	}

	public int getPhase()
	{
		return m_Phase;
	}

	public int getCurrentPlayer()
	{
		return m_CurrentPlayer;
	}

	public int getOpponent(int player)
	{
		return 3 - player;
	}

	public int[] getCells()
	{
		return m_Cells;
	}

	public ArrayList<CMove> getAllPossibleMoves(int player)
	{
		ArrayList<CMove> moves = new ArrayList<CMove>();
		for ( int i = 10; i < 90; i++ ) {
			int col = i % 10;

			if ( col != 0 && col != 9 ) {
				if ( m_Cells[i] == EMPTY ) {
					ArrayList<Integer> flips = getFlips(i, player);
					if ( flips.size() > 0 ) {
						CMove mv = new CMove();
						mv.setFlipSquares(flips);
						mv.setFieldIndex(i);
						mv.setPlayer(player);
						moves.add(mv);
					}
				}
			}
		}

		if ( moves.size() == 0 ) {
			CMove mv = new CMove();
			mv.setPlayer(getOpponent(player));
			moves.add(mv);
		}
		return moves;
	}

	public ArrayList<Integer> getFlips(int idx, int player)
	{
		int opponent = getOpponent(player);
		ArrayList<Integer> flips = new ArrayList<Integer>();

		for ( Integer dir : DIRECTIONS ) {
			int distance = 1;
			int tempIdx = idx;

			while ( m_Cells[tempIdx += dir] == opponent )
				distance++;

			if ( (m_Cells[tempIdx] == player) && (distance > 1) ) {
				while ( distance-- > 1 ) {
					tempIdx -= dir;
					flips.add(tempIdx);
				}
			}
		}
		return flips;
	}

	public void updatePhase()
	{
		if ( m_EmptyCellsCount > 45 )
			m_Phase = PHASE_OPENING;
		else if ( m_EmptyCellsCount < 15 )
			m_Phase = PHASE_ENDGAME;
		else
			m_Phase = PHASE_MIDGAME;
	}

	public void makeMove(CMove move)
	{
		int player = move.getPlayer();
		ArrayList<Integer> flips = move.getFlipSquares();

		if ( flips != null ) {
			int idx = move.getFieldIndex();
			m_Cells[idx] = player;
			for ( Integer flip : flips )
				m_Cells[flip] = player;

			m_EmptyCellsCount--;
			updatePhase();
		}
		toogleCurrentPlayer();
	}

	public void undoMove(CMove move)
	{
		int player = move.getPlayer();
		ArrayList<Integer> flips = move.getFlipSquares();
		int opponent = getOpponent(player);

		if ( flips != null ) {
			int idx = move.getFieldIndex();

			m_Cells[idx] = EMPTY;
			for ( Integer flip : flips )
				m_Cells[flip] = opponent;

			m_EmptyCellsCount++;
			updatePhase();
		}
		toogleCurrentPlayer();
	}

	public int countItems(int player)
	{
		int discs = 0;
		for ( int i = 10; i < 90; i++ ) {
			int col = i % 10;

			if ( col != 0 && col != 9 ) {
				if ( m_Cells[i] == player )
					discs++;
			}
		}
		return discs;
	}

	private int m_EmptyCellsCount;
	private int m_Phase;
	private int m_CurrentPlayer = BLACK;
	private int[] m_Cells = new int[(BOARD_SIZE + 2) * (BOARD_SIZE + 2)];
}
