package com.zhaoxinms.framework.config;

import java.util.Date;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sun.jna.platform.win32.Netapi32Util.UserInfo;
import com.zhaoxinms.common.constant.HttpStatus;
import com.zhaoxinms.common.core.domain.entity.SysUser;
import com.zhaoxinms.common.core.mybatisplus.BaseEntity;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        insertFillOld(metaObject);
        insertFillNew(metaObject);
    }
    
    private void insertFillOld(MetaObject metaObject) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        Object enabledMark = this.getFieldValByName("enabledMark", metaObject);
        Object creatorUserId = this.getFieldValByName("creatorUserId", metaObject);
        Object creatorTime = this.getFieldValByName("creatorTime", metaObject);
        Object creatorUser = this.getFieldValByName("creatorUser", metaObject);
        if (enabledMark == null) {
            this.setFieldValByName("enabledMark", 1, metaObject);
        }
        if (creatorUserId == null) {
            this.setFieldValByName("creatorUserId", ""+user.getUserId(), metaObject);
        }
        if (creatorTime == null) {
            this.setFieldValByName("creatorTime", new Date(), metaObject);
        }
        if (creatorUser == null) {
            this.setFieldValByName("creatorUser", ""+user.getUserId(), metaObject);
        }
    }
    
    private void insertFillNew(MetaObject metaObject) {
        try {
            if (metaObject != null && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                Date current = new Date();
                // 创建时间为空 则填充
                if (baseEntity.getCreateTime() == null) {
                    baseEntity.setCreateTime(current);
                }
                // 更新时间为空 则填充
                if (baseEntity.getUpdateTime() == null) {
                    baseEntity.setUpdateTime(current);
                }
                SysUser user = SecurityUtils.getLoginUser().getUser();
                // 当前已登录 且 创建人为空 则填充
                if (StringUtils.isNotBlank(user.getUserName())
                        && StringUtils.isBlank(baseEntity.getCreateBy())) {
                    baseEntity.setCreateBy(user.getUserName());
                }
                // 当前已登录 且 更新人为空 则填充
                if (StringUtils.isNotBlank(user.getUserName())
                        && StringUtils.isBlank(baseEntity.getUpdateBy())) {
                    baseEntity.setUpdateBy(user.getUserName());
                }
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        oldUpdate(metaObject);
        newUpdate(metaObject);
    }

    private void newUpdate(MetaObject metaObject) {
        try {
            if (metaObject != null && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                Date current = new Date();
                // 更新时间填充(不管为不为空)
                baseEntity.setUpdateTime(current);
                SysUser user = SecurityUtils.getLoginUser().getUser();
                // 当前已登录 更新人填充(不管为不为空)
                if (StringUtils.isNotBlank(user.getUserName())) {
                    baseEntity.setUpdateBy(user.getUserName());
                }
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private void oldUpdate(MetaObject metaObject) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        this.setFieldValByName("lastModifyTime", new Date(), metaObject);
        this.setFieldValByName("lastModifyUserId", ""+user.getUserId(), metaObject);
        this.setFieldValByName("lastModifyUser", ""+user.getUserId(), metaObject);
    }


}
