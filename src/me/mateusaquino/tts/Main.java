package me.mateusaquino.tts;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	public static Stage s; 
	@Override
	public void start(Stage primaryStage) throws Exception {
		s = primaryStage;
		primaryStage.setScene(new Scene(FXMLLoader.load(Main.class.getResource("Principal.fxml"))));
		primaryStage.show();
		primaryStage.setTitle("Tabs to Sheets");
	}
	
	public static void main(String args[]){
		launch();
	}
}