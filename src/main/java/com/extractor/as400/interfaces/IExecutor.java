package com.extractor.as400.interfaces;

import com.extractor.as400.exceptions.ExecutorAS400Exception;

public interface IExecutor {
    void execute() throws ExecutorAS400Exception;
}
