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

/**
 * Created by ZhukovSD on 05.05.2016.
 */
public enum ServerMessageType {
    INIT_MESSAGE (0),
    ACTION_MESSAGE (1);

    public final int value;

    ServerMessageType(int value) {
        this.value = value;
    }
}
