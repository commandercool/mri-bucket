package com.commandercool;

import javax.swing.JFrame;
import javax.swing.JSlider;

import com.commandercool.components.MriView;
import com.commandercool.context.BucketContext;

public class Application {

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("MRI Flood Fill");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final MriView mriView = new MriView("C:\\freesurfer\\sources\\NOVIKOVA_A_S__20150420.nii");
        frame.setMinimumSize(mriView.getMriDimensions());
        frame.add(mriView);

        frame.pack();
        frame.setVisible(true);

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
