package chess.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public final class ChessClock extends Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2917618081806596492L;

	private static Font font;

	private boolean running = false;

	long msecleft = 0; // time left, in msec

	private long referencetime; // system time @ setClock() call

	private long referencetimeleft; // argument to setClock() call

	private static Color colorHighlight = Config.colorClockRunning; // color of

	// running
	// clock

	private static Color colorIdle = Config.colorClockIdle; // color of idle

	// clock

	public ChessClock() {
		this.setFont(font);
		this.setForeground(colorIdle);

		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (running) {
					msecleft = referencetimeleft - System.currentTimeMillis()
							+ referencetime;
					repaint();
				}
			}
		};
		new Timer(Config.clockSleepTime, taskPerformer).start();
		stopClock();
		setClock(3 * 60 * 1000);
	}

	public static void swap(ChessClock a, ChessClock b) {
		boolean r = a.running;
		long l1 = a.msecleft, l2 = a.referencetime, l3 = a.referencetimeleft;
		// Thread t = a.runner;

		a.running = b.running;
		a.msecleft = b.msecleft;
		a.referencetime = b.referencetime;
		a.referencetimeleft = b.referencetimeleft;
		// a.runner = b.runner;

		b.running = r;
		b.msecleft = l1;
		b.referencetime = l2;
		b.referencetimeleft = l3;
		// b.runner = t;
	}
	
	public void stopClock() {
		running = false;
		repaint();
	}

	public void startClock() {
		setClock(msecleft); // shouldn't be necessary, but suspend/resume don't work
		running = true;
	}

	public void setClock(long l) { // set clock, in milliseconds
		msecleft = l;
		referencetimeleft = l;
		referencetime = System.currentTimeMillis();
		repaint();
	}

	public void paint(Graphics g) {
		StringBuilder s = new StringBuilder(5);

		int timeleft = (int) (msecleft / 1000);

		int min = Math.abs(timeleft / 60);
		int sec;
		if (timeleft > 0)
			sec = timeleft % 60;
		else
			sec = (-timeleft) % 60;

		if (timeleft < 0) {
			s.append("-" + min + ":");
		} else {
			s.append(min + ":");
		}
		if (sec < 10) {
			s.append("0" + sec);
		} else {
			s.append(sec);
		}

		if (running) { // highlight this clock
			g.setColor(colorHighlight);
		}
		g.drawString(s.toString(), 0, 30);
	}

	public Dimension minimumSize() {
		return new Dimension(60, 30);
	}

	public Dimension preferredSize() {
		return new Dimension(60, 30);
	}
}