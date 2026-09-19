package com.sunyongjie.blog.service.impl;

import com.sunyongjie.blog.entity.Tag;
import com.sunyongjie.blog.mapper.TagMapper;
import com.sunyongjie.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签业务实现。标签目前只读，随文章一起维护。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<Tag> list() {
        return tagMapper.selectAll();
    }
}
