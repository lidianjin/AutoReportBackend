package com.zhaoxinms.owner.service.impl;

import java.util.List;
import com.zhaoxinms.common.utils.DateUtils;
import com.zhaoxinms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhaoxinms.base.util.UserProvider;
import com.zhaoxinms.owner.mapper.ReportUserMapper;
import com.zhaoxinms.owner.entity.ReportUser;
import com.zhaoxinms.owner.service.IReportUserService;
import com.zhaoxinms.owner.entity.pagination.ReportUserPagination;

/**
 * 报告用户信息Service业务层处理
 * 
 * @author sxh
 * @date 2022-03-30
 */
@Service
public class ReportUserServiceImpl extends ServiceImpl<ReportUserMapper, ReportUser> implements IReportUserService 
{
    @Autowired
    private ReportUserMapper reportUserMapper;
    @Autowired
    private UserProvider userProvider;
    
    @Override
    public List<ReportUser> getList(ReportUserPagination pagination) {
    	LambdaQueryWrapper<ReportUser> lqw = buildQueryWrapper(pagination);
    	lqw.orderByDesc(ReportUser::getCreateTime);
    	
        Page<ReportUser> page =
            new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<ReportUser> userIPage = this.page(page, lqw);
        return pagination.setData(userIPage.getRecords(), userIPage.getTotal());
    }
    
    @Override
    public ReportUser getInfo(String id) {
        QueryWrapper<ReportUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportUser::getId, id);
        return this.getOne(queryWrapper);
    }
    
    @Override
    public void create(ReportUser entity) {
		validEntityBeforeSave(entity);
        this.save(entity);
    }
    
    @Override
    public boolean update(String id, ReportUser entity) {
      	entity.setId(Long.valueOf(id));
      	validEntityBeforeSave(entity);
        return this.updateById(entity);
    }
    
    @Override
    public void delete(ReportUser entity) {
        if (entity != null) {
        	 this.removeById(entity.getId());
        }
    }

	private LambdaQueryWrapper<ReportUser> buildQueryWrapper(ReportUserPagination pagination) {
        LambdaQueryWrapper<ReportUser> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(pagination.getUserName()), ReportUser::getUserName, pagination.getUserName());
        lqw.eq(StringUtils.isNotBlank(pagination.getSex()), ReportUser::getSex, pagination.getSex());
        lqw.eq(StringUtils.isNotBlank(pagination.getHeight()), ReportUser::getHeight, pagination.getHeight());
        lqw.eq(StringUtils.isNotBlank(pagination.getWeight()), ReportUser::getWeight, pagination.getWeight());
        lqw.eq(StringUtils.isNotBlank(pagination.getReportUrl()), ReportUser::getReportUrl, pagination.getReportUrl());
        return lqw;
    }


    /**
     * 保存前的数据校验
     *
     * @param entity 实体类数据
     */
    private void validEntityBeforeSave(ReportUser entity){
        //TODO 做一些数据校验,如唯一约束
    }
}
