CREATE TABLE `account` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `coin_id` bigint(20) NOT NULL COMMENT '币种ID',
                           `address` varchar(255) NOT NULL COMMENT '账户地址',
                           `balance` decimal(18,8) NOT NULL COMMENT '账户余额',
                           `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `account_transaction` (
                                                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                             `account_id` bigint(20) NOT NULL COMMENT '账户ID',
                                                             `ref_id` bigint(20) NOT NULL COMMENT '参考ID，用于关联其他记录或外部数据',
                                                             `type` varchar(50) NOT NULL COMMENT '交易类型',
                                                             `from_address` varchar(255) NOT NULL COMMENT '转出地址',
                                                             `to_address` varchar(255) NOT NULL COMMENT '接收地址',
                                                             `amount` decimal(18,8) NOT NULL COMMENT '交易金额',
                                                             `real_amount` decimal(18,8) NOT NULL COMMENT '实际到账金额',
                                                             `fee` decimal(18,8) NOT NULL COMMENT '交易手续费',
                                                             PRIMARY KEY (`id`)
                      ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `address` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                     `address` varchar(255) NOT NULL COMMENT '地址',
                                     `private_key` varchar(255) NOT NULL COMMENT '私钥',
                                     `ctime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `mtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`)
          ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='地址表' ;

CREATE TABLE `coin` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                `address` varchar(255) NOT NULL COMMENT '合约地址',
                                `coin_name` varchar(255) NOT NULL COMMENT '币种名称',
                                `coin_decimals` int(11) NOT NULL COMMENT '币种精度',
                                `coin_confirm` int(11) NOT NULL COMMENT '币种确认数',
                                `ctime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `mtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `min_collect_amount` decimal(18,10) NOT NULL DEFAULT '0.0000000000' COMMENT '最小归集数量',
                                `type` varchar(255) NOT NULL DEFAULT '' COMMENT '币种类型',
                                PRIMARY KEY (`id`)
        ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='币种信息表';

CREATE TABLE `collect` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `transaction_id` bigint(20) NOT NULL COMMENT '流水ID',
                           `type` varchar(255) NOT NULL COMMENT '归集类型',
                           `contract_address` varchar(255) NOT NULL COMMENT '合约地址',
                           `collect_from_address` varchar(255) NOT NULL COMMENT '归集来源地址',
                           `collect_to_address` varchar(255) NOT NULL COMMENT '归集目的地址',
                           `collect_amount` decimal(18,8) NOT NULL COMMENT '归集金额',
                           `collect_hash` varchar(255) NOT NULL COMMENT '归集交易哈希',
                           `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `deposit` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                           `symbol` varchar(255) NOT NULL COMMENT '币种符号',
                           `transaction_type` int(11) NOT NULL COMMENT '交易类型：0-主链币交易，1-代币交易',
                           `from_address` varchar(255) NOT NULL COMMENT '转出地址',
                           `to_address` varchar(255) NOT NULL COMMENT '转入地址',
                           `amount` decimal(20,8) NOT NULL COMMENT '金额',
                           `contract` varchar(255) DEFAULT NULL COMMENT '合约地址',
                           `hash` varchar(255) NOT NULL COMMENT '交易哈希',
                           `block_number` bigint(20) NOT NULL COMMENT '区块号',
                           `status` int(11) NOT NULL COMMENT '状态',
                           `is_collect` int(11) NOT NULL COMMENT '是否归集：0-未归集，1-已归集',
                           `ctime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `mtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `hash` (`hash`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='存款信息表';


CREATE TABLE `fee_transfer` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `from_address` varchar(255) NOT NULL COMMENT '转账来源地址',
                                `to_address` varchar(255) NOT NULL COMMENT '转账目标地址',
                                `amount` decimal(20,8) NOT NULL COMMENT '转账金额',
                                `txid` varchar(255) NOT NULL COMMENT '交易ID',
                                `status` int(11) NOT NULL COMMENT '状态',
                                `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `account_id` bigint(20) DEFAULT NULL COMMENT '账户ID',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='转账费用表';

CREATE TABLE `notify` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                          `application_type` varchar(255) NOT NULL COMMENT 'Application Type',
                          `params` text COMMENT 'Parameters',
                          `url` varchar(255) NOT NULL COMMENT 'Notification Target URL',
                          `status` int(11) NOT NULL COMMENT 'Status: 0 - Notification, 1 - Success, 2 - Exception, 3 - Exception Terminated',
                          `error_count` int(11) NOT NULL DEFAULT '0' COMMENT 'Error Count',
                          `message` text COMMENT 'Result or Error Message',
                          `ctime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
                          `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last Modification Time',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='Notification Information Table';

CREATE TABLE `withdraw` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                            `transaction_type` int(11) NOT NULL COMMENT '交易类型：0-主链币交易，1-代币交易',
                            `biz_id` bigint(20) DEFAULT NULL COMMENT 'bizId',
                            `address` varchar(255) NOT NULL COMMENT '提现地址',
                            `amount` decimal(20,8) NOT NULL COMMENT '提现金额',
                            `contract` varchar(255) DEFAULT NULL COMMENT '合约地址',
                            `hash` varchar(255) DEFAULT NULL COMMENT '交易哈希',
                            `status` int(11) NOT NULL COMMENT '状态：0-刚入库，1-上链',
                            `ctime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `mtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='提现信息表' ;


ALTER TABLE `account`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `account_transaction`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `address`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `coin`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `collect`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `deposit`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `fee_transfer`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `notify`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';

ALTER TABLE `withdraw`
    ADD COLUMN `chain_type` VARCHAR(255) DEFAULT '' COMMENT '主链类型';


ALTER TABLE `account`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `account_transaction`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `address`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `coin`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `collect`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `deposit`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `fee_transfer`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `notify`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';

ALTER TABLE `withdraw`
    ADD `ref_wallet_id` BIGINT(20) COMMENT '关联钱包ID';
