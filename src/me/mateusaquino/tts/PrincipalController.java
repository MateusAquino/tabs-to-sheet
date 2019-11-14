package me.mateusaquino.tts;

import java.lang.String;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.Sequence;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.midi.MidiParserListener;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.ManagedPlayer;
import org.staccato.StaccatoParser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class PrincipalController {
    @FXML private Button play;
    @FXML private Button stop;
    @FXML private Button save;
    @FXML private Slider speed;
    @FXML private Slider octave;
    @FXML private SplitMenuButton file;
    @FXML private TextArea tab;
    @FXML private Label fail;
    @FXML private TextField bpm;
    @FXML private ComboBox<String> instrument;
    private ManagedPlayer player;
    private Pattern seq;
    private File saveFile;
    private StaccatoParser staccatoParser;
    private MidiParserListener midiParserListener;
    Thread playerThread;
    
    @FXML public void initialize(){
    	player = new ManagedPlayer();
    	stop.setDisable(true);
    	saveFile = new File(System.getProperty("user.home")+"/Desktop/file.midi");
    	seq = new Pattern("").setTempo((int) (speed.getValue()*5));
    	staccatoParser = new StaccatoParser();
		midiParserListener = new MidiParserListener();
		staccatoParser.addParserListener(midiParserListener);
    	file.setText("file.midi");
    	instrument.setValue("Piano");
    	instrument.getItems().addAll("Piano", "Violin", "Guitar", "Harmonica",
    								 "Drawbar_Organ", "Acoustic_Bass", "Guitar_Harmonics", 
    								 "Steel_String_Guitar", "Distortion_Guitar", "Viola", 
    								 "Cello", "Pizzicato_Strings", "Trumpet", "Trombone",
    								 "Soprano_Sax", "Clarinet");
    	play.setOnMouseClicked(e->actionPlay());
    	stop.setOnMouseClicked(e->actionStop());
    	save.setOnMouseClicked(e->actionSave());
    	file.setOnMouseClicked(e->actionChoose());
    	
    	speed.valueProperty().addListener((e, o, n)->bpm.setText(n.intValue()+""));
    	bpm.textProperty().addListener((e, o, n)->{
    		if(n.isEmpty())
    			bpm.setText("0");
    		else if (!n.matches("^\\d+$"))
    			bpm.setText(o);
    		int val = Integer.parseInt(bpm.getText());
    		if (val>50&&val<250)
    			speed.setValue(val);
    		bpm.setText(val+"");
    	});
    	
    	bpm.focusedProperty().addListener((e, o, n) -> {if (!n) {
    		int val = Integer.parseInt(bpm.getText());
    		val = val>250 ? 250 : val<50 ? 50 : val; // Valores apenas entre 50-250 para bpm
    		speed.setValue(val);
    		bpm.setText(val+"");
    	}});
    	
    	tab.setText("E|----3-3-5-5--3-3-1-1-0-0-------------------|\n"+
    				"B|-1-1--------------------3-3-1--------------|\n"+
    				"G|-------------------------------------------|\n"+
    				"D|-------------------------------------------|\n"+
    				"A|-------------------------------------------|\n"+
    				"E|-------------------------------------------|\n"+
    				"\n"+
    				"E|-3-3-1-1-0-0-----3-3-1-1-0-0---------------|\n"+
    				"B|-------------3-3-------------3-------------|\n"+
    				"G|-------------------------------------------|\n"+
    				"D|-------------------------------------------|\n"+
    				"A|-------------------------------------------|\n"+
    				"E|-------------------------------------------|\n"+
    				"\n"+
    				"E|----3-3-5-5-3-3-1-1-0-0--------------------|\n"+
    				"B|-1-1--------------------3-3-1--------------|\n"+
    				"G|-------------------------------------------|\n"+
    				"D|-------------------------------------------|\n"+
    				"A|-------------------------------------------|\n"+
    				"E|-------------------------------------------|\n");
    }
    
    public void actionStop(){
    	play.setText("Play");
    	try {
    		player.pause();
    		player.finish();
    	} catch(Exception e){}
    	playerThread.interrupt();
    	stop.setDisable(true);
    }
    
    public void actionPlay(){
    	if (stop.isDisabled()){
    		playerThread = new Thread(()->{
    			try {
    				tabToMidi();
    			} catch(Exception e){
    				System.out.println("Failed to recognize tab!");
    				Platform.runLater(()->{
    					fail.setVisible(true);
    					play.setText("Play");
    					stop.setDisable(true);
    				});
    				return;
    			}
    			Platform.runLater(()->fail.setVisible(false));
				try {
					player.start(getSequence());
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (!player.isFinished()) {
					try { Thread.sleep(20L); } catch (InterruptedException arg2) {}
				}
				Platform.runLater(()->{
					play.setText("Play");
					stop.setDisable(true);
				});
			});
    		playerThread.start();
    		play.setText("Pause");
    	} else {
    		if (play.getText().equals("Play")) {
    			player.resume();
    			play.setText("Pause");
    		} else {
    			player.pause();
    			play.setText("Play");
    		}
    	}
    	stop.setDisable(false);
    }
    
    @SuppressWarnings("serial")
	public void actionChoose(){
    	FileChooser fc = new FileChooser();
    	fc.setInitialDirectory(saveFile.getParentFile());
    	fc.setInitialFileName("file.midi");
    	fc.getExtensionFilters().add(new ExtensionFilter("Midi Files", new ArrayList<String>(){{add("*.midi");}}));
    	File f = fc.showSaveDialog(Main.s);
    	if (f!=null){
    		file.setText(f.getName());
    		saveFile = f;
    	}
    }
    
    public void actionSave(){
    	tabToMidi();
    	try {
			MidiFileManager.save(getSequence(), saveFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
    public void tabToMidi(){
    	String tabs = tab.getText().trim();
    	System.out.println(tabs);
    	Guitar g = new Guitar(tabs, (int)octave.getValue());
    	String melody = g.toMelody();
    	melody = MelodyFix.fix(melody, instrument.getValue());
    	System.out.println("RES: " + melody);
    	
    	seq = new Pattern(""+melody).setTempo((int) (speed.getValue()));
    }
    
    private Sequence getSequence(){
    	staccatoParser.parse(seq);
		return midiParserListener.getSequence();
    }
}