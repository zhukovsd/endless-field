package com.zhukovsd.serverapp.endpoints.websocket;

import com.zhukovsd.endlessfield.field.ChunkIdGenerator;
import com.zhukovsd.endlessfield.field.ChunkSize;

/**
 * Created by ZhukovSD on 05.05.2016.
 */
public class ActionEndpointInitMessage extends ActionEndpointMessage {
    final String wsSessionId;
    final ChunkSize chunkSize;
    final Integer initialChunkId;
    final int chunkIdFactor = ChunkIdGenerator.idFactor;

    public ActionEndpointInitMessage(String wsSessionId, ChunkSize chunkSize, Integer initialChunkId) {
        super(ActionEndpointMessageType.INIT_MESSAGE);
        this.wsSessionId = wsSessionId;
        this.chunkSize = chunkSize;
        this.initialChunkId = initialChunkId;
    }
}
