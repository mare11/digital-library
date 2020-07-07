package org.udd.digitallibrary.handler;

import org.udd.digitallibrary.model.IndexUnit;

import java.io.File;

public interface DocumentHandler {

    IndexUnit getIndexUnit(File file);

    String getText(File file);

}
