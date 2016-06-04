package com.zhukovsd.simplefield;

import com.sun.org.apache.xml.internal.utils.StringBufferPool;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.serverapp.endpoints.http.FieldResponseData;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpointInitMessage;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpointMessage;
import com.zhukovsd.serverapp.serialization.EndlessFieldSerializer;

import java.io.IOException;

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

                String cellsSeparator = "";
                for (EndlessFieldCell cell : chunk.cells) {
                    sb.append(cellsSeparator);

                    // cell begin
                    sb.append('{');
                    SimpleFieldCell casted = ((SimpleFieldCell) cell);

                    if (casted.isChecked()) {
                        sb.append("\"c\":");
                        sb.append(String.valueOf(((SimpleFieldCell) cell).isChecked()));
        //                                sb.append(',');
                    }
        //                            sb.append("\"s\":\"");
        //                            sb.append(casted.s);
        //                            sb.append("\"");

                    // cell end
                    sb.append('}');

                    cellsSeparator = ",";
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
    public String actionEndpointMessageToJSON(ActionEndpointMessage message) {
        String content = "";

        if (message instanceof ActionEndpointInitMessage) {
            ActionEndpointInitMessage casted = ((ActionEndpointInitMessage) message);

            content = ',' + String.format(
                    "\"wsSessionId\":\"%s\",\"chunkSize\":{\"rowCount\":%d,\"columnCount\":%d},\"initialChunkId\":%d," +
                            "\"chunkIdFactor\":%d",
                    casted.wsSessionId, casted.chunkSize.rowCount, casted.chunkSize.columnCount,
                    casted.initialChunkId, casted.chunkIdFactor
            );
        }

        return String.format(
                "{\"type\":%d%s}", message.type, content
        );
    }
}
