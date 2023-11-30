<template>
  <a-result status="403" subtitle="抱歉，你没有访问该页面的权限">
    <template #extra>
      <a-space>
        <a-button type="primary" @click="goBack">Back</a-button>
      </a-space>
    </template>
  </a-result>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();

/**
 * 跳转回上一页面
 */
const goBack = () => {
  router.go(-1);
  return;
};
</script>
