package com.spring.dishcovery.code.domain.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeVO {

    private String codeHead;
    private String code;
    private String codeName;
    private String parentCode;
    private int codeSort;

}
