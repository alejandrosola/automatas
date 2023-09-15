package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class TestInterfaz {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tabla de Automata");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Crear un modelo de tabla
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Estado");
            model.addColumn("Símbolo de Entrada");
            model.addColumn("Siguiente Estado");

            // Crear la tabla
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            // Ajustar el tamaño de las columnas
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);

            // Crear un campo de texto para mostrar el nombre del archivo
            JTextField fileNameTextField = new JTextField(20);
            fileNameTextField.setEditable(false);

            // Crear un botón para abrir el cuadro de diálogo del selector de archivos
            JButton openFileButton = new JButton("Seleccionar Archivo");
            openFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showOpenDialog(frame);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        String fileName = selectedFile.getName();
                        fileNameTextField.setText(fileName);
                    }
                }
            });

            // Crear un panel para los componentes de selección de archivo
            JPanel fileSelectionPanel = new JPanel();
            fileSelectionPanel.add(fileNameTextField);
            fileSelectionPanel.add(openFileButton);

            // Agregar la tabla y el panel de selección de archivo al frame
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(fileSelectionPanel, BorderLayout.SOUTH);

            // Mostrar el frame
            frame.setSize(400, 300);
            frame.setVisible(true);
        });
    }
}
