package com.commandercool.components;

import static com.commandercool.context.BucketContext.getCurrentContext;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.commandercool.context.BucketContext;
import com.commandercool.context.Mode;
import com.commandercool.geometry.Point3D;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;

@Getter
public class MriView extends JPanel {

    private static final int EMPTY_VALUE = 0;

    private static int SCALE = 2;

    private int scroll = 0;

    private int mouseX = 0;
    private int mouseY = 0;

    private int mouseXPrev = 0;
    private int mouseYPrev = 0;
    private double startMin = 0;
    private double startMax = 0;

    private boolean pressed = false;

    public MriView() {

        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        addMouseWheelListener(e -> {
            scroll += e.getWheelRotation();
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

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                final int button = e.getButton();
                if (button == 1) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    if (getCurrentContext().getMode() == Mode.ERASE) {
                        int size = 4;
                        for (int x = mouseX / SCALE - size; x < mouseX / SCALE + size; x++) {
                            for (int y = mouseY / SCALE - size; y < mouseY / SCALE + size; y++) {
                                getVolume().data.set(y, scroll, x, 0, EMPTY_VALUE);
                            }
                        }
                        repaint();
                    }
                } else if (pressed) {
                    final int intesityDiff = mouseYPrev - e.getY();
                    final int shift = e.getX() - mouseXPrev;

                    double newMin = startMin + intesityDiff + shift;
                    double newMax = startMax - intesityDiff + shift;

                    if (newMax - newMin > 10) {
                        final double maxRange = getCurrentContext().getMaxIntensityRange();
                        if (newMin < 0) {
                            newMin = 0;
                        }
                        if (newMax > maxRange) {
                            newMax = maxRange;
                        }
                        getCurrentContext().setMinIntensity(newMin);
                        getCurrentContext().setMaxIntensity(newMax);
                    }

                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (getCurrentContext().getMode() == Mode.BUCKET) {
                    new Thread(() -> {
                        getCurrentContext().saveState();
                        floodFill();
                    }).start();
                }
            }

            public void mousePressed(MouseEvent e) {
                mouseXPrev = e.getX();
                mouseYPrev = e.getY();
                startMin = getCurrentContext().getMinIntensity();
                startMax = getCurrentContext().getMaxIntensity();
                pressed = true;
            }

            public void mouseReleased(MouseEvent e) {
                pressed = false;
            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public void resetSelection() {
        getCurrentContext().saveState();
        final NiftiVolume volume = getVolume();
        getCurrentContext().setFilledArray(new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]]);
    }

    private NiftiVolume getVolume() {
        return getCurrentContext().getVolume();
    }

    private byte[][][] getFilledArray() {
        return getCurrentContext().getFilledArray();
    }

    public void subtractSelection() {
        getCurrentContext().saveState();
        final NiftiVolume volume = getVolume();
        final byte[][][] filledArray = getFilledArray();
        for (int i = 0; i < volume.header.dim[1]; i++) {
            for (int j = 0; j < volume.header.dim[2]; j++) {
                for (int k = 0; k < volume.header.dim[3]; k++) {
                    if (filledArray[i][j][k] == 1) {
                        volume.data.set(i, j, k, 0, EMPTY_VALUE);
                    }
                }
            }
        }
        resetSelection();
    }

    public void invertSelection() {
        getCurrentContext().saveState();
        final NiftiVolume volume = getVolume();
        final byte[][][] filledArray = getFilledArray();
        for (int i = 0; i < volume.header.dim[1]; i++) {
            for (int j = 0; j < volume.header.dim[2]; j++) {
                for (int k = 0; k < volume.header.dim[3]; k++) {
                    if (filledArray[i][j][k] == 1) {
                        filledArray[i][j][k] = 0;
                    } else {
                        filledArray[i][j][k] = 1;
                    }
                }
            }
        }
    }

    public void floodFill() {
        final JProgressBar progressBar = getCurrentContext().getProgressBar();
        progressBar.setValue(0);

        getCurrentContext().setFillRunning(true);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final NiftiVolume volume = getVolume();

        List<Point3D> toFill = new ArrayList<>(volume.header.dim[1] * volume.header.dim[2] * volume.header.dim[3] / 10);

        final Point3D referencePoint = new Point3D(mouseX / SCALE, mouseY / SCALE, scroll);
        toFill.add(referencePoint);
        double reference = valueAt(referencePoint.getY(), referencePoint.getZ(), referencePoint.getX());

        getCurrentContext().setFilledArray(new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]]);
        final byte[][][] filledArray = getFilledArray();

        progressBar.setMaximum(1);
        int processed = 0;

        while (!toFill.isEmpty() && !getCurrentContext().isCanceled()) {
            processed++;
            final Point3D n = toFill.remove(toFill.size() - 1);
            final double intensity = valueAt(n.getY(), n.getZ(), n.getX());

            final int threshold = getCurrentContext().getThreshold();

            if ((int) intensity != EMPTY_VALUE && Math.abs(intensity - reference) < threshold) {
                filledArray[n.getY()][n.getZ()][n.getX()] = 1;
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        if (i != 0 || j != 0) {
                            addIfMissing(new Point3D(n.getX() + i, n.getY() + j, n.getZ()), toFill);
                            addIfMissing(new Point3D(n.getX(), n.getY() + i, n.getZ() + j), toFill);
                        }
                    }
                }
            } else {
                filledArray[n.getY()][n.getZ()][n.getX()] = -1;
            }
            if (toFill.size() > progressBar.getMaximum()) {
                progressBar.setMaximum(toFill.size());
            }
            progressBar.setValue(processed);
            progressBar.repaint();
            repaint();
        }
        getCurrentContext().setCanceled(false);
        getCurrentContext().setFillRunning(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public double getMaxIntensity() {
        final NiftiVolume volume = getVolume();
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

    // TODO: move to NiftiVolume
    public double getMinIntensity() {
        final NiftiVolume volume = getVolume();
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

    private void addIfMissing(Point3D point, List<Point3D> toFill) {
        final NiftiVolume volume = getVolume();
        final byte[][][] filledArray = getFilledArray();
        if (point.getY() < volume.header.dim[1] && point.getZ() < volume.header.dim[2]
                && point.getX() < volume.header.dim[3] && point.getZ() >= 0 && point.getX() >= 0 && point.getY() >= 0) {
            if (filledArray[point.getY()][point.getZ()][point.getX()] == 0) {
                filledArray[point.getY()][point.getZ()][point.getX()] = 2;
                toFill.add(point);
            }
        }
    }

    // TODO: add to nifti
    private double valueAt(int x, int y, int z) {
        final NiftiVolume volume = getVolume();
        double value = volume.data.get(x, y, z, 0);
        if ((int) value == EMPTY_VALUE) {
            return value;
        }
        final BucketContext context = getCurrentContext();

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
        final NiftiVolume volume = getVolume();
        if (volume != null) {
            return new Dimension(volume.header.dim[3] * SCALE + 15, volume.header.dim[2] * SCALE + 36);
        } else {
            return new Dimension(480, 480);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final NiftiVolume volume = getVolume();
        final byte[][][] filledArray = getFilledArray();
        if (volume != null) {
            int nx = volume.header.dim[1];
            int ny = volume.header.dim[2];
            int nz = volume.header.dim[3];

            if (scroll >= ny) {
                scroll = ny - 1;
            } else if (scroll <= getCurrentContext().getMinDimension()) {
                scroll = getCurrentContext().getMinDimension();
            }

            for (int z = 0; z < nz; z++) {
                for (int x = 0; x < nx; x++) {
                    final int intensity = (int) valueAt(x, scroll, z);
                    g.setColor(new Color(intensity, intensity, intensity));
                    g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);

                    if (filledArray[x][scroll][z] == 1) {
                        g.setColor(new Color(250, 0, 0));
                        g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);
                    } else if (filledArray[x][scroll][z] == 2) {
                        g.setColor(new Color(0, 0, 250));
                        g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);
                    }
                }
            }
        }
    }

}
