package core;

import java.io.IOException;
import java.util.List;

public interface IReportWriter {

    void write(List<List<Object>> rows) throws IOException;

}
