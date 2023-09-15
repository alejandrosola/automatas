package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.EstadoComparator;

public class Automata {
    private List<String> lenguaje;
    private Map<String, Estado> estados;
    private String estadoInicial;

    public Automata() {
        this.lenguaje = new ArrayList<>();
        this.estados = new HashMap<>();
    }

    public List<String> getLenguaje() {
        return this.lenguaje;
    }

    public void setEstadoInicial(String nombre) {
        this.estadoInicial = nombre;
    }

    public Estado getEstadoInicial() {
        return this.estados.get(this.estadoInicial);
    }

    public boolean isInputAceptado(List<String> input, String estadoInicial) throws Exception {
        if (!this.isDeterministico())
            throw new Exception("El automáta debe ser determinístico");
        for (String i : input)
            if (!lenguaje.contains(i))
                throw new Exception("Input inválido");
        return this.estados.get(estadoInicial).isInputAceptado(input);
    }

    public boolean isInputAceptado(List<String> input, Estado estadoInicial) throws Exception {
        if (!this.isDeterministico())
            throw new Exception("El automáta debe ser determinístico");
        for (String i : input)
            if (!lenguaje.contains(i))
                throw new Exception("Input inválido");
        return this.estados.get(estadoInicial.getNombre()).isInputAceptado(input);
    }

    public boolean isDeterministico() {
        for (Estado e : this.estados.values()) {
            if (!e.isDeterministico())
                return false;
        }
        return true;
    }

    private boolean destinosContains(List<Transicion> transiciones, Estado estado) {
        for (Transicion transicion : transiciones) {
            for (Estado destino : transicion.getDestinos()) {
                if (destino.equals(estado))
                    return true;
            }
        }
        return false;
    }

    private Estado getNewEstado(List<Estado> estados, String nombre) {
        boolean aceptador = false;
        Estado newEstado;
        for (String s : nombre.split(" ")) {
            if (this.getEstado(s).isAceptador())
                aceptador = true;
        }

        newEstado = new Estado(nombre, aceptador);
        Map<String, List<Transicion>> destinos = new HashMap<>();
        for (String input : this.lenguaje) {
            destinos.put(input, new ArrayList<>());
            for (Estado e : estados) {
                for (Estado destino : e.getDestinos(input)) {
                    if (!this.destinosContains(destinos.get(input), destino)) {
                        destinos.get(input).add(new Transicion(input, "", destino));
                    }
                }
            }

            newEstado.setTransicionesForInput(destinos.get(input), input);
        }

        return newEstado;
    }

    public Automata getDeterministicoEquivalente() {
        if (this.isDeterministico())
            return this;

        Automata deterministicoEquivalente = new Automata();
        List<Estado> estadosDeterministicos = new ArrayList<>();
        Estado nuevoEstadoInicial = new Estado(this.getEstadoInicial().getNombre(),
                this.getEstadoInicial().isDeterministico());

        List<Estado> destinosTemp = new ArrayList<>();
        for (String input : this.lenguaje) {
            destinosTemp = (this.getEstadoInicial().getTransiciones().get(input).getDestinos());
            nuevoEstadoInicial.setTransicionForInput(new Transicion(input, "", destinosTemp), input);
        }

        List<Estado> estadosSinProcesar = new ArrayList<>();
        // Deep copy de los estados del automata original
        for (Estado e : this.getEstadosList()) {
            destinosTemp = new ArrayList<>();
            estadosSinProcesar.add(0, new Estado(e.getNombre(), e.isAceptador()));
            for (String input : this.lenguaje) {
                destinosTemp = (e.getTransiciones().get(input).getDestinos());
                estadosSinProcesar.get(0).setTransicionForInput(new Transicion(input, "", destinosTemp), input);
            }
        }

        boolean contiene;
        while (!estadosSinProcesar.isEmpty()) {
            Estado estadoActual = estadosSinProcesar.remove(0);

            for (String input : this.lenguaje) {
                if (estadoActual.getDestinos(input).size() > 1) {
                    String nombre = "";

                    for (Estado d : estadoActual.getDestinos(input)) {
                        for (String s : d.getNombre().split(" ")) {
                            if (!nombre.contains(s))
                                nombre += s + " ";
                        }
                    }

                    if (estadoActual.getNombre().equals(nombre)) {
                        estadoActual.setTransicionForInput(new Transicion(input, "", estadoActual), input);
                    } else {
                        contiene = false;
                        for (Estado esp : estadosSinProcesar) {
                            if (esp.getNombre().equals(nombre)) {
                                contiene = true;
                                estadoActual.setTransicionForInput(new Transicion(input, "", esp), input);
                            }
                        }

                        for (Estado ed : estadosDeterministicos) {
                            if (ed.getNombre().equals(nombre)) {
                                contiene = true;
                                estadoActual.setTransicionForInput(new Transicion(input, "", ed), input);
                            }
                        }

                        if (!contiene) {
                            Estado newEstado = this.getNewEstado(estadoActual.getDestinos(input), nombre);
                            estadoActual.setTransicionForInput(new Transicion(input, "", newEstado), input);
                            estadosSinProcesar.add(newEstado);
                        }
                    }
                }
            }
            // Siempre que llegue acá deberia ser deterministico
            if (estadoActual.isDeterministico()) {
                estadosDeterministicos.add(estadoActual);
            }
        }

        // Creo la copia del automáta con sus estados determinísticos
        deterministicoEquivalente = new Automata();
        deterministicoEquivalente.setEstados(estadosDeterministicos);
        deterministicoEquivalente.setEstadoInicial(nuevoEstadoInicial.getNombre());
        deterministicoEquivalente.setLenguaje(this.lenguaje);

        return deterministicoEquivalente;
    }

    public Estado getEstado(String nombre) {
        return this.estados.get(nombre);
    }

    public void addEstado(Estado estado) {
        this.estados.put(estado.getNombre(), estado);
    }

    public List<Estado> getEstadosList() {
        List<Estado> answer = new ArrayList<>();
        for (Estado e : this.estados.values()) {
            answer.add(e);
        }
        return answer;
    }

    public void setEstados(Map<String, Estado> estados) {
        this.estados = estados;
    }

    public void setEstados(List<Estado> estados) {
        Map<String, Estado> newEstados = new HashMap<>();
        for (Estado e : estados) {
            newEstados.put(e.getNombre(), e);
        }
        this.estados = newEstados;
    }

    public void setLenguaje(List<String> lenguaje) {
        this.lenguaje = lenguaje;
    }

}
