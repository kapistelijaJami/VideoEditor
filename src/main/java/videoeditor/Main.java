package videoeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import uilibrary.Canvas;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Main {
    public static void main(String[] args) {
		//Absolute path has to start with backslash (after drive letter), the rest can be either one.
		String videoPath = "video.mp4";
		new VideoEditor(videoPath).start();
		
		//test(videoPath);
		//simpleExample(videoPath);
    }
	
	public static void test(String videoPath) {
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		
		JFrame frame = new JFrame("Test window");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1280, 720));
		
		JLayeredPane layeredPane = new JLayeredPane();
		//layeredPane.setLayout(new BorderLayout());
		
		/*Canvas canvas = new Canvas();
		canvas.setBackground(Color.BLUE);
		canvas.setSize(600, 400);*/
		mediaPlayerComponent.setSize(600, 400);
		mediaPlayerComponent.setLocation(100, 70);
		mediaPlayerComponent.setPreferredSize(new Dimension(600, 400));
		
		
		//layeredPane.add(canvas, 0);
		JLabel label = new JLabel("Hello!");
        label.setOpaque(true);
		label.setBackground(Color.yellow);
        label.setForeground(Color.black);
        label.setBounds(50, 50, 140, 140);
		
		layeredPane.add(label, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(mediaPlayerComponent, JLayeredPane.FRAME_CONTENT_LAYER);
		//primitive int (index) is which one is on top within a layer, where smaller is on top.
		//Object, Integer (Constraint) is the layer, and higher is on top. It could have an index too, where smaller is on top within that layer.
		
		frame.setContentPane(layeredPane);
		/*frame.add(canvas);
		frame.add(mediaPlayerComponent); //WHY IS CANVAS VISIBLE WHEN IT'S ADDED FIRST, BUT VIDEO IS NOT IF IT'S ADDED FIRST?
		*/
		
		//frame.add(layeredPane);
		
		//window.setContentPane(mediaPlayerComponent); //replaces the canvas
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		EmbeddedMediaPlayer player = mediaPlayerComponent.mediaPlayer();
		player.media().prepare(videoPath);
		
		
		//try {
			//Thread.sleep(2000);
			/*frame.remove(canvas);
			frame.pack();
			Thread.sleep(2000);
			frame.add(canvas, BorderLayout.EAST);
			frame.pack();*/
			player.controls().play();
			/*Thread.sleep(2000);
			frame.remove(canvas);
			frame.add(canvas, BorderLayout.CENTER);
			frame.pack();
			//frame.remove(mediaPlayerComponent);*/
		//} catch (InterruptedException e) {
			
		//}
	}
	
	public static void simpleExample(String videoPath) {
		EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setPreferredSize(new Dimension(500, 500));
		
		JLayeredPane contentPane = new JLayeredPane();
		contentPane.setLayout(new BorderLayout()); //Layout is null at the start
		//mediaPlayerComponent.setSize(500, 500); //Without layout, had to put size for the player. Layout changes the bounds for you
		System.out.println(contentPane.getLayout());
		contentPane.setPreferredSize(new Dimension(500, 500));
		contentPane.add(mediaPlayerComponent);
		
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		
		mediaPlayerComponent.mediaPlayer().media().play(videoPath);
	}
}
