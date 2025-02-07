package com.trabalho.util;

public class MenuInterface {

        public static void mostrarOpcoes() {
                System.out.println(" _ __ ___   ___ _ __  _   _ \n" + //
                                "| '_ ` _ \\ / _ \\ '_ \\| | | |\n" + //
                                "| | | | | |  __/ | | | |_| |\n" + //
                                "|_| |_| |_|\\___|_| |_|\\__,_|");
                System.out.println(
                                "OPCOES\n" + //
                                                "[0] --> USAR MICROCONTROLADOR\n" + //
                                                "[1] --> CONECTAR-SE A OUTRO SERVIDOR\n" + //
                                                "[2] --> LISTAR CONEXOES\n" + //
                                                "[3] --> ADICIONAR CONEXÃO\n" + //
                                                "OPCAO:");
        }

        public static void microcontroladorOpcoes() {
                System.out.println("\n" + //
                                "  __  __  _                                       _                _             _              \n"
                                + //
                                " |  \\/  |(_)                                     | |              | |           | |             \n"
                                + //
                                " | \\  / | _   ___  _ __  ___    ___  ___   _ __  | |_  _ __  ___  | |  __ _   __| |  ___   _ __ \n"
                                + //
                                " | |\\/| || | / __|| '__|/ _ \\  / __|/ _ \\ | '_ \\ | __|| '__|/ _ \\ | | / _` | / _` | / _ \\ | '__|\n"
                                + //
                                " | |  | || || (__ | |  | (_) || (__| (_) || | | || |_ | |  | (_) || || (_| || (_| || (_) || |   \n"
                                + //
                                " |_|  |_||_| \\___||_|   \\___/  \\___|\\___/ |_| |_| \\__||_|   \\___/ |_| \\__,_| \\__,_| \\___/ |_|   \n"
                                + //
                                "                                                                                                \n"
                                + //
                                "                                                                                                \n"
                                + //
                                "");
                System.out.println(
                                "OPCOES DO MICROCONTROLADOR\n" + //
                                                "[0] --> DESLIGAR SALA\n" + //
                                                "[1] --> LIGAR SALA\n" + //
                                                "[2] --> DESCREVER SALA\n" + //
                                                "[3] --> LIGAR TODAS AS SALAS\n" + //
                                                "[4] --> DESLIGAR TODAS AS SALAS\n" + //
                                                "[5] --> DESCREVER TODAS AS SALAS");
        }

        public static void controlarServer() {
                System.out.println("\n" + //
                                "   _____                               \n" + //
                                "  / ____|                              \n" + //
                                " | (___    ___  _ __ __   __ ___  _ __ \n" + //
                                "  \\___ \\  / _ \\| '__|\\ \\ / // _ \\| '__|\n" + //
                                "  ____) ||  __/| |    \\ V /|  __/| |   \n" + //
                                " |_____/  \\___||_|     \\_/  \\___||_|   \n" + //
                                "                                       \n" + //
                                "                                       \n" + //
                                "");
                System.out.println(
                                "OPCOES DO SERVER\n" + //
                                                "[0] --> DESLIGAR SALA\n" + //
                                                "[1] --> LIGAR SALA\n" + //
                                                "[2] --> DESCREVER SALA\n" + //
                                                "[3] --> LIGAR TODAS AS SALAS\n" + //
                                                "[4] --> DESLIGAR TODAS AS SALAS\n" + //
                                                "[5] --> DESCREVER TODAS AS SALAS\n" + //
                                                "[6] --> LISTAR CONEXOES");
        }

}
