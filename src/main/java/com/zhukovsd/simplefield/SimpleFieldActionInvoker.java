/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldAction;
import com.zhukovsd.endlessfield.field.EndlessFieldActionInvoker;

/**
 * Created by ZhukovSD on 07.06.2016.
 */
public class SimpleFieldActionInvoker extends EndlessFieldActionInvoker<SimpleFieldCell> {
    public SimpleFieldActionInvoker(EndlessField<SimpleFieldCell> field) {
        super(field);
    }

    @Override
    public SimpleFieldAction selectActionByNumber(int number) {
        return SimpleFieldAction.values()[number];
    }
}