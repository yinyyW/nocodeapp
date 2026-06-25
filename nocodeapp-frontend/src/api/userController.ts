/* eslint-disable */
import request from "@/common/network";

/** 此处后端没有提供注释 POST /user/admin/add */
export async function add(
  body: API.UserAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserRegisterResponse>("/user/admin/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /user/admin/list/userVO */
export async function userVoList(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO>("/user/admin/list/userVO", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /user/admin/user/info */
export async function adminUserInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.adminUserInfoParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUser>("/user/admin/user/info", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /user/delete */
export async function deleteUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>("/user/delete", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /user/login */
export async function login(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserLoginResponse>("/user/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /user/login/info */
export async function loginInfo(options?: { [key: string]: any }) {
  return request<API.BaseResponseUserLoginResponse>("/user/login/info", {
    method: "GET",
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /user/logout */
export async function logout(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>("/user/logout", {
    method: "POST",
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /user/register */
export async function register(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserRegisterResponse>("/user/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /user/update */
export async function update(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean>("/user/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /user/userVO/info */
export async function userVoInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.userVOInfoParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserVO>("/user/userVO/info", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
