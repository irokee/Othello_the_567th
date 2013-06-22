package othello.view;

import java.awt.Color;
import java.awt.Graphics;

import othello.model.CBoard;


public class CItem
{
	public CItem(int color, int size)
	{
		this.m_Color = color;
		this.m_Size = size;
	}

	public void draw(Graphics g, int x, int y)
	{
		Color col = null;
		if (m_Color == CBoard.WHITE) {
			col = Color.white;
		} else {
			col = Color.black;
		}
		g.setColor(col);
		g.fillOval(x, y, m_Size, m_Size);
	}

	private int m_Color;
	private int m_Size;

}
