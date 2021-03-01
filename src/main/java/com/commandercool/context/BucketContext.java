package com.commandercool.context;

import com.commandercool.utils.LimitedQueue;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BucketContext {

    private static BucketContext current = new BucketContext();

    private double maxIntensity = 100;
    private double minIntensity = 0;
    private int threshold = 10;
    private int minDimension = 0;

    private NiftiVolume volume;
    private byte[][][] filledArray;

    private LimitedQueue<State> states = new LimitedQueue<>(10);

    public synchronized static BucketContext getCurrent() {
        return current;
    }

    public void setVolume(NiftiVolume volume) {
        this.volume = volume;
        filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];
    }

    public void saveState() {
        states.push(new State(volume, filledArray));
    }

    public void undo() {
        final State state = states.pop();
        this.volume = state.getVolume();
        this.filledArray = state.getFilledArray();
    }

}
