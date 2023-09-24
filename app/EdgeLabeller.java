package app;

import org.apache.commons.collections15.Transformer;

public class EdgeLabeller<V> implements Transformer<V, String> {
    @Override
    public String transform(V v) {
        return v.toString().split(" ")[0];
    }

}
