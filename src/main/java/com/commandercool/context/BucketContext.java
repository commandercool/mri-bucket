package com.commandercool.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import com.commandercool.utils.LimitedQueue;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BucketContext {

    private static BucketContext current = new BucketContext();
    private static List<IContextUpdateListener> listeners = new ArrayList<>();
    private static List<ContextProperty<Object>> properties = new ArrayList<>();

    static {
        Arrays.stream(BucketContext.class.getDeclaredFields()).forEach(f -> {
            if (f.getType().isInstance(ContextProperty.class)) {
                f.setAccessible(true);
                try {
                    properties.add((ContextProperty<Object>) f.get(getCurrentContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Mode mode = Mode.BUCKET;
    private double maxIntensity = 100;
    private double minIntensity = 0;

    private double maxIntensityRange = 100;

    private MriLayer mriLayer;
    // Flood fill stuff
    private int threshold = 10;
    private ContextProperty<Integer> progress = new ContextProperty<>(0, 0);
    private ContextProperty<Integer> toFillSize = new ContextProperty<>(0, 0);
    private ContextProperty<Integer> scroll = new ContextProperty<>(0, 0);

    private int minDimension = 0;
    private volatile boolean fillRunning = false;
    private volatile boolean canceled = false;

    private JLabel minIntLabel;
    private JLabel maxIntLabel;

    private NiftiVolume volume;
    private MriFill mriFill = new MriFill(0, new byte[0][0][0]);

    private LimitedQueue<State> states = new LimitedQueue<>(10);

    public static BucketContext getCurrentContext() {
        return current;
    }

    public static void subscribe(IContextUpdateListener listener) {
        listeners.add(listener);
    }

    static void notifyListeners() {
        listeners.forEach(l -> l.processUpdate(getCurrentContext()));
        properties.forEach(ContextProperty::reset);
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
        this.volume = volume;
        this.mriLayer = new MriLayer(-1, new short[volume.header.dim[3]][volume.header.dim[1]]);
        mriFill = new MriFill(0, new byte[volume.header.dim[1]][volume.header.dim[2]][volume.header.dim[3]]);
    }

    public void setMaxIntensity(double maxIntensity) {
        this.maxIntensity = maxIntensity;
        this.maxIntLabel.setText("max: " + (int) maxIntensity + " v: " + mriFill.getVolume());
    }

    public void setMinIntensity(double minIntensity) {
        this.minIntensity = minIntensity;
        this.minIntLabel.setText("min: " + (int) minIntensity);
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
