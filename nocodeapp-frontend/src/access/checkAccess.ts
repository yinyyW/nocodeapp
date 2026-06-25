import { USER_ROLE } from "@/common/userRole";

export const ACCESS_ENUM = {
  NOT_LOGIN: 'NOT_LOGIN',
  USER: 'USER',
  ADMIN: 'ADMIN',
}

export const checkAccess = (loginUser: API.UserVO, needAccess = ACCESS_ENUM.NOT_LOGIN) => {
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true
  }

  const loginUserRole = loginUser?.userRole
  if (!loginUserRole) {
    return false
  }
  if (needAccess === ACCESS_ENUM.ADMIN && loginUserRole !== USER_ROLE.ADMIN) {
    return false
  }
  return true
}
