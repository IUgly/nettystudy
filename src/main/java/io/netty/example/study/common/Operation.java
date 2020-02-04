package io.netty.example.study.common;

/**
 * @author kuangjunlin
 */
public abstract class Operation extends MessageBody{
    public abstract OperationResult execute();
}
