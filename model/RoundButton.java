package model;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * A custom JButton that paints the icon only. Requires three types of icon,
 * normal, rollover and pressed. Edited version of the code found on
 * (http://terai.xrea.jp/Swing/RoundImageButton.html)
 */
@SuppressWarnings("serial")
public class RoundButton extends JButton {
	protected Shape shape, base;
	protected String _description;

	/**
	 * This constructor takes three icons, normal, rollover and pressed.
	 * @param icon - the normal state button icon
	 * @param i2 - the rollover state button icon
	 * @param i3 - the pressed state button icon
	 * @param description - description of the button
	 */
	public RoundButton(Icon icon, Icon i2, Icon i3, String description) {
		super(icon);
		setPressedIcon(i2);
		setRolloverIcon(i3);
		_description = description;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setBackground(Color.BLACK);
		setContentAreaFilled(false);
		setFocusPainted(false);
		initShape();
	}

	@Override
	public Dimension getPreferredSize() {
		Icon icon = getIcon();
		Insets i = getInsets();
		int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
		return new Dimension(iw + i.right + i.left, iw + i.top + i.bottom);
	}

	/**
	 * This method sets the round shape of the button
	 */
	protected void initShape() {
		if (!getBounds().equals(base)) {
			Dimension s = getPreferredSize();
			base = getBounds();
			shape = new Ellipse2D.Float(0, 0, s.width - 1, s.height - 1);
		}
	}

	@Override
	protected void paintBorder(Graphics g) {
		initShape();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getBackground());
		g2.dispose();
	}

	@Override
	public boolean contains(int x, int y) {
		initShape();
		return shape == null ? false : shape.contains(x, y);
	}

	/**
	 * This method changes the three icons and the description of the button
	 * @param icon - normal state icon
	 * @param pressedIcon - pressed state icon
	 * @param rolloverIcon -rollover state icon
	 * @param description - description of the button
	 */
	public void setIcons(Icon icon, Icon pressedIcon, Icon rolloverIcon,
			String description) {
		setIcon(icon);
		setPressedIcon(pressedIcon);
		setRolloverIcon(rolloverIcon);
		_description = description;
	}

	/**
	 * Returns the description of the button
	 * @return description of the button
	 */
	public String getDescription() {
		return _description;
	}
}