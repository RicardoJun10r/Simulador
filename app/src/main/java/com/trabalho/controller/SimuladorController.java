package com.trabalho.controller;

import com.trabalho.server.Servidor;
import com.trabalho.shared.Comando;
import com.trabalho.util.Aparelho;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SimuladorController {

    private String endereco;

    private int porta;

    private Servidor servidor;

    private Thread t_server;

    private Thread t_queue;

    @FXML
    private Circle circle_toogle;

    @FXML
    private Label endereco_porta_lb;

    @FXML
    private TableView<Aparelho> microcontroladores_tabela;

    @FXML
    private TableView<?> servidores_tabela;

    private ObservableList<Aparelho> microcontroladoresData = FXCollections.observableArrayList();

    public ObservableList<Aparelho> getData(){ return this.microcontroladoresData; }

    @FXML
    private TableColumn<Aparelho, Integer> colId;

    @FXML
    private TableColumn<Aparelho, String> colEndereco;

    @FXML
    private TableColumn<Aparelho, Integer> colAparelhosLigados;

    @FXML
    private TableColumn<Aparelho, Integer> colAparelhosDesligados;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colAparelhosLigados.setCellValueFactory(new PropertyValueFactory<>("aparelhosLigados"));
        colAparelhosDesligados.setCellValueFactory(new PropertyValueFactory<>("aparelhosDesligados"));

        microcontroladores_tabela.setItems(microcontroladoresData);
    }

    @FXML
    void ligarServidor(ActionEvent event) {
        ligarServidorDialog();
    }

    @FXML
    void microcontroladorDialog(ActionEvent event) {
        microcontrolador();
    }

    @FXML
    void servidorDialog(ActionEvent event) {
        servidor();
    }

    @FXML
    void sair(ActionEvent event) {
        if (this.t_server != null)
            this.t_server.interrupt();
        if (this.t_queue != null)
            this.t_queue.interrupt();
        Platform.exit();
    }

    private void ligarServidorDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Ligar Servidor");

        Label instrucao = new Label("Digite o endereço e porta do seu servidor e\na uri do seu broker de mensagens.");

        Label addressLabel = new Label("Endereço:");
        TextField addressField = new TextField();
        addressField.setText("127.0.0.1");

        Label portLabel = new Label("Porta:");
        TextField portField = new TextField();
        portField.setText("5000");

        Label queueLabel = new Label("Nome da Fila:");
        TextField queueField = new TextField();
        queueField.setText("tcp://mqtt.eclipseprojects.io:1883");

        Button ligar = new Button("Ligar");
        Button cancelar = new Button("Cancelar");

        ligar.setOnAction(e -> {
            endereco = addressField.getText();
            String portStr = portField.getText();
            String queueName = queueField.getText();

            if (!endereco.isEmpty() && !portStr.isEmpty() && !queueName.isEmpty()) {
                try {
                    porta = Integer.parseInt(portStr);
                    this.circle_toogle.setFill(Color.GREEN);
                    this.servidor = new Servidor(endereco, porta, queueName, true, this);
                    this.t_server = new Thread(() -> {
                        this.servidor.startServer();
                    });

                    this.t_queue = new Thread(() -> {
                        this.servidor.startQueue();
                    });

                    this.t_server.start();
                    this.t_queue.start();

                    this.endereco_porta_lb.setText("Servidor ligado (" + endereco + ":" + portStr + ")");
                    this.endereco_porta_lb.setVisible(true);
                    dialog.close();
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Porta deve ser um número inteiro.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, preencha todos os campos.");
                alert.showAndWait();
            }
        });

        cancelar.setOnAction(e -> {
            dialog.close();
        });

        VBox vbox = new VBox(10, instrucao, addressLabel, addressField, portLabel, portField, queueLabel, queueField,
                ligar,
                cancelar);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 400, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void servidor() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Servidor");

        Label instrucao = new Label(
                "Aqui é possível acessar e controlar remotamente\nos servidores conectados a esse servidor,\n digite a opção que deseja e o destino da mensagem.");

        Label addressLabel = new Label("Endereço:");
        TextField addressField = new TextField();

        Label portLabel = new Label("Porta:");
        TextField portField = new TextField();

        Button conectar = new Button("Conectar");
        Button cancelar = new Button("Cancelar");

        conectar.setOnAction(e -> {
            String endereco_nc = addressField.getText();
            String portStr = portField.getText();

            if (!endereco_nc.isEmpty() && !portStr.isEmpty()) {
                try {
                    int porta_nc = Integer.parseInt(portStr);

                    Comando comando = new Comando(3, endereco_nc, porta_nc);

                    this.servidor.addComando(comando);

                    dialog.close();
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Porta deve ser um número inteiro.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, preencha todos os campos.");
                alert.showAndWait();
            }
        });

        cancelar.setOnAction(e -> {
            dialog.close();
        });

        VBox vbox = new VBox(10, instrucao, addressLabel, addressField, portLabel, portField, conectar, cancelar);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 300, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void microcontrolador() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Microcontrolador");

        Label instrucao = new Label(
                "Aqui é possível acessar e controlar remotamente\nos microcontroladores conectados a esse servidor,\n digite a opção que deseja e o destino da mensagem.");

        Label group1Label = new Label("Opções:");
        ToggleGroup opcao_microcontrolador = new ToggleGroup();
        RadioButton ligar = new RadioButton("Ligar");
        RadioButton desligar = new RadioButton("Desligar");
        RadioButton descrever = new RadioButton("Descrever");
        ligar.setToggleGroup(opcao_microcontrolador);
        desligar.setToggleGroup(opcao_microcontrolador);
        descrever.setToggleGroup(opcao_microcontrolador);

        VBox group1Box = new VBox(5, ligar, desligar, descrever);
        group1Box.setAlignment(Pos.CENTER);

        VBox card1 = new VBox(10, group1Label, group1Box);
        card1.setAlignment(Pos.CENTER);
        card1.setPadding(new Insets(10));
        card1.setStyle(
                "-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        Label group2Label = new Label("Opções:");
        ToggleGroup id_microcontrolador = new ToggleGroup();
        RadioButton enviar_para_um = new RadioButton("Uma Sala");
        RadioButton enviar_para_todos = new RadioButton("Todas as salas");
        enviar_para_um.setToggleGroup(id_microcontrolador);
        enviar_para_todos.setToggleGroup(id_microcontrolador);

        VBox group2Box = new VBox(5, enviar_para_um, enviar_para_todos);
        group2Box.setAlignment(Pos.CENTER);

        Label inputLabel = new Label("Digite o ID da sala:");
        TextField inputField = new TextField();
        inputLabel.setVisible(false);
        inputField.setVisible(false);

        id_microcontrolador.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == enviar_para_um) {
                inputLabel.setVisible(true);
                inputField.setVisible(true);
            } else {
                inputLabel.setVisible(false);
                inputField.setVisible(false);
            }
        });

        VBox card2 = new VBox(10, group2Label, group2Box, inputLabel, inputField);
        card2.setAlignment(Pos.CENTER);
        card2.setPadding(new Insets(10));
        card2.setStyle(
                "-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        Button submit = new Button("Enviar");
        Button cancelar = new Button("Cancelar");

        submit.setOnAction(e -> {
            RadioButton opcaoSelecionada = (RadioButton) opcao_microcontrolador.getSelectedToggle();
            RadioButton destinatarioSelecionado = (RadioButton) id_microcontrolador.getSelectedToggle();

            if (opcaoSelecionada != null && destinatarioSelecionado != null) {
                int op;
                switch (opcaoSelecionada.getText()) {
                    case "Desligar":
                        op = 0;
                        break;
                    case "Ligar":
                        op = 1;
                        break;
                    case "Descrever":
                        op = 2;
                        break;
                    default:
                        op = 2;
                        break;
                }

                if (destinatarioSelecionado == enviar_para_um) {
                    String idSalaStr = inputField.getText();
                    if (!idSalaStr.isEmpty()) {
                        int idSala = Integer.parseInt(idSalaStr);
                        // Send command to specific room
                        Comando comando = new Comando(0, op, idSala, -1, "", -1);
                        servidor.addComando(comando);
                        dialog.close();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText(null);
                        alert.setContentText("Por favor, digite o ID da sala.");
                        alert.showAndWait();
                        return;
                    }
                } else {
                    // Send command to all rooms
                    Comando comando = new Comando(0, op, -1, -1, "", -1);
                    servidor.addComando(comando);
                    dialog.close();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, selecione todas as opções.");
                alert.showAndWait();
            }
        });

        cancelar.setOnAction(e -> {
            dialog.close();
        });

        HBox buttonBox = new HBox(10, submit, cancelar);
        buttonBox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(20, instrucao, card1, card2, buttonBox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 400, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
