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

    /* =================== Constantes de Estilo =================== */
    private static final String DIALOG_STYLE = "-fx-background-color: #F2F2F2;";
    private static final String CARD_STYLE = """
            -fx-background-color: #FFFFFF;
            -fx-border-color: #7F534B;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 0);
            -fx-padding: 15;
            """;
    private static final String BUTTON_STYLE = """
            -fx-background-color: #7F534B;
            -fx-text-fill: #E5F2C9;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;

    private static final String CANCEL_BUTTON_STYLE = """
            -fx-background-color: #8C705F;
            -fx-text-fill: #E5F2C9;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;
    private static final String TITLE_STYLE = """
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: #1E1A1D;
            -fx-padding: 0 0 10 0;
            """;
    private static final String LABEL_STYLE = """
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-text-fill: #1E1A1D;
            """;
    private static final String TABLE_STYLE = """
            -fx-background-color: #1E1A1D;
            -fx-border-color: #7F534B;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 0);
            -fx-text-fill: #1E1A1D;
            """;
    private static final String TABLE_HEADER_STYLE = """
            -fx-background-color: #7F534B;
            -fx-font-weight: bold;
            -fx-font-size: 13px;
            -fx-padding: 12px;
            -fx-border-color: transparent transparent #8C705F transparent;
            -fx-text-fill: #1E1A1D;
            """;
    private static final String TABLE_CELL_STYLE = """
            -fx-padding: 12px;
            -fx-alignment: center-left;
            -fx-text-fill: #1E1A1D;
            """;
    private static final String RADIO_BUTTON_STYLE = "-fx-text-fill: #1E1A1D;";
    private static final String TEXT_FIELD_STYLE = """
            -fx-background-color: #F2F2F2;
            -fx-text-fill: #1E1A1D;
            -fx-prompt-text-fill: #8C705F;
            -fx-border-color: #7F534B;
            -fx-border-radius: 4;
            -fx-background-radius: 4;
            """;
    private static final String COMBO_BOX_STYLE = """
            -fx-background-color: #F2F2F2;
            -fx-text-fill: #1E1A1D;
            -fx-prompt-text-fill: #8C705F;
            -fx-background-radius: 4;
            -fx-border-color: #7F534B;
            -fx-border-radius: 4;
            """;
    private static final String COMBO_BOX_STYLESHEET = """
            .custom-combo-box .list-cell {
                -fx-text-fill: #1E1A1D;
                -fx-background-color: #F2F2F2;
            }
            .custom-combo-box .list-cell:hover {
                -fx-background-color: #7F534B;
            }
            .custom-combo-box .list-view {
                -fx-background-color: #F2F2F2;
                -fx-border-color: #7F534B;
            }
            .custom-combo-box .arrow-button {
                -fx-background-color: #7F534B;
            }
            .custom-combo-box .arrow {
                -fx-background-color: #E5F2C9;
            }
            """;

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
        microcontroladores_tabela.setStyle(TABLE_STYLE);
        servidores_tabela.setStyle(TABLE_STYLE);

        colMId.setStyle(TABLE_HEADER_STYLE);
        colMEndereco.setStyle(TABLE_HEADER_STYLE);
        colAparelhosLigados.setStyle(TABLE_HEADER_STYLE);
        colAparelhosDesligados.setStyle(TABLE_HEADER_STYLE);
        colSId.setStyle(TABLE_HEADER_STYLE);
        colSEndereco.setStyle(TABLE_HEADER_STYLE);
        colPorta.setStyle(TABLE_HEADER_STYLE);

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
                    setStyle(TABLE_CELL_STYLE);
                }
            }
        };
    }

    private <T> TableRow<T> createHoverableTableRow() {
        TableRow<T> row = new TableRow<>();
        row.setStyle("-fx-background-color: white;");
        row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
            if (!row.isEmpty()) {
                row.setStyle(isNowHovered ? "-fx-background-color: #f5f5f5;" : "-fx-background-color: white;");
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
                    x - triangleSize / 2, serverY + triangleSize / 2, // canto inferior esquerdo
                    x + triangleSize / 2, serverY + triangleSize / 2, // canto inferior direito
                    x, serverY - triangleSize / 2 // topo central
            );
            triangle.setFill(Color.DARKBLUE);
            animationPane.getChildren().add(triangle);
        }

        // Conecta os servidores entre si (se houver mais de um)
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

            // Cria um quadrado centrado em (microX, microY)
            double rectSize = 20;
            Rectangle square = new Rectangle(microX - rectSize / 2, microY - rectSize / 2, rectSize, rectSize);
            square.setFill(Color.FORESTGREEN);
            animationPane.getChildren().add(square);

            // Animação de pulso para o quadrado
            ScaleTransition st = new ScaleTransition(Duration.seconds(1.5), square);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(1.5);
            st.setToY(1.5);
            st.setCycleCount(Animation.INDEFINITE);
            st.setAutoReverse(true);
            st.play();

            // Conecta este microcontrolador a cada servidor
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
        titleLabel.setStyle(TITLE_STYLE);

        VBox form = new VBox(15);
        form.setStyle(CARD_STYLE);

        Label addressLabel = new Label("Endereço:");
        addressLabel.setStyle(LABEL_STYLE);
        TextField addressField = new TextField("127.0.0.1");
        addressField.setStyle(TEXT_FIELD_STYLE);

        Label portLabel = new Label("Porta:");
        portLabel.setStyle(LABEL_STYLE);
        TextField portField = new TextField("5001");
        portField.setStyle(TEXT_FIELD_STYLE);

        Button conectar = new Button("Conectar");
        conectar.setStyle(BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(CANCEL_BUTTON_STYLE);

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
        titleLabel.setStyle(TITLE_STYLE);

        VBox form = new VBox(15);
        form.setStyle(CARD_STYLE);

        Label addressLabel = new Label("Endereço:");
        addressLabel.setStyle(LABEL_STYLE);
        TextField addressField = new TextField("127.0.0.1");
        addressField.setStyle(TEXT_FIELD_STYLE);

        Label portLabel = new Label("Porta:");
        portLabel.setStyle(LABEL_STYLE);
        TextField portField = new TextField("5000");
        portField.setStyle(TEXT_FIELD_STYLE);

        Label servidorLabel = new Label("login");
        servidorLabel.setStyle(LABEL_STYLE);
        TextField servidorField = new TextField();
        servidorField.setStyle(TEXT_FIELD_STYLE);

        Label topicoLabel = new Label("Conectar a qual sala ?");
        topicoLabel.setStyle(LABEL_STYLE);
        TextField topicoField = new TextField("sala1");
        topicoField.setStyle(TEXT_FIELD_STYLE);

        Button ligar = new Button("Iniciar Servidor");
        ligar.setStyle(BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(CANCEL_BUTTON_STYLE);

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
        // Adiciona stylesheet customizado para ComboBox
        scene.getStylesheets().add("data:text/css," + COMBO_BOX_STYLESHEET.replaceAll("\n", ""));
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showServidorDialog() {
        Stage dialog = createDialogStage("Servidor");

        Label titleLabel = new Label("Controle de Servidores");
        titleLabel.setStyle(TITLE_STYLE);

        VBox form = new VBox(10);
        form.setStyle(CARD_STYLE);

        Label idServidorLabel = new Label("ID do Servidor:");
        idServidorLabel.setStyle(LABEL_STYLE);
        ComboBox<Integer> idServidorComboBox = new ComboBox<>(idServidorList);
        idServidorComboBox.setMaxWidth(Double.MAX_VALUE);
        idServidorComboBox.setStyle(COMBO_BOX_STYLE);

        Label opcoesLabel = new Label("Opções:");
        opcoesLabel.setStyle(LABEL_STYLE);
        ToggleGroup opcaoServidorGroup = new ToggleGroup();
        RadioButton desligar = createRadioButton("Desligar", opcaoServidorGroup);
        RadioButton ligar = createRadioButton("Ligar", opcaoServidorGroup);
        RadioButton descrever = createRadioButton("Descrever", opcaoServidorGroup);
        VBox opcoesBox = new VBox(5, desligar, ligar, descrever);

        Label destinoLabel = new Label("Destino:");
        destinoLabel.setStyle(LABEL_STYLE);
        ToggleGroup destinoGroup = new ToggleGroup();
        RadioButton umaSala = createRadioButton("Uma Sala", destinoGroup);
        RadioButton todasSalas = createRadioButton("Todas as Salas", destinoGroup);
        VBox destinoBox = new VBox(5, umaSala, todasSalas);

        Label idMicroLabel = new Label("ID do Microcontrolador:");
        idMicroLabel.setStyle(LABEL_STYLE);
        TextField idMicroField = new TextField();
        idMicroField.setStyle(TEXT_FIELD_STYLE);
        idMicroLabel.setVisible(false);
        idMicroField.setVisible(false);
        destinoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean isUmaSala = newToggle == umaSala;
            idMicroLabel.setVisible(isUmaSala);
            idMicroField.setVisible(isUmaSala);
        });

        Button enviar = new Button("Enviar");
        enviar.setStyle(BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(CANCEL_BUTTON_STYLE);

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
        scene.getStylesheets().add("data:text/css," + COMBO_BOX_STYLESHEET.replaceAll("\n", ""));
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showMicrocontroladorDialog() {
        Stage dialog = createDialogStage("Microcontrolador");

        Label titleLabel = new Label("Controle de Microcontroladores");
        titleLabel.setStyle(TITLE_STYLE);

        VBox form = new VBox(10);
        form.setStyle(CARD_STYLE);

        // Card para Ações
        VBox card1 = new VBox(10);
        card1.setStyle(CARD_STYLE);
        Label group1Label = new Label("Ações:");
        group1Label.setStyle(LABEL_STYLE);
        ToggleGroup opcaoMicrocontrolador = new ToggleGroup();
        RadioButton ligar = createRadioButton("Ligar", opcaoMicrocontrolador);
        RadioButton desligar = createRadioButton("Desligar", opcaoMicrocontrolador);
        RadioButton descrever = createRadioButton("Descrever", opcaoMicrocontrolador);
        VBox group1Box = new VBox(10, ligar, desligar, descrever);
        group1Box.setStyle("-fx-padding: 10;");
        card1.getChildren().addAll(group1Label, group1Box);

        // Card para Destino
        VBox card2 = new VBox(10);
        card2.setStyle(CARD_STYLE);
        Label group2Label = new Label("Destino:");
        group2Label.setStyle(LABEL_STYLE);
        ToggleGroup destinoGroup = new ToggleGroup();
        RadioButton enviarParaUm = createRadioButton("Uma Sala", destinoGroup);
        RadioButton enviarParaTodos = createRadioButton("Todas as salas", destinoGroup);
        VBox group2Box = new VBox(10, enviarParaUm, enviarParaTodos);
        group2Box.setStyle("-fx-padding: 10;");

        Label inputLabel = new Label("ID da sala:");
        inputLabel.setStyle(LABEL_STYLE);
        TextField inputField = new TextField();
        inputField.setStyle(TEXT_FIELD_STYLE);
        inputLabel.setVisible(false);
        inputField.setVisible(false);
        destinoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean showInput = newToggle == enviarParaUm;
            inputLabel.setVisible(showInput);
            inputField.setVisible(showInput);
        });
        card2.getChildren().addAll(group2Label, group2Box, inputLabel, inputField);

        Button submit = new Button("Enviar");
        submit.setStyle(BUTTON_STYLE);
        Button cancelar = new Button("Cancelar");
        cancelar.setStyle(CANCEL_BUTTON_STYLE);

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
        rb.setStyle(RADIO_BUTTON_STYLE);
        return rb;
    }

    private Stage createDialogStage(String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        return stage;
    }

    private void styleDialog(Stage dialog, VBox container) {
        container.setStyle(DIALOG_STYLE);
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
