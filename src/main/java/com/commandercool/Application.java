package com.commandercool;

import static com.commandercool.context.BucketContext.getCurrentContext;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.commandercool.components.MriView;
import com.ericbarnhill.niftijio.NiftiVolume;

public class Application {

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("MRI Flood Fill");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final MriView mriView = new MriView();
        frame.setMinimumSize(mriView.getMriDimensions());
        frame.add(mriView);

        frame.pack();
        frame.setVisible(true);

        //File chooser
        final JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MRI Files", "nii"));

        //Tools panel
        final JFrame tools = new JFrame("Tools");

        final JSlider minSlider = new JSlider(0, 100);
        minSlider.addChangeListener(e -> {
            getCurrentContext().setMinIntensity(minSlider.getValue());
            mriView.repaint();
        });

        final JSlider maxSlider = new JSlider(0, 100);
        maxSlider.addChangeListener(e -> {
            getCurrentContext().setMaxIntensity(maxSlider.getValue());
            mriView.repaint();
        });

        tools.setLayout(new FlowLayout());

        tools.add(new Label("Min intensity:"));
        tools.add(minSlider);
        tools.add(new Label("Max intensity:"));
        tools.add(maxSlider);

        final JSlider threshold = new JSlider(1, 255);
        threshold.setValue(100);
        threshold.addChangeListener(e -> {
            getCurrentContext().setThreshold(threshold.getValue());
        });
        tools.add(new JLabel("Threshold (1 to 255):"));
        tools.add(threshold);

        tools.setMinimumSize(new Dimension(256, 256));

        //Menu bar
        final JMenuBar jMenuBar = new JMenuBar();
        final JMenu file = new JMenu("File");
        final JMenuItem open = new JMenuItem("Open");
        open.addActionListener(e -> {
            int returnVal = jFileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    getCurrentContext().setVolume(NiftiVolume.read(jFileChooser.getSelectedFile().getPath()));
                } catch (IOException ioException) {
                    // ignore
                }
                mriView.repaint();
                final Dimension mriDimensions = mriView.getMriDimensions();
                frame.setMinimumSize(mriDimensions);
                frame.setSize(mriDimensions);

                minSlider.setMaximum((int) mriView.getMaxIntensity());
                minSlider.setMinimum((int) mriView.getMinIntensity());
                minSlider.setValue(minSlider.getMinimum());
                minSlider.repaint();

                maxSlider.setMaximum((int) mriView.getMaxIntensity());
                maxSlider.setMinimum((int) mriView.getMinIntensity());
                maxSlider.setValue(maxSlider.getMaximum());
                maxSlider.repaint();

                System.out.println("Max intensity: " + mriView.getMaxIntensity() + "; min intensity: " + mriView.getMinIntensity());
            }
        });
        file.add(open);

        final JMenu view = new JMenu("View");
        final JMenuItem toolsMenuItem = new JMenuItem("Tools");
        toolsMenuItem.addActionListener(e -> {
            tools.setVisible(!tools.isVisible());
        });
        view.add(toolsMenuItem);

        final JMenu edit = new JMenu("Edit");

        final JMenuItem cancel = new JMenuItem("Cancel fill");
        cancel.addActionListener(e -> {
            if (getCurrentContext().isFillRunning()) {
                getCurrentContext().setCanceled(true);
            }
        });
        edit.add(cancel);
        edit.addSeparator();

        final JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> {
            getCurrentContext().undo();
            mriView.repaint();
        });
        edit.add(undo);
        edit.addSeparator();

        final JMenuItem reset = new JMenuItem("Reset");
        reset.addActionListener(e -> {
            mriView.resetSelection();
            mriView.repaint();
        });

        final JMenuItem subtract = new JMenuItem("Subtract");
        subtract.addActionListener(e -> {
            mriView.subtractSelection();
            mriView.repaint();
        });

        final JMenuItem invert = new JMenuItem("Invert");
        invert.addActionListener(e -> {
            mriView.invertSelection();
            mriView.repaint();
        });


        edit.add(reset);
        edit.add(subtract);
        edit.add(invert);

        final JMenu layer = new JMenu("Layer");
        final JMenuItem curLower = new JMenuItem("Cut lower");
        curLower.addActionListener(e -> {
            getCurrentContext().setMinDimension(mriView.getScroll());
        });
        layer.add(curLower);

        jMenuBar.add(file);
        jMenuBar.add(view);
        jMenuBar.add(edit);
        jMenuBar.add(layer);

        frame.setJMenuBar(jMenuBar);

    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(Application::createAndShowGUI);
    }

}
