package com.commandercool.graphics;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorMap {

    private static Map<Integer, Color> colorMap = new HashMap<>();

    static {
        for (int i = 0; i < 256; i++) {
            colorMap.put(i, new Color(i, i, i));
        }
    }

    public static Color getColor(int intensity) {
        return colorMap.get(intensity);
    }

}
