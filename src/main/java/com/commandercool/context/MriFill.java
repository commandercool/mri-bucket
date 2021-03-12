package com.commandercool.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MriFill {

    private int volume;
    private byte[][][] filledArray;

    public void setFilledArray(byte[][][] filledArray) {
        volume = 0;
        this.filledArray = filledArray;
    }

    public void set(int x, int y, int z, byte value) {
        if (value == 1) {
            volume++;
            System.out.println(volume);
        }
        filledArray[x][y][z] = value;
    }

}
