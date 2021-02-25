-- 任务表
create database ant;
CREATE TABLE IF NOT EXISTS ant.task(
    `task_id` MEDIUMINT NOT NULL AUTO_INCREMENT,
    `task_name` varchar(100) NOT NULL COMMENT 'task名称',
    `task_status` CHAR(1) DEFAULT '1' COMMENT '任务状态,0:禁用,1:启用,2:删除',
    `task_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `task_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次修改时间',
    `task_param_id` MEDIUMINT NOT NULL COMMENT '任务参数id,与任务关联',
    `task_file_id` MEDIUMINT COMMENT '任务文件id,与任务关联',
    `task_command` TEXT NOT NULL COMMENT '任务运行命令'
    PRIMARY KEY (`task_id`)
);

-- 任务参数表
CREATE TABLE IF NOT EXISTS ant.task_param (
    `param_id` MEDIUMINT NOT NULL AUTO_INCREMENT,
    `param_file_id` MEDIUMINT NOT NULL COMMENT '关联的文件id',
    `param_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `param_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次修改时间'
    PRIMARY KEY (`param_id`)
);

-- 文件表
CREATE TABLE IF NOT EXISTS ant.task_file(
    `file_id` MEDIUMINT NOT NULL AUTO_INCREMENT,
    `file_type` VARCHAR(100) NOT NULL COMMENT '文件类型',
    `file_content` LONGTEXT COMMENT '文件内容,二进制存储',
    `file_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `file_update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次修改时间'
    PRIMARY KEY (`file_id`)
);

-- 任务运行结果表
CREATE TABLE IF NOT EXISTS ant.task_running_info(
    `task_running_info_id` MEDIUMINT NOT NULL AUTO_INCREMENT,
    `task_running_info_status` CHAR(1) DEFAULT '0' COMMENT '任务运行结果状态,0:成功,1:失败',
    `task_running_info_error` BLOB DEFAULT '' COMMENT '任务错误日志',
    `task_running_info_duration` BIGINT DEFAULT 0 COMMENT '任务运行耗时,单位毫秒',
    `task_running_info_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
    PRIMARY KEY (`file_id`)
);

-- 集群信息暂时不考虑存储