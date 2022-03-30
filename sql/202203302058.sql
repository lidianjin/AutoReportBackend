-- 报告用户信息表
create table report_user
(
    id          bigint auto_increment comment '报告用户信息ID'
        primary key,
    user_id     bigint                   null comment '用户 ID',
    user_name   varchar(30)              null comment '姓名',
    sex         char         default '0' null comment '性别（0男 1女 2未知）',
    height      varchar(10)  default ''  null comment '身高',
    weight      varchar(10)  default ''  null comment '体重',
    report_url  varchar(100) default ''  null comment '报告地址',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '报告用户信息表' charset = utf8;

-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('报告用户信息', '3080', '1', 'report', 'owner/report/index', 1, 0, 'C', '0', '0', 'owner:report:list', '#', 'admin', sysdate(), '', null, '报告用户信息菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('报告用户信息查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'owner:report:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('报告用户信息新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'owner:report:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('报告用户信息修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'owner:report:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('报告用户信息删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'owner:report:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('报告用户信息导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'owner:report:export',       '#', 'admin', sysdate(), '', null, '');

-- 用户
INSERT INTO pms.sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, create_by, create_time, update_by, update_time, remark) VALUES (31312313129, null, 'reportAdmin', '报告管理员', '00', '', '', '0', '', '$2a$10$asTaiAzq/Fp3tYoaYTqyDuIbLcGYLy0u6V098YH5AkMYDMhWlnOIO', '0', '0', '192.168.6.3', '2022-03-30 20:25:57', 'admin', '2022-03-30 20:19:19', '', '2022-03-30 20:25:57', null);
INSERT INTO pms.sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, create_by, create_time, update_by, update_time, remark) VALUES (31312313130, null, 'reportUser', '报告用户', '00', '', '', '0', '', '$2a$10$jXB1gaSMmoGjTPmm0hXYjujQbZT9G8ZbWsyB.t4FGRD8eqCeHWWHi', '0', '0', '192.168.6.3', '2022-03-30 20:31:27', 'admin', '2022-03-30 20:22:14', '', '2022-03-30 20:31:27', null);

-- 角色
INSERT INTO pms.sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark) VALUES (15, '报告管理员', 'reportAdmin', 2, '1', 1, 1, '0', '0', 'admin', '2022-03-30 20:16:39', '', null, null);
INSERT INTO pms.sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark) VALUES (16, '报告用户', 'reportUser', 3, '1', 1, 1, '0', '0', 'admin', '2022-03-30 20:17:36', '', null, null);

-- 菜单和角色关系
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3080);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3081);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3082);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3083);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3084);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3085);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (15, 3086);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3080);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3081);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3082);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3083);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3084);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3085);
INSERT INTO pms.sys_role_menu (role_id, menu_id) VALUES (16, 3086);

-- 用户和角色关系
INSERT INTO pms.sys_user_role (user_id, role_id) VALUES (31312313129, 15);
INSERT INTO pms.sys_user_role (user_id, role_id) VALUES (31312313130, 16);


commit;