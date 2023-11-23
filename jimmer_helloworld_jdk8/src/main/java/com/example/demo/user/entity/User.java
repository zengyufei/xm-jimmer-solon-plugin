package com.example.demo.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.babyfish.jimmer.sql.*;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(name = User.TABLE_NAME)
public interface User {
	String TABLE_NAME = "user";
	@Id
    @GeneratedValue(generatorType = UserIdGen.class)
	String userId();

	@Nullable
	@Column(name = Columns.userName)
	String userName();

	@Nullable
	@Column(name = Columns.isImported)
	Integer isImported();

	@Nullable
	@Column(name = Columns.importTime)
	LocalDateTime importTime();

	@Nullable
	@Column(name = Columns.isSystemDefault)
	Integer isSystemDefault();

	@Nullable
	@Column(name = Columns.status)
	String status();

    @Nullable
    @IdView
    String createId();

    @Nullable
    @ManyToOne
    @JoinColumn(name = Columns.createId)
    User create();

    @IdView
    @Nullable
    String updateId();

    @ManyToOne
    @Nullable
    @JoinColumn(name = Columns.updateId)
    User update();

    @Nullable
    @Column(name = Columns.createTime)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createTime();

    @Nullable
    @Column(name = Columns.updateTime)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updateTime();

	interface Columns {
		String userId = "user_id"; // 用户ID
		String userName = "user_name"; // 用户名
		String createTime = "create_time"; // 创建时间
		String updateTime = "update_time"; // 修改时间
		String createId = "create_id"; // 创建人ID
		String updateId = "update_id"; // 修改人ID
		String isImported = "is_imported"; // 是否导入
		String importTime = "import_time"; // 导入时间
		String isSystemDefault = "is_system_default"; // 是否系统默认
		String status = "status"; // 状态
	}

	interface FieldNames {
		String userId = "userId"; // 用户ID
		String userName = "userName"; // 用户名
		String createTime = "createTime"; // 创建时间
		String updateTime = "updateTime"; // 修改时间
		String createId = "createId"; // 创建人ID
		String updateId = "updateId"; // 修改人ID
		String createName = "createName"; // 创建人用户名
		String updateName = "updateName"; // 修改人用户名
		String isImported = "isImported"; // 是否导入
		String importTime = "importTime"; // 导入时间
		String isSystemDefault = "isSystemDefault"; // 是否系统默认
		String status = "status"; // 状态
	}
}
