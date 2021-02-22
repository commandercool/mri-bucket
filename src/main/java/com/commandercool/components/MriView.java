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

        setFocusable(true);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
//                System.out.println("Key pressed! " + e.getKeyChar());
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

    public void floodFill() {

        final short ny = volume.header.dim[2];
        double reference = volume.data.get(x, ny - 1 - mouseY / SCALE, mouseX / SCALE, 0);

        filled = new ConcurrentLinkedQueue<>();
        LinkedList<Point3D> toFill = new LinkedList<>();

        toFill.add(new Point3D(mouseX / SCALE, mouseY / SCALE, x));

        filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];

        while (!toFill.isEmpty()) {
            final Point3D n = toFill.poll();
            final double intensity = valueAt(n);

            final double relative = intensity / reference;
            if (relative > 0.95 && relative < 1.05) {
                filledArray[n.getZ()][ny - 1 - n.getY()][n.getX()] = 1;
                addIfMissing(new Point3D(n.getX() + 1, n.getY(), n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX() - 1, n.getY(), n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY() + 1, n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY() - 1, n.getZ()), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY(), n.getZ() + 1), toFill);
                addIfMissing(new Point3D(n.getX(), n.getY(), n.getZ() - 1), toFill);
            } else {
                filledArray[n.getZ()][ny - 1 - n.getY()][n.getX()] = -1;
            }
            repaint();
        }

    }

    private void addIfMissing(Point3D point, LinkedList<Point3D> toFill) {
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

                int color = (int) data;

                int blue = color & 0xff;
                int green = color << 8 & 0xff;
                int red = color << 2 * 8 & 0xff;

                g.setColor(new Color(red, green, blue));
                g.fillRect(k * SCALE, j * SCALE, SCALE, SCALE);

                if (filledArray[x][ny - 1 - j][k] == 1) {
                    g.setColor(new Color(250, 0, 0));
                    g.fillRect(k * SCALE, j * SCALE, SCALE, SCALE);
                }
            }
            System.out.println();
        }

        g.setColor(new Color(255, 0, 0));
        g.fillRect(mouseX, mouseY, 4,4);
    }

}
