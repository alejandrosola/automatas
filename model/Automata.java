package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

import util.Constantes;
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

    public boolean isInputAceptado(List<String> input) throws Exception {
        return this.isInputAceptado(input, this.getEstadoInicial());
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
        if (input.get(0).equals("")) {
            return this.getEstadoInicial().isAceptador();
        }
        for (String i : input) {
            if (!lenguaje.contains(i))
                throw new Exception("Input inválido");
        }
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

    private Automata getCopy(Automata automata) {
        Map<String, Estado> estados = new HashMap<>();
        List<String> lenguaje = new ArrayList<>();

        for (String i : automata.getLenguaje()) {
            lenguaje.add(i);
        }

        for (Estado e : automata.getEstadosList()) {
            estados.put(e.getNombre(), new Estado(e.getNombre(), e.isAceptador()));
        }

        for (Estado e : estados.values()) {
            for (String i : automata.getLenguaje()) {
                List<Estado> destinos = new ArrayList<>();
                for (Estado destino : automata.getEstado(e.getNombre()).getDestinos(i)) {
                    destinos.add(estados.get(destino.getNombre()));
                }
                e.setTransicionForInput(new Transicion(i, "", destinos), i);
            }
        }

        Automata answer = new Automata();
        answer.setEstados(estados);
        answer.setEstadoInicial(automata.getEstadoInicial().getNombre());
        answer.setLenguaje(lenguaje);

        return answer;
    }

    public Automata getDeterministicoEquivalente() {
        if (this.isDeterministico())
            return this.getCopy(this);

        Automata deterministicoEquivalente = new Automata();
        List<Estado> estadosDeterministicos = new ArrayList<>();
        Estado nuevoEstadoInicial = new Estado(this.getEstadoInicial().getNombre(),
                this.getEstadoInicial().isDeterministico());

        Map<String, Estado> copiaEstados = new HashMap<>();
        for (Estado e : this.getEstadosList()) {
            copiaEstados.put(e.getNombre(), new Estado(e.getNombre(), e.isAceptador()));
        }

        List<Estado> destinosTemp = new ArrayList<>();
        for (String input : this.lenguaje) {
            for (Estado d : this.getEstadoInicial().getDestinos(input)) {
                destinosTemp.add(copiaEstados.get(d.getNombre()));
            }
            nuevoEstadoInicial.setTransicionForInput(new Transicion(input, "", destinosTemp), input);
        }

        List<Estado> estadosSinProcesar = new ArrayList<>();
        for (Estado e : this.getEstadosList()) {
            estadosSinProcesar.add(0, copiaEstados.get(e.getNombre()));
            for (String input : this.lenguaje) {
                destinosTemp = new ArrayList<>();
                for (Estado d : e.getDestinos(input)) {
                    destinosTemp.add(copiaEstados.get(d.getNombre()));
                }
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
                        if (d != null)
                            for (String s : d.getNombre().split(" ")) {
                                if (!nombre.contains(s) && !d.getNombre().equals(Constantes.ERROR))
                                    nombre += s + " ";
                            }
                    }
                    nombre = nombre.trim();

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
            // Siempre que llegue acá debería ser deterministico, pero chequeo por si acaso
            if (estadoActual.isDeterministico()) {
                estadosDeterministicos.add(estadoActual);
            }
        }

        // Creo la copia del automáta con sus estados determinísticos
        deterministicoEquivalente = new Automata();
        deterministicoEquivalente.setEstados(estadosDeterministicos);
        deterministicoEquivalente.setEstadoInicial(nuevoEstadoInicial.getNombre());
        deterministicoEquivalente.setLenguaje(new ArrayList<String>(this.lenguaje));

        return deterministicoEquivalente;
    }

    public Automata getMinimizado() throws Exception {
        if (!this.isDeterministico())
            throw new Exception("El autómata debe ser determinístico");
        Estado nuevoEstadoInicial = new Estado(this.getEstadoInicial().getNombre(),
                this.getEstadoInicial().isDeterministico());

        Map<String, Estado> copiaEstados = new HashMap<>();
        for (Estado e : this.getEstadosList()) {
            copiaEstados.put(e.getNombre(), new Estado(e.getNombre(), e.isAceptador()));
        }

        for (Estado e : this.getEstadosList())
            for (String input : this.lenguaje) {
                List<Estado> destinosTemp = new ArrayList<>();
                for (Estado d : e.getDestinos(input)) {
                    destinosTemp.add(copiaEstados.get(d.getNombre()));
                }
                copiaEstados.get(e.getNombre()).setTransicionForInput(new Transicion(input, "", destinosTemp),
                        input);
            }

        for (String input : this.lenguaje) {
            List<Estado> destinosTemp = new ArrayList<>();
            for (Estado d : this.getEstadoInicial().getDestinos(input)) {
                destinosTemp.add(copiaEstados.get(d.getNombre()));
            }
            nuevoEstadoInicial.setTransicionForInput(new Transicion(input, "", destinosTemp), input);
        }
        this.eliminarEstadosInalcanzables(copiaEstados, nuevoEstadoInicial);

        // Separar los estados aceptadores de los no aceptadores en dos grupos
        List<List<Estado>> gruposSinProcesar = new ArrayList<>();
        List<List<Estado>> gruposMinimizados = new ArrayList<>();
        gruposSinProcesar.add(0, new ArrayList<>());
        gruposSinProcesar.add(1, new ArrayList<>());
        for (Estado e : this.getEstadosList()) {
            if (e.isAceptador()) {
                if (copiaEstados.get(e.getNombre()) != null)
                    gruposSinProcesar.get(1).add(copiaEstados.get(e.getNombre()));
            } else {
                if (copiaEstados.get(e.getNombre()) != null)
                    gruposSinProcesar.get(0).add(copiaEstados.get(e.getNombre()));
            }
        }

        int longitud = 0;
        boolean termine = false;
        List<List<Estado>> tempList;
        // Recorrer cada grupo con las cadenas generadas para ver si se separan
        while (!termine) {
            termine = true;
            longitud++;
            tempList = new ArrayList<>();
            TreeSet<String> cadenas = this.getCadenas(longitud);
            while (!gruposSinProcesar.isEmpty()) {
                List<Estado> grupo = gruposSinProcesar.remove(0);
                Map<Estado, String> resultados = new HashMap<>();
                for (Estado e : grupo) {
                    for (String cadena : cadenas) {
                        if (resultados.get(e) == null) {
                            resultados.put(e, "");
                        }
                        String temp = e.isInputAceptado(Arrays.asList(cadena.split(" "))) ? "1" : "0";
                        resultados.put(e, resultados.get(e) + temp);
                    }
                }
                List<List<Estado>> nuevosGrupos = this.nuevosGrupos(resultados);
                if (nuevosGrupos.size() > 1) {
                    termine = false;
                }
                for (List<Estado> g : nuevosGrupos) {
                    tempList.add(g);
                }
            }
            gruposSinProcesar.addAll(tempList);
        }

        Automata minimizado = new Automata();
        gruposMinimizados = gruposSinProcesar;
        Map<String, Estado> estadosMin = new HashMap<>();
        List<Estado> grupoAceptador = new ArrayList<>();
        for (List<Estado> grupoMin : gruposMinimizados) {
            if (this.grupoContiene(grupoMin, this.getEstadoInicial())) {
                grupoAceptador = grupoMin;
            }
            boolean aceptador = false;
            for (Estado e : grupoMin) {
                aceptador = e.isAceptador() ? true : aceptador;
            }
            estadosMin.put(grupoMin.get(0).getNombre(),
                    new Estado(grupoMin.get(0).getNombre(), aceptador));

        }
        Estado nuevoInicial = null;
        for (Estado em : estadosMin.values()) {
            if (grupoContiene(grupoAceptador, em)) {
                nuevoInicial = em;
            }
            for (String i : this.getLenguaje()) {
                for (List<Estado> grupoMin : gruposMinimizados) {
                    if (grupoContiene(grupoMin, copiaEstados.get(em.getNombre()).getDestinos(i).get(0))) {
                        em.addTransicion(
                                new Transicion(i, "", estadosMin.get(grupoMin.get(0).getNombre())));

                    }
                }
            }
        }

        /*
         * Si un grupo me quedo con nombre de estado de error se lo cambio si es posible
         * porque se me complica desp si no
         */
        for (List<Estado> grupoMin : gruposMinimizados) {
            if (estadosMin.get(grupoMin.get(0).getNombre()).getNombre().equals("-")) {
                if (grupoMin.size() > 1)
                    estadosMin.get(grupoMin.get(0).getNombre()).setNombre(grupoMin.get(1).getNombre());
            }

        }

        minimizado.setEstados(estadosMin);
        minimizado.setEstadoInicial(nuevoInicial.getNombre());
        minimizado.setLenguaje(new ArrayList<String>(this.lenguaje));

        return minimizado;
    }

    private boolean grupoContiene(List<Estado> grupo, Estado e) {
        for (Estado es : grupo) {
            if (es.getNombre().equals(e.getNombre())) {
                return true;
            }
        }
        return false;
    }

    private void eliminarEstadosInalcanzables(List<Estado> estados, Estado inicial) {
        Set<Estado> estadosAlcanzables = new HashSet<>();
        Set<Estado> estadosNoAlcanzables = new HashSet<>(estados);

        Queue<Estado> cola = new LinkedList<>();

        // Agregar el estado inicial a la lista de alcanzables
        Estado estadoInicial = inicial;
        estadosAlcanzables.add(estadoInicial);
        estadosNoAlcanzables.remove(estadoInicial);
        cola.add(estadoInicial);

        // Realizar un recorrido en anchura para encontrar estados alcanzables
        while (!cola.isEmpty()) {
            Estado estadoActual = cola.poll();
            for (String input : this.lenguaje) {
                Estado estadoDestino = estadoActual.getNext(input);
                if (estadoDestino != null && !estadosAlcanzables.contains(estadoDestino)) {
                    estadosAlcanzables.add(estadoDestino);
                    estadosNoAlcanzables.remove(estadoDestino);
                    cola.add(estadoDestino);
                }
            }
        }

        // Eliminar estados inalcanzables
        for (Estado estadoNoAlcanzable : estadosNoAlcanzables) {
            estados.remove(estadoNoAlcanzable);
        }
    }

    public void eliminarEstadosInalcanzables(Map<String, Estado> estados, Estado inicial) {
        Set<Estado> estadosAlcanzables = new HashSet<>();
        Set<Estado> estadosNoAlcanzables = new HashSet<>(estados.values());

        Queue<Estado> cola = new LinkedList<>();

        // Agregar el estado inicial a la lista de alcanzables
        estadosAlcanzables.add(inicial);
        estadosNoAlcanzables.remove(inicial);
        cola.add(inicial);

        // Realizar un recorrido en anchura para encontrar estados alcanzables
        while (!cola.isEmpty()) {
            Estado estadoActual = cola.poll();
            for (String input : this.lenguaje) {
                Estado estadoDestino = estadoActual.getNext(input);
                if (estadoDestino != null && !estadosAlcanzables.contains(estadoDestino)) {
                    estadosAlcanzables.add(estadoDestino);
                    estadosNoAlcanzables.remove(estadoDestino);
                    cola.add(estadoDestino);
                }
            }
        }

        // Eliminar estados inalcanzables
        for (Estado estadoNoAlcanzable : estadosNoAlcanzables) {
            if (!estadoNoAlcanzable.getNombre().equals(inicial.getNombre()))
                estados.remove(estadoNoAlcanzable.getNombre());
        }
    }

    private List<List<Estado>> nuevosGrupos(Map<Estado, String> resultados) {
        List<List<Estado>> answer = new ArrayList<>();
        Map<String, List<Estado>> temp = new HashMap<>();

        for (Entry<Estado, String> e : resultados.entrySet()) {
            if (temp.get(e.getValue()) == null) {
                temp.put(e.getValue(), new ArrayList<>());
            }
            temp.get(e.getValue()).add(e.getKey());
        }

        for (List<Estado> l : temp.values()) {
            answer.add(l);
        }
        return answer;
    }

    private TreeSet<String> getCadenas(int n) {
        String cadenaParcial = "";
        TreeSet<String> resultado = new TreeSet<String>();
        if (n == 0) {
            resultado.add(cadenaParcial.trim());
        } else {
            for (String c : this.getLenguaje()) {
                getCadenas(cadenaParcial + c + " ", n - 1, resultado);
            }
        }

        return resultado;
    }

    private TreeSet<String> getCadenas(String cadenaParcial, int n, TreeSet<String> resultado) {
        if (n == 0) {
            resultado.add(cadenaParcial.trim());
        } else {
            for (String c : this.getLenguaje()) {
                getCadenas(cadenaParcial + c + " ", n - 1, resultado);
            }
        }

        return resultado;
    }

    public Estado getEstado(String nombre) {
        return this.estados.get(nombre);
    }

    public void addEstado(Estado estado) {
        this.estados.put(estado.getNombre(), estado);
    }

    public List<Estado> getEstadosList() {
        List<Estado> answer = new ArrayList<>();
        answer.add(this.getEstadoInicial());

        List<Estado> estadosTemp = new ArrayList<>();
        for (Estado e : this.estados.values()) {
            estadosTemp.add(e);
        }

        Collections.sort(estadosTemp,
                new EstadoComparator());

        for (Estado e : estadosTemp) {
            if (!e.getNombre().equals(this.getEstadoInicial().getNombre()))
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

    public DirectedGraph<String, String> getGrafo() {
        DirectedGraph<String, String> grafo = new DirectedSparseGraph<>();

        for (Estado e : this.getEstadosList()) {
            if (!e.getNombre().equals(Constantes.ERROR))
                grafo.addVertex(e.getNombre());
        }
        Integer n = 0;
        for (Estado e : this.getEstadosList()) {
            for (String i : this.getLenguaje()) {
                for (Estado d : e.getDestinos(i)) {
                    // Le agrego n porque el identificador del arco tiene que ser único
                    if (!d.getNombre().equals(Constantes.ERROR)) {
                        if (!grafo.addEdge(i + " " + n.toString(), e.getNombre(), d.getNombre())) {
                            String edge = grafo.findEdge(e.getNombre(), d.getNombre());
                            grafo.removeEdge(edge);
                            grafo.addEdge(edge.toString().split(" ")[0] + "," + i + " " + n.toString(),
                                    e.getNombre(), d.getNombre());
                        }

                    }
                    n++;
                }
            }
        }

        return grafo;
    }

}
