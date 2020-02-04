package io.netty.example.study.common.auth;

import io.netty.example.study.common.Operation;
import io.netty.example.study.common.OperationResult;
import lombok.Data;
import lombok.extern.java.Log;

/**
 * @author kuangjunlin
 */
@Data
@Log
public class AuthOperation extends Operation {
    private final String username;
    private final String password;
    @Override
    public OperationResult execute() {
        if ("admin".equals(this.username)) {
            AuthOperationResult orderResponse = new AuthOperationResult(true);
            return orderResponse;
        }
        return new AuthOperationResult(false);
    }
}
