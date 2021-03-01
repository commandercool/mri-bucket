package com.commandercool;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.commandercool.components.MriView;
import com.commandercool.context.BucketContext;

public class Application {

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("MRI Flood Fill");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final MriView mriView = new MriView("");
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
            BucketContext.getCurrent().setMinIntensity(minSlider.getValue());
            mriView.repaint();
        });

        final JSlider maxSlider = new JSlider(0, 100);
        maxSlider.addChangeListener(e -> {
            BucketContext.getCurrent().setMaxIntensity(maxSlider.getValue());
            mriView.repaint();
        });

        tools.setLayout(new FlowLayout());

        tools.add(new Label("Min intensity:"));
        tools.add(minSlider);
        tools.add(new Label("Max intensity:"));
        tools.add(maxSlider);

        final JSlider threshold = new JSlider(0, 255);
        threshold.setValue(100);
        threshold.addChangeListener(e -> {
            BucketContext.getCurrent().setThreshold(threshold.getValue());
        });
        tools.add(new JLabel("Threshold (0 to 255):"));
        tools.add(threshold);

        tools.setMinimumSize(new Dimension(256, 256));

        //Menu bar
        final JMenuBar jMenuBar = new JMenuBar();
        final JMenu file = new JMenu("File");
        final JMenuItem open = new JMenuItem("Open");
        open.addActionListener(e -> {
            int returnVal = jFileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                mriView.setNifti(jFileChooser.getSelectedFile().getPath());
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

        final JMenu selection = new JMenu("Selection");
        final JMenuItem reset = new JMenuItem("Reset");
        reset.addActionListener(e -> {
            mriView.resetSelection();
            mriView.repaint();
        });

        final JMenuItem subtract = new JMenuItem("Subtract");
        subtract.addActionListener(e -> {
            mriView.substractSelection();
            mriView.repaint();
        });

        final JMenuItem invert = new JMenuItem("Invert");
        invert.addActionListener(e -> {
            mriView.invertSelection();
            mriView.repaint();
        });


        selection.add(reset);
        selection.add(subtract);
        selection.add(invert);

        final JMenu layer = new JMenu("Layer");
        final JMenuItem curLower = new JMenuItem("Cut lower");
        curLower.addActionListener(e -> {
            BucketContext.getCurrent().setMinDimension(mriView.getScroll());
        });
        layer.add(curLower);

        jMenuBar.add(file);
        jMenuBar.add(view);
        jMenuBar.add(selection);
        jMenuBar.add(layer);

        frame.setJMenuBar(jMenuBar);

    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
