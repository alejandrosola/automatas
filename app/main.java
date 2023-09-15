package app;

import java.util.Arrays;
import java.util.Scanner;

import model.Automata;
import model.Estado;
import util.Helpers;

public class main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Ingrese el archivo csv del autómata");
            String archivo = scanner.nextLine();
            Automata automata = Helpers.readAutomataFromCSV(archivo);
            System.out.println("Ingrese una cadena separada por espacios");

            String cadenaLeida = scanner.nextLine();
            scanner.close();
            String[] input = cadenaLeida.split(" ");

            automata = automata.getDeterministicoEquivalente();

            /* 
             * for (Estado e : automata.getEstadosList()) {
             * for (String i : automata.getLenguaje()) {
             * System.out.println(e + " " + e.isAceptador() + " " + i + ": " +
             * e.getDestinos(i));
             * }
             * }
             */

            if (automata.isInputAceptado(Arrays.asList(input), automata.getEstadoInicial())) {
                System.out.println("Cadena aceptada");
            } else {
                System.out.println("Cadena rechazada");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
