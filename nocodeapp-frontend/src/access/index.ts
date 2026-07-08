import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'
import { USER_ROLE } from '@/common/userRole'
import { ACCESS_ENUM } from './checkAccess'

router.beforeEach(async (to, from, next) => {
  const needAccess = to?.meta?.access
  if (!needAccess) {
    next()
    return
  }
  const loginUserStore = useLoginUserStore()
  if (loginUserStore.fetchUserFlag || !loginUserStore?.loginUser?.id) {
    await loginUserStore.fetchLoginUser()
  }
  const user = loginUserStore?.loginUser
  if (needAccess === ACCESS_ENUM.USER) {
    if (!user || !user?.id || !user?.userRole) {
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  if (needAccess === ACCESS_ENUM.ADMIN) {
    if (!user || !user?.id || !user?.userRole || user?.userRole !== USER_ROLE.ADMIN) {
      next('/noAuth')
      return
    }
  }
  next()
})
