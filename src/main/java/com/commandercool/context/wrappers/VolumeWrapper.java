package com.commandercool.context.wrappers;

import static com.commandercool.context.BucketContext.notifyListeners;

import com.commandercool.context.api.IContextProperty;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;

public class VolumeWrapper implements IContextProperty {

    @Getter
    private NiftiVolume volume;
    private volatile boolean updated = false;

    public void setVolume(NiftiVolume volume) {
        this.volume = volume;
        updated = true;
        notifyListeners();
    }

    @Override
    public boolean hasChanged() {
        return updated;
    }

    @Override
    public void reset() {
        updated = false;
    }
}
