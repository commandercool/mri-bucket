package com.commandercool.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BucketContext {

    private static BucketContext current = new BucketContext();

    private double maxIntensity = 100;
    private double minIntensity = 0;
    private int threshold = 10;

    public synchronized static BucketContext getCurrent() {
        return current;
    }

}
