package com.trabalho.view;

import com.trabalho.server.Servidor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {

    private Circle statusCircle;
    private Label addressPortLabel;
    private Servidor servidor;
    private TextArea responses;
    private String endereco;
    private int porta;

    private ObservableList<Device> conexoesList = FXCollections.observableArrayList();
    private ObservableList<Topic> topicsList = FXCollections.observableArrayList();
    private ObservableList<Status> statusList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // AppBar at the top
        BorderPane appBar = new BorderPane();
        appBar.setPadding(new Insets(10));
        appBar.setStyle("-fx-background-color: #f0f0f0;");

        Label titleLabel = new Label("Simulador");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        statusCircle = new Circle(10, Color.RED);

        addressPortLabel = new Label("");
        addressPortLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox centerAppBar = new HBox();
        centerAppBar.setAlignment(Pos.CENTER);
        centerAppBar.getChildren().add(addressPortLabel);

        // Group the status circle
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.getChildren().add(statusCircle);

        appBar.setLeft(titleLabel);
        appBar.setCenter(centerAppBar);
        appBar.setRight(statusBox);
        BorderPane.setAlignment(statusBox, Pos.CENTER_RIGHT);

        // Sidebar on the left
        VBox sideBar = new VBox(10);
        sideBar.setPadding(new Insets(10));
        sideBar.setStyle("-fx-background-color: #e0e0e0;");
        sideBar.setPrefWidth(250); // Increase Sidebar width

        Button ligar = new Button("Iniciar Servidor");
        Button microcontrolador = new Button("Microcontrolador");
        Button sair = new Button("Sair");

        ligar.setOnAction(e -> ligarDialog());
        microcontrolador.setOnAction(e -> microcontroladorDialog());
        sair.setOnAction(e -> Platform.exit());

        sideBar.getChildren().addAll(ligar, microcontrolador, sair);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER);

        grid.setEffect(new DropShadow());

        for (int i = 0; i < 2; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(50);
            grid.getRowConstraints().add(rowConstraints);

            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(50);
            grid.getColumnConstraints().add(colConstraints);
        }

        // **First Change: Replace first table with scrollable card for connections**

        // Create a VBox to hold connections
        VBox connectionsBox = new VBox();
        connectionsBox.setSpacing(5);
        connectionsBox.setPadding(new Insets(10));

        // Wrap the VBox in a ScrollPane
        ScrollPane connectionsScrollPane = new ScrollPane(connectionsBox);
        connectionsScrollPane.setFitToWidth(true);

        // Add the ScrollPane to the grid
        grid.add(connectionsScrollPane, 0, 0);

        // Observe the connections list and update the VBox when it changes
        conexoesList.addListener((ListChangeListener<Device>) c -> {
            Platform.runLater(() -> {
                connectionsBox.getChildren().clear();
                for (Device device : conexoesList) {
                    Label label = new Label("id: " + device.getId() + " endereco: " + device.getAddress() + " porta: " + device.getPort());
                    connectionsBox.getChildren().add(label);
                }
            });
        });

        // **Second Change: Replace second table with scrollable card for topics**

        // Create a VBox to hold topics
        VBox topicsBox = new VBox();
        topicsBox.setSpacing(5);
        topicsBox.setPadding(new Insets(10));

        // Wrap the VBox in a ScrollPane
        ScrollPane topicsScrollPane = new ScrollPane(topicsBox);
        topicsScrollPane.setFitToWidth(true);

        // Add the ScrollPane to the grid
        grid.add(topicsScrollPane, 1, 0);

        // Observe the topics list and update the VBox when it changes
        topicsList.addListener((ListChangeListener<Topic>) c -> {
            Platform.runLater(() -> {
                topicsBox.getChildren().clear();
                for (Topic topic : topicsList) {
                    Label label = new Label("id: " + topic.getId() + " topico: " + topic.getTopic());
                    topicsBox.getChildren().add(label);
                }
            });
        });

        responses = new TextArea();
        grid.add(responses, 0, 1);

        // **Third Change: Add a new table in cell (1,1) with specified columns**

        // Create the status table and add it to the grid
        TableView<Status> tabela_status = tabelaStatus();
        grid.add(tabela_status, 1, 1);

        // Set grow priorities for the grid cells
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                GridPane.setHgrow(grid.getChildren().get(row * 2 + col), Priority.ALWAYS);
                GridPane.setVgrow(grid.getChildren().get(row * 2 + col), Priority.ALWAYS);
            }
        }

        root.setCenter(grid);
        BorderPane.setAlignment(grid, Pos.CENTER);
        BorderPane.setMargin(grid, new Insets(10));

        root.setTop(appBar);
        root.setLeft(sideBar);

        // Scene configuration
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Simulador");
        primaryStage.setScene(scene);

        primaryStage.setFullScreen(true);

        primaryStage.show();
    }

    private void ligarDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Ligar Servidor");

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
                    statusCircle.setFill(Color.GREEN);
                    this.servidor = new Servidor(endereco, porta, queueName, true, this.responses);
                    new Thread(() -> {
                        this.servidor.startServer();
                    }).start();

                    new Thread(() -> {
                        this.servidor.startQueue();
                    }).start();

                    addressPortLabel.setText("Servidor ligado (" + endereco + ":" + portStr + ")");
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

        VBox vbox = new VBox(10, addressLabel, addressField, portLabel, portField, queueLabel, queueField, ligar, cancelar);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 400, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void microcontroladorDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Microcontrolador");

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
                        System.out.println(op + " " + idSala);
                        // Send command to specific room
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

        VBox vbox = new VBox(20, card1, card2, buttonBox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 400, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // Method to create the status table
    @SuppressWarnings("unchecked")
    private TableView<Status> tabelaStatus() {
        TableView<Status> table = new TableView<>();

        TableColumn<Status, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<Status, Number> ligadosColumn = new TableColumn<>("Ligados");
        ligadosColumn.setCellValueFactory(cellData -> cellData.getValue().ligadosProperty());

        TableColumn<Status, Number> desligadosColumn = new TableColumn<>("Desligados");
        desligadosColumn.setCellValueFactory(cellData -> cellData.getValue().desligadosProperty());

        TableColumn<Status, String> servidorColumn = new TableColumn<>("Servidor");
        servidorColumn.setCellValueFactory(cellData -> cellData.getValue().servidorProperty());

        table.getColumns().addAll(idColumn, ligadosColumn, desligadosColumn, servidorColumn);

        table.setItems(statusList);

        return table;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Class to represent a Device
    public static class Device {
        private Integer id;
        private String address;
        private String port;

        public Device(Integer id, String address, String port) {
            this.id = id;
            this.address = address;
            this.port = port;
        }

        public Integer getId() {
            return id;
        }

        public String getAddress() {
            return address;
        }

        public String getPort() {
            return port;
        }
    }

    // Class to represent a Topic
    public static class Topic {
        private int id;
        private String topic;

        public Topic(int id, String topic) {
            this.id = id;
            this.topic = topic;
        }

        public int getId() {
            return id;
        }

        public String getTopic() {
            return topic;
        }
    }

    // Class to represent Status data
    public static class Status {
        private IntegerProperty id;
        private IntegerProperty ligados;
        private IntegerProperty desligados;
        private StringProperty servidor;

        public Status(int id, int ligados, int desligados, String servidor) {
            this.id = new SimpleIntegerProperty(id);
            this.ligados = new SimpleIntegerProperty(ligados);
            this.desligados = new SimpleIntegerProperty(desligados);
            this.servidor = new SimpleStringProperty(servidor);
        }

        public int getId() {
            return id.get();
        }

        public IntegerProperty idProperty() {
            return id;
        }

        public int getLigados() {
            return ligados.get();
        }

        public IntegerProperty ligadosProperty() {
            return ligados;
        }

        public int getDesligados() {
            return desligados.get();
        }

        public IntegerProperty desligadosProperty() {
            return desligados;
        }

        public String getServidor() {
            return servidor.get();
        }

        public StringProperty servidorProperty() {
            return servidor;
        }
    }

    // Methods to update the lists
    public void addConnection(Device device) {
        Platform.runLater(() -> {
            conexoesList.add(device);
        });
    }

    public void addTopic(Topic topic) {
        Platform.runLater(() -> {
            topicsList.add(topic);
        });
    }

    public void updateStatus(Status status) {
        Platform.runLater(() -> {
            statusList.add(status);
        });
    }
}
