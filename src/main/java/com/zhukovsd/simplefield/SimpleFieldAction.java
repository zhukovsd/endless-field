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
import com.zhukovsd.endlessfield.ChunkIdGenerator;
import com.zhukovsd.endlessfield.EndlessFieldArea;
import com.zhukovsd.endlessfield.field.EndlessField;
import com.zhukovsd.endlessfield.field.EndlessFieldAction;
import com.zhukovsd.endlessfield.field.EndlessFieldActionBehavior;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;

import java.util.*;

/**
 * Created by ZhukovSD on 07.06.2016.
 */
enum SimpleFieldAction implements EndlessFieldAction {
    TOGGLE_CELL(new EndlessFieldActionBehavior() {
        @Override
        public Collection<Integer> getChunkIds(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
            return Collections.singleton(ChunkIdGenerator.chunkIdByPosition(field.chunkSize, position));
        }

        @Override
        public LinkedHashMap<CellPosition, ? extends EndlessFieldCell> perform(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
            LinkedHashMap<CellPosition, EndlessFieldCell> result = new LinkedHashMap<>(1);

            SimpleFieldCell cell = ((SimpleFieldCell) field.getCell(position));
            synchronized (cell) {
                cell.setChecked(!cell.isChecked());
            }

            result.put(position, cell);
            return result;
        }
    }),

    TOGGLE_SQUARE_REGION(new EndlessFieldActionBehavior() {
        @Override
        public Collection<Integer> getChunkIds(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
            return ChunkIdGenerator.chunkIdsByArea(field.chunkSize, new EndlessFieldArea(field, position, 1, 1).expandFromCenter(1));
        }

        @Override
        public LinkedHashMap<CellPosition, ? extends EndlessFieldCell> perform(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
            EndlessFieldArea area = new EndlessFieldArea(field, position, 1, 1).expandFromCenter(1);
            LinkedHashMap<CellPosition, ? extends EndlessFieldCell> entries = field.getEntries(area);

            boolean value = !((SimpleFieldCell) entries.get(position)).isChecked();

            for (EndlessFieldCell cell : entries.values()) {
                SimpleFieldCell casted = ((SimpleFieldCell) cell);

                synchronized (casted) {
                    casted.setChecked(value);
                }
            }

            return entries;
        }
    }),

    TOGGLE_ROUND_REGION(new EndlessFieldActionBehavior() {
        private int radius = 6;

        @Override
        public Collection<Integer> getChunkIds(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
            return ChunkIdGenerator.chunkIdsByArea(field.chunkSize, new EndlessFieldArea(field, position, 1, 1).expandFromCenter(radius));
        }

        @Override
        public LinkedHashMap<CellPosition, ? extends EndlessFieldCell> perform(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
            EndlessFieldArea area = new EndlessFieldArea(field, position, 1, 1).expandFromCenter(radius);
            LinkedHashMap<CellPosition, ? extends EndlessFieldCell> entries = field.getEntries(area);
            LinkedHashMap<CellPosition, EndlessFieldCell> result = new LinkedHashMap<>();

            boolean value = !((SimpleFieldCell) entries.get(position)).isChecked();

            for (Map.Entry<CellPosition, ? extends EndlessFieldCell> entry : entries.entrySet()) {
                CellPosition pos = entry.getKey();

                if (Math.round(Math.sqrt(Math.pow(pos.row - position.row, 2) + Math.pow(pos.column - position.column, 2))) <= radius) {
                    ((SimpleFieldCell) entry.getValue()).setChecked(value);
                    result.put(pos, entry.getValue());
                }
            }

            return result;
        }
    });

    private final EndlessFieldActionBehavior behavior;

    SimpleFieldAction(EndlessFieldActionBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public Collection<Integer> getChunkIds(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
        return behavior.getChunkIds(field, position);
    }

    @Override
    public LinkedHashMap<CellPosition, ? extends EndlessFieldCell> perform(EndlessField<? extends EndlessFieldCell> field, CellPosition position) {
        return behavior.perform(field, position);
    }
}