package com.spring.dishcovery.code.domain.mapper;

import com.spring.dishcovery.code.domain.entity.CodeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodeMapper {

    public List<CodeVO> codeList(String codeHead);

}
