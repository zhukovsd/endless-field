package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.field.EndlessCellCloneFactory;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

/**
 * Thread-safe only within locked chunk, not by itself.
 */
public class SimpleFieldCell extends EndlessFieldCell<SimpleFieldCell> {
    public SimpleFieldCell(boolean isChecked) {
        this.isChecked = isChecked;
    }

    // clone constructor. should be called only if source is locked, otherwise transitional state may be cloned
    private SimpleFieldCell(EndlessFieldCell source) {
        SimpleFieldCell casted = ((SimpleFieldCell) source);
        this.isChecked = casted.isChecked;
//        this.s = casted.s;
    }

    @Override
    public EndlessCellCloneFactory cloneFactory() {
        return (source) -> new SimpleFieldCell(source);
    }

    //@SerializedName("c")
    private boolean isChecked;

    transient int a = 0, b = 0, c = 123;
//    public transient String s;

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
