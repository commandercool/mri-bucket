package com.commandercool.context.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MriLayer {

    private int layer;
    private short[][] cut;

}
