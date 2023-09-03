package videoeditor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class EditAreaInput implements MouseListener, MouseMotionListener, KeyListener {
	private VideoEditor editor;
	
	public EditAreaInput(VideoEditor editor) {
		this.editor = editor;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		editor.editAreaMouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		editor.editAreaMousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		editor.editAreaMouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			editor.toggleDeleteSelected();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
