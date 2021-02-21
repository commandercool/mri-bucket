package com.commandercool.geometry;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Point3D {

    private int x;
    private int y;
    private int z;

    @Override
    public String toString() {
        return "x=" + x + ", y=" + y + ", z=" + z;
    }
}
