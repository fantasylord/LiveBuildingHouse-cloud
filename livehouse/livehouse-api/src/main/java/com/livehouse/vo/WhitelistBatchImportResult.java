package com.livehouse.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WhitelistBatchImportResult {

    private int total;

    private int success;

    private int duplicate;

    private int invalid;

    private List<String> errors = new ArrayList<>();
}
