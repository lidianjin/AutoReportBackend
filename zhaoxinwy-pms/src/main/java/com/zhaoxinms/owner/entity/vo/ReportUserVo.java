package com.zhaoxinms.owner.entity.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;



/**
 * 报告用户信息视图对象 report_user
 *
 * @author shx
 * @date 2022-03-30
 */
@Data
@ApiModel("报告用户信息视图对象")
public class ReportUserVo {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("id")
	private String id;

	/**
	 * 用户 ID
	 */
	@Excel(name = "用户 ID")
	@ApiModelProperty("用户 ID")
	private String userId;

    /**
     * 姓名
     */
	@Excel(name = "姓名")
	@ApiModelProperty("姓名")
	private String userName;

    /**
     * 性别（0男 1女 2未知）
     */
	@Excel(name = "性别")
	@ApiModelProperty("性别（0男 1女 2未知）")
	private String sex;

    /**
     * 身高
     */
	@Excel(name = "身高")
	@ApiModelProperty("身高")
	private String height;

    /**
     * 体重
     */
	@Excel(name = "体重")
	@ApiModelProperty("体重")
	private String weight;

    /**
     * 报告地址
     */
	@Excel(name = "报告地址")
	@ApiModelProperty("报告地址")
	private String reportUrl;

    /**
     * 备注
     */
	@Excel(name = "备注")
	@ApiModelProperty("备注")
	private String remark;


}
