package com.trabalho;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Simulador.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Aplicativo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
