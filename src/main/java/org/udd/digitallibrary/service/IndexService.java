package org.udd.digitallibrary.service;

import org.udd.digitallibrary.model.QueryData;
import org.udd.digitallibrary.model.ResultData;
import org.udd.digitallibrary.model.UploadModel;

import java.util.List;

public interface IndexService {

    void index(UploadModel model);

    List<ResultData> search(List<QueryData> queryData);

}
