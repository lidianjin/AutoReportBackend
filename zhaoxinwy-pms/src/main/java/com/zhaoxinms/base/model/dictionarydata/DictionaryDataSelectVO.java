package com.zhaoxinms.base.model.dictionarydata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryDataSelectVO {
    private String id;
    private String parentId;
    private Boolean hasChildren;
    private List<DictionaryDataSelectVO> children;
    private String fullName;
    private String icon;
    private String dictionaryTypeId;
}
