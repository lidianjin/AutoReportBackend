package com.zhaoxinms.base.service.impl;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.base.entity.DictionaryDataEntity;
import com.zhaoxinms.base.mapper.DictionaryDataMapper;
import com.zhaoxinms.base.service.DictionaryDataService;
import com.zhaoxinms.base.util.DateUtil;
import com.zhaoxinms.base.util.PinYinUtil;
import com.zhaoxinms.base.util.RandomUtil;
import com.zhaoxinms.base.util.StringUtil;
import com.zhaoxinms.base.util.UserProvider;

@Service
public class DictionaryDataServiceImpl extends ServiceImpl<DictionaryDataMapper, DictionaryDataEntity> implements DictionaryDataService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<DictionaryDataEntity> getList() {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(DictionaryDataEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<DictionaryDataEntity> getList(String dictionaryTypeId) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getDictionaryTypeId, dictionaryTypeId);
        queryWrapper.lambda().orderByAsc(DictionaryDataEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public DictionaryDataEntity getInfo(String id) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String dictionaryTypeId, String fullName, String id) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getFullName, fullName).eq(DictionaryDataEntity::getDictionaryTypeId, dictionaryTypeId);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DictionaryDataEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String dictionaryTypeId, String enCode, String id) {
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryDataEntity::getEnCode, enCode).eq(DictionaryDataEntity::getDictionaryTypeId, dictionaryTypeId);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DictionaryDataEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void delete(DictionaryDataEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public void create(DictionaryDataEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setSimpleSpelling(PinYinUtil.getFirstSpell(entity.getFullName()).toUpperCase());
        entity.setCreatorUserId(""+userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, DictionaryDataEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(""+userProvider.get().getUserId());
       return this.updateById(entity);
    }

    @Override
    @Transactional
    public boolean first(String id) {
        boolean isOk = false;
        //???????????????????????????????????????
        DictionaryDataEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //?????????????????????
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DictionaryDataEntity::getDictionaryTypeId, upEntity.getDictionaryTypeId())
                .eq(DictionaryDataEntity::getParentId, upEntity.getParentId())
                .lt(DictionaryDataEntity::getSortCode, upSortCode)
                .orderByDesc(DictionaryDataEntity::getSortCode);
        List<DictionaryDataEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //?????????????????????sort???
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            updateById(downEntity.get(0));
            updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    @Transactional
    public boolean next(String id) {
        boolean isOk = false;
        //???????????????????????????????????????
        DictionaryDataEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //?????????????????????
        QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DictionaryDataEntity::getDictionaryTypeId, downEntity.getDictionaryTypeId())
                .eq(DictionaryDataEntity::getParentId, downEntity.getParentId())
                .gt(DictionaryDataEntity::getSortCode, upSortCode)
                .orderByAsc(DictionaryDataEntity::getSortCode);
        List<DictionaryDataEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //?????????????????????sort???
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            upEntity.get(0).setSortCode(temp);
            updateById(upEntity.get(0));
            updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public List<DictionaryDataEntity> getDictionName(List<String> id) {
        List<DictionaryDataEntity> dictionList = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<DictionaryDataEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(DictionaryDataEntity::getEnCode, id);
            dictionList = this.list(queryWrapper);
        }
        return dictionList;
    }
}
