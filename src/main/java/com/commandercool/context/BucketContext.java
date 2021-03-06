package com.commandercool.context;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.commandercool.utils.LimitedQueue;
import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BucketContext {

    private static BucketContext current = new BucketContext();

    private Mode mode = Mode.BUCKET;
    private double maxIntensity = 100;
    private double minIntensity = 0;

    private double maxIntensityRange = 100;

    private int threshold = 10;
    private int minDimension = 0;
    private volatile boolean fillRunning = false;
    private volatile boolean canceled = false;

    private JProgressBar progressBar;
    private JLabel minIntLabel;
    private JLabel maxIntLabel;

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

    public void setMaxIntensity(double maxIntensity) {
        this.maxIntensity = maxIntensity;
        this.maxIntLabel.setText("max: " + (int) maxIntensity);
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
