package com.trabalho.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;

import com.trabalho.server.Servidor;
import com.trabalho.shared.Comando;
import com.trabalho.util.Aparelho;
import com.trabalho.util.Conexao;
import com.trabalho.controller.styles.Estilos;

public class SimuladorController {

    /* =================== FXML =================== */

    @FXML
    private Pane animationPane;
    @FXML
    private Circle circle_toogle;
    @FXML
    private Label endereco_porta_lb;
    @FXML
    private TableView<Aparelho> microcontroladores_tabela;
    @FXML
    private TableView<Conexao> servidores_tabela;
    @FXML
    private TableColumn<Conexao, Integer> colSId;
    @FXML
    private TableColumn<Conexao, String> colSEndereco;
    @FXML
    private TableColumn<Conexao, Integer> colPorta;
    @FXML
    private TableColumn<Aparelho, Integer> colMId;
    @FXML
    private TableColumn<Aparelho, String> colMEndereco;
    @FXML
    private TableColumn<Aparelho, Integer> colAparelhosLigados;
    @FXML
    private TableColumn<Aparelho, Integer> colAparelhosDesligados;

    /* =================== Variáveis de Instância =================== */
    private String endereco;
    private int porta;
    private Servidor servidor;
    private Thread t_server;
    private Thread t_queue;

    private final ObservableList<Integer> idServidorList = FXCollections.observableArrayList();
    private final ObservableList<Aparelho> microcontroladoresData = FXCollections.observableArrayList();
    private final ObservableList<Conexao> servidoresData = FXCollections.observableArrayList();

    /* =================== Métodos de Acesso =================== */
    public ObservableList<Aparelho> getMicrocontroladoresTable() {
        return microcontroladoresData;
    }

    public ObservableList<Conexao> getServidoresTable() {
        return servidoresData;
    }

    /* =================== Inicialização =================== */
    @FXML
    private void initialize() {
        // Configuração das colunas da tabela de microcontroladores
        colMId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colMEndereco.setCellValueFactory(cellData -> cellData.getValue().enderecoProperty());
        colAparelhosLigados.setCellValueFactory(cellData -> cellData.getValue().aparelhosLigadosProperty().asObject());
        colAparelhosDesligados
                .setCellValueFactory(cellData -> cellData.getValue().aparelhosDesligadosProperty().asObject());

        // Configuração das colunas da tabela de servidores
        colSId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colSEndereco.setCellValueFactory(cellData -> cellData.getValue().enderecoProperty());
        colPorta.setCellValueFactory(cellData -> cellData.getValue().portaProperty().asObject());

        // Associação dos dados às tabelas
        servidores_tabela.setItems(servidoresData);
        microcontroladores_tabela.setItems(microcontroladoresData);

        // Atualiza a lista de IDs de servidor quando houver mudanças
        servidoresData.addListener((ListChangeListener<Conexao>) change -> idServidorList
                .setAll(servidoresData.stream().map(Conexao::getId).collect(Collectors.toList())));
        idServidorList.setAll(servidoresData.stream().map(Conexao::getId).collect(Collectors.toList()));

        // Aplica estilos às tabelas e cabeçalhos
        microcontroladores_tabela.setStyle(Estilos.TABLE_STYLE);
        servidores_tabela.setStyle(Estilos.TABLE_STYLE);

        colMId.setStyle(Estilos.TABLE_HEADER_STYLE);
        colMEndereco.setStyle(Estilos.TABLE_HEADER_STYLE);
        colAparelhosLigados.setStyle(Estilos.TABLE_HEADER_STYLE);
        colAparelhosDesligados.setStyle(Estilos.TABLE_HEADER_STYLE);
        colSId.setStyle(Estilos.TABLE_HEADER_STYLE);
        colSEndereco.setStyle(Estilos.TABLE_HEADER_STYLE);
        colPorta.setStyle(Estilos.TABLE_HEADER_STYLE);

        // Define células com estilo para cada coluna
        colMId.setCellFactory(col -> createStyledTableCell());
        colMEndereco.setCellFactory(col -> createStyledTableCell());
        colAparelhosLigados.setCellFactory(col -> createStyledTableCell());
        colAparelhosDesligados.setCellFactory(col -> createStyledTableCell());
        colSId.setCellFactory(col -> createStyledTableCell());
        colSEndereco.setCellFactory(col -> createStyledTableCell());
        colPorta.setCellFactory(col -> createStyledTableCell());

        // Configura linhas com efeito hover para ambas as tabelas
        microcontroladores_tabela.setRowFactory(tv -> createHoverableTableRow());
        servidores_tabela.setRowFactory(tv -> createHoverableTableRow());

        // Configura a animação na célula do grid
        animationPane.widthProperty().addListener((obs, oldVal, newVal) -> setupNetworkAnimation());
        animationPane.heightProperty().addListener((obs, oldVal, newVal) -> setupNetworkAnimation());

        // Inicializa a animação (ela exibirá 4 círculos por padrão se não houver
        // microcontroladores)
        servidoresData.addListener((ListChangeListener<Conexao>) change -> setupNetworkAnimation());
        microcontroladoresData.addListener((ListChangeListener<Aparelho>) change -> setupNetworkAnimation());
    }

    private <S, T> TableCell<S, T> createStyledTableCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setStyle(Estilos.TABLE_CELL_STYLE);
                }
            }
        };
    }

    private <T> TableRow<T> createHoverableTableRow() {
        TableRow<T> row = new TableRow<>();
        row.setStyle("-fx-background-color: #FFFFFF;");
        row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (!row.isEmpty()) {
                row.setStyle(isNowHovered ? "-fx-background-color: #F5F5F5;" : "-fx-background-color: #FFFFFF;");
            }
        });
        return row;
    }

    private void setupNetworkAnimation() {
        animationPane.getChildren().clear();

        double paneWidth = animationPane.getWidth();
        double paneHeight = animationPane.getHeight();
        if (paneWidth <= 0 || paneHeight <= 0) {
            paneWidth = 400;
            paneHeight = 400;
        }

        // ---- Desenhar os Servidores (triângulos) ----
        int serverCount = servidoresData.size();
        if (serverCount == 0) {
            serverCount = 1; // Caso não haja servidores, usa 1 para demonstração
        }
        double serverY = paneHeight * 0.25;
        double spacing = paneWidth / (serverCount + 1);
        List<Point2D> serverPositions = new ArrayList<>();
        for (int i = 0; i < serverCount; i++) {
            double x = spacing * (i + 1);
            serverPositions.add(new Point2D(x, serverY));

            // Cria o triângulo para o servidor
            double triangleSize = 40;
            Polygon triangle = new Polygon();
            triangle.getPoints().addAll(
                    x - triangleSize / 2, serverY + triangleSize / 2,
                    x + triangleSize / 2, serverY + triangleSize / 2,
                    x, serverY - triangleSize / 2);
            triangle.setFill(Color.DARKBLUE);
            animationPane.getChildren().add(triangle);
        }

        // Conecta os servidores entre si
        if (serverPositions.size() > 1) {
            for (int i = 0; i < serverPositions.size() - 1; i++) {
                Point2D p1 = serverPositions.get(i);
                Point2D p2 = serverPositions.get(i + 1);
                Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(2);
                animationPane.getChildren().add(0, line);
            }
        }

        // ---- Desenhar os Microcontroladores (quadrados) e conexões ----
        int microCount = microcontroladoresData.size();
        double microCenterX = paneWidth / 2;
        double microCenterY = paneHeight * 0.65;
        double distribRadius = Math.min(paneWidth, paneHeight) / 3;

        for (int i = 0; i < microCount; i++) {
            double angle = 2 * Math.PI * i / microCount;
            double microX = microCenterX + distribRadius * Math.cos(angle);
            double microY = microCenterY + distribRadius * Math.sin(angle);

            double rectSize = 20;
            Rectangle square = new Rectangle(microX - rectSize / 2, microY - rectSize / 2, rectSize, rectSize);
            square.setFill(Color.FORESTGREEN);
            animationPane.getChildren().add(square);

            ScaleTransition st = new ScaleTransition(Duration.seconds(1.5), square);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(1.5);
            st.setToY(1.5);
            st.setCycleCount(Animation.INDEFINITE);
            st.setAutoReverse(true);
            st.play();

            for (Point2D serverPos : serverPositions) {
                Line connectionLine = new Line(serverPos.getX(), serverPos.getY(), microX, microY);
                connectionLine.setStroke(Color.GRAY);
                connectionLine.setStrokeWidth(2);
                animationPane.getChildren().add(0, connectionLine);
            }
        }
    }

    /* =================== Handlers FXML =================== */
    @FXML
    void ligarServidor(ActionEvent event) {
        showLigarServidorDialog();
    }

    @FXML
    void microcontroladorDialog(ActionEvent event) {
        showMicrocontroladorDialog();
    }

    @FXML
    void servidorDialog(ActionEvent event) {
        showServidorDialog();
    }

    @FXML
    void conexaoDialog(ActionEvent event) {
        showAdicionarConexaoDialog();
    }

    @FXML
    void sair(ActionEvent event) {
        if (t_server != null) {
            t_server.interrupt();
        }
        if (t_queue != null) {
            t_queue.interrupt();
        }
        javafx.application.Platform.exit();
    }

    /* =================== Diálogos =================== */
    private void showAdicionarConexaoDialog() {
        Stage dialog = createDialogStage("Adicionar Conexão");

        Label titleLabel = new Label("Adicionar Novas Conexões");
        titleLabel.setStyle(Estilos.TITLE_STYLE);

        VBox form = new VBox(15);
        form.setStyle(Estilos.CARD_STYLE);

        Label addressLabel = new Label("Endereço:");
        addressLabel.setStyle(Estilos.LABEL_STYLE);
        TextField addressField = new TextField("127.0.0.1");
        addressField.setStyle(Estilos.TEXT_FIELD_STYLE);

        Label portLabel = new Label("Porta:");
        portLabel.setStyle(Estilos.LABEL_STYLE);
        TextField portField = new TextField("5001");
        portField.setStyle(Estilos.TEXT_FIELD_STYLE);

        Button conectar = new Button("Conectar");
        conectar.setStyle(Estilos.BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(Estilos.CANCEL_BUTTON_STYLE);

        conectar.setOnAction(e -> {
            String enderecoNc = addressField.getText();
            String portStr = portField.getText();
            if (!enderecoNc.isEmpty() && !portStr.isEmpty()) {
                try {
                    int portaNc = Integer.parseInt(portStr);
                    Comando comando = new Comando(3, enderecoNc, portaNc);
                    servidor.addComando(comando);
                    dialog.close();
                } catch (NumberFormatException ex) {
                    showAlert("Erro", "Porta deve ser um número inteiro.");
                }
            } else {
                showAlert("Erro", "Por favor, preencha todos os campos.");
            }
        });

        cancelar.setOnAction(e -> dialog.close());

        form.getChildren().addAll(addressLabel, addressField, portLabel, portField);

        HBox buttons = new HBox(10, conectar, cancelar);
        buttons.setAlignment(Pos.CENTER);

        VBox container = new VBox(20, titleLabel, form, buttons);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        styleDialog(dialog, container);
        dialog.setScene(new Scene(container));
        dialog.showAndWait();
    }

    private void showLigarServidorDialog() {
        Stage dialog = createDialogStage("Configuração do Servidor");

        Label titleLabel = new Label("Configurar Servidor");
        titleLabel.setStyle(Estilos.TITLE_STYLE);

        VBox form = new VBox(15);
        form.setStyle(Estilos.CARD_STYLE);

        Label addressLabel = new Label("Endereço:");
        addressLabel.setStyle(Estilos.LABEL_STYLE);
        TextField addressField = new TextField("127.0.0.1");
        addressField.setStyle(Estilos.TEXT_FIELD_STYLE);

        Label portLabel = new Label("Porta:");
        portLabel.setStyle(Estilos.LABEL_STYLE);
        TextField portField = new TextField("5000");
        portField.setStyle(Estilos.TEXT_FIELD_STYLE);

        Label servidorLabel = new Label("login");
        servidorLabel.setStyle(Estilos.LABEL_STYLE);
        TextField servidorField = new TextField();
        servidorField.setStyle(Estilos.TEXT_FIELD_STYLE);

        Label topicoLabel = new Label("Conectar a qual sala ?");
        topicoLabel.setStyle(Estilos.LABEL_STYLE);
        TextField topicoField = new TextField("sala1");
        topicoField.setStyle(Estilos.TEXT_FIELD_STYLE);

        Button ligar = new Button("Iniciar Servidor");
        ligar.setStyle(Estilos.BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(Estilos.CANCEL_BUTTON_STYLE);

        ligar.setOnAction(e -> {
            endereco = addressField.getText();
            String portStr = portField.getText();
            String queueName = topicoField.getText();
            String loginName = servidorField.getText();
            String[] topicos = { loginName, queueName };

            if (!endereco.isEmpty() && !portStr.isEmpty() && !queueName.isEmpty()) {
                try {
                    porta = Integer.parseInt(portStr);
                    circle_toogle.setFill(Color.GREEN);
                    servidor = new Servidor(endereco, porta, "tcp://mqtt.eclipseprojects.io:1883", topicos, true, this);
                    t_server = new Thread(servidor::startServer);
                    t_queue = new Thread(servidor::startQueue);
                    t_server.start();
                    t_queue.start();

                    endereco_porta_lb.setText("Servidor ligado (" + endereco + ":" + portStr + ")");
                    endereco_porta_lb.setVisible(true);
                    dialog.close();
                } catch (NumberFormatException ex) {
                    showAlert("Erro", "Porta deve ser um número inteiro.");
                }
            } else {
                showAlert("Erro", "Por favor, preencha todos os campos.");
            }
        });

        cancelar.setOnAction(e -> dialog.close());

        form.getChildren().addAll(addressLabel, addressField, portLabel, portField, servidorLabel, servidorField,
                topicoLabel, topicoField);

        HBox buttons = new HBox(10, ligar, cancelar);
        buttons.setAlignment(Pos.CENTER);

        VBox container = new VBox(20, titleLabel, form, buttons);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        styleDialog(dialog, container);
        Scene scene = new Scene(container);
        scene.getStylesheets().add("data:text/css," + Estilos.COMBO_BOX_STYLESHEET.replaceAll("\n", ""));
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showServidorDialog() {
        Stage dialog = createDialogStage("Servidor");

        Label titleLabel = new Label("Controle de Servidores");
        titleLabel.setStyle(Estilos.TITLE_STYLE);

        VBox form = new VBox(10);
        form.setStyle(Estilos.CARD_STYLE);

        Label idServidorLabel = new Label("ID do Servidor:");
        idServidorLabel.setStyle(Estilos.LABEL_STYLE);
        ComboBox<Integer> idServidorComboBox = new ComboBox<>(idServidorList);
        idServidorComboBox.setMaxWidth(Double.MAX_VALUE);
        idServidorComboBox.setStyle(Estilos.COMBO_BOX_STYLE);

        Label opcoesLabel = new Label("Opções:");
        opcoesLabel.setStyle(Estilos.LABEL_STYLE);
        ToggleGroup opcaoServidorGroup = new ToggleGroup();
        RadioButton desligar = createRadioButton("Desligar", opcaoServidorGroup);
        RadioButton ligar = createRadioButton("Ligar", opcaoServidorGroup);
        RadioButton descrever = createRadioButton("Descrever", opcaoServidorGroup);
        VBox opcoesBox = new VBox(5, desligar, ligar, descrever);

        Label destinoLabel = new Label("Destino:");
        destinoLabel.setStyle(Estilos.LABEL_STYLE);
        ToggleGroup destinoGroup = new ToggleGroup();
        RadioButton umaSala = createRadioButton("Uma Sala", destinoGroup);
        RadioButton todasSalas = createRadioButton("Todas as Salas", destinoGroup);
        VBox destinoBox = new VBox(5, umaSala, todasSalas);

        Label idMicroLabel = new Label("ID do Microcontrolador:");
        idMicroLabel.setStyle(Estilos.LABEL_STYLE);
        TextField idMicroField = new TextField();
        idMicroField.setStyle(Estilos.TEXT_FIELD_STYLE);
        idMicroLabel.setVisible(false);
        idMicroField.setVisible(false);
        destinoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean isUmaSala = newToggle == umaSala;
            idMicroLabel.setVisible(isUmaSala);
            idMicroField.setVisible(isUmaSala);
        });

        Button enviar = new Button("Enviar");
        enviar.setStyle(Estilos.BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(Estilos.CANCEL_BUTTON_STYLE);

        enviar.setOnAction(e -> {
            Integer idServidor = idServidorComboBox.getValue();
            RadioButton opcaoSelecionada = (RadioButton) opcaoServidorGroup.getSelectedToggle();
            RadioButton destinoSelecionado = (RadioButton) destinoGroup.getSelectedToggle();

            if (idServidor == null || opcaoSelecionada == null || destinoSelecionado == null) {
                showAlert("Erro", "Por favor, preencha todos os campos e selecione as opções.");
                return;
            }

            int op = switch (opcaoSelecionada.getText()) {
                case "Desligar" -> 0;
                case "Ligar" -> 1;
                case "Descrever" -> 2;
                default -> 2;
            };

            if (destinoSelecionado == umaSala) {
                String idMicroStr = idMicroField.getText();
                if (idMicroStr.isEmpty()) {
                    showAlert("Erro", "Por favor, digite o ID do microcontrolador.");
                    return;
                }
                try {
                    int idMicro = Integer.parseInt(idMicroStr);
                    Comando comando = new Comando(1, idMicro, idServidor, op, op);
                    servidor.addComando(comando);
                } catch (NumberFormatException ex) {
                    showAlert("Erro", "ID do microcontrolador deve ser um número inteiro.");
                    return;
                }
            } else {
                Comando comando = new Comando(1, -1, idServidor, op, op);
                servidor.addComando(comando);
            }
            dialog.close();
        });

        cancelar.setOnAction(e -> dialog.close());

        form.getChildren().addAll(
                idServidorLabel, idServidorComboBox,
                opcoesLabel, opcoesBox,
                destinoLabel, destinoBox,
                idMicroLabel, idMicroField);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefViewportHeight(350);

        HBox buttons = new HBox(10, enviar, cancelar);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));

        VBox container = new VBox(10, titleLabel, scrollPane, buttons);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(15));

        styleDialog(dialog, container);
        Scene scene = new Scene(container);
        idServidorComboBox.getStyleClass().add("custom-combo-box");
        scene.getStylesheets().add("data:text/css," + Estilos.COMBO_BOX_STYLESHEET.replaceAll("\n", ""));
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showMicrocontroladorDialog() {
        Stage dialog = createDialogStage("Microcontrolador");

        Label titleLabel = new Label("Controle de Microcontroladores");
        titleLabel.setStyle(Estilos.TITLE_STYLE);

        VBox form = new VBox(10);
        form.setStyle(Estilos.CARD_STYLE);

        // Card para Ações
        VBox card1 = new VBox(10);
        card1.setStyle(Estilos.CARD_STYLE);
        Label group1Label = new Label("Ações:");
        group1Label.setStyle(Estilos.LABEL_STYLE);
        ToggleGroup opcaoMicrocontrolador = new ToggleGroup();
        RadioButton ligar = createRadioButton("Ligar", opcaoMicrocontrolador);
        RadioButton desligar = createRadioButton("Desligar", opcaoMicrocontrolador);
        RadioButton descrever = createRadioButton("Descrever", opcaoMicrocontrolador);
        VBox group1Box = new VBox(10, ligar, desligar, descrever);
        group1Box.setStyle("-fx-padding: 10;");
        card1.getChildren().addAll(group1Label, group1Box);

        // Card para Destino
        VBox card2 = new VBox(10);
        card2.setStyle(Estilos.CARD_STYLE);
        Label group2Label = new Label("Destino:");
        group2Label.setStyle(Estilos.LABEL_STYLE);
        ToggleGroup destinoGroup = new ToggleGroup();
        RadioButton enviarParaUm = createRadioButton("Uma Sala", destinoGroup);
        RadioButton enviarParaTodos = createRadioButton("Todas as salas", destinoGroup);
        VBox group2Box = new VBox(10, enviarParaUm, enviarParaTodos);
        group2Box.setStyle("-fx-padding: 10;");

        Label inputLabel = new Label("ID da sala:");
        inputLabel.setStyle(Estilos.LABEL_STYLE);
        TextField inputField = new TextField();
        inputField.setStyle(Estilos.TEXT_FIELD_STYLE);
        inputLabel.setVisible(false);
        inputField.setVisible(false);
        destinoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean showInput = newToggle == enviarParaUm;
            inputLabel.setVisible(showInput);
            inputField.setVisible(showInput);
        });
        card2.getChildren().addAll(group2Label, group2Box, inputLabel, inputField);

        Button submit = new Button("Enviar");
        submit.setStyle(Estilos.BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(Estilos.CANCEL_BUTTON_STYLE);

        submit.setOnAction(e -> {
            RadioButton opcaoSelecionada = (RadioButton) opcaoMicrocontrolador.getSelectedToggle();
            RadioButton destinatarioSelecionado = (RadioButton) destinoGroup.getSelectedToggle();
            if (opcaoSelecionada != null && destinatarioSelecionado != null) {
                int op = switch (opcaoSelecionada.getText()) {
                    case "Desligar" -> 0;
                    case "Ligar" -> 1;
                    case "Descrever" -> 2;
                    default -> 2;
                };
                if (destinatarioSelecionado == enviarParaUm) {
                    String idSalaStr = inputField.getText();
                    if (!idSalaStr.isEmpty()) {
                        try {
                            int idSala = Integer.parseInt(idSalaStr);
                            Comando comando = new Comando(0, op, idSala, -1, "", -1);
                            servidor.addComando(comando);
                            dialog.close();
                        } catch (NumberFormatException ex) {
                            showAlert("Erro", "ID da sala deve ser um número inteiro.");
                        }
                    } else {
                        showAlert("Erro", "Por favor, digite o ID da sala.");
                    }
                } else {
                    Comando comando = new Comando(0, op, -1, -1, "", -1);
                    servidor.addComando(comando);
                    dialog.close();
                }
            } else {
                showAlert("Erro", "Por favor, selecione todas as opções.");
            }
        });

        cancelar.setOnAction(e -> dialog.close());

        form.getChildren().addAll(card1, card2);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefViewportHeight(350);

        HBox buttons = new HBox(10, submit, cancelar);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));

        VBox container = new VBox(10, titleLabel, scrollPane, buttons);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(15));

        styleDialog(dialog, container);
        dialog.setScene(new Scene(container));
        dialog.showAndWait();
    }

    /* =================== Métodos Auxiliares =================== */
    private RadioButton createRadioButton(String text, ToggleGroup group) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group);
        rb.setStyle(Estilos.RADIO_BUTTON_STYLE);
        return rb;
    }

    private Stage createDialogStage(String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        return stage;
    }

    private void styleDialog(Stage dialog, VBox container) {
        container.setStyle(Estilos.DIALOG_STYLE);
        dialog.setMinWidth(450);
        dialog.setMinHeight(500);
        dialog.setResizable(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
