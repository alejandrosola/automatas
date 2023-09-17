package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Automata;
import model.Estado;
import model.Transicion;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class Helpers {

    public static Automata readAutomataFromCSV(String csvFilePath) throws IOException {
        Automata automata = new Automata();
        List<String> sigma = new ArrayList<>();
        Map<String, Estado> estadoMap = new HashMap<>();

        CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFilePath)).withSkipLines(1).build();
        List<String[]> allData = csvReader.readAll();

        if (allData.size() > 0) {
            String[] sigmaRow = allData.get(0);
            for (String value : sigmaRow) {
                sigma.add(value);
            }
            automata.setLenguaje(sigma);
        }
        int count = 0;
        for (int i = 2; i < allData.size(); i++) {
            String[] row = allData.get(i);
            String estadoActualNombre = row[0];
            if (count == 0)
                automata.setEstadoInicial(estadoActualNombre);
            count++;
            Estado estadoActual = estadoMap.get(estadoActualNombre);
            if (estadoActual == null) {
                boolean aceptador = "1".equals(row[row.length - 2]);
                estadoActual = new Estado(estadoActualNombre, aceptador);
                automata.addEstado(estadoActual);
                estadoMap.put(estadoActualNombre, estadoActual);
            }
        }
        Estado estadoTemp = new Estado(Constantes.ERROR, false);
        estadoMap.put(Constantes.ERROR, estadoTemp);
        automata.addEstado(estadoTemp);
        for (String i : sigma) {
            estadoMap.get(Constantes.ERROR).addTransicion(new Transicion(i, "", estadoMap.get(Constantes.ERROR)));
        }

        for (int i = 2; i < allData.size(); i++) {
            String[] row = allData.get(i);
            String estadoActualNombre = row[0];
            Estado estadoActual = estadoMap.get(estadoActualNombre);
            for (int j = 1; j < row.length - 2; j++) {

                String input = sigma.get(j - 1);
                String destinoNombre = !row[j].equals("") ? row[j] : Constantes.ERROR;
                List<Estado> destinos = new ArrayList<>();
                if (estadoMap.get(destinoNombre) == null) {
                    for (String s : destinoNombre.split(" ")) {
                        destinos.add(estadoMap.get(s));
                    }
                } else {
                    destinos.add(estadoMap.get(destinoNombre));
                }

                Transicion transicion = new Transicion(input, "", destinos);
                estadoActual.addTransicion(transicion);
            }
        }

        return automata;
    }
}
