package com.commandercool.components.contextaware;

import static com.commandercool.context.BucketContext.subscribe;

import javax.swing.JLabel;

import com.commandercool.context.BucketContext;
import com.commandercool.context.api.IContextUpdateListener;

public class ContextAwareMaxIntLabel extends JLabel implements IContextUpdateListener {

    private static final String LABEL = "min: ";

    public ContextAwareMaxIntLabel() {
        super();
        setText(LABEL + 0);
        subscribe(this);
    }

    @Override
    public void processUpdate(BucketContext context) {
        if (context.getMaxIntensity().hasChanged()) {
            setText(LABEL + context.getMaxIntensity().getCurrent());
            repaint();
        }
    }

}
