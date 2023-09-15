package util;

import java.util.Comparator;

import model.Estado;

public class EstadoComparator implements Comparator<Estado> {
    @Override
    public int compare(Estado o1, Estado o2) {
        return o1.getNombre().compareTo(o2.getNombre());
    }
}
