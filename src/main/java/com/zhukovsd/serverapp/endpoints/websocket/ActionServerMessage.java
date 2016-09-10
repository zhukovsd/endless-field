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

package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldCellView;

import java.util.HashMap;

/**
 * Created by ZhukovSD on 06.06.2016.
 */
public class ActionServerMessage extends ServerMessage {
    public static class User {
        public final String id, name;

        public User(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public final HashMap<CellPosition, EndlessFieldCellView> cells;
    public final CellPosition origin;
    public final User player;

    ActionServerMessage(HashMap<CellPosition, EndlessFieldCellView> cells, CellPosition origin, String userId, String username) {
        super(ServerMessageType.ACTION_MESSAGE);
        this.cells = cells;
        this.origin = origin;
        this.player = new User(userId, username);
    }
}
