package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.EndlessFieldCell;

/**
 * Thread-safe only within locked chunk, not by itself.
 */
public class SimpleFieldCell extends EndlessFieldCell {
    public SimpleFieldCell(boolean isChecked) {
        this.isChecked = isChecked;
    }

    private boolean isChecked;

    // TODO: 25.03.2016 consider single cell synchronization
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "isChecked = " + isChecked();
    }
}
