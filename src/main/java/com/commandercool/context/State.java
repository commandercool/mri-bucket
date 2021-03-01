package com.commandercool.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class State {

    private final Object volumeData;
    private final byte[][][] filledArray;

}
