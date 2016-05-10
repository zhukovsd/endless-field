package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.EndlessCellCloneFactory;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

/**
 * Thread-safe only within locked chunk, not by itself.
 */
public class SimpleFieldCell extends EndlessFieldCell {
    public SimpleFieldCell(boolean isChecked) {
        this.isChecked = isChecked;
    }

    protected SimpleFieldCell(EndlessFieldCell source) {
        SimpleFieldCell casted = ((SimpleFieldCell) source);
        this.isChecked = casted.isChecked;
    }

    @Override
    public EndlessCellCloneFactory getFactory() {
        return SimpleFieldCell::new;
    }

    //@SerializedName("c")
    private boolean isChecked;

    transient private int a = 0, b = 0, c = 123;

    // TODO: 25.03.2016 consider single cell synchronization
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void incA() {
        a++;
    }

    public void incB() {
        b++;
    }

    public int a() {
        return a;
    }

    public int b() {
        return a;
    }

    @Override
    public String toString() {
        return super.toString() + ", isChecked = " + isChecked();
    }
}
