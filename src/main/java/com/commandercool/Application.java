package com.commandercool;

import static com.commandercool.context.BucketContext.getCurrentContext;
import static com.commandercool.context.BucketContext.subscribe;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.commandercool.components.MriView;
import com.commandercool.components.contextaware.ContextAwareProgressBar;
import com.commandercool.context.Mode;
import com.ericbarnhill.niftijio.NiftiVolume;

public class Application {

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("MRI Flood Fill");
        frame.setIconImage(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icon.png")).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final MriView mriView = new MriView();
        final int width = 600;
        frame.setMinimumSize(new Dimension(width, (int) mriView.getMriDimensions().getHeight()));
        frame.add(mriView, BorderLayout.CENTER);
        subscribe(mriView);

        final JPanel toolPanel = new JPanel();
        frame.add(toolPanel, BorderLayout.LINE_END);

        frame.pack();
        frame.setVisible(true);

        //Bottom panel
        final JPanel bottom = new JPanel();
        final JLabel minIntLabel = new JLabel();
        final JLabel maxIntLabel = new JLabel();

        getCurrentContext().setMaxIntLabel(maxIntLabel);
        getCurrentContext().setMinIntLabel(minIntLabel);

        bottom.add(minIntLabel);
        bottom.add(maxIntLabel);
        final ContextAwareProgressBar progressBar = new ContextAwareProgressBar();
        subscribe(progressBar);
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            if (getCurrentContext().isFillRunning()) {
                getCurrentContext().setCanceled(true);
            }
        });
        bottom.add(progressBar);
        bottom.add(cancelButton);

        frame.add(bottom, BorderLayout.PAGE_END);

        //File chooser
        final JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MRI Files", "nii"));

        //Export folder chooser
        final JFileChooser exportFolderChooser = new JFileChooser();

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
        threshold.setPaintTicks(true);
        threshold.setMajorTickSpacing(50);
        threshold.setMinorTickSpacing(10);
        threshold.addChangeListener(e -> {
            getCurrentContext().setThreshold(threshold.getValue());
        });
        toolPanel.add(new JLabel("Threshold:"));
        toolPanel.add(threshold);

        tools.setMinimumSize(new Dimension(256, 256));

        //Menu bar
        final JMenuBar jMenuBar = new JMenuBar();
        final JMenu file = new JMenu("File");
        final JMenuItem open = new JMenuItem("Open");
        open.addActionListener(e -> {
            int returnVal = jFileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    getCurrentContext().setVolume(new NiftiVolume(0,0,0,0));
                    getCurrentContext().setVolume(NiftiVolume.read(jFileChooser.getSelectedFile().getPath()));
                } catch (IOException ioException) {
                    // ignore
                }
                mriView.repaint();
                final Dimension frameDimension = new Dimension(width, (int) mriView.getMriDimensions().getHeight());
                frame.setMinimumSize(frameDimension);
                frame.setSize(frameDimension);

                final double maxIntensity = mriView.getMaxIntensity();
                final double minIntensity = mriView.getMinIntensity();

                minSlider.setMaximum((int) maxIntensity);
                minSlider.setMinimum((int) minIntensity);
                minSlider.setValue(minSlider.getMinimum());
                minSlider.repaint();

                maxSlider.setMaximum((int) maxIntensity);
                maxSlider.setMinimum((int) minIntensity);
                getCurrentContext().setMaxIntensityRange(maxIntensity);
                maxSlider.setValue(maxSlider.getMaximum());
                maxSlider.repaint();
            }
        });
        file.add(open);

        final JMenuItem export = new JMenuItem("Export");
        export.addActionListener(e -> {
            final int chooseResult = exportFolderChooser.showOpenDialog(frame);
            if (chooseResult == JFileChooser.APPROVE_OPTION) {
                final String path = exportFolderChooser.getSelectedFile().getPath();
                try {
                    getCurrentContext().getVolume().write(path);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        file.add(export);

        final JMenuItem applyMask = new JMenuItem("Apply Mask");
        applyMask.addActionListener(e -> {
            int returnVal = jFileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    final NiftiVolume mask = NiftiVolume.read(jFileChooser.getSelectedFile().getPath());
                    for (int x = 0; x < mask.header.dim[1]; x++) {
                        for (int y = 0; y < mask.header.dim[2]; y++) {
                            for (int z = 0; z < mask.header.dim[3]; z++) {
                                if (!(mask.data.get(x, y, z, 0) > 0)) {
                                    getCurrentContext().getVolume().data.set(x, y, z, 0, 0.0);
                                }
                            }
                        }
                    }
                } catch (IOException ioException) {
                    // ignore
                }
            }
        });
        file.add(applyMask);

        final JMenu view = new JMenu("View");
        final JMenuItem toolsMenuItem = new JMenuItem("Tools");
        toolsMenuItem.addActionListener(e -> {
            tools.setVisible(!tools.isVisible());
        });
        view.add(toolsMenuItem);

        final JMenu edit = new JMenu("Edit");

        final JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> {
            getCurrentContext().undo();
            mriView.repaint();
        });
        edit.add(undo);
        edit.addSeparator();

        final Toolkit toolkit = Toolkit.getDefaultToolkit();

        int w = 16;
        int h = 16;
        int pix[] = new int[w * h];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                pix[y + w * x] = Integer.MAX_VALUE;
            }
        }

        final MemoryImageSource memoryImageSource = new MemoryImageSource(w, h, pix, 0, w);
        final Cursor eraserCursor = toolkit.createCustomCursor(toolkit.createImage(memoryImageSource), new Point(0, 0), "Eraser");

        final JMenuItem erase = new JMenuItem("Erase");
        erase.addActionListener(e -> {
            final Mode mode = getCurrentContext().getMode();
            final JMenuItem menuItem = (JMenuItem) e.getSource();
            if (mode == Mode.ERASE) {
                menuItem.setText("Erase");
                getCurrentContext().setMode(Mode.BUCKET);
                mriView.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else if (mode == Mode.BUCKET) {
                menuItem.setText("Bucket");
                getCurrentContext().setMode(Mode.ERASE);
                mriView.setCursor(eraserCursor);
            }
            menuItem.repaint();
        });
        edit.add(erase);
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
            getCurrentContext().setMinDimension(getCurrentContext().getScroll().getCurrent());
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
