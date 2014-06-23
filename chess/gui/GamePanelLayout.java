package chess.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public final class GamePanelLayout implements LayoutManager {
	private int m_width = 0, m_height = 0;

	private Component m_board = null, m_uppername = null, m_lowername = null,
			m_upperclock = null, m_lowerclock = null,
			m_whitePlayerLabel = null, m_blackPlayerLabel = null,
			m_whitePlayerDropdown = null, m_blackPlayerDropdown = null,
			m_timeLabel = null, m_incrementLabel = null,
			m_timeDropdown = null, m_incrementDropdown = null,
			m_buttonNewGame = null;

	public GamePanelLayout(int w, int h) {
		m_width = w;
		m_height = h;
	}

	// LayoutManager Methods
	public void addLayoutComponent(String name, Component comp) {
		if ("Board".equals(name)) {
			m_board = comp;
		} else if ("UpperName".equals(name)) {
			m_uppername = comp;
		} else if ("LowerName".equals(name)) {
			m_lowername = comp;
		} else if ("UpperClock".equals(name)) {
			m_upperclock = comp;
		} else if ("LowerClock".equals(name)) {
			m_lowerclock = comp;
		} else if ("ButtonNewGame".equals(name)) {
			m_buttonNewGame = comp;
		} else if ("WhitePlayerLabel".equals(name)) {
			m_whitePlayerLabel = comp;
		} else if ("BlackPlayerLabel".equals(name)) {
			m_blackPlayerLabel = comp;
		} else if ("WhitePlayerDropdown".equals(name)) {
			m_whitePlayerDropdown = comp;
		} else if ("BlackPlayerDropdown".equals(name)) {
			m_blackPlayerDropdown = comp;
		} else if("TimeLabel".equals(name)) {
			m_timeLabel = comp;
		} else if("IncrementLabel".equals(name)) {
			m_incrementLabel = comp;
		} else if("TimeDropdown".equals(name)) {
			m_timeDropdown = comp;
		} else if("IncrementDropdown".equals(name)) {
			m_incrementDropdown = comp;
		}		else {
			throw new RuntimeException("GamePanelLayout: Unsupported component type \"" + name + "\"");
		}
	}

	public void removeLayoutComponent(Component comp) {
		if (comp == m_buttonNewGame)
			m_buttonNewGame = null;
	}

	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(m_width, m_height);
	}

	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(m_width, m_height);
	}

	public void layoutContainer(Container parent) {
		Insets insets = new Insets(2, 2, 2, 2); // parent.insets();
		Dimension dim = null;
		int top, bottom;
		int vpad = 2, hpad = 2;

		m_width = parent.getSize().width;
		m_height = parent.getSize().height;

		top = insets.top;
		bottom = m_height - insets.bottom;

		if (m_board != null && m_uppername != null && m_lowername != null
				&& m_upperclock != null && m_lowerclock != null) {

			int len1 = bottom - top; // dimension of board
			int len2 = m_width - insets.left - insets.right; // width of
			// leftover

			len1 -= len1 % 8;
			m_board.setBounds(insets.left, top + (bottom - top - len1) / 2, len1,
					len1);

			len2 = m_width - len1 - insets.left - insets.right - hpad;
			int left = insets.left + len1 + hpad;

			// place name labels
			dim = m_uppername.getPreferredSize();
			m_uppername.setBounds(left, top, len2, dim.height);
			top += dim.height + vpad;

			dim = m_lowername.getPreferredSize();
			m_lowername.setBounds(left, bottom - dim.height, len2, dim.height);
			bottom -= dim.height + vpad;

			// place clocks
			dim = m_upperclock.getPreferredSize();
			m_upperclock.setBounds(left, top, len2, dim.height);
			top += dim.height + vpad;

			dim = m_lowerclock.getPreferredSize();
			m_lowerclock.setBounds(left, bottom - dim.height, len2, dim.height);
			bottom -= dim.height + vpad;

			// Draw the user selected options
			int midline = (top + bottom) / 2;						
			if (m_whitePlayerLabel != null && m_blackPlayerLabel != null &&
					m_whitePlayerDropdown != null & m_blackPlayerDropdown != null &&
					m_buttonNewGame != null) {

				int totalHeight = m_whitePlayerLabel.getPreferredSize().height +
						m_whitePlayerDropdown.getPreferredSize().height +
						m_blackPlayerLabel.getPreferredSize().height +
						m_blackPlayerDropdown.getPreferredSize().height +
						m_timeLabel.getPreferredSize().height +
						m_incrementLabel.getPreferredSize().height +
						m_timeDropdown.getPreferredSize().height +
						m_incrementDropdown.getPreferredSize().height +
						m_buttonNewGame.getPreferredSize().height +						
						+ vpad*9;

				// Center the options between clocks
				top = midline - totalHeight / 2;

				dim = m_whitePlayerLabel.getPreferredSize();				
				m_whitePlayerLabel.setBounds(left, top, len2, dim.height);

				top += dim.height + vpad;
				dim = m_whitePlayerDropdown.getPreferredSize();
				m_whitePlayerDropdown.setBounds(left, top, len2, dim.height);

				top += dim.height + vpad;
				dim = m_blackPlayerLabel.getPreferredSize();
				m_blackPlayerLabel.setBounds(left, top, len2, dim.height);

				top += dim.height + vpad;
				dim = m_blackPlayerDropdown.getPreferredSize();
				m_blackPlayerDropdown.setBounds(left, top, len2, dim.height);
				
				top += dim.height + vpad;
				dim = m_timeLabel.getPreferredSize();
				m_timeLabel.setBounds(left, top, len2, dim.height);
				
				top += dim.height + vpad;
				dim = m_timeDropdown.getPreferredSize();
				m_timeDropdown.setBounds(left, top, len2, dim.height);

				top += dim.height + vpad;
				dim = m_incrementLabel.getPreferredSize();
				m_incrementLabel.setBounds(left, top, len2, dim.height);

				top += dim.height + vpad;
				dim = m_incrementDropdown.getPreferredSize();
				m_incrementDropdown.setBounds(left, top, len2, dim.height);

				top += dim.height + vpad;				
				dim = m_buttonNewGame.getPreferredSize();
				m_buttonNewGame.setBounds(left, top, len2, dim.height);
			}
		}
	}
}
