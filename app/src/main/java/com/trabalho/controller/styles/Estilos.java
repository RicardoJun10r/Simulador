package com.trabalho.controller.styles;

public class Estilos {
    public static final String DIALOG_STYLE = "-fx-background-color: #FFFFFF;";
    public static final String CARD_STYLE = """
            -fx-background-color: #FFFFFF;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 0);
            -fx-padding: 15;
            """;
    public static final String BUTTON_STYLE = """
            -fx-background-color: #007BFF;
            -fx-text-fill: #FFFFFF;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;
    public static final String CANCEL_BUTTON_STYLE = """
            -fx-background-color: #6C757D;
            -fx-text-fill: #FFFFFF;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """;
    public static final String TITLE_STYLE = """
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: #333333;
            -fx-padding: 0 0 10 0;
            """;
    public static final String LABEL_STYLE = """
            -fx-font-size: 14px;
            -fx-font-weight: normal;
            -fx-text-fill: #333333;
            """;
    public static final String TABLE_STYLE = """
            -fx-background-color: #FFFFFF;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 0);
            -fx-text-fill: #333333;
            """;
    public static final String TABLE_HEADER_STYLE = """
            -fx-background-color: #F8F9FA;
            -fx-font-weight: bold;
            -fx-font-size: 13px;
            -fx-padding: 12px;
            -fx-border-color: transparent transparent #CCCCCC transparent;
            -fx-text-fill: #333333;
            """;
    public static final String TABLE_CELL_STYLE = """
            -fx-padding: 12px;
            -fx-alignment: center-left;
            -fx-text-fill: #333333;
            """;
    public static final String RADIO_BUTTON_STYLE = "-fx-text-fill: #333333;";
    public static final String TEXT_FIELD_STYLE = """
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #333333;
            -fx-prompt-text-fill: #888888;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 4;
            -fx-background-radius: 4;
            """;
    public static final String COMBO_BOX_STYLE = """
            -fx-background-color: #FFFFFF;
            -fx-text-fill: #333333;
            -fx-prompt-text-fill: #888888;
            -fx-background-radius: 4;
            -fx-border-color: #CCCCCC;
            -fx-border-radius: 4;
            """;
    public static final String COMBO_BOX_STYLESHEET = """
            .custom-combo-box .list-cell {
                -fx-text-fill: #333333;
                -fx-background-color: #FFFFFF;
            }
            .custom-combo-box .list-cell:hover {
                -fx-background-color: #E9ECEF;
            }
            .custom-combo-box .list-view {
                -fx-background-color: #FFFFFF;
                -fx-border-color: #CCCCCC;
            }
            .custom-combo-box .arrow-button {
                -fx-background-color: #007BFF;
            }
            .custom-combo-box .arrow {
                -fx-background-color: #FFFFFF;
            }
            """;
}
