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
        try {
            volume = NiftiVolume.read(filename);
            filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];
        } catch (IOException e) {
            e.printStackTrace();
        }
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
//                repaint();
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
        if (value < 145 + SHIFT) {
            return 0;
        } else if (value > 701 + SHIFT) {
            return 255;
        } else {
            return (int) (value - (145 + SHIFT))/ 2.18;
        }
    }

    public Dimension getMriDimensions() {
        return new Dimension(volume.header.dim[3] * SCALE + 15, volume.header.dim[2] * SCALE + 36);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


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

//        for (int j = 0; j < ny; j++) {
//            for (int k = 0; k < nz; k++) {
//                final double data = volume.data.get(x, ny - 1 - j, k, 0);
//
//                g.setColor(getWindowedColor(data));
//                g.fillRect(k * SCALE, j * SCALE, SCALE, SCALE);
//
//                if (filledArray[x][ny - 1 - j][k] == 1) {
//                    g.setColor(new Color(250, 0, 0));
//                    g.fillRect(k * SCALE, j * SCALE, SCALE, SCALE);
//                }
//            }
//        }
//
//        System.out.println("Intencity: " + volume.data.get(x, ny - 1 - mouseY / SCALE, mouseX / SCALE, 0));
//
//        g.setColor(new Color(255, 0, 0));
//        g.fillRect(mouseX, mouseY, 4,4);
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
