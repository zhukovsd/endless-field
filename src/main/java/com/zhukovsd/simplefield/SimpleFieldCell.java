package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.EndlessFieldCell;

/**
 * Created by ZhukovSD on 20.03.2016.
 */
public class SimpleFieldCell extends EndlessFieldCell {
    public SimpleFieldCell(boolean isChecked) {
        this.isChecked = isChecked;
    }

    private boolean isChecked;

    // TODO: 25.03.2016 consider single cell synchronization
    public synchronized boolean isChecked() {
        return isChecked;
    }

    public synchronized void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "isChecked = " + isChecked();
    }
}
