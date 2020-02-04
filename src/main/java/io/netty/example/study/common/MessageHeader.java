package io.netty.example.study.common;

import lombok.Data;

/**
 * @author kuangjunlin
 */
@Data
public class MessageHeader {
    private int version = 1;
    private int opCode;
    private long streamId;
}
