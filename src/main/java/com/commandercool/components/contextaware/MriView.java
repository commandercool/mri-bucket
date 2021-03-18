package com.commandercool.components.contextaware;

import static com.commandercool.context.BucketContext.getCurrentContext;
import static com.commandercool.context.BucketContext.subscribe;
import static com.commandercool.context.Mode.BUCKET;
import static com.commandercool.context.Mode.ERASE;
import static com.commandercool.graphics.ColorMap.getColor;

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

import com.commandercool.context.BucketContext;
import com.commandercool.context.api.IContextUpdateListener;
import com.commandercool.context.wrappers.MriFill;
import com.commandercool.context.wrappers.MriLayer;
import com.commandercool.geometry.Point3D;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;

@Getter
public class MriView extends JPanel implements IContextUpdateListener {

    private static final int EMPTY_VALUE = 0;
    public static final Color FILL_COLOR = new Color(250, 0, 0);

    private static int SCALE = 2;

    private int mouseX = 0;
    private int mouseY = 0;

    private int mouseXPrev = 0;
    private int mouseYPrev = 0;
    private double startMin = 0;
    private double startMax = 0;

    private boolean pressed = false;

    private int[][] mriLayer;

    public MriView() {
        super();
        subscribe(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        addMouseWheelListener(e -> {
            final Integer current = getCurrentContext().getScroll().getCurrent();
            int scroll = current + e.getWheelRotation();
            final short ny = getVolume().header.dim[2];
            if (scroll >= ny) {
                scroll = ny - 1;
            } else if (scroll <= 0) {
                scroll = 0;
            }
            getCurrentContext().setScroll(scroll);
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
                if (getCurrentContext().getMode() == ERASE) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                    if (getCurrentContext().getMode() == ERASE) {
                        int size = 4;
                        int scroll = getCurrentContext().getScroll().getCurrent();
                        for (int x = mouseX / SCALE; x < mouseX / SCALE + 2 * size; x++) {
                            for (int y = mouseY / SCALE; y < mouseY / SCALE + 2 * size; y++) {
                                getCurrentContext().getMriFill().getFilledArray()[y][scroll][x] = 0;
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
                        getCurrentContext().setMinIntensity((int) newMin);
                        getCurrentContext().setMaxIntensity((int) newMax);
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
                if (getCurrentContext().getMode() == BUCKET && !getCurrentContext().isFillRunning()
                        && getCurrentContext().getVolumeWrapper().getVolume() != null) {
                    new Thread(() -> {
                        getCurrentContext().saveState();
                        floodFill();
                    }).start();
                }
            }

            public void mousePressed(MouseEvent e) {
                mouseXPrev = e.getX();
                mouseYPrev = e.getY();
                startMin = getCurrentContext().getMinIntensity().getCurrent();
                startMax = getCurrentContext().getMaxIntensity().getCurrent();
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
        getCurrentContext().getMriFill()
                .setFilledArray(new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]]);
    }

    private NiftiVolume getVolume() {
        return getCurrentContext().getVolumeWrapper().getVolume();
    }

    private MriFill getFilledArray() {
        return getCurrentContext().getMriFill();
    }

    public void subtractSelection() {
        getCurrentContext().saveState();
        final NiftiVolume volume = getVolume();
        final MriFill mriFill = getFilledArray();
        for (int i = 0; i < volume.header.dim[1]; i++) {
            for (int j = 0; j < volume.header.dim[2]; j++) {
                for (int k = 0; k < volume.header.dim[3]; k++) {
                    if (mriFill.getFilledArray()[i][j][k] == 1) {
                        volume.data.set(i, j, k, 0, EMPTY_VALUE);
                    }
                }
            }
        }
        resetSelection();
    }

    public void invertSelection() {
        getCurrentContext().clearSelectedVolume();
        final NiftiVolume volume = getVolume();
        final MriFill mriFill = getFilledArray();
        for (int i = 0; i < volume.header.dim[1]; i++) {
            for (int j = 0; j < volume.header.dim[2]; j++) {
                for (int k = 0; k < volume.header.dim[3]; k++) {
                    if (j >= getCurrentContext().getMinDimension().getCurrent()) {
                        if (mriFill.getFilledArray()[i][j][k] == 1) {
                            mriFill.set(i, j, k, (byte) 0);
                        } else {
                            mriFill.set(i, j, k, (byte) 1);
                        }
                    }
                }
            }
        }
        getCurrentContext().updateSelectedVolume();
    }

    public void floodFill() {
        getCurrentContext().setProgress(0);
        getCurrentContext().setToFillSize(1);
        getCurrentContext().clearSelectedVolume();

        getCurrentContext().setFillRunning(true);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final NiftiVolume volume = getVolume();

        List<Point3D> toFill = new ArrayList<>(volume.header.dim[1] * volume.header.dim[2] * volume.header.dim[3] / 10);

        int scroll = getCurrentContext().getScroll().getCurrent();

        final Point3D referencePoint = new Point3D(mouseX / SCALE, mouseY / SCALE, scroll);
        toFill.add(referencePoint);
        double reference = valueAt(referencePoint.getY(), referencePoint.getZ(), referencePoint.getX());

        getCurrentContext().getMriFill()
                .setFilledArray(new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]]);
        final MriFill mriFill = getFilledArray();

        int processed = 0;

        while (!toFill.isEmpty() && !getCurrentContext().isCanceled()) {
            processed++;
            final Point3D n = toFill.remove(toFill.size() - 1);
            final double intensity = valueAt(n.getY(), n.getZ(), n.getX());

            final int threshold = getCurrentContext().getThreshold();

            if ((int) intensity != EMPTY_VALUE && Math.abs(intensity - reference) < threshold) {
                mriFill.set(n.getY(), n.getZ(), n.getX(), (byte) 1);
                if (n.getZ() == getCurrentContext().getScroll().getCurrent()) {
                    repaint();
                }
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        if (i != 0 || j != 0) {
                            addIfMissing(new Point3D(n.getX() + i, n.getY() + j, n.getZ()), toFill);
                            addIfMissing(new Point3D(n.getX(), n.getY() + i, n.getZ() + j), toFill);
                        }
                    }
                }
            } else {
                mriFill.set(n.getY(), n.getZ(), n.getX(), (byte) -1);
            }
            getCurrentContext().setProgress(processed);
        }
        getCurrentContext().setCanceled(false);
        getCurrentContext().setFillRunning(false);
        getCurrentContext().updateSelectedVolume();
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

    // TODO: move to NiftiVolume or to wrapper
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
        final MriFill mriFill = getFilledArray();
        if (point.getY() < volume.header.dim[1] && point.getZ() < volume.header.dim[2]
                && point.getX() < volume.header.dim[3] && point.getZ() >= getCurrentContext().getMinDimension()
                .getCurrent() && point.getX() >= 0 && point.getY() >= 0) {
            if (mriFill.getFilledArray()[point.getY()][point.getZ()][point.getX()] == 0) {
                mriFill.set(point.getY(), point.getZ(), point.getX(), (byte) 2);
                toFill.add(point);
                getCurrentContext().setToFillSize(getCurrentContext().getToFillSize().getCurrent() + 1);
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

        Integer maxIntensity = context.getMaxIntensity().getCurrent();
        Integer minIntensity = context.getMinIntensity().getCurrent();

        final double k = (maxIntensity - minIntensity + 1) / 255.0;

        if (value < minIntensity) {
            return 0;
        } else if (value > maxIntensity) {
            return 255;
        } else {
            return (int) (value - minIntensity + 1) / k;
        }
    }

    public Dimension getMriDimensions() {
        final NiftiVolume volume = getVolume();
        if (volume != null) {
            return new Dimension(volume.header.dim[3] * SCALE, volume.header.dim[2] * SCALE);
        } else {
            return new Dimension(480, 480);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final NiftiVolume volume = getVolume();
        final MriFill filledArray = getFilledArray();
        if (volume != null) {
            int nx = volume.header.dim[1];
            int nz = volume.header.dim[3];

            if (mriLayer == null) {
                mriLayer = new int[nz][nx];
            }

            int scroll = getCurrentContext().getScroll().getCurrent();
            final MriLayer mriLayer = getCurrentContext().getMriLayer();

            for (int z = 0; z < nz; z++) {
                for (int x = 0; x < nx; x++) {
                    if (mriLayer.getLayer() != scroll) {
                        mriLayer.getCut()[z][x] = (short) valueAt(x, scroll, z);
                    }

                    Color color = getColor(mriLayer.getCut()[z][x]);
                    if (scroll == getCurrentContext().getMinDimension().getCurrent()) {
                        color = new Color(0, color.getGreen(), 0);
                    }
                    g.setColor(color);
                    g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);

                    if (filledArray.getFilledArray()[x][scroll][z] == 1) {
                        if (scroll == getCurrentContext().getMinDimension().getCurrent()) {
                            g.setColor(new Color(255, color.getGreen(), 0));
                        } else {
                            g.setColor(FILL_COLOR);
                        }
                        g.fillRect(z * SCALE, x * SCALE, SCALE, SCALE);
                    }
                }
            }
        }
    }

    @Override
    public void processUpdate(BucketContext context) {
        if (context.getVolumeWrapper().hasChanged()) {
            setPreferredSize(getMriDimensions());
            repaint();
            return;
        }
        if (context.getScroll().hasChanged() || getCurrentContext().getSelectedVolume().hasChanged()
                || getCurrentContext().getMinDimension().hasChanged()) {
            repaint();
        }
    }
}
