package com.commandercool.context;

import com.ericbarnhill.niftijio.NiftiVolume;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class State {

    private final Object volumeData;
    private final byte[][][] filledArray;

}
