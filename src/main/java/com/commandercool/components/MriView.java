package com.commandercool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;

import com.commandercool.context.BucketContext;
import com.commandercool.geometry.Point3D;
import com.ericbarnhill.niftijio.NiftiVolume;

public class MriView extends JPanel {

    private static int SCALE = 3;
    private static int SHIFT = 0;

    private NiftiVolume volume;
    private int scroll = 0;

    private int mouseX = 0;
    private int mouseY = 0;

    private ConcurrentLinkedQueue<Point3D> filled = new ConcurrentLinkedQueue<>();
    private byte[][][] filledArray;

    public MriView(String filename) {

        setNifti(filename);

        addMouseWheelListener(e -> {
            scroll +=e.getUnitsToScroll();
            repaint();
        });

        setFocusable(true);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '+') {
                    SCALE++;
                    repaint();
                } else if (e.getKeyChar() == '-') {
                    SCALE--;
                    if (SCALE < 1) {
                        SCALE = 1;
                    }
                    repaint();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                new Thread(() -> {
                    floodFill();
                }).start();
            }

            public void mousePressed(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public void setNifti(String path) {
        try {
            volume = NiftiVolume.read(path);
            filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void floodFill() {

        final short ny = volume.header.dim[2];
        double oldReference = volume.data.get(scroll, ny - 1 - mouseY / SCALE, mouseX / SCALE, 0);

        filled = new ConcurrentLinkedQueue<>();
        LinkedList<Point3D> toFill = new LinkedList<>();

        final Point3D referencePoint = new Point3D(mouseX / SCALE, mouseY / SCALE, scroll);
        toFill.add(referencePoint);
        double reference = valueAt(referencePoint.getY(), referencePoint.getZ(), referencePoint.getX());

        filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];

        while (!toFill.isEmpty()) {
            final Point3D n = toFill.poll();
            final double intensity = valueAt(n.getY(), n.getZ(), n.getX());

            final double relative = intensity / reference;
            if (Math.abs(intensity - reference) < 10) {
                filledArray[n.getY()][n.getZ()][n.getX()] = 1;
                addIfMissing(new Point3D(n.getX() + 1, n.getY(), n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX() - 1, n.getY(), n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY() + 1, n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY() - 1, n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY(), n.getZ() + 1), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY(), n.getZ() - 1), toFill);
            } else {
                filledArray[n.getY()][n.getZ()][n.getX()] = -1;
            }
            repaint();
        }

    }

    public double getMaxIntensity() {
        double max = 0.0;
        for (int i = 0; i < volume.header.dim[1]; i++) {
            for (int j = 0; j < volume.header.dim[2]; j++) {
                for (int k = 0; k < volume.header.dim[3]; k++) {
                    final double intensity = volume.data.get(i, j, k, 0);
                    if (intensity > max) {
                        max = intensity;
                    }
                }
            }
        }
        return max;
    }

    public double getMinIntensity() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < volume.header.dim[1]; i++) {
            for (int j = 0; j < volume.header.dim[2]; j++) {
                for (int k = 0; k < volume.header.dim[3]; k++) {
                    final double intensity = volume.data.get(i, j, k, 0);
                    if (intensity < min) {
                        min = intensity;
                    }
                }
            }
        }
        return min;
    }

    private void addIfMissing(Point3D point, LinkedList<Point3D> toFill) {
        if (point.getY() < volume.header.dim[1] && point.getZ() < volume.header.dim[2]
                && point.getX() < volume.header.dim[3] && point.getZ() >= 0 && point.getX() >= 0 && point.getY() >= 0) {
            if (filledArray[point.getY()][point.getZ()][point.getX()] == 0) {
                toFill.push(point);
            }
        }
    }

    private double valueAt(int x, int y, int z) {
        double value = volume.data.get(x, y, z, 0);
        final BucketContext context = BucketContext.getCurrent();

        final double k = (context.getMaxIntensity() - context.getMinIntensity() + 1) / 255.0;

        if (value < context.getMinIntensity()) {
            return 0;
        } else if (value > context.getMaxIntensity()) {
            return 255;
        } else {
            return (int) (value - context.getMinIntensity() + 1)/ k;
        }
    }

    public Dimension getMriDimensions() {
        if (volume != null) {
            return new Dimension(volume.header.dim[3] * SCALE + 15, volume.header.dim[2] * SCALE + 36);
        } else {
            return new Dimension(480, 480);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (volume != null) {
            int nx = volume.header.dim[1];
            int ny = volume.header.dim[2];
            int nz = volume.header.dim[3];
            int dim = volume.header.dim[4];

            if (scroll >= ny) {
                scroll = ny - 1;
            } else if (scroll <= 0) {
                scroll = 0;
            }

            for (int z = 0; z < nz; z++) {
                for (int x = 0; x < nx; x++) {
                    final double data = volume.data.get(x, scroll, z, 0);
                    final int intensity = (int) valueAt(x, scroll, z);
                    g.setColor(new Color(intensity, intensity, intensity));
                    g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);

                    if (filledArray[x][scroll][z] == 1) {
                        g.setColor(new Color(250, 0, 0));
                        g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);
                    }

                }
            }
        }
    }

    private Color getRealColor(double data) {
        int color = (int) data;

        int blue = color & 0xff;
        int green = color << 8 & 0xff;
        int red = color << 2 * 8 & 0xff;

        return new Color(red, green, blue);
    }

    private Color getWindowedColor(double data) {
        int rgb;
        if (data < 145 + SHIFT) {
            rgb = 0;
        } else if (data > 701 + SHIFT) {
            rgb =  255;
        } else {
            rgb = (int)((data - (145 + SHIFT))/ 2.18);
        }

        return new Color(rgb, rgb, rgb);

    }

}
