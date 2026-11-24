-- 所有的主键id使用mp生成
create table `user` (
    id bigint primary key comment '主键ID',
    user_no bigint not null unique comment '用户唯一编号',
    username varchar(64) not null comment '用户名',
    nickname varchar(64) not null comment '昵称',
    avatar varchar(255) comment '头像',
    gender tinyint not null default 0 comment '性别：0-未设置，1-男，2-女',
    create_time datetime not null comment '创建时间',
    update_time datetime not null comment '更新时间',
    status tinyint not null default 1 comment '账号状态：0-禁用，1-启用',
    deleted bigint not null default 0 comment '逻辑删除：0-未删除，id-已删除',
    delete_time datetime default null comment '逻辑删除时间',
    unique key uk_username (username, deleted)
)engine = innodb, default charset = utf8mb4 comment='用户表';
insert into user values
        (1, 10001, 'user', 'user', null,
         0, now(), now(), 1, 0, null);

create table user_auth_mobile (
    id bigint primary key comment '主键ID',
    user_id bigint not null comment '用户ID',
    mobile_cipher varbinary(255) not null comment 'aes-256-gcm密文',
    mobile_hash binary(32) not null comment 'hmac-sha256盲索引',
    key_version varchar(10) not null default 'v1' comment '加密密钥版本',
    mobile_masked varchar(20) not null default '' comment '脱敏展示，如138****8000',
    create_time datetime not null comment '创建时间',
    deleted bigint not null default 0 comment '逻辑删除：0-未删除，id-已删除',
    delete_time datetime default null comment '逻辑删除时间',
    unique key uk_user_id (user_id, deleted),
    unique key uk_mobile_hash (mobile_hash, deleted)
)engine = innodb, default charset = utf8mb4 comment='用户手机认证表';
insert into user_auth_mobile values
        (1, 1, 'xxx', 'xxx',
         1, '138****8000', now(), 0, null);


create table user_auth_email (
    id bigint primary key comment '主键ID',
    user_id bigint not null comment '用户ID',
    email_cipher varbinary(255) not null comment 'aes-256-gcm密文',
    email_hash binary(32) not null comment 'hmac-sha256盲索引',
    key_version varchar(10) not null default 'v1' comment '加密密钥版本',
    email_masked varchar(255) not null default '' comment '脱敏展示，如abc****@example.com',
    create_time datetime not null comment '创建时间',
    deleted bigint not null default 0 comment '逻辑删除：0-未删除，id-已删除',
    delete_time datetime default null comment '逻辑删除时间',
    unique key uk_user_id (user_id, deleted),
    unique key uk_email_hash (email_hash, deleted)
)engine = innodb, default charset = utf8mb4 comment='用户邮箱认证表';
insert into user_auth_email values
        (1, 1, 'xxx', 'xxx',
         1, 'abc****@example.com', now(), 0, null);


create table user_auth_credential (
    id bigint primary key comment '主键ID',
    user_id bigint not null comment '用户ID',
    password_hash varchar(255) not null comment 'bcrypt 2b，单向',
    create_time datetime not null comment '创建时间',
    update_time datetime not null comment '更新时间',
    deleted bigint not null default 0 comment '逻辑删除：0-未删除，id-已删除',
    delete_time datetime default null comment '逻辑删除时间',
    unique key uk_user_id (user_id, deleted)
) engine = innodb, default charset = utf8mb4 comment='用户凭证表';
insert into user_auth_credential values
        (1, 1, '$2b$10$HhWq27BtaeYOWo2vwmge1upjA2uTE8PnAXM8UMnXblZOqxCWxOEWi',
         now(), now(), 0, null);


create table user_gender (
    id tinyint primary key comment '用户性别ID',
    gender varchar(3) not null unique comment '性别'
)engine = innodb, default charset = utf8mb4 comment='性别表';
insert into user_gender values (0, '未设置'), (1, '男'), (2, '女');

create table user_status (
    id tinyint primary key comment '用户状态ID',
    status varchar(32) not null unique comment '状态'
)engine = innodb, default charset = utf8mb4 comment='用户状态表';
insert into user_status values (0, '禁用'), (1, '启用');


create table `role` (
    id tinyint primary key comment '用户角色ID',
    role varchar(32) not null unique comment '角色',
    description varchar(32) comment '描述'
)engine = innodb, default charset = utf8mb4 comment='角色表';
insert into `role` values (1, 'ADMIN', '管理员'), (2, 'CUSTOMER_SERVICE', '客服'), (3,'USER', '用户' );

create table user_role (
    id bigint primary key comment '主键ID',
    user_id bigint not null comment '用户ID',
    role_id tinyint not null comment '角色ID',
    unique key uk_user_role (user_id, role_id) comment '用户角色唯一键'
)engine = innodb, default charset = utf8mb4 comment='用户角色表';
insert into user_role (id, user_id, role_id) values (1, 1, 1), (2, 1, 2), (3, 1, 3);

create table user_address (
    id bigint primary key comment '主键ID',
    user_id bigint not null comment '用户ID',
    name varchar(64) not null comment '收货人姓名',
    province_code varchar(12) not null comment '省份代码',
    city_code varchar(12) not null comment '城市代码',
    county_code varchar(12) not null comment '区县代码',
    region_name varchar(128) not null default '' comment '省市区全称快照，如“北京市市辖区东城区”，与element-china-area-data v6.1一致',
    address_cipher varbinary(255) not null comment '地址aes-256-gcm密文',
    phone_cipher varbinary(255) not null comment '联系方式aes-256-gcm密文',
    key_version varchar(10) not null default 'v1' comment '加密密钥版本',
    phone_masked varchar(20) not null default '' comment '脱敏展示，如138****8000',
    is_default tinyint not null default 0 comment '是否默认地址：0-否，1-是',
    create_time datetime not null comment '创建时间',
    update_time datetime not null comment '更新时间',
    deleted bigint not null default 0 comment '逻辑删除：0-未删除，id-已删除',
    delete_time datetime default null comment '逻辑删除时间',
    index idx_user_id (user_id, deleted, is_default) comment '用户地址索引'
)engine = innodb, default charset = utf8mb4 comment='用户地址表';

create table region_code (
    id varchar(12) primary key comment '区域代码',
    pid varchar(12) not null comment '父区域代码',
    name varchar(32) not null comment '区域名称',
    index idx_pid (pid) comment '父区域代码索引'
)engine = innodb, default charset = utf8mb4 comment='区域代码表';

create table user_balance (
    id bigint primary key comment '主键ID',
    user_id bigint unique not null comment '用户ID',
    balance bigint not null default 0 comment '余额，单位分',
    version bigint not null default 0 comment '乐观锁版本号',
    create_time datetime not null comment '创建时间',
    update_time datetime not null comment '更新时间'
)engine = innodb, default charset = utf8mb4 comment='用户余额表';

create table user_balance_log (
    id bigint primary key comment '主键ID',
    user_id bigint not null comment '用户ID',
    request_id varchar(64) unique not null comment '幂等请求ID，全局唯一；业务方生成，重试复用；格式：{业务类型}:{业务单号}:user:{userId}:{动作}，如 recharge:RC202411240001:user:1001:success',
    biz_no varchar(64) comment '业务单号，如充值单号、订单号、提现单号、退款单号、调整单号',
    change_type tinyint not null comment '变更类型：1-初始化余额，2-充值，3-支付，4-提现，5-退款，6-人工调整',
    change_amount bigint not null comment '变更金额，单位：分，正负：初始化/充值/退款/人工调增为正，支付/提现为负，人工调整按实际方向',
    before_balance bigint not null comment '变更前余额，单位分',
    after_balance bigint not null comment '变更后余额，单位分',
    create_time datetime not null comment '创建时间',
    index idx_user_time(user_id, create_time) comment '用户时间索引'
) engine = innodb, default charset = utf8mb4 comment='用户余额日志表';

create table user_balance_change_type (
    id tinyint primary key comment '变更类型ID',
    change_type varchar(16) not null unique comment '变更类型',
    operation_code varchar(16) not null unique comment '变更类型编码'
)engine = innodb, default charset = utf8mb4 comment='用户余额变更类型表';
insert into user_balance_change_type values
        (1, '初始化余额', 'init'),
        (2, '充值', 'recharge'),
        (3, '支付', 'pay'),
        (4, '提现', 'withdraw'),
        (5, '退款', 'refund'),
        (6, '人工调整', 'manual');






