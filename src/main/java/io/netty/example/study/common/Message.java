package io.netty.example.study.common;

import io.netty.buffer.ByteBuf;
import io.netty.example.study.util.JsonUtil;
import lombok.Data;

import java.nio.charset.Charset;

/**
 * @author kuangjunlin
 */
@Data
public abstract class Message <T extends MessageBody> {
    private MessageHeader messageHeader;
    private T messageBody;

    public T getMessageBody() { return messageBody; }

    public void encode (ByteBuf byteBuf) {
        byteBuf.writeInt(messageHeader.getVersion());
        byteBuf.writeLong(messageHeader.getStreamId());
        byteBuf.writeInt(messageHeader.getOpCode());
        byteBuf.writeBytes(JsonUtil.toJson(messageBody).getBytes());
    }

    /**
     * TODO
     * @param opcode
     * @return
     */
    public abstract Class<T> getMessageBodyDecodeClass (int opcode);

    public void decode (ByteBuf msg) {
        int version = msg.readInt();
        long streamId = msg.readLong();
        int opCode = msg.readInt();

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setOpCode(opCode);
        messageHeader.setStreamId(streamId);
        messageHeader.setVersion(version);

        this.messageHeader = messageHeader;

        Class<T> bodyClazz = getMessageBodyDecodeClass(opCode);
        T body = JsonUtil.fromJson(msg.toString(Charset.forName("UTF-8")), bodyClazz);
        this.messageBody = body;
    }
}
