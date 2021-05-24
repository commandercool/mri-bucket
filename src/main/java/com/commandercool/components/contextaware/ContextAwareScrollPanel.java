package com.commandercool.components.contextaware;

import static com.commandercool.context.BucketContext.getCurrentContext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.commandercool.context.BucketContext;
import com.commandercool.context.api.IContextUpdateListener;
import com.ericbarnhill.niftijio.NiftiVolume;

public class ContextAwareScrollPanel extends JPanel implements IContextUpdateListener {

    public static final Color SCROLL_BAR_COLOR = new Color(0, 0, 0);
    public static final Color CUT_ABOVE_COLOR = new Color(0, 0, 255);
    public static final Color CURL_BELOW_COLOR = new Color(0, 255, 0);
    public static final Color SCROLL_MARK_COLOR = new Color(255, 0, 0);

    int scale = 1;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        NiftiVolume volume = getCurrentContext().getVolumeWrapper().getVolume();
        if (volume != null) {
            int height = volume.header.dim[1] * scale;
            int minScroll = getCurrentContext().getMinDimension().getCurrent() * scale;
            int maxScroll = getCurrentContext().getMaxDimension().getCurrent() * scale;

            g.setColor(CUT_ABOVE_COLOR);
            g.fillRect(2, 0, 2, height - maxScroll);

            g.setColor(SCROLL_BAR_COLOR);
            g.fillRect(2, height - maxScroll, 2, maxScroll - minScroll);

            g.setColor(CURL_BELOW_COLOR);
            g.fillRect(2, height - minScroll, 2, minScroll);

            int scrollPosition = height - getCurrentContext().getScroll().getCurrent() * scale;

            g.setColor(SCROLL_MARK_COLOR);
            g.fillRect(0, scrollPosition, 6, 4);
        }
    }

    @Override
    public void processUpdate(BucketContext context) {
        if (context.getVolumeWrapper().hasChanged() && getCurrentContext().getVolumeWrapper().getVolume() != null) {
            setPreferredSize(new Dimension(6, getCurrentContext().getVolumeWrapper().getVolume().header.dim[1] * scale));
            repaint();
            return;
        }
        if (context.getScroll().hasChanged() || context.getMaxDimension().hasChanged() || context.getMinDimension()
                .hasChanged()) {
            repaint();
        }
    }
}
