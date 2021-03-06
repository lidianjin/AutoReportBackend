package com.zhaoxinms.payment.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zhaoxinms.base.ActionResult;
import com.zhaoxinms.base.service.FileService;
import com.zhaoxinms.base.util.ConfigValueUtil;
import com.zhaoxinms.base.util.ExcelUtil;
import com.zhaoxinms.base.util.FileTypeEnum;
import com.zhaoxinms.base.util.FileUtil;
import com.zhaoxinms.base.util.RandomUtil;
import com.zhaoxinms.base.util.ServletUtil;
import com.zhaoxinms.base.util.UpUtil;
import com.zhaoxinms.base.vo.DownloadVO;
import com.zhaoxinms.baseconfig.entity.ConfigFeeItemEntity;
import com.zhaoxinms.baseconfig.service.ConfigFeeItemService;
import com.zhaoxinms.baseconfig.service.ConfigHouseService;
import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.payment.entity.PaymentMeterEntity;
import com.zhaoxinms.payment.entity.PaymentMeterIndexEntity;
import com.zhaoxinms.payment.model.paymentcontract.PaymentContractFeeListVO;
import com.zhaoxinms.payment.model.paymentmeter.PaymentMeterImport;
import com.zhaoxinms.payment.model.paymentmeter.PaymentMeterPagination;
import com.zhaoxinms.payment.model.paymentmeterindex.PaymentMeterIndexPagination;
import com.zhaoxinms.payment.service.PaymentContractFeeService;
import com.zhaoxinms.payment.service.PaymentMeterIndexService;
import com.zhaoxinms.payment.service.PaymentMeterService;
import com.zhaoxinms.util.DateUtils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "??????????????????", description = "")
@RequestMapping("/payment/PaymentMeterImport")
public class PaymentMeterImportController {
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private FileService fileService;
    @Autowired
    private PaymentMeterService paymentMeterService;
    @Autowired
    private PaymentMeterIndexService paymentMeterIndexService;
    @Autowired
    private ConfigFeeItemService configFeeItemService;
    @Autowired
    private PaymentContractFeeService paymentContractFeeService;

    @GetMapping("/Template")
    public void info(@RequestParam("feeId") String feeId) {
        ConfigFeeItemEntity fee = configFeeItemService.getById(feeId);
        String filePath = configValueUtil.getTemplateFilePath() + "meter_import_template.xlsx";
        // 1.??????excel??????
        TemplateExportParams params = new TemplateExportParams(filePath);

        // 2.???????????????????????????
        List<PaymentMeterIndexEntity> indexList =
            paymentMeterIndexService.getTypeList(new PaymentMeterIndexPagination(), "1");

        // 3.?????????????????????????????????
        List<PaymentContractFeeListVO> list = paymentContractFeeService.getByFeeId(feeId);

        // 3.???????????????????????????
        List<PaymentMeterImport> meters = new ArrayList<PaymentMeterImport>();
        for (PaymentContractFeeListVO contractFee : list) {
            PaymentMeterImport meter = new PaymentMeterImport();
            meter.setBlock(contractFee.getBlockCode());
            meter.setCode(contractFee.getResourceCode());
            meter.setLastIndex("0");
            meter.setLastIndexDate("");
            for (PaymentMeterIndexEntity m : indexList) {
                if (m.getResourceName().equals(contractFee.getResourceName())) {
                    meter.setLastIndex(m.getCurrentIndex());
                    meter.setLastIndexDate(DateUtils.formatDate(m.getCurrentIndexDate(), "yyyy-MM-dd"));
                }
            }
            meter.setCurrentIndexDate("");
            meter.setCurrentIndex("");
            meter.setFeeItemName(fee.getName());
            meter.setMultiple("1");
            meter.setLoss("0");
            meters.add(meter);
        }

        Map<String, Object> map = new HashMap<String, Object>(100);
        map.put("indexList", meters);

        // 2.??????excel??????
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);

        // 3.????????????
        String fileName = "????????????????????????.xlsx";
        try {
            HttpServletResponse response = ServletUtil.getResponse();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("download-filename",  URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/Download")
    public void download() {
        String filePath = configValueUtil.getTemplateFilePath() + "meter_import_template.xlsx";
        // 1.??????excel??????
        TemplateExportParams params = new TemplateExportParams(filePath);

        List<PaymentMeterEntity> list = paymentMeterService.getTypeList(new PaymentMeterPagination(), "1");
        Map<String, Object> map = new HashMap<String, Object>(100);
        map.put("indexList", list);

        // 2.??????excel??????
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);

        // 3.????????????
        String fileName = "??????????????????.xlsx";
        try {
            HttpServletResponse response = ServletUtil.getResponse();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????(excel)
     *
     * @return
     */
    @ApiOperation("????????????")
    @PostMapping("/Uploader")
    @Transactional
    public ActionResult uploader() {
        List<MultipartFile> list = UpUtil.getFileAll();
        MultipartFile file = list.get(0);
        if (file.getOriginalFilename().contains(".xlsx")) {
            String filePath = fileService.getFilePath(FileTypeEnum.TEMPORARY);
            String fileName = RandomUtil.uuId() + "." + UpUtil.getFileType(file);
            // ????????????
            FileUtil.upFile(file, filePath, fileName);
            DownloadVO vo = DownloadVO.builder().build();
            vo.setName(fileName);
            return ActionResult.success(vo);
        } else {
            return ActionResult.fail("???????????????????????????");
        }

    }

    /**
     * ????????????
     *
     * @return
     */
    @PreAuthorize("@ss.hasRole('manager')")
    @Log(title = "??????????????????", businessType = BusinessType.IMPORT)
    @GetMapping("/Import")
    @Transactional
    public ActionResult importPreview(String fileName) {
        String filePath = fileService.getFilePath(FileTypeEnum.TEMPORARY);
        File temporary = new File(filePath + fileName);
        // ????????????
        List<PaymentMeterImport> meterList = ExcelUtil.importExcel(temporary, 1, 1, PaymentMeterImport.class);
        int num = paymentMeterService.importMeter(meterList);
        Map<String, String> map = new HashMap<String, String>();
        map.put("num", "" + num);
        return ActionResult.success(map);
    }
}
