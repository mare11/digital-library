package org.udd.digitallibrary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.udd.digitallibrary.model.QueryData;
import org.udd.digitallibrary.model.ResultData;
import org.udd.digitallibrary.model.UploadModel;
import org.udd.digitallibrary.service.IndexService;

import java.util.List;

@RestController
public class IndexController {

    private final IndexService indexService;

    @Autowired
    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping("/index")
    public ResponseEntity<String> multiUploadFileModel(@ModelAttribute UploadModel model) {
        indexService.index(model);
        return new ResponseEntity<>("Successfully uploaded!", HttpStatus.OK);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResultData>> search(@RequestBody List<QueryData> queryData) {
        List<ResultData> results = indexService.search(queryData);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
