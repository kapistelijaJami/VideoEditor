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
		switch (e.getKeyCode()) {
			case KeyEvent.VK_DELETE:
				editor.toggleDeleteSelected();
				break;
			case KeyEvent.VK_C:
				editor.cutCurrentTime();
				break;
			case KeyEvent.VK_SPACE:
				editor.togglePause();
				break;
			case KeyEvent.VK_LEFT:
				editor.skip(-5000);
				break;
			case KeyEvent.VK_RIGHT:
				editor.skip(5000);
				break;
			case KeyEvent.VK_ESCAPE:
				editor.stop();
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
