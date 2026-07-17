package com.spring.dishcovery.service;

import com.spring.dishcovery.entity.CodeVO;
import com.spring.dishcovery.mapper.CodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeMapper codeMapper;

    public List<CodeVO> codeList(String codeHead) {
        return codeMapper.codeList(codeHead);
    }


}
