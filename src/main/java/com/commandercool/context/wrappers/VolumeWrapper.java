package com.commandercool.context.wrappers;

import static com.commandercool.context.BucketContext.notifyListeners;

import com.commandercool.context.api.IContextProperty;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;
import lombok.Setter;

public class VolumeWrapper implements IContextProperty {

    @Getter
    @Setter
    private NiftiVolume volume;
    private boolean updated = false;

    public VolumeWrapper(NiftiVolume volume) {
        this.volume = volume;
    }

    public void update() {
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
