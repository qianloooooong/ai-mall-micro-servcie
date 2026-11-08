-- 所有的主键id使用mp生成
create table `user` (
    id bigint primary key comment '主键ID',
    user_no bigint not null unique comment '用户唯一编号',
    username varchar(64) not null comment '用户名',
    nick_name varchar(64) not null comment '昵称',
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




