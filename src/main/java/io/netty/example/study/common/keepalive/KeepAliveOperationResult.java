package io.netty.example.study.common.keepalive;

import io.netty.example.study.common.OperationResult;
import lombok.Data;

@Data
public class KeepAliveOperationResult extends OperationResult {
    private final long time;
}
