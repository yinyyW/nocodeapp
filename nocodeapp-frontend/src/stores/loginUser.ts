import { ref } from "vue";
import { defineStore } from "pinia";
import { loginInfo } from "@/api/userController"
import { USER_ROLE } from "@/common/userRole";

export const useLoginUserStore = defineStore("loginUser", () => {
  const loginUser = ref<API.UserVO>({
    userName: "未登录",
    userRole: USER_ROLE.USER,
  });

  const fetchUserFlag = ref(true);

  async function fetchLoginUser() {
    const res = await loginInfo();
    if (res?.data?.code === 0 && res?.data?.data) {
      const userInfo = res?.data?.data;
      loginUser.value = {
        id: userInfo.id,
        userAccount: userInfo.userAccount,
        userName: userInfo.userName,
        userAvatar: userInfo.userAvatar,
        userProfile: userInfo.userProfile,
        userRole: userInfo.userRole as USER_ROLE,
        createTime: userInfo.createTime,
      };
    }
    fetchUserFlag.value = false;
  }

  function setLoginUser(userInfo: API.UserVO | API.User) {
    loginUser.value = userInfo;
  }

  function userLogout() {
    loginUser.value = {
      userName: "未登录",
      userRole: USER_ROLE.USER,
    };
  }

  function setFetchUserFlag(flag: boolean) {
    fetchUserFlag.value = flag;
  }

  return {
    loginUser,
    fetchUserFlag,
    fetchLoginUser,
    setLoginUser,
    userLogout,
    setFetchUserFlag,
  };
});
