package com.ckinan.core;

import java.io.IOException;
import java.util.List;

public interface IDataSource {

    List<Object> findLast() throws IOException;
    void save(List<List<Object>> newValues) throws IOException;

}
