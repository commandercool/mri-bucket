package com.commandercool.context.wrappers;

import com.commandercool.context.BucketContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MriFill {

    private byte[][][] filledArray;

    public void setFilledArray(byte[][][] filledArray) {
        this.filledArray = filledArray;
    }

    public void set(int x, int y, int z, byte value) {
        if (value == 1) {
            int volume = BucketContext.getCurrentContext().getSelectedVolume().getCurrent();
            volume++;
            BucketContext.getCurrentContext().setSelectedVolume(volume);
        }
        filledArray[x][y][z] = value;
    }

}
