package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.EstadoComparator;

public class Estado {
    private String nombre;
    private Map<String, Transicion> transiciones;
    private boolean aceptador;

    public Estado(String nombre, boolean aceptador) {
        this.nombre = nombre;
        this.aceptador = aceptador;
        this.transiciones = new HashMap<>();
    }

    public boolean isAceptador() {
        return this.aceptador;
    }

    public void setAceptador(boolean aceptador) {
        this.aceptador = aceptador;
    }

    public boolean isInputAceptado(List<String> input) {
        Estado walker = this;
        for (String i : input) {
            walker = walker.getNext(i);
            System.out.println(i + " " + walker);
            if (walker == null)
                return false;
        }
        return walker.isAceptador();
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Map<String, Transicion> getTransiciones() {
        return this.transiciones;
    }

    public Transicion addTransicion(Transicion transicion) {
        if (this.transiciones.get(transicion.getInput()) == null) {
            this.transiciones.put(transicion.getInput(), transicion);
        } else {
            this.transiciones.get(transicion.getInput()).addDestinos(transicion.getDestinos());
        }

        return transicion;
    }

    public void setTransiciones(Map<String, Transicion> transiciones) {
        this.transiciones = transiciones;
    }

    public void setTransicionForInput(Transicion transicion, String input) {
        this.transiciones.put(input, transicion);
    }

    public void setTransicionesForInput(List<Transicion> transiciones, String input) {
        List<Estado> destinos = new ArrayList<>();
        for (Transicion t : transiciones) {
            if (t.getInput().equals(input)) {
                for (Estado destino : t.getDestinos()) {
                    destinos.add(destino);
                }
            }
        }
        Transicion transicion = new Transicion(input, "", destinos);
        this.transiciones.put(input, transicion);
    }

    private Estado getNext(String input) {
        if (this.transiciones.get(input) != null)
            return this.transiciones.get(input).getDestinos().get(0);
        return null;
    }

    public boolean equals(Estado e) {
        return this.nombre.equals(e.getNombre());
    }

    public boolean isDeterministico() {
        for (Transicion t : this.transiciones.values()) {
            if (t.getDestinos().size() > 1)
                return false;
        }
        return true;
    }

    public String toString() {
        return this.nombre;
    }

    public List<Estado> getDestinos(String input) {
        List<Estado> answer = new ArrayList<>();
        if (this.transiciones.get(input) == null)
            return null;

        Collections.sort(this.transiciones.get(input).getDestinos(),
                new EstadoComparator());
        for (Estado destino : this.transiciones.get(input).getDestinos()) {
            answer.add(destino);
        }
        return answer;
    }

}