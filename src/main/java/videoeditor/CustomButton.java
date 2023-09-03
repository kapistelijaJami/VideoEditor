package videoeditor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JButton;
import uilibrary.util.RenderText;

public class CustomButton extends JButton {
	public CustomButton(String text) {
		super(text);
	}
	
	@Override
	protected void paintComponent(Graphics g2) { //Just to render text when the button is small.
		Graphics2D g = (Graphics2D) g2;
		
		String text = getText();
		setText("");
		super.paintComponent(g); //Paint normally without text
		setText(text);
		
		Rectangle buttonBounds = new Rectangle(0, 0, getWidth(), getHeight());
		
		g.setColor(getForeground());
		RenderText.drawStringWithAlignment(g, text, buttonBounds, getFont());
	}
}
