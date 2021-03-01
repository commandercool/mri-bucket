package com.commandercool;

import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
            }
        });

        file.add(open);
        jMenuBar.add(file);

        frame.setJMenuBar(jMenuBar);

        //Tools panel
        final JFrame tools = new JFrame("Tools");

        final JSlider slider = new JSlider(0, (int) BucketContext.getCurrent().getMaxIntensity());
        tools.add(slider);

        tools.setVisible(true);
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
