package com.commandercool.components.contextaware;

import static com.commandercool.context.BucketContext.subscribe;

import javax.swing.JProgressBar;

import com.commandercool.context.BucketContext;
import com.commandercool.context.api.IContextUpdateListener;
import com.commandercool.context.property.ContextProperty;

public class ContextAwareProgressBar extends JProgressBar implements IContextUpdateListener {

    public ContextAwareProgressBar() {
        super();
        subscribe(this);
    }

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
