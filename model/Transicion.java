package model;

import java.util.ArrayList;
import java.util.List;

public class Transicion {
    private String input;
    private String output;
    private List<Estado> destinos;

    public Transicion(String input, String output, List<Estado> destinos) {
        this.input = input;
        this.output = output;
        this.destinos = destinos;
    }

    public Transicion(String input, String output, Estado destino) {
        this.input = input;
        this.output = output;
        this.destinos = new ArrayList<>();
        this.destinos.add(destino);
    }

    public String getInput() {
        return this.input;
    }

    public List<Estado> getDestinos() {
        return this.destinos;
    }

    public Estado getDestino() {
        return this.destinos.size() >= 1 ? this.destinos.get(0) : null;
    }

    public void addDestino(Estado estado) {
        this.destinos.add(estado);
    }

    public void addDestinos(List<Estado> estados) {
        for (Estado destino : estados)
            this.destinos.add(destino);
    }

    public String getOutput() {
        return this.output;
    }

    public boolean equals(Transicion t) {

        return this.input.equals(t.getInput()) && this.destinos.contains(t.getDestinos());
    }

}
