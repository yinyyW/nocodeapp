declare namespace API {
  type adminUserInfoParams = {
    userId: number;
  };

  type BaseResponseBoolean = {
    code?: number;
    message?: string;
    data?: boolean;
  };

  type BaseResponsePageUserVO = {
    code?: number;
    message?: string;
    data?: PageUserVO;
  };

  type BaseResponseString = {
    code?: number;
    message?: string;
    data?: string;
  };

  type BaseResponseUser = {
    code?: number;
    message?: string;
    data?: User;
  };

  type BaseResponseUserLoginResponse = {
    code?: number;
    message?: string;
    data?: UserLoginResponse;
  };

  type BaseResponseUserRegisterResponse = {
    code?: number;
    message?: string;
    data?: UserRegisterResponse;
  };

  type BaseResponseUserVO = {
    code?: number;
    message?: string;
    data?: UserVO;
  };

  type deleteUsingPOSTParams = {
    userId: number;
  };

  type PageUserVO = {
    records?: UserVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type User = {
    id?: number;
    userAccount?: string;
    userPassword?: string;
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: "user" | "admin";
    editTime?: string;
    createTime?: string;
    updateTime?: string;
    isDelete?: number;
  };

  type UserAddRequest = {
    userName?: string;
    userAccount?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: "user" | "admin";
  };

  type UserLoginRequest = {
    username?: string;
    password?: string;
  };

  type UserLoginResponse = {
    id?: number;
    userAccount?: string;
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: "user" | "admin";
    editTime?: string;
    createTime?: string;
    updateTime?: string;
  };

  type UserQueryRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    id?: number;
    userName?: string;
    userAccount?: string;
    userProfile?: string;
    userRole?: "user" | "admin";
  };

  type UserRegisterRequest = {
    userAccount?: string;
    userPassword?: string;
    checkPassword?: string;
  };

  type UserRegisterResponse = {
    userAccount?: string;
    userId?: number;
  };

  type UserUpdateRequest = {
    id?: number;
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: "user" | "admin";
  };

  type UserVO = {
    id?: number;
    userAccount?: string;
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: "user" | "admin";
    createTime?: string;
    updateTime?: string;
  };

  type userVOInfoParams = {
    userId: number;
  };
}
