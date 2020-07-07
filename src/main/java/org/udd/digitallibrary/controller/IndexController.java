package org.udd.digitallibrary.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udd.digitallibrary.model.QueryData;
import org.udd.digitallibrary.model.ResultUnit;
import org.udd.digitallibrary.model.ResultUser;
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

    @PostMapping(value = "/search/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResultUnit>> querySearch(@RequestBody List<QueryData> queryData) {
        List<ResultUnit> results = indexService.querySearch(queryData);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping(value = "/search/more-like-this/{filename}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResultUnit>> moreLikeThisSearch(@PathVariable String filename) {
        List<ResultUnit> results = indexService.moreLikeThisSearch(filename);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping(value = "/search/geo-distance/{filename}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResultUser>> geoDistanceSearch(@PathVariable String filename) {
        List<ResultUser> results = indexService.geoDistanceSearch(filename);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @GetMapping(value = "/download/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource file = indexService.getFile(filename);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }
}
