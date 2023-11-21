package com.example.demo.user.query;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserQuery implements Serializable {

	private String userId;
	private String userName;

	private LocalDateTime createTime;
	private LocalDateTime beginCreateTime;
	private LocalDateTime endCreateTime;

	private LocalDateTime updateTime;
	private LocalDateTime beginUpdateTime;
	private LocalDateTime endUpdateTime;

	private String createId;
	private String createName;

	private String updateId;
	private String updateName;

	private String approvalStatus;
	private String approverId;
	private String approverName;
	private String approvalComment;
	private LocalDateTime approvalTime;
	private LocalDateTime beginApprovalTime;
	private LocalDateTime endApprovalTime;

	private Integer isImported;

	private LocalDateTime importTime;
	private LocalDateTime beginImportTime;
	private LocalDateTime endImportTime;

	private Integer isSystemDefault;
	private String status;
}
