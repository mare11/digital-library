package org.udd.digitallibrary.service;

import org.springframework.core.io.Resource;
import org.udd.digitallibrary.model.QueryData;
import org.udd.digitallibrary.model.ResultUnit;
import org.udd.digitallibrary.model.ResultUser;
import org.udd.digitallibrary.model.UploadModel;

import java.util.List;

public interface IndexService {

    void index(UploadModel model);

    List<ResultUnit> querySearch(List<QueryData> queryData);

    List<ResultUnit> moreLikeThisSearch(String filename);

    List<ResultUser> geoDistanceSearch(String filename);

    Resource getFile(String filename);
}
