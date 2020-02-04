package io.netty.example.study.common.keepalive;

import io.netty.example.study.common.Operation;
import io.netty.example.study.common.OperationResult;
import lombok.Data;

/**
 * @author kuangjunlin
 */
@Data
public class KeepAliveOperation extends Operation {
    private long time;

    public KeepAliveOperation() {
        this.time = System.nanoTime();
    }



    @Override
    public OperationResult execute() {
        KeepAliveOperationResult orderResponse = new KeepAliveOperationResult(time);
        return orderResponse;
    }
}
