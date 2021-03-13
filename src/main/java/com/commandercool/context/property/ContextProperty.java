package com.commandercool.context.property;

import static com.commandercool.context.BucketContext.notifyListeners;

import com.commandercool.context.api.IContextProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContextProperty<T> implements IContextProperty {

    private T current;
    private T previous;
    private boolean changed;

    public ContextProperty(T current, T previous) {
        this.current = current;
        this.previous = previous;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void reset() {
        changed = false;
    }

    public void setCurrent(T current) {
        this.previous = this.current;
        this.current = current;
        changed = true;
        notifyListeners();
    }
}
