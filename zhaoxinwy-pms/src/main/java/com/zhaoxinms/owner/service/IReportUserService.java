package com.zhaoxinms.owner.service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhaoxinms.owner.entity.ReportUser;
import com.zhaoxinms.owner.entity.pagination.ReportUserPagination;

/**
 * 报告用户信息Service接口
 * 
 * @author sxh
 * @date 2022-03-30
 */
public interface IReportUserService extends IService<ReportUser>
{

    List<ReportUser> getList(ReportUserPagination pagination);

    ReportUser getInfo(String id);

    void delete(ReportUser entity);

    void create(ReportUser entity);

    boolean update(String id, ReportUser entity);
}
