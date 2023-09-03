package videoeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import processes.Execute;
import processes.StreamGobbler;
import processes.StreamGobblerText;
import timer.DurationFormat;
import timer.Timer;
import uilibrary.Canvas;
import uilibrary.DragAndDrop;
import uilibrary.GameLoop;
import uilibrary.Window;
import uilibrary.arrangement.Margin;
import uilibrary.enums.Alignment;
import uilibrary.util.RenderMultilineText;
import uilibrary.util.RenderText;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VideoEditor extends GameLoop {
	private final Window window;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private String videoPath;
	private Canvas editorCanvas;
	private JPanel videoplayerPanel;
	private Tool currentTool = Tool.SELECT;
	private EditArea editArea;
	private JButton pauseButton;
	private JButton deleteButton;
	private JLabel timeLabel;
	
	private boolean paused = true;
	private final Timer timer;
	private double videoDuration;
	
	private boolean exporting = false;
	
	public VideoEditor() {
		super(60);
		
		window = new Window(1280, 900, false, "Video Editor");
		timer = new Timer(Timer.Type.MILLIS);
		timer.pause();
	}
	
	@Override
	protected void init() {
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		
		createUI();
		editArea = new EditArea(this, editorCanvas);
		
		
		EditAreaInput input = new EditAreaInput(this);
		editorCanvas.addMouseListener(input);
		editorCanvas.addMouseMotionListener(input);
		//editorCanvas.addKeyListener(input);
		
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent e) -> { //dispatches key events
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				input.keyPressed(e);
			}
			return false; // Return false to allow further event processing
		});
		
		window.setTransferHandler(new DragAndDrop(this::loadVideo));
	}
	
	public boolean loadVideo(List<File> files) {
		this.videoPath = files.get(0).getAbsolutePath();
		
		EmbeddedMediaPlayer player = mediaPlayerComponent.mediaPlayer();
		player.media().startPaused(videoPath); //Waits for the video to load, so we can access the metadata and set up the editor.
		
		insertVideoItemToEditor();
		
		togglePause();
		timer.start();
		
		window.setOnClosingFunction(() -> mediaPlayerComponent.release());
		
		
		player.controls().setRepeat(true);
		
		player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			@Override
			public void playing(MediaPlayer mediaPlayer) {
				timer.unPause();
			}
			
			@Override
			public void paused(MediaPlayer mediaPlayer) {
				timer.pause();
			}
			
			@Override
			public void buffering(MediaPlayer mediaPlayer, float newCache) {
				if (!timer.isPaused()) {
					if (newCache < 100) {
						timer.pause();
					}
				} else if (newCache >= 100 && mediaPlayer.status().isPlaying()) {
					timer.unPause();
				} else if (!mediaPlayer.status().isPlaying()) {
					timer.startPaused(mediaPlayer.status().time());
				}
			}
			
			@Override
			public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
				timer.start(newTime); //only updates while playing
			}
			
			@Override
			public void finished(MediaPlayer mediaPlayer) {
				timer.startPaused(0);
				pauseButton.setText("Play");
			}
		});
		
		return true;
	}
	
	@Override
	protected void update() {
		DurationFormat df = new DurationFormat("hh:mm:ss.lll");
		String time = df.format(Duration.ofMillis((long) getCurrentTime()));
		String duration = df.format(Duration.ofMillis((long) videoDuration));
		timeLabel.setText(time + " / " + duration);
		
		Clip selected = editArea.getSelectedClip();
		if (selected != null && selected.isDisabled()) {
			deleteButton.setText("Un del");
		} else if (selected != null && !selected.isDisabled()) {
			deleteButton.setText("Delete");
		}
	}
	
	@Override
	protected void render() {
		renderEditArea();
	}
	
	private void renderEditArea() {
		Graphics2D g = window.getGraphics2D(editorCanvas);
		
		editArea.render(g);
		
		if (exporting) {
			String text = "Exporting the video! Please wait...";
			Rectangle bounds = RenderMultilineText.getRenderedBounds(g, text, null, editArea.getBounds());
			new Margin(15, 10).widenRectWithMargin(bounds);
			
			g.setColor(new Color(150, 0, 0));
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			
			g.setColor(Color.black);
			RenderText.drawStringWithAlignment(g, text, editArea.getBounds(), null);
		}
		
		window.display(g, editorCanvas);
	}
	
	private void createUI() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		videoplayerPanel = new JPanel(new BorderLayout());
		videoplayerPanel.add(mediaPlayerComponent);
		splitPane.add(videoplayerPanel, JSplitPane.TOP);
		mediaPlayerComponent.setMinimumSize(new Dimension(100, 100));
		
		JPanel editorPane = new JPanel(new BorderLayout());
		editorPane.setMinimumSize(new Dimension(200, 170));
		splitPane.add(editorPane, JSplitPane.BOTTOM);
		
		JPanel controlsPane = new JPanel(new BorderLayout(10, 10));
		
		timeLabel = new JLabel("");
		timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
		
		controlsPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //10px padding
		controlsPane.setMinimumSize(new Dimension(200, 50));
		controlsPane.setPreferredSize(new Dimension(200, 50));
		
		JPanel buttonsPane = new JPanel(new GridLayout(1, 0, 5, 0));
		pauseButton = new JButton("Pause");
		pauseButton.setPreferredSize(new Dimension(70, 50));
		buttonsPane.add(pauseButton);
		JButton rewindFullButton = new JButton("<<<");
		buttonsPane.add(rewindFullButton);
		JButton rewindButton = new JButton("<<");
		buttonsPane.add(rewindButton);
		JButton skipButton = new JButton(">>");
		buttonsPane.add(skipButton);
		JButton rewindFrameButton = new JButton("<");
		buttonsPane.add(rewindFrameButton);
		JButton skipFrameButton = new JButton(">");
		buttonsPane.add(skipFrameButton);
		
		JPanel exportButtonPane = new JPanel(new GridLayout());
		JButton exportButton = new JButton("Export");
		exportButtonPane.add(exportButton);
		
		controlsPane.add(buttonsPane, BorderLayout.WEST);
		controlsPane.add(exportButtonPane, BorderLayout.EAST);
		controlsPane.add(timeLabel, BorderLayout.CENTER);
		
		JPanel editorCanvasPane = new JPanel(new BorderLayout(0, 0));
		//editorCanvasPane.setBorder(BorderFactory.createEmptyBorder());
		editorCanvas = new Canvas();
		editorCanvas.setBackground(Color.LIGHT_GRAY);
		editorCanvasPane.add(editorCanvas, BorderLayout.CENTER);
		editorCanvasPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		editorPane.add(editorCanvasPane, BorderLayout.CENTER);
		
		
		JPanel toolBarPane = new JPanel();
		toolBarPane.setLayout(new BoxLayout(toolBarPane, BoxLayout.Y_AXIS));
		toolBarPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		toolBarPane.setMinimumSize(new Dimension(50, 50));
		toolBarPane.setPreferredSize(new Dimension(50, 50));
		
		JButton selectTool = new CustomButton("Select");
		toolBarPane.add(selectTool);
        toolBarPane.add(Box.createVerticalStrut(3));
		JButton cutTool = new CustomButton("Cut");
		toolBarPane.add(cutTool);
        toolBarPane.add(Box.createVerticalStrut(3));
		deleteButton = new CustomButton("Delete");
		toolBarPane.add(deleteButton);
		
		editorPane.add(toolBarPane, BorderLayout.WEST);
		
		window.add(splitPane, BorderLayout.CENTER);
		window.add(controlsPane, BorderLayout.SOUTH);
		window.pack();
		
		window.setOnResizedFunction(() -> splitPane.setDividerLocation(getOptimalDividerLocation(splitPane, editorPane)));
		
		
		splitPane.setDividerLocation(0.7);
		splitPane.setContinuousLayout(true);
		
		pauseButton.addActionListener((ActionEvent e) -> {
			togglePause();
		});
		rewindFullButton.addActionListener((ActionEvent e) -> skipTo(0));
		rewindButton.addActionListener((ActionEvent e) -> skip(-5000));
		skipButton.addActionListener((ActionEvent e) -> skip(5000));
		rewindFrameButton.addActionListener((ActionEvent e) -> previousFrame());
		skipFrameButton.addActionListener((ActionEvent e) -> nextFrame());
		
		selectTool.addActionListener((ActionEvent e) -> {
			currentTool = Tool.SELECT;
			editorCanvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		});
		cutTool.addActionListener((ActionEvent e) -> {
			currentTool = Tool.CUT;
			editorCanvas.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		});
		deleteButton.addActionListener((ActionEvent e) -> {
			toggleDeleteSelected();
		});
		
		exportButton.addActionListener((ActionEvent e) -> new Thread(() -> export()).start());
	}
	
	private void insertVideoItemToEditor() {
		videoDuration = mediaPlayerComponent.mediaPlayer().status().length();
		editArea.setVideo(videoDuration, new File(videoPath).getName());
		
		//mediaPlayerComponent.mediaPlayer().media().meta().asMetaData().get(Meta.TITLE)
	}
	
	private int getOptimalDividerLocation(JSplitPane splitPane, JPanel editorPane) {
		return (int) Math.min(0.7 * splitPane.getHeight(), splitPane.getHeight() - editorPane.getMinimumSize().height);
	}
	
	public double getCurrentTime() {
		return timer.time();
	}
	
	public void togglePause() {
		EmbeddedMediaPlayer player = mediaPlayerComponent.mediaPlayer();
		if (player.status().isPlaying()) { //PAUSED
			pauseButton.setText("Play");
		} else { //PLAYED
			pauseButton.setText("Pause");
		}
		player.controls().pause();
		paused = !paused;
	}
	
	public void play() {
		if (!paused) {
			return;
		}
		
		togglePause();
	}
	
	public void pause() {
		if (paused) {
			return;
		}
		
		togglePause();
	}
	
	public void skip(long amount) {
		skipTo(mediaPlayerComponent.mediaPlayer().status().time() + amount);
	}
	
	public void skipTo(long time) {
		timer.startPaused(time);
		mediaPlayerComponent.mediaPlayer().controls().setTime(time);
	}
	
	public void editAreaMouseClicked(MouseEvent e) {
		if (currentTool == Tool.CUT) {
			editArea.cut(e.getX(), e.getY());
		}
	}

	public void editAreaMousePressed(MouseEvent e) {
		if (currentTool == Tool.SELECT) {
			editArea.click(e.getX(), e.getY());
		}
	}

	public void editAreaMouseDragged(MouseEvent e) {
		if (currentTool == Tool.CUT) {
			
		} else {
			editArea.mouseDragged(e.getX(), e.getY());
		}
	}

	public void toggleDeleteSelected() {
		editArea.toggleDeleteSelected();
	}
	
	private void export() {
		try {
			System.out.println("Original file path: " + videoPath);
			String text = new File(videoPath).getName();
			if (Execute.programExists("ffmpeg -version")) {
				String filePath = chooseSaveLocation(new File(getDownloadsFolder(), getDefaultFileName(text) + ".mp4"));
				if (filePath == null) {
					return;
				}
				
				String[] choices = {"Very fast export without re-encode", "Slow export with re-encode"};
				
				int choice = JOptionPane.showOptionDialog(
					null,
					"Choose an encoding option. Very fast might have freezes at the starts of segments. While slow export will not.",
					"Encoding Options",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					choices,
					choices[0]
				);
				
				boolean speed;
				if (choice == 0) { //speed
					speed = true;
				} else if (choice == 1) { //slow
					speed = false;
				} else {
					return;
				}
				
				exporting = true;
				
				//make temp dir
				new File("temp").mkdir();
				
				List<Clip> clips = editArea.getActiveClips();
				
				String command = buildExportCommand1(clips, speed);
				//System.out.println("command = " + command);
				//command = "ffmpeg -i G:\\Lataukset\\Sarjat\\Foundation\\Foundation.2021.S02.720p\\Foundation.S02E01.In.Seldons.Shadow.720p.ATVP.WEB-DL.DDP5.1.H.264-CasStudio.mkv -ss 00:00:00 -t 00:01:00 -c copy -avoid_negative_ts make_zero segment1.mp4 -ss 00:02:30 -t 00:01:30 -c copy -avoid_negative_ts make_zero segment2.mp4 -ss 00:04:30 -t 68 -c copy -avoid_negative_ts make_zero segment3.mp4";
				System.out.println("Command 1: " + command);
				
				Process process = Execute.executeCommandGetProcess(command);
				new StreamGobblerText(process.getErrorStream(), StreamGobbler.Type.ERROR, true).start();
				
				process.waitFor();
				process.destroy();
				
				String command2 = buildExportCommand2(filePath, clips.size());
				System.out.println("Command 2: " + command2);
				
				Process process2 = Execute.executeCommandGetProcess(command2);
				new StreamGobblerText(process2.getErrorStream(), StreamGobbler.Type.ERROR, true).start();
				
				process2.waitFor();
				process2.destroy();
				
				//remove temp dir
				deleteWholeFolder("temp");
				
				exporting = false;
				
			} else {
				JOptionPane.showMessageDialog(
						window,
						"FFmpeg is not installed!\nInstall it first before using this program.",
						"FFmpeg is not installed",
						JOptionPane.ERROR_MESSAGE
				);
			}
		} catch (HeadlessException | InterruptedException e) {
			System.err.println("Couldn't download the file.");
			e.printStackTrace();
		}
	}
	
	private String chooseSaveLocation(File defaultFile) {
		LookAndFeel oldLook = UIManager.getLookAndFeel();
		setSystemLookAndFeel();
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		fileChooser.setSelectedFile(defaultFile);
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MP4 Files (*.mp4)", "mp4");
		fileChooser.setFileFilter(filter);

		String filePath;
		int choice = fileChooser.showSaveDialog(window);
		if (choice == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();

			if (selectedFile.exists()) {
				JOptionPane.showMessageDialog(
						window,
						"File already exists. Please choose a different name.",
						"File Exists",
						JOptionPane.WARNING_MESSAGE
				);
				return chooseSaveLocation(selectedFile); //File exists, recursively ask for save location again.
			} else {
				filePath = selectedFile.getAbsolutePath();
				
				if (!selectedFile.getName().toLowerCase().endsWith(".mp4")) {
					filePath = filePath + ".mp4";
				}
				
				System.out.println("Selected save location: " + filePath);
				setLookAndFeel(oldLook);
				return filePath;
			}
		} else if (choice == JFileChooser.CANCEL_OPTION) {
			System.out.println("User canceled!");
			setLookAndFeel(oldLook);
			return null;
		}
		
		setLookAndFeel(oldLook);
		return null;
	}
	
	private void setLookAndFeel(LookAndFeel lookAndFeel) {
		try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
	}
	
	private void setSystemLookAndFeel() {
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
	}
	
	private static File getDownloadsFolder() {
		String userHome = System.getProperty("user.home");
        File downloads = new File(userHome, "Downloads");
		if (downloads.exists() && downloads.isDirectory()) {
            return downloads;
        } else {
            return getDefaultDirectory();
        }
    }
	
	private static File getDefaultDirectory() {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        return fileSystemView.getDefaultDirectory();
    }
	
	private static String addQuotes(String text) {
		return "\"" + text + "\"";
	}
	
	private static String getDefaultFileName(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
		
		if (lastDotIndex >= 0) {
			return filename.substring(0, lastDotIndex);
		}
		
		return "filename";
	}
	
	public Tool getCurrentTool() {
		return currentTool;
	}
	
	private String buildExportCommand1(List<Clip> clips, boolean speed) {
		String command = "ffmpeg -i " + addQuotes(videoPath);
		
		DecimalFormat df = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
		
		for (int i = 0; i < clips.size(); i++) {
			Clip clip = clips.get(i);
			String start = df.format(clip.getOriginalStart() / 1000);
			String dur = df.format((clip.getOriginalEnd() - clip.getOriginalStart()) / 1000);
			
			
			if (speed) {
				//command += " -ss " + start + " -t " + dur + " -c copy -avoid_negative_ts make_zero temp/segment" + i + ".mp4"; //Seems to have big freeze at the start
				command += " -ss " + start + " -t " + dur + " -c copy -copyts temp/segment" + i + ".mp4"; //might be smaller freeze, but the first 2 commands are very fast
			} else {
				//command += " -ss " + start + " -t " + dur + " -c:v libx264 -c:a copy temp/segment" + i + ".mp4";
				command += " -ss " + start + " -t " + dur + " -c:v libx264 -crf 18 -c:a copy temp/segment" + i + ".mp4"; //doesn't have freeze, but is a re-encode, slow.
			}
			
			//command += " -ss " + start + " -t " + dur + " -c:v libx264 -crf 18 -tune film -profile:v high -c:a copy temp/segment" + i + ".mp4"; //re-encode, not sure about quality
		}
		
		//Will look like this: "ffmpeg -i " + addQuotes(videoPath) + " -ss 00:00:00 -t 00:01:00 -c copy -avoid_negative_ts make_zero temp/segment1.mp4 -ss 00:02:30 -t 00:01:30 -c copy -avoid_negative_ts make_zero temp/segment2.mp4 -ss 00:04:30 -c copy -avoid_negative_ts make_zero temp/segment3.mp4";
		
		return command;
	}
	
	private String buildExportCommand2(String filePath, int nrOfClips) {
		String command = "(echo";
		
		//(this is the cmd version)
		for (int i = 0; i < nrOfClips; i++) {
			//needs absolute paths
			File file = new File("temp/segment" + i + ".mp4");
			if (i != 0) {
				command += " & echo";
			}
			command += " file '" + file.getAbsolutePath() + "'";
		}
		
		command += ") | ffmpeg -protocol_whitelist file,pipe -f concat -safe 0 -i pipe:0 -c copy " + filePath;
		
		//Will look like this (but with absolute paths) (cmd version): "(echo" + " file 'temp/segment1.mp4'" + " & echo file 'temp/segment2.mp4'" + " & echo file 'temp/segment3.mp4')" + " | ffmpeg -protocol_whitelist file,pipe -f concat -safe 0 -i pipe:0 -c copy output_combined.mp4";
		
		return command;
	}
	
	private void deleteWholeFolder(String name) {
		File folder = new File(name);
		
		String[] entries = folder.list();
		
		for (String s : entries){
			File currentFile = new File(folder.getPath(), s);
			currentFile.delete();
		}
		
		folder.delete();
	}
	
	private void previousFrame() {
		long diff = 100; //since there's not a good function for this, and it's buggy, we just go back 100ms
		skipTo(timer.time() - diff);
	}
	
	private void nextFrame() {
		mediaPlayerComponent.mediaPlayer().controls().nextFrame();
	}

	public void cutCurrentTime() {
		editArea.cutAtTime(getCurrentTime());
	}
}
