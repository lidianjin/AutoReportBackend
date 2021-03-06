package com.zhaoxinms.base.service.impl;


import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoxinms.base.entity.DictionaryTypeEntity;
import com.zhaoxinms.base.mapper.DictionaryTypeMapper;
import com.zhaoxinms.base.service.DictionaryTypeService;
import com.zhaoxinms.base.util.RandomUtil;
import com.zhaoxinms.base.util.StringUtil;
import com.zhaoxinms.base.util.UserProvider;

@Service
public class DictionaryTypeServiceImpl extends ServiceImpl<DictionaryTypeMapper, DictionaryTypeEntity> implements DictionaryTypeService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<DictionaryTypeEntity> getList() {
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(DictionaryTypeEntity::getSortCode).orderByDesc(DictionaryTypeEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public DictionaryTypeEntity getInfoByEnCode(String enCode) {
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryTypeEntity::getEnCode, enCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public DictionaryTypeEntity getInfo(String id) {
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryTypeEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryTypeEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DictionaryTypeEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DictionaryTypeEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DictionaryTypeEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void create(DictionaryTypeEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(""+userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean update(String id, DictionaryTypeEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(""+userProvider.get().getUserId());
         return this.updateById(entity);
    }

    @Override
    public void delete(DictionaryTypeEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    @Transactional
    public boolean first(String id) {
        boolean isOk = false;
        //???????????????????????????????????????
        DictionaryTypeEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //?????????????????????
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(DictionaryTypeEntity::getSortCode, upSortCode)
                .eq(DictionaryTypeEntity::getParentId, upEntity.getParentId())
                .orderByDesc(DictionaryTypeEntity::getSortCode);
        List<DictionaryTypeEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //?????????????????????sort???
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            this.updateById(downEntity.get(0));
            this.updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    @Transactional
    public boolean next(String id) {
        boolean isOk = false;
        //???????????????????????????????????????
        DictionaryTypeEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //?????????????????????
        QueryWrapper<DictionaryTypeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(DictionaryTypeEntity::getSortCode, upSortCode)
                .eq(DictionaryTypeEntity::getParentId, downEntity.getParentId())
                .orderByAsc(DictionaryTypeEntity::getSortCode);
        List<DictionaryTypeEntity> upEntity = this.list(queryWrapper);
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
}
