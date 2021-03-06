package com.zhukovsd.simplefield;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.endlessfield.field.EndlessFieldCellView;
import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.serverapp.endpoints.websocket.ActionServerMessage;
import com.zhukovsd.serverapp.endpoints.websocket.InitServerMessage;
import com.zhukovsd.serverapp.endpoints.websocket.ServerMessage;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ZhukovSD on 03.06.2016.
 */
public class SimpleFieldSerializer implements EndlessFieldSerializer {
    @Override
    public void fieldResponseDataToJSON(FieldResponseData data, Appendable out) throws IOException {
        if (data != null) {
            StringBuilder sb = new StringBuilder(50000);
            sb.append("{\"responseCode\":0,\"chunks\":[");
            int c = 0;

            String chunkSeparator = "";
            for (FieldResponseData.ChunkData chunk : data.chunks) {
                sb.append(chunkSeparator);

                // chunk begin
                sb.append('{');

                // origin begin
                sb.append("\"origin\":");
                sb.append("{\"row\":");
                sb.append(chunk.origin.row);
                sb.append(",\"column\":");
                sb.append(chunk.origin.column);
                // origin end
                sb.append('}');

                // cells begin
                sb.append(",\"cells\":[");

                String cellSeparator = "";
                for (EndlessFieldCellView cell : chunk.cells) {
                    sb.append(cellSeparator);

                    // cell begin
                    sb.append('{');
                    SimpleFieldCell casted = ((SimpleFieldCell) cell);

                    if (casted.isChecked()) {
                        sb.append("\"c\":true");
                    }

                    // cell end
                    sb.append('}');

                    cellSeparator = ",";
                }

                // cells end
                sb.append("]");

                // chunk end
                sb.append('}');

                chunkSeparator = ",";
            }
            sb.append("]}");

            out.append(sb);
        } else
            out.append("null");
    }

    @Override
    public String actionEndpointMessageToJSON(ServerMessage message) {
        String content = "";

        if (message instanceof InitServerMessage) {
            InitServerMessage casted = ((InitServerMessage) message);

            content = ',' + String.format(
                    "\"wsSessionId\":\"%s\",\"userId\":\"%s\",\"chunkSize\":{\"rowCount\":%d,\"columnCount\":%d},\"initialChunkId\":%d," +
                            "\"chunkIdFactor\":%d",
                    casted.wsSessionId, casted.userId, casted.chunkSize.rowCount, casted.chunkSize.columnCount,
                    casted.initialChunkId, casted.chunkIdFactor
            );
        } else {
            if (message instanceof ActionServerMessage) {
                // {"cells":{"0, 41":{"isChecked":true}},"type":1}

                ActionServerMessage casted = ((ActionServerMessage) message);

                StringBuilder sb = new StringBuilder();
                sb.append(',');

                // cells start
                sb.append("\"cells\":{");

                String cellSeparator = "";
                for (Map.Entry<CellPosition, ? extends EndlessFieldCellView> entry : casted.cells.entrySet()) {
                    CellPosition position = entry.getKey();

                    sb.append(cellSeparator);

                    // cell position
                    sb.append('"');
                    sb.append(position.row);
                    sb.append(',');
                    sb.append(position.column);
                    sb.append('"');

                    // cell
                    sb.append(":{");
                    SimpleFieldCell cell = ((SimpleFieldCell) entry.getValue());
                    if (cell.isChecked()) {
                        sb.append("\"c\":true");
                    }

                    sb.append('}');

                    cellSeparator = ",";
                }

                // cells end
                sb.append('}');

                // origin
                sb.append(",\"origin\":{\"row\":");
                sb.append(casted.origin.row);
                sb.append(",\"column\":");
                sb.append(casted.origin.column);
                sb.append("}");

                // player
                sb.append(",\"player\":{\"id\":\"");
                sb.append(casted.player.id);
                sb.append("\",\"name\":\"");
                sb.append(casted.player.name);
                sb.append("\"}");

                content = sb.toString();
            }
        }

        return String.format(
                "{\"type\":%d%s}", message.type, content
        );
    }
}
