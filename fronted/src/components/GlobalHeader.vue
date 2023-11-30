<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar">
            <img
              class="logo"
              src="//p3-armor.byteimg.com/tos-cn-i-49unhts6dw/dfdba5317c0c20ce20e64fac803d52bc.svg~tplv-49unhts6dw-image.image"
            />
            <div class="title">YukeSeko OJ</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="160px">
      <a-dropdown trigger="hover">
        <span>
          {{ store.state.user?.loginUser?.userName ?? "请登录" }}<icon-down />
        </span>
        <template #content>
          <a-doption @click="dropSubmit">
            <template #icon>
              <icon-import />
            </template>
            <template #default
              >{{
                store.state.user?.loginUser?.userName === "未登录"
                  ? "立即登录"
                  : "退出登录"
              }}
            </template>
          </a-doption>
        </template>
      </a-dropdown>
    </a-col>
  </a-row>
  <a-modal
    v-model:visible="visible"
    @ok="loginOut"
    @cancel="visible = false"
    message-type="warning"
  >
    <template #title>退出登录</template>
    <div>您确定要退出登录吗？</div>
  </a-modal>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { routes } from "@/router/routers";
import { computed, onMounted, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import { UserControllerService } from "@/api";
import message from "@arco-design/web-vue/es/message";

const store = useStore();
const router = useRouter();
// 默认主页
const selectedKeys = ref(["/"]);
const visible = ref(false);

// 路由跳转后，更新选中的菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});

onMounted(() => {
  console.log(store.state.user.loginUser);
});

/**
 * 处理退出请求
 */
const loginOut = () => {
  try {
    console.log("退出登录");
    UserControllerService.userLogoutUsingPost();
    //退出登录后跳转到登录页面
    router.push({
      path: "/user/login",
      replace: true,
    });
    message.success("退出成功！");
  } catch (e) {
    console.log("退出操作异常");
  }
};

/**
 * 处理头像下拉框操作
 */
const dropSubmit = () => {
  if (store.state.user?.loginUser?.userName !== "未登录") {
    visible.value = true;
  } else {
    router.push({
      path: "/user/login",
      replace: true,
    });
  }
};

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

// setTimeout(() => {
//   store.dispatch("user/getLoginUser", {
//     userName: "鱼皮管理员",
//     userRole: ACCESS_ENUM.ADMIN,
//   });
// }, 3000);

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: #444;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>
