package com.commandercool.context;

import static com.commandercool.context.BucketContext.notifyListeners;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContextProperty<T> {

    private T current;
    private T previous;
    private boolean changed;

    public ContextProperty(T current, T previous) {
        this.current = current;
        this.previous = previous;
    }

    public boolean hasChanged() {
        return changed;
    }

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