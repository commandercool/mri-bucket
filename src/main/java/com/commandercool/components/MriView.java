package com.commandercool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;

import com.commandercool.geometry.Point3D;
import com.ericbarnhill.niftijio.NiftiVolume;

public class MriView extends JPanel {

    private static final int SCALE = 2;

    private NiftiVolume volume;
    private int x = 0;

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
            x+=e.getUnitsToScroll();
            repaint();
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

    public void floodFill() {

        final short ny = volume.header.dim[2];
        double reference = volume.data.get(x, ny - 1 - mouseY / SCALE, mouseX / SCALE, 0);

        filled = new ConcurrentLinkedQueue<>();
        LinkedList<Point3D> rejected = new LinkedList<>();
        LinkedList<Point3D> toFill = new LinkedList<>();

        toFill.add(new Point3D(mouseX / SCALE, mouseY / SCALE, x));

        filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];

        while (!toFill.isEmpty()) {
            final Point3D n = toFill.poll();
            if (Math.abs(reference - valueAt(n)) < 100) {
                filledArray[n.getZ()][ny - 1 - n.getY()][n.getX()] = 1;
//                filled.add(n);
                addIfMissing(new Point3D(n.getX() + 1, n.getY(), n.getZ()), toFill, filled, rejected);
                addIfMissing(new Point3D(n.getX() - 1, n.getY(), n.getZ()), toFill, filled, rejected);
                addIfMissing(new Point3D(n.getX(), n.getY() + 1, n.getZ()), toFill, filled, rejected);
                addIfMissing(new Point3D(n.getX(), n.getY() - 1, n.getZ()), toFill, filled, rejected);
                addIfMissing(new Point3D(n.getX(), n.getY(), n.getZ() + 1), toFill, filled, rejected);
                addIfMissing(new Point3D(n.getX(), n.getY(), n.getZ() - 1), toFill, filled, rejected);
            } else {
                filledArray[n.getZ()][ny - 1 - n.getY()][n.getX()] = -1;
            }
            repaint();
        }

    }

    private void addIfMissing(Point3D point, LinkedList<Point3D> toFill, ConcurrentLinkedQueue<Point3D> filled, LinkedList<Point3D> rejected) {
        if (filledArray[point.getZ()][volume.header.dim[2] - 1 - point.getY()][point.getX()] == 0) {
            toFill.push(point);
        }
    }

    private double valueAt(Point3D point) {
        final short ny = volume.header.dim[2];
        return volume.data.get(point.getZ(), ny - 1 - point.getY(), point.getX(), 0);
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

        if (x >= nx) {
            x = nx - 1;
        } else if (x <= 0) {
            x = 0;
        }

        for (int j = 0; j < ny; j++) {
            for (int k = 0; k < nz; k++) {
                final double data = volume.data.get(x, ny - 1 - j, k, 0);
                int rgb = (int) data / 3;
                if (rgb > 255) {
                    rgb = 255;
                }
                g.setColor(new Color(rgb, rgb, rgb));
                g.fillRect(k * SCALE, j * SCALE, SCALE, SCALE);

                if (filledArray[x][ny - 1 - j][k] == 1) {
                    g.setColor(new Color(250, 0, 0));
                    g.fillRect(k * SCALE, j * SCALE, SCALE, SCALE);
                }
            }
            System.out.println();
        }

//        filled.stream().filter(p -> p.getZ() == x).forEach(p -> {
//            double data = volume.data.get(x, ny - 1 - p.getY(), p.getX(), 0);
//            int rgb = (int) data / 5;
//            if (rgb > 255) {
//                rgb = 255;
//            }
//            g.setColor(new Color(250, rgb, rgb));
//            g.fillRect(p.getX() * SCALE, p.getY() * SCALE, SCALE,SCALE);
//        });

//        System.out.println("Selected pixel: " + volume.data.get(x, ny - 1 - mouseY / SCALE, mouseX / SCALE, 0));

        g.setColor(new Color(255, 0, 0));
        g.fillRect(mouseX, mouseY, 4,4);
    }

}
