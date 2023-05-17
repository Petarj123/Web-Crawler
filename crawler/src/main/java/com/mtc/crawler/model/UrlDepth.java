package com.mtc.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlDepth {
    private String url;
    private int depth;
}
