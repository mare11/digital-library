package org.udd.digitallibrary.handler;

import org.udd.digitallibrary.model.IndexUnit;

import java.io.File;

public abstract class DocumentHandler {

    public abstract IndexUnit getIndexUnit(File file);

    public abstract String getText(File file);

}
