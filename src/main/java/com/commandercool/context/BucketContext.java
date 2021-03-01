package com.commandercool.context;

import java.lang.reflect.Field;

import com.commandercool.utils.LimitedQueue;
import com.ericbarnhill.niftijio.FourDimensionalArray;
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

    public synchronized static BucketContext getCurrentContext() {
        return current;
    }

    public void setVolume(NiftiVolume volume) {
        this.volume = volume;
        filledArray = new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]];
    }

    public void saveState() {
        try {
            final FourDimensionalArray data = volume.data;
            final Field dataField = data.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            dataField.get(data);
            states.push(new State(((double[])dataField.get(data)).clone(), filledArray.clone()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void undo() {
        if (!states.isEmpty()) {
            try {
                final State state = states.pop();
                final FourDimensionalArray data = this.volume.data;
                final Field dataField = data.getClass().getDeclaredField("data");
                dataField.setAccessible(true);
                dataField.set(data, state.getVolumeData());
                this.filledArray = state.getFilledArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
