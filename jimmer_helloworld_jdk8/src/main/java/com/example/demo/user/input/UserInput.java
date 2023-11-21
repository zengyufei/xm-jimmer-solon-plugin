//package com.example.demo.user.input;
//
//import com.example.demo.user.entity.User;
//import lombok.Data;
//import org.babyfish.jimmer.Input;
//import org.mapstruct.BeanMapping;
//import org.mapstruct.Mapper;
//import org.mapstruct.ReportingPolicy;
//import org.mapstruct.factory.Mappers;
//
//import java.time.LocalDateTime;
//
//@Data
//public class UserInput implements Input<User> {
//
//	private static final Converter CONVERTER = Mappers.getMapper(Converter.class);
//
//	private String userId;
//	private String userName;
//
//	private String createId;
//	private String createName;
//	private LocalDateTime createTime;
//
//	private String updateId;
//	private String updateName;
//	private LocalDateTime updateTime;
//
//	private String approvalStatus;
//	private String approverId;
//	private String approverName;
//	private String approvalComment;
//	private LocalDateTime approvalTime;
//
//
//	private Integer isImported;
//	private LocalDateTime importTime;
//
//	private Integer isSystemDefault;
//	private String tenantId;
//	private Integer version;
//	private String status;
//
//	@Override
//	public User toEntity() {
//		return CONVERTER.toUser(this);
//	}
//	@Mapper
//	interface Converter {
//		@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
//		User toUser(UserInput input);
//	}
//}
