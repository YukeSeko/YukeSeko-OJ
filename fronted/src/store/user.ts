import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "@/api/services/UserControllerService";

export default {
  namespaced: true,
  state: () => ({
    loginUser: {
      userName: "未登录",
    },
  }),
  actions: {
    async getLoginUser({ commit, state }, payload) {
      try {
        //拿到payload信息，如果没有内容，则远程进行请求数据
        if (payload === undefined) {
          payload = await UserControllerService.getLoginUserUsingGet();
        }
        // 从远程请求获取登录信息
        if (payload.code === 0) {
          commit("updateUser", payload.data);
        }
      } catch (e) {
        commit("updateUser", {
          ...state.loginUser,
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
      // //拿到payload信息，如果没有内容，则远程进行请求数据
      // if (payload === undefined) {
      //   payload = await UserControllerService.getLoginUserUsingGet();
      // }
      // // 从远程请求获取登录信息
      // if (payload.code === 0) {
      //   commit("updateUser", payload.data);
      // } else {
      //   commit("updateUser", {
      //     ...state.loginUser,
      //     userRole: ACCESS_ENUM.NOT_LOGIN,
      //   });
      // }
    },
  },
  mutations: {
    updateUser(state, payload) {
      state.loginUser = payload;
    },
  },
} as StoreOptions<any>;
