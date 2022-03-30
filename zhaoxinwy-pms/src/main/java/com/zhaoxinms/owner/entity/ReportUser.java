package com.zhaoxinms.owner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.zhaoxinms.common.core.mybatisplus.BaseEntity;

/**
 * 报告用户信息对象 report_user
 *
 * @author shx
 * @date 2022-03-30
 */
@Data
@Accessors(chain = true)
@TableName("report_user")
public class ReportUser extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 报告用户信息ID
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 姓名
     */
    private String userName;
    /**
     * 性别（0男 1女 2未知）
     */
    private String sex;
    /**
     * 身高
     */
    private String height;
    /**
     * 体重
     */
    private String weight;
    /**
     * 报告地址
     */
    private String reportUrl;

}
