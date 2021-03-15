package com.commandercool.context.wrappers;

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
        filledArray[x][y][z] = value;
    }

}
