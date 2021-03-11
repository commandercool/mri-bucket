package com.commandercool.components.contextaware;

import javax.swing.JProgressBar;

import com.commandercool.context.BucketContext;
import com.commandercool.context.ContextProperty;
import com.commandercool.context.IContextUpdateListener;

public class ContextAwareProgressBar extends JProgressBar implements IContextUpdateListener {

    @Override
    public void processUpdate(BucketContext context) {
        boolean needsRepaint = false;
        final ContextProperty<Integer> progress = context.getProgress();
        if (progress.hasChanged()) {
            setValue(progress.getCurrent());
            needsRepaint = true;
        }
        final ContextProperty<Integer> toFillSize = context.getToFillSize();
        if (toFillSize.hasChanged()) {
            setMaximum(toFillSize.getCurrent());
            needsRepaint = true;
        }
        if (needsRepaint) {
            repaint();
        }
    }

}
