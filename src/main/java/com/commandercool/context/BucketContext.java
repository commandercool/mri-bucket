package com.commandercool.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import com.commandercool.context.api.IContextProperty;
import com.commandercool.context.api.IContextUpdateListener;
import com.commandercool.context.property.ContextProperty;
import com.commandercool.context.wrappers.MriFill;
import com.commandercool.context.wrappers.MriLayer;
import com.commandercool.context.wrappers.VolumeWrapper;
import com.commandercool.utils.LimitedQueue;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BucketContext {

    private static BucketContext current = new BucketContext();
    private static List<IContextUpdateListener> listeners = new ArrayList<>();
    private static List<IContextProperty> properties = new ArrayList<>();

    static {
        Arrays.stream(BucketContext.class.getDeclaredFields()).forEach(f -> {
            if (IContextProperty.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                try {
                    properties.add((IContextProperty) f.get(getCurrentContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Mode mode = Mode.BUCKET;
    private ContextProperty<Integer> maxIntensity = new ContextProperty<>(0, 0);
    private ContextProperty<Integer> minIntensity = new ContextProperty<>(0, 0);

    private double maxIntensityRange = 100;

    private MriLayer mriLayer;
    // Flood fill stuff
    private int threshold = 10;
    private final ContextProperty<Integer> progress = new ContextProperty<>(0, 0);
    private final ContextProperty<Integer> toFillSize = new ContextProperty<>(0, 0);
    private final ContextProperty<Integer> scroll = new ContextProperty<>(0, 0);
    private final ContextProperty<Integer> selectedVolume = new ContextProperty<>(0, 0);

    private int minDimension = 0;
    private volatile boolean fillRunning = false;
    private volatile boolean canceled = false;

    private JLabel minIntLabel;
    private JLabel maxIntLabel;

    private final VolumeWrapper volumeWrapper = new VolumeWrapper();
    private MriFill mriFill = new MriFill(new byte[0][0][0]);

    private LimitedQueue<State> states = new LimitedQueue<>(10);

    public static BucketContext getCurrentContext() {
        return current;
    }

    public static void subscribe(IContextUpdateListener listener) {
        listeners.add(listener);
    }

    public static void notifyListeners() {
        listeners.forEach(l -> l.processUpdate(getCurrentContext()));
        properties.forEach(IContextProperty::reset);
    }

    public void setProgress(int progress) {
        this.progress.setCurrent(progress);
    }

    public void setToFillSize(int toFillSize) {
        this.toFillSize.setCurrent(toFillSize);
    }

    public void setScroll(int scroll) {
        this.scroll.setCurrent(scroll);
    }

    public void setVolume(NiftiVolume volume) {
        volumeWrapper.setVolume(volume);
        mriLayer = new MriLayer(-1, new short[volume.header.dim[3]][volume.header.dim[1]]);
        mriFill = new MriFill(new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]]);
    }

    public void clearSelectedVolume() {
        this.selectedVolume.setCurrent(0);
    }

    public void updateSelectedVolume() {
        byte[][][] filledArray = getMriFill().getFilledArray();
        int volumeSize = 0;
        for (int i = 0; i < filledArray.length; i++) {
            for (int j = 0; j < filledArray[0].length; j++) {
                for (int k = 0; k < filledArray[0][0].length; k++) {
                    if (filledArray[i][j][k] == 1) {
                        volumeSize++;
                    }
                }
            }
        }
        this.selectedVolume.setCurrent(volumeSize);
    }

    public void setMaxIntensity(int maxIntensity) {
        this.maxIntensity.setCurrent(maxIntensity);
    }

    public void setMinIntensity(int minIntensity) {
        this.minIntensity.setCurrent(minIntensity);
    }

    public void saveState() {
        //        try {
        //            final FourDimensionalArray data = volume.data;
        //            final Field dataField = data.getClass().getDeclaredField("data");
        //            dataField.setAccessible(true);
        //            dataField.get(data);
        //            states.push(new State(((double[])dataField.get(data)).clone(), filledArray.clone()));
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
    }

    public void undo() {
        //        if (!states.isEmpty()) {
        //            try {
        //                final State state = states.pop();
        //                final FourDimensionalArray data = this.volume.data;
        //                final Field dataField = data.getClass().getDeclaredField("data");
        //                dataField.setAccessible(true);
        //                dataField.set(data, state.getVolumeData());
        //                this.filledArray = state.getFilledArray();
        //            } catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        }
    }

}
