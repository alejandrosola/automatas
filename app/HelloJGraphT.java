package app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import model.Automata;
import util.Helpers;

public class HelloJGraphT {
    public static void main(String[] args) throws Exception {
        // Crear un grafo con vértices de tipo String y aristas de tipo Integer

        Automata automata;
        automata = Helpers.readAutomataFromCSV("./automatas/segundo.csv");

        Graph<String, String> grafo = automata.getGrafo();

        Layout<String, String> layout = new CircleLayout(grafo);
        layout.setSize(new Dimension(300, 300)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        VisualizationViewer<String, String> vv = new VisualizationViewer<String, String>(layout);
        vv.setPreferredSize(new Dimension(350, 350)); // Sets the viewing area size
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new EdgeLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

        Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
            public Paint transform(String vertex) {
                // Cambiar el color del vértice "A" a rojo, los demás en negro
                if (vertex.equals(automata.getEstadoInicial().getNombre())) {
                    return Color.GREEN;
                } else {
                    return Color.WHITE;
                }
            }
        };

        // Asignar el Transformer al renderizador de vértices
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

        DefaultModalGraphMouse<Integer, String> gm = new DefaultModalGraphMouse<>();
        vv.setGraphMouse(gm);

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }
}
