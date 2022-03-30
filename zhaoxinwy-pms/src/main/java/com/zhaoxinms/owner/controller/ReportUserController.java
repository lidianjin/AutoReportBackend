package com.zhaoxinms.owner.controller;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.zhaoxinms.common.annotation.RepeatSubmit;
import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.common.core.validate.QueryGroup;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.utils.poi.ExcelUtil;
import com.zhaoxinms.base.ActionResult;
import com.zhaoxinms.base.exception.DataException;
import com.zhaoxinms.base.util.JsonUtil;
import com.zhaoxinms.base.util.UserProvider;
import com.zhaoxinms.base.vo.PageListVO;
import com.zhaoxinms.base.vo.PaginationVO;
import com.zhaoxinms.common.core.domain.entity.SysUser;
import com.zhaoxinms.owner.entity.ReportUser;
import com.zhaoxinms.owner.entity.vo.ReportUserVo;
import com.zhaoxinms.owner.entity.bo.ReportUserBo;
import com.zhaoxinms.owner.entity.pagination.ReportUserPagination;
import com.zhaoxinms.owner.service.IReportUserService;
import com.zhaoxinms.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;
/**
 * 报告用户信息Controller
 * 
 * @author shx
 * @date 2022-03-30
 */
@RestController
@RequestMapping("/owner/report")
public class ReportUserController
{

	@Autowired
    private UserProvider userProvider;

    @Autowired
    private IReportUserService reportUserService;

    /**
     * 查询报告用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('owner:report:list')")
    @GetMapping("/list")
    public ActionResult list(ReportUserPagination reportUserPagination) {
        List<ReportUser> list = reportUserService.getList(reportUserPagination);
        List<ReportUserVo> listVO = JsonUtil.getJsonToList(list, ReportUserVo.class);
        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        PaginationVO page = JsonUtil.getJsonToBean(reportUserPagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }

    /**
     * 获取报告用户信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('owner:report:query')")
    @GetMapping(value = "/{id}")
 	public ActionResult<ReportUserVo> info(@PathVariable("id") String id) {
        ReportUser entity = reportUserService.getInfo(id);
        ReportUserVo vo = JsonUtil.getJsonToBean(entity, ReportUserVo.class);
        return ActionResult.success(vo);
    }

    /**
     * 新增报告用户信息
     */
    @PreAuthorize("@ss.hasPermi('owner:report:add')")
    @Log(title = "报告用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional
    public ActionResult create(@Validated(AddGroup.class) @RequestBody ReportUserBo bo) throws DataException {
        SysUser userInfo = userProvider.get();
        ReportUser entity = JsonUtil.getJsonToBean(bo, ReportUser.class);
        reportUserService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改报告用户信息
     */
    @PreAuthorize("@ss.hasPermi('owner:report:edit')")
    @Log(title = "报告用户信息", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    @Transactional
    public ActionResult update(@PathVariable("id") String id, @Validated(EditGroup.class) @RequestBody ReportUserBo bo)
        throws DataException {
        ReportUser entity = JsonUtil.getJsonToBean(bo, ReportUser.class);
        reportUserService.update(id, entity);
        return ActionResult.success("更新成功");
    }

    /**
     * 删除报告用户信息
     */
    @PreAuthorize("@ss.hasPermi('owner:report:remove')")
    @Log(title = "报告用户信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{id}")
	@Transactional
    public ActionResult delete(@PathVariable("id") String id) {
        ReportUser entity = reportUserService.getInfo(id);
        if (entity != null) {
            reportUserService.delete(entity);
        }
        return ActionResult.success("删除成功");
    }
}
