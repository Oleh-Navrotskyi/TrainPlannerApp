package com.oleje.trainplannerapp.domain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Interface representing object-oriented replacement for C++ stream operators << and >>.
 */
public interface StreamableTicket {
    void writeToStream(BufferedWriter writer) throws IOException;
    void readFromStream(BufferedReader reader) throws IOException, InvalidTicketDataException;
}
