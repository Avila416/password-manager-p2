package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
import lombok.Data;

@Data
@Slf4j
public class SearchFilterDTO {
    private String keyword;
    private String category;
}

