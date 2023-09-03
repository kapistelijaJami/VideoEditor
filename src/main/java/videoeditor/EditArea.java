package videoeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import uilibrary.arrangement.Margin;
import uilibrary.elements.InteractableElement;
import uilibrary.enums.Alignment;
import uilibrary.interfaces.HasBounds;
import uilibrary.util.HelperFunctions;
import uilibrary.util.RenderText;

public class EditArea extends InteractableElement {
	private List<Clip> clips;
	private double areaStart; //in milliseconds
	private double areaEnd;
	private double videoDuration;
	
	private String videoName;
	private VideoEditor editor;
	private Clip selectedClip;
	private boolean dragging = false;
	
	public EditArea(VideoEditor editor, HasBounds hasBounds, double videoLength, String videoName) {
		super(hasBounds, false);
		clips = new ArrayList<>();
		
		this.videoDuration = videoLength;
		this.areaStart = 0;
		this.areaEnd = videoLength;
		this.videoName = videoName;
		
		clips.add(new Clip(areaStart, areaEnd));
		this.editor = editor;
	}
	
	@Override
	public void render(Graphics2D g) {
		for (int i = 0; i < clips.size(); i++) {
			Clip clip = clips.get(i);
			if (clip.isDisabled()) {
				renderDisabledClip(g, clip);
			} else {
				renderClip(g, clip);
			}
		}
		
		if (selectedClip != null) {
			renderEdge(g, selectedClip, Color.YELLOW);
		}
		
		renderCurrentTime(g);
	}
	
	private void renderClip(Graphics2D g, Clip clip) {
		if (!isVisible(clip)) {
			return;
		}
		
		int startX = timeToCoords(clip.getOriginalStart());
		int width = timeToCoords(clip.getOriginalEnd()) - startX;
		
		int clipHeight = getHeight() / 2;
		
		g.setColor(Color.BLACK);
		g.fillRect(startX, getY() + clipHeight / 2, width, clipHeight);
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(startX + 1, getY() + clipHeight / 2 + 2, width - 2, clipHeight - 4);
		
		g.setColor(Color.GRAY);
		g.fillRect(startX + 1, getY() + clipHeight / 2 + 2, width - 2, clipHeight / 4 - 2);
		
		Rectangle bounds = new Rectangle(startX + 1, getY() + clipHeight / 2 + 2, width - 2, clipHeight / 4 - 2);
		new Margin(3, 0).shrinkRectWithMargin(bounds);
		g.setColor(Color.BLACK);
		RenderText.drawStringWithAlignment(g, videoName, bounds, new Font("Arial", Font.PLAIN, 16), Alignment.LEFT);
	}
	
	private void renderDisabledClip(Graphics2D g, Clip clip) {
		if (!isVisible(clip)) {
			return;
		}
		
		int startX = timeToCoords(clip.getOriginalStart());
		int width = timeToCoords(clip.getOriginalEnd()) - startX;
		
		int clipHeight = getHeight() / 2;
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(startX, getY() + clipHeight / 2, width, clipHeight);
		
		g.setColor(new Color(147, 147, 147));
		g.fillRect(startX + 1, getY() + clipHeight / 2 + 2, width - 2, clipHeight - 4);
		
		g.setColor(new Color(196, 196, 196));
		g.fillRect(startX + 1, getY() + clipHeight / 2 + 2, width - 2, clipHeight / 4 - 2);
		
		Rectangle bounds = new Rectangle(startX + 1, getY() + clipHeight / 2 + 2, width - 2, clipHeight / 4 - 2);
		new Margin(3, 0).shrinkRectWithMargin(bounds);
		g.setColor(Color.DARK_GRAY);
		RenderText.drawStringWithAlignment(g, videoName, bounds, new Font("Arial", Font.PLAIN, 16), Alignment.LEFT);
	}
	
	private void renderEdge(Graphics2D g, Clip clip, Color color) {
		g.setColor(color);
		g.setStroke(new BasicStroke(2f));
		int startX = timeToCoords(clip.getOriginalStart());
		int width = timeToCoords(clip.getOriginalEnd()) - startX;
		int clipHeight = getHeight() / 2;
		g.drawRect(startX, getY() + clipHeight / 2, width, clipHeight);
	}
	
	private boolean isVisible(Clip clip) {
		return clip.getOriginalEnd() > areaStart && clip.getOriginalStart() < areaEnd;
	}
	
	private int timeToCoords(double time) {
		double t = (time - areaStart) / (areaEnd - areaStart);
		return (int) HelperFunctions.lerp(t, getX(), getX() + getWidth());
	}
	
	private double coordsToTime(int coords) {
		double t = (coords - getX()) / (double) getWidth();
		return HelperFunctions.lerp(t, areaStart, areaEnd);
	}

	private void renderCurrentTime(Graphics2D g) {
		double time = editor.getCurrentTime();
		int x = timeToCoords(time);
		g.setColor(Color.red);
		g.drawLine(x, getY(), x, getY() + getHeight());
	}

	@Override
	public boolean hover(int x, int y) {
		
		return false;
	}

	@Override
	public boolean click(int x, int y) {
		if (getBounds().contains(x, y)) {
			Clip clip = isInsideClip(x, y);
			if (clip != null) {
				selectedClip = clip;
			} else if (selectedClip != null) {
				selectedClip = null;
			} else {
				editor.skipTo((long) coordsToTime(x));
			}
			
			dragging = true;
			return true;
		}
		selectedClip = null;
		
		return false;
	}
	
	public boolean mouseDragged(int x, int y) {
		if (dragging || getBounds().contains(x, y)) {
			if (selectedClip != null) {
				
			} else {
				editor.skipTo((long) coordsToTime(HelperFunctions.clamp(x, getX(), getX() + getWidth())));
			}
			return true;
		}
		
		return false;
	}
	
	public void toggleDeleteSelected() {
		if (selectedClip != null) {
			selectedClip.setDisabled(!selectedClip.isDisabled());
		}
	}

	private Clip isInsideClip(int x, int y) {
		int clipHeight = getHeight() / 2;
		int clipY = getY() + clipHeight / 2;
		for (Clip clip : clips) {
			if (isInside(coordsToTime(x), clip.getOriginalStart(), clip.getOriginalEnd()) && isInside(y, clipY, clipY + clipHeight)) {
				return clip;
			}
		}
		return null;
	}
	
	private boolean isInside(double x, double a, double b) {
		return x >= a && x <= b;
	}

	public void cut(int x, int y) {
		selectedClip = null;
		if (getBounds().contains(x, y)) {
			Clip clip = isInsideClip(x, y);
			if (clip != null) {
				int currentX = timeToCoords(editor.getCurrentTime());
				
				if (Math.abs(currentX - x) < 3) { //If clicked close enough to current time, we split at the time
					splitClip(clip, editor.getCurrentTime());
				} else {
					double time = coordsToTime(x);
					splitClip(clip, time);
				}
			}
		}
	}
	
	private void splitClip(Clip clip, double time) {
		double endTime = clip.getOriginalEnd();
		clip.setOriginalEnd(time);
		clips.add(new Clip(time, endTime));
		clips.sort(null);
	}

	public List<Clip> getActiveClips() {
		List<Clip> list = new ArrayList<>();
		
		for (Clip clip : clips) {
			if (!clip.isDisabled()) {
				list.add(clip);
			}
		}
		return list;
	}

	public Clip getSelectedClip() {
		return selectedClip;
	}
}
