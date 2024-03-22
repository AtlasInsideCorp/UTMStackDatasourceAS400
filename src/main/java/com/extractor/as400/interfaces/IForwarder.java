package com.extractor.as400.interfaces;

import java.util.List;

/**
 * @author Freddy R. Laffita Almaguer
 * Interface to define the forwarding logs behavior
 * All classes that implement it can override the methods to send logs
 * to a specific destination
 */
public interface IForwarder {
    boolean forwardLogs(List<String> messages);
}
