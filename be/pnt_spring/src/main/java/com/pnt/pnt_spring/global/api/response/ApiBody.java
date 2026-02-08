package com.pnt.pnt_spring.global.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiBody<T> {
	private T data;
	private String message;
	private Integer code;
}
