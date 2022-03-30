package com.zhaoxinms.owner.entity.pagination;

import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import com.zhaoxinms.base.vo.Pagination;
import java.util.Date;

import com.zhaoxinms.common.core.domain.BaseEntity;

/**
 * 报告用户信息业务对象 report_user
 *
 * @author shx
 * @date 2022-03-30
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("报告用户信息业务对象")
public class ReportUserPagination extends Pagination {

    @ApiModelProperty(value = "姓名")
    private String userName;

    @ApiModelProperty(value = "性别（0男 1女 2未知）")
    private String sex;

    @ApiModelProperty(value = "身高")
    private String height;

    @ApiModelProperty(value = "体重")
    private String weight;

    @ApiModelProperty(value = "报告地址")
    private String reportUrl;


}
