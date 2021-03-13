package com.commandercool.components.contextaware;

import static com.commandercool.context.BucketContext.subscribe;

import javax.swing.JLabel;

import com.commandercool.context.BucketContext;
import com.commandercool.context.api.IContextUpdateListener;

public class ContextAwareVolumeLabel extends JLabel implements IContextUpdateListener {

    private static final String LABEL = "v: ";

    public ContextAwareVolumeLabel() {
        super();
        setText(LABEL + 0);
        subscribe(this);
    }

    @Override
    public void processUpdate(BucketContext context) {
        if (context.getSelectedVolume().hasChanged()) {
            setText(LABEL + context.getSelectedVolume().getCurrent());
            repaint();
        }
    }
}
