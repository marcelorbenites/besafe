package com.panicbutton.common;

import java.util.List;

public interface PanicReportProvider {

    long insert(PanicReport panicReport);

    PanicReport get(long id);

    List<PanicReport> getAll();

    boolean delete(long id);

}
