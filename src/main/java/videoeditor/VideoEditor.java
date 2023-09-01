package videoeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import uilibrary.GameLoop;
import uilibrary.Window;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VideoEditor extends GameLoop {
	private final Window window;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private String videoPath;
	
	public VideoEditor(String videoPath) {
		super(60);
		
		this.videoPath = videoPath;
		window = new Window(1280, 720, "Title");
		window.removeCanvas();
	}
	
	@Override
	protected void init() {
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		splitPane.add(mediaPlayerComponent, JSplitPane.TOP);
		mediaPlayerComponent.setMinimumSize(new Dimension(300, 300));
		
		JPanel editorPane = new JPanel();
		editorPane.setMinimumSize(new Dimension(200, 170));
		splitPane.add(editorPane, JSplitPane.BOTTOM);
		
		JPanel controlsPane = new JPanel();
		controlsPane.setBackground(Color.red);
		controlsPane.setMinimumSize(new Dimension(200, 50));
		controlsPane.setPreferredSize(new Dimension(200, 50));
		
		JButton pauseButton = new JButton("Pause");
		controlsPane.add(pauseButton);
		
		JButton rewindButton = new JButton("<<");
		controlsPane.add(rewindButton);
		
		JButton skipButton = new JButton(">>");
		controlsPane.add(skipButton);
		
		window.add(splitPane, BorderLayout.CENTER);
		window.add(controlsPane, BorderLayout.SOUTH);
		window.pack();
		
		splitPane.setDividerLocation(0.7);
		splitPane.setContinuousLayout(true);
		
		EmbeddedMediaPlayer player = mediaPlayerComponent.mediaPlayer();
		
		pauseButton.addActionListener((ActionEvent e) -> player.controls().pause());
		rewindButton.addActionListener((ActionEvent e) -> player.controls().skipTime(-5000));
		skipButton.addActionListener((ActionEvent e) -> player.controls().skipTime(5000));
		
		
		
		
		player.media().prepare(videoPath);
		
		player.controls().play();
		System.out.println("playing " + videoPath);
		
		window.setOnCloseFunction(() -> mediaPlayerComponent.release());
		
	}
	
	@Override
	protected void update() {
		
	}
	
	@Override
	protected void render() {
		
	}
}
