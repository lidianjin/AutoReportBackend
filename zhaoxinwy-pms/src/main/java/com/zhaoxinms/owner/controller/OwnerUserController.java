package com.zhaoxinms.owner.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import com.zhaoxinms.owner.entity.OwnerUser;
import com.zhaoxinms.owner.entity.vo.OwnerUserVo;
import com.zhaoxinms.owner.entity.bo.OwnerUserBo;
import com.zhaoxinms.owner.entity.pagination.OwnerUserPagination;
import com.zhaoxinms.owner.service.IOwnerUserService;
import com.zhaoxinms.payment.entity.PaymentBillEntity;
import com.zhaoxinms.payment.entity.PaymentContractEntity;
import com.zhaoxinms.payment.model.paymentbill.PaymentBillListVO;
import com.zhaoxinms.payment.model.paymentcontract.PaymentContractListVO;
import com.zhaoxinms.payment.service.PaymentBillService;
import com.zhaoxinms.payment.service.PaymentContractService;
import com.zhaoxinms.statistics.model.PaymentFeeStatisticsModel;
import com.zhaoxinms.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;
/**
 * ????????????Controller
 * 
 * @author cycberform
 * @date 2022-02-23
 */
@RestController
@RequestMapping("/owner/ownerUser")
public class OwnerUserController
{
	@Autowired
    private UserProvider userProvider;
    @Autowired
    private IOwnerUserService ownerUserService;
    @Autowired
    private PaymentContractService paymentContractService;
    @Autowired
    private PaymentBillService paymentBillService;

    /**
     * ????????????????????????
     */
    @PreAuthorize("@ss.hasPermi('owner:ownerUser:list')")
    @GetMapping("/list")
    public ActionResult list(OwnerUserPagination ownerUserPagination) {
        List<OwnerUser> list = ownerUserService.getList(ownerUserPagination);
        List<OwnerUserVo> listVO = JsonUtil.getJsonToList(list, OwnerUserVo.class);
        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        PaginationVO page = JsonUtil.getJsonToBean(ownerUserPagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }
    
    /**
     * ??????????????????????????????
     */
    @PreAuthorize("@ss.hasPermi('owner:ownerUser:query')")
    @GetMapping(value = "{id}")
    public ActionResult<OwnerUserVo> info(@PathVariable("id") Long id) {
        OwnerUser entity = ownerUserService.getInfo(id);
        OwnerUserVo vo = JsonUtil.getJsonToBean(entity, OwnerUserVo.class);
        return ActionResult.success(vo);
    }
    

    /**
     * ??????????????????????????????
     */
    @PreAuthorize("@ss.hasPermi('owner:ownerUser:query')")
    @GetMapping(value = "detail/{id}")
 	public ActionResult<OwnerUserVo> detail(@PathVariable("id") Long id) {
        OwnerUser entity = ownerUserService.getInfo(id);
        OwnerUserVo vo = JsonUtil.getJsonToBean(entity, OwnerUserVo.class);
        
        //????????????????????????????????????
        List<PaymentContractEntity> currentContracts =  paymentContractService.getByOwnerId(String.valueOf(id));
        List<PaymentContractListVO> listCurrent = JsonUtil.getJsonToList(currentContracts, PaymentContractListVO.class);
        List<PaymentContractEntity> historyContracts = paymentContractService.getDisabledByOwnerId(String.valueOf(id));
        List<PaymentContractListVO> listHostory = JsonUtil.getJsonToList(historyContracts, PaymentContractListVO.class);
       
        //??????????????????????????????????????????
        List<String> resources = currentContracts.stream().map(PaymentContractEntity::getResourceName).collect(Collectors.toList());
        List<PaymentBillEntity> bills = paymentBillService.getUnpaiedListByResources(resources);
        List<PaymentBillListVO> billsVo = JsonUtil.getJsonToList(bills, PaymentBillListVO.class);
        
        //??????????????????????????????????????????
        List<String> contracts = currentContracts.stream().map(PaymentContractEntity::getId).collect(Collectors.toList());
        List<PaymentBillEntity> historyBbills = paymentBillService.getPaiedListByContracts(contracts);
        List<PaymentBillListVO> historyBillsVo = JsonUtil.getJsonToList(historyBbills, PaymentBillListVO.class);
        
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("currentContracts", listCurrent);
        result.put("historyContracts", listHostory);
        result.put("unpaiedBills", billsVo);
        result.put("paiedBills", historyBillsVo);
        return ActionResult.success(result);
    }

    /**
     * ??????????????????
     */
    @PreAuthorize("@ss.hasPermi('owner:ownerUser:add')")
    @Log(title = "????????????", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional
    public ActionResult create(@Validated(AddGroup.class) @RequestBody OwnerUserBo bo) throws DataException {
        SysUser userInfo = userProvider.get();
        OwnerUser entity = JsonUtil.getJsonToBean(bo, OwnerUser.class);
        ownerUserService.create(entity);
        return ActionResult.success("????????????");
    }

    /**
     * ??????????????????
     */
    @PreAuthorize("@ss.hasPermi('owner:ownerUser:edit')")
    @Log(title = "????????????", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    @Transactional
    public ActionResult update(@PathVariable("id") Long id, @Validated(EditGroup.class) @RequestBody OwnerUserBo bo)
        throws DataException {
        OwnerUser entity = JsonUtil.getJsonToBean(bo, OwnerUser.class);
        ownerUserService.update(id, entity);
        return ActionResult.success("????????????");
    }

    /**
     * ??????????????????
     */
    @PreAuthorize("@ss.hasPermi('owner:ownerUser:remove')")
    @Log(title = "????????????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{id}")
	@Transactional
    public ActionResult delete(@PathVariable("id") Long id) {
        OwnerUser entity = ownerUserService.getInfo(id);
        if (entity != null) {
            ownerUserService.delete(entity);
        }
        return ActionResult.success("????????????");
    }
}
