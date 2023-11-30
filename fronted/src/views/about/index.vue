<template>
  <a-typography :style="{ marginTop: '-40px' }">
    <a-typography-title> 系统整体设计</a-typography-title>
    <a-typography-paragraph>
      YukeSeko OJ在线判题系统是基于 Spring Cloud 微服务 + MQ + Docker (+ Vue 3 +
      Arco Design) 的
      <a-typography-text mark>编程题目评测系统</a-typography-text>
      。系统能够根据管理员预设的题目用例对用户提交的代码进行执行和评测;系统中
      <a-typography-text mark>自主实现的代码沙箱</a-typography-text>
      可作为独立服务供其他开发者调用。
    </a-typography-paragraph>
    <a-typography-title :heading="3">前端设计</a-typography-title>
    <a-typography-paragraph>
      <ol>
        <li>
          基于 Vue3 + Arco Design
          组件库，自主实现了在线做题、题目检索和管理、提交列表、用户登录等页面。
        </li>
        <li>
          全局权限管理: 通过给 Vue Router 路由的 meta 属性增加 access
          字段来定义页面权限，然后通过 beforeEach
          全局路由守卫集中校验用户进入页面的权限，并进一步将权限管理相关代码统一封装为
          access.ts 模块，简化用户使用。
        </li>
        <li>
          选用 ByteMD 开源 Markdown 文本编辑器组件，引入 gfm 插件
          (支持表格语法)并进步自行封装了可复用的 Editor 和
          Viewer，实现了题目内容及答案的编辑功能。
        </li>
        <li>
          全局状态管理: 基于 Vuex 定义 User Module
          实现了对登录用户的状态存储，并通过组合式 API (useStore)
          在页面中访问用户信息。
        </li>
      </ol>
    </a-typography-paragraph>
    <a-typography-title :heading="3">后端设计</a-typography-title>
    <a-typography-paragraph>
      <ol>
        <li>
          系统架构:
          根据功能职责，将系统划分为负责核心业务的后端模块、负责校验结果的判题模块、负责编译执行代码的可复用代码沙箱。各模块相互独立，并通过
          API 接口和分包的方式实现协作。
        </li>
        <li>
          自主设计判题机模块的架构，定义了代码沙箱的抽象调用接口和多种实现类
          (比如远程第三方代码沙箱)，并通过
          <a-typography-text mark
            >静态工厂模式 + Spring 配置化
          </a-typography-text>
          的方式实现了对多种代码沙箱的灵活调用。
        </li>
        <li>
          使用
          <a-typography-text mark>代理模式</a-typography-text>
          对代码沙箱接口进行能力增强，统一实现了对代码沙箱调用前后的日志记录，减少重复代码。
        </li>
        <li>
          由于判题逻辑复杂、且不同题目的判题算法可能不同 (比如 Java
          题目额外增加空间限制)，选用
          <a-typography-text mark>策略模式</a-typography-text>
          代替 if else 独立封装了不同语言的判题算法，提高系统的可维护 性
        </li>
        <li>
          为防止判题操作执行时间较长，
          <a-typography-text underline
            >系统选用异步的方式，在题目服务中将用户提交 id 发送给 RabbitMQ
            消息队列，并通过 Direct 交换机转发给判题队列
          </a-typography-text>
          ，由判题服务进行消费异步更新提交状态。
        </li>
        <li>
          为保证沙箱宿主机的稳定性，选用 Docker 隔离用户代码，使用 Docker Java
          库创建容器隔离执行代码，并通过 tty 和 Docker
          进行传参交互，从而实现了更安全的代码沙箱。
        </li>
        <li>
          使用 Java Runtime 对象的 exec 方法实现了对 Java
          程序的编译和执行，并通过Process 类的输入流获取执行结果，实现了 Java
          原生代码沙箱
        </li>
      </ol>
    </a-typography-paragraph>
    <a-typography-paragraph blockquote>
      本项目已经在GitHub上开源，有关YukeSeko的
      <a-typography-text underline>其他项目</a-typography-text>
      更多详情请了解：
      <a-link href="http://117.72.15.110/">YukeSeko的博客</a-link>
    </a-typography-paragraph>
  </a-typography>
</template>

<script lang="ts" setup>
import Banner from "./components/bannerWorkPlace.vue";
import DataPanel from "./components/data-panel.vue";
import PopularContent from "./components/popular-content.vue";
import CategoriesPercent from "./components/categories-percent.vue";
</script>

<script lang="ts">
export default {
  name: "aboutOj", // If you want the include property of keep-alive to take effect, you must name the component
};
</script>
