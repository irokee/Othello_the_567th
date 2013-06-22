package othello.controller;

import java.lang.String;
import javax.swing.*;
import othello.model.*;
import othello.model.algorithms.*;
import othello.view.CItem;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class CPanel extends JPanel implements Runnable
{
	public static void main(String[] args) throws Exception
	{
		CPanel panel = new CPanel(60);
		JFrame window = new JFrame("Othello the 567th");
		window.getContentPane().add(panel);
		window.setResizable(false);
		window.pack();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		new Thread(panel).start();
	}

	public CPanel(int cellSize)
	{
		m_Board = new CBoard();
		m_Player1Color = CBoard.BLACK;
		m_Player2Color = 3 - m_Player1Color;

		m_CellSize = cellSize;
		m_ItemSize = (int) (cellSize * 0.75);
		setDoubleBuffered(true);
		int screenSize = (2 + CBoard.BOARD_SIZE) * cellSize;
		setPreferredSize(new Dimension(screenSize, screenSize));
		addMouseWatcher();

		UIManager.put("OptionPane.noButtonText", "No");
		UIManager.put("OptionPane.yesButtonText", "Yes");
	}

	public void startNewGame()
	{
		m_Board = new CBoard();
		m_Finished = false;
		m_WaitingInput = false;
		m_LastMoveIndex = -666;

		int temp = m_Player2Color;
		m_Player2Color = m_Player1Color;
		m_Player1Color = temp;

		new Thread(this).start();
	}

	public void run()
	{
		getSettings();
		
		while ( !m_Finished ) {
			performStep();
			repaint();

			try {
				if ( !isHumanTurn() ) Thread.sleep(ADDITIONAL_SLEEP_PER_STEP);
				Thread.sleep(20L);
			} catch ( InterruptedException exception ) {
			}
		}

		int player1 = m_Board.countItems(m_Player1Color);
		int player2 = m_Board.countItems(m_Player2Color);
		String message = null;
		String player1Color = m_Player1Color == CBoard.BLACK ? "Black" : "White";
		String player2Color = m_Player2Color == CBoard.BLACK ? "Black" : "White";
		if ( player1 > player2 ) {
			message = PLAYER_MODES[m_Player1Mode] + " (" + player1Color +") has won versus "
				+ PLAYER_MODES[m_Player2Mode] + "(" + player2Color +") !";
		} else if ( player1 < player2 ) {
			message = PLAYER_MODES[m_Player2Mode] + " (" + player2Color +") has won versus "
				+ PLAYER_MODES[m_Player1Mode] + " (" + player1Color +") !";
		} else {
			message = "Draw!";
		}

		message += "\nNext Game?";
		int x = JOptionPane.showConfirmDialog(null,
			message,
			"Game Over",
			JOptionPane.YES_NO_OPTION);
		if ( x == 0 ) {
			startNewGame();
		}
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);

		drawGUI(g);
		drawBoard(g);
		drawScore(g);
		drawPossibleMoves(g);
	}

	private static final boolean DEBUG = false;
	private static final int ADDITIONAL_SLEEP_PER_STEP = 200;
	private static int PLAYER_MODE_HUMAN = 0;
	private static int PLAYER_MODE_CPU_ALPHA_BETA = 1;
	private static int PLAYER_MODE_CPU_MINMAX = 2;
	private static String[] PLAYER_MODES = {"Human", "CPU-AlphaBeta", "CPU-MinMax", };
	private static String[] ALLOWED_MAX_DEPTH = {"3", "4", "5", "6", "7", "8"};

	private int m_CellSize;
	private int m_ItemSize;
	private CBoard m_Board = null;
	private int m_Player1Mode;
	private int m_Player2Mode;
	private int m_Player1ModeDepth = 1;
	private int m_Player2ModeDepth = 1;
	private int m_Player1Color;
	private int m_Player2Color;
	private boolean m_WaitingInput = false;
	private boolean m_Finished = false;
	private int m_LastMoveIndex;

	private void getSettings()
	{
		Object select = JOptionPane.showInputDialog(null, 
			"Please select the first player.",
			"Settings",
			JOptionPane.QUESTION_MESSAGE, 
			null, 
			PLAYER_MODES, 
			PLAYER_MODE_HUMAN);
		for ( int i = 0; i < PLAYER_MODES.length; ++ i ) {
			if ( PLAYER_MODES[i] == String.valueOf(select) ) {
				m_Player1Mode = i;
				if ( m_Player1Mode == PLAYER_MODE_CPU_ALPHA_BETA || m_Player1Mode == PLAYER_MODE_CPU_MINMAX ) {
					select = JOptionPane.showInputDialog(null, 
						"Please select the search depth for player 1.",
						"Settings",
						JOptionPane.QUESTION_MESSAGE, 
						null, 
						ALLOWED_MAX_DEPTH, 
						ALLOWED_MAX_DEPTH[0]);
					for ( int j = 0; j < ALLOWED_MAX_DEPTH.length; ++ j ) {
						if ( ALLOWED_MAX_DEPTH[j] == String.valueOf(select) ) {
							m_Player1ModeDepth = Integer.valueOf(ALLOWED_MAX_DEPTH[j]);
						}
					}
				}
			}
		}
		select = JOptionPane.showInputDialog(null, 
			"Please select the second player.",
			"Settings",
			JOptionPane.QUESTION_MESSAGE, 
			null, 
			PLAYER_MODES, 
			PLAYER_MODE_HUMAN);
		for ( int i = 0; i < PLAYER_MODES.length; ++ i ) {
			if ( PLAYER_MODES[i] == String.valueOf(select) ) {
				m_Player2Mode = i;
				if ( m_Player2Mode == PLAYER_MODE_CPU_ALPHA_BETA || m_Player2Mode == PLAYER_MODE_CPU_MINMAX ) {
					select = JOptionPane.showInputDialog(null, 
						"Please select the search depth for player 2.",
						"Settings",
						JOptionPane.QUESTION_MESSAGE, 
						null, 
						ALLOWED_MAX_DEPTH, 
						ALLOWED_MAX_DEPTH[0]);
					for ( int j = 0; j < ALLOWED_MAX_DEPTH.length; ++ j ) {
						if ( ALLOWED_MAX_DEPTH[j] == String.valueOf(select) ) {
							m_Player2ModeDepth = Integer.valueOf(ALLOWED_MAX_DEPTH[j]);
						}
					}
				}
			}
		}
	}

	private boolean isFinished()
	{
		return m_Board.isGameEnded();
	}

	private boolean isHumanTurn()
	{
		return getCurrentMode() == PLAYER_MODE_HUMAN;
	}

	private int getCurrentMode()
	{
		if ( m_Player1Color == m_Board.getCurrentPlayer() ) {
			return m_Player1Mode;
		} else if ( m_Player2Color == m_Board.getCurrentPlayer() ) {
			return m_Player2Mode;
		} else {
			return 0;
		}
	}

	private void performStep()
	{
		if ( isFinished() ) {
			m_Finished = true;
			return;
		}

		if ( isHumanTurn() ) {
			if ( m_WaitingInput )
				return;
			ArrayList<CMove> moves = m_Board.getAllPossibleMoves(m_Board.getCurrentPlayer());
			if ( moves.get(0).getFlipSquares() == null ) {
				m_Board.makeMove(moves.get(0));
				m_WaitingInput = false;
			} else {
				m_WaitingInput = true;
			}
		} else if ( getCurrentMode() == PLAYER_MODE_CPU_ALPHA_BETA ) {
			int maxDepth = getMaxDepth(m_Board.getCurrentPlayer());
			boolean solve = m_Board.getPhase() == CBoard.PHASE_ENDGAME;
			CAlphaBeta AI = new CAlphaBeta(m_Board.cloneBoard(), maxDepth, solve);
			AI.calculate();
			CMove mv = AI.getBestFound();
			m_Board.makeMove(mv);
			m_LastMoveIndex = mv.getFieldIndex();
			System.out.println("AI move: " + mv.getFieldIndex());
		} else if ( getCurrentMode() == PLAYER_MODE_CPU_MINMAX ) {
			int maxDepth = getMaxDepth(m_Board.getCurrentPlayer());
			boolean solve = m_Board.getPhase() == CBoard.PHASE_ENDGAME;
			CMinMax AI = new CMinMax(m_Board.cloneBoard(), maxDepth, solve);
			AI.calculate();
			CMove mv = AI.getBestFound();
			m_Board.makeMove(mv);
			m_LastMoveIndex = mv.getFieldIndex();
			System.out.println("AI move: " + mv.getFieldIndex());
		}
	}
	
	private int getMaxDepth(int player)
	{
		if ( player == m_Player1Color) return m_Player1ModeDepth;
		else if ( player == m_Player2Color) return m_Player2ModeDepth;
		else return 1;
	}

	private void setHumanItem(int index)
	{
		if ( isHumanTurn() ) {
			int[] cells = m_Board.getCells();
			if ( cells[index] != CBoard.EMPTY )
				return;
			ArrayList<Integer> flips = m_Board.getFlips(index, m_Board.getCurrentPlayer());
			if ( flips.size() > 0 ) {
				CMove mv = new CMove();
				mv.setFlipSquares(flips);
				mv.setFieldIndex(index);
				mv.setPlayer(m_Board.getCurrentPlayer());
				m_Board.makeMove(mv);
				m_LastMoveIndex = index;
				repaint();
				m_WaitingInput = false;
			}
		}
	}

	private void drawGUI(Graphics g)
	{
		Graphics2D g2D = (Graphics2D) g;
		g2D.setStroke(new BasicStroke(1.5f));
		Color bg = Color.gray;
		setBackground(bg);
		int w = CBoard.BOARD_SIZE * m_CellSize;
		int origin = m_CellSize;
		g.setColor(Color.black);
		for ( int i = 0; i < CBoard.BOARD_SIZE + 1; i++ ) {
			g.drawLine(origin, i * m_CellSize + origin, origin + w, i
				* m_CellSize + origin);
		}
		for ( int i = 0; i < CBoard.BOARD_SIZE + 1; i++ ) {
			g.drawLine(i * m_CellSize + origin,
				origin,
				i * m_CellSize + origin,
				w + origin);
		}
		if ( DEBUG ) {
			g.setColor(Color.yellow);
			for ( int i = 10; i < m_Board.getCells().length - 10; i ++ ) {
				int col = i % 10;
				int row = (i - col) / 10;
				if ( col != 0 && col != 9 ) {
					g.drawString(String.valueOf(i),
						col * m_CellSize + m_CellSize / 2 - m_CellSize / 3 + m_CellSize / 2,
						row * m_CellSize + m_CellSize / 2 - m_CellSize / 4);
				}
			}
		}
	}

	private void drawScore(Graphics g)
	{
		g.setColor(Color.black);
		g.setFont(new Font(null, Font.BOLD, 15));
		int wd = m_Board.countItems(CBoard.WHITE);
		int bd = m_Board.countItems(CBoard.BLACK);
		g.drawString("White: " + wd, m_CellSize * 8, (int) (m_CellSize * 9.5));
		g.drawString("Black: " + bd, m_CellSize * 1, (int) (m_CellSize * 9.5));
		String playerWhiteEngine = m_Player1Color == CBoard.WHITE ? PLAYER_MODES[m_Player1Mode] : PLAYER_MODES[m_Player2Mode];
		String playerBlackEngine = m_Player1Color == CBoard.BLACK ? PLAYER_MODES[m_Player1Mode] : PLAYER_MODES[m_Player2Mode];
		g.drawString(playerWhiteEngine, m_CellSize * 8, (int) (m_CellSize * 9.75));
		g.drawString(playerBlackEngine, m_CellSize * 1, (int) (m_CellSize * 9.75));

	}

	private void drawBoard(Graphics g)
	{
		int[] cells = m_Board.getCells();
		int gameCols = CBoard.BOARD_SIZE;
		int hiddenCols = gameCols + 2;
		int mid = (m_CellSize - m_ItemSize) / 2;
		for ( int i = hiddenCols; i < cells.length - hiddenCols; i++ ) {
			int col = i % hiddenCols;
			int row = i / hiddenCols;

			if ( (col != 0) && (col != hiddenCols - 1) ) {
				int piece = cells[i];
				if ( piece == CBoard.EMPTY )
					continue;
				CItem p = new CItem(cells[i], m_ItemSize);
				p.draw(g, col * m_CellSize + mid, row * m_CellSize + mid);
			}
		}
		if ( m_LastMoveIndex > 0 ) {
			g.setColor(Color.white);
			int col = m_LastMoveIndex % hiddenCols;
			int row = m_LastMoveIndex / hiddenCols;
			g.drawRect(col * m_CellSize,
				row * m_CellSize,
				m_CellSize,
				m_CellSize);
		}
	}

	private void drawPossibleMoves(Graphics g)
	{
		ArrayList<CMove> moves = m_Board.getAllPossibleMoves(m_Board.getCurrentPlayer());
		if ( isHumanTurn() && moves.get(0).getFlipSquares() != null ) {
			/* for each moves */
			for ( CMove mv : moves ) {
				int idx = mv.getFieldIndex();
				int col = idx % 10;
				int row = idx / 10;
				g.setColor(new Color(0, 0, 0, 30));
				g.fillOval(col * m_CellSize + m_CellSize / 2 - m_CellSize / 4,
					row * m_CellSize + m_CellSize / 2 - m_CellSize / 4,
					m_CellSize / 2,
					m_CellSize / 2);
				if ( DEBUG ) {
					g.setColor(Color.blue);
					g.drawString(String.valueOf(CBoardHeuristics
						.getFieldEvaluation(idx)), 
						col * m_CellSize + m_CellSize / 2 - m_CellSize / 3,
						row * m_CellSize + m_CellSize / 2 - m_CellSize / 4);
				}
			}
		}
	}

	private void addMouseWatcher()
	{
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int col = e.getX() / m_CellSize;
				int row = e.getY() / m_CellSize;
				int index = row * (CBoard.BOARD_SIZE + 2) + col;
				if ( (row > 0 && row < 9) && (col > 0 && col < 9) ) {
					setHumanItem(index);
				}
			}
		});
	}
}
