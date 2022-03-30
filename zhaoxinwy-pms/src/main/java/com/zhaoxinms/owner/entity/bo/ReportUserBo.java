package com.zhaoxinms.owner.entity.bo;

import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.zhaoxinms.common.core.mybatisplus.BaseEntity;

/**
 * 报告用户信息业务对象 report_user
 *
 * @author shx
 * @date 2022-03-30
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("报告用户信息业务对象")
public class ReportUserBo extends BaseEntity {

    /**
     * 报告用户信息ID
     */
    @ApiModelProperty(value = "报告用户信息ID")
    private Long id;

    /**
     * 用户 ID
     */
    @ApiModelProperty(value = "用户 ID")
    private Long userId;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String userName;

    /**
     * 性别（0男 1女 2未知）
     */
    @ApiModelProperty(value = "性别（0男 1女 2未知）")
    private String sex;

    /**
     * 身高
     */
    @ApiModelProperty(value = "身高")
    private String height;

    /**
     * 体重
     */
    @ApiModelProperty(value = "体重")
    private String weight;

    /**
     * 报告地址
     */
    @ApiModelProperty(value = "报告地址")
    private String reportUrl;


}
